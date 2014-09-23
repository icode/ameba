package ameba.enhancers.model;

import ameba.db.model.Model;
import ameba.enhancers.Enhancer;
import ameba.enhancers.EnhancingException;
import ameba.exceptions.UnexpectedException;
import ameba.util.IOUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javassist.*;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.ResourceFinder;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 模型管理器
 *
 * @author ICode
 * @since 13-8-18 上午10:39
 */
public class ModelManager extends Enhancer {
    public static final Logger logger = LoggerFactory.getLogger(ModelManager.class);
    public static final Map<String, ModelManager> managerMap = Maps.newHashMap();
    public static final String ID_SETTER_NAME = "__setId__";
    public static final String ID_GETTER_NAME = "__getId__";
    public static final String GET_FINDER_M_NAME = "withFinder";
    public static final String FINDER_C_NAME = "ameba.db.model.Finder";
    public final static String BASE_MODEL_PKG = "ameba.db.model";
    private static final Map<String, ModelDescription> descCache = Maps.newHashMap();
    private static ClassPool classpool;
    private List<ModelDescription> modelClassesDescList = Lists.newArrayList();
    private List<ModelEventListener> listeners = Lists.newArrayList();
    private String[] packages;
    private ModelManager(String[] packages) {
        this.packages = packages;
    }

    public static void reset() {
        managerMap.clear();
        classpool = newClassPool();
        descCache.clear();
    }

    public static ModelManager create(String name, String[] packages) {
        ModelManager manager = getManager(name);
        if (manager == null) {
            synchronized (managerMap) {
                if (getManager(name) == null) {
                    manager = new ModelManager(packages);
                    managerMap.put(name, manager);
                }
            }
        }
        return manager;
    }

    private static String decodeClassFile(ModelDescription desc){
        try {
            return URLDecoder.decode(desc.classFile, Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException e) {
            return desc.classFile;
        }
    }

    public static void loadAndClearDesc(String name) {
        ModelManager manager = getManager(name);
        if (manager != null) {
            manager.loadClass();
            int size = manager.modelClassesDescList.size();
            int index = 0;
            for (ModelDescription desc : manager.modelClassesDescList) {
                if (desc.clazz == null) {
                    InputStream in = new ByteArrayInputStream(desc.classBytecode);
                    try {
                        logger.trace("load {} model manager class {}", name, decodeClassFile(desc));
                        desc.clazz = classpool.makeClass(in).toClass();
                        manager.fireModelLoaded(desc.clazz, desc, index, size);
                    } catch (IOException e) {
                        logger.warn("load model class file [" + decodeClassFile(desc) + "] error", e);
                    } catch (CannotCompileException e) {
                        logger.warn("load model class file [" + decodeClassFile(desc) + "] error", e);
                    } finally {
                        index++;
                        IOUtils.closeQuietly(in);
                    }
                } else {
                    manager.fireModelLoaded(desc.clazz, desc, index, size);
                    index++;
                }
                logger.trace("clear {} model manager class {} desc", name, decodeClassFile(desc));
                desc.classBytecode = null;
            }
        }
    }

    public static void loadAndClearDesc() {
        Set<String> keys = managerMap.keySet();
        for (String key : keys) {
            loadAndClearDesc(key);
        }
        descCache.clear();
    }

    public static ModelManager getManager(String name) {
        return managerMap.get(name);
    }

    public void addModelLoadedListener(ModelEventListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    private void fireModelLoaded(Class clazz, ModelDescription desc, int index, int size) {
        for (ModelEventListener listener : listeners) {
            listener.loaded(clazz, desc, index, size);
        }
    }

    private void fireModelEnhancing(ModelDescription desc) {
        for (ModelEventListener listener : listeners) {
            byte[] bytes = listener.enhancing(desc);
            if (bytes != null)
                desc.classBytecode = bytes;
            else
                throw new EnhancingException("Enhance class byte code is null.");
        }
    }

    private void loadClass() {
        ResourceFinder scanner = new PackageNamesScanner(packages, true);
        while (scanner.hasNext()) {
            if (!scanner.next().endsWith(".class")) {
                continue;
            }

            InputStream in = scanner.open();
            try {
                ModelDescription desc = enhanceModel(in);
                if (desc != null && !modelClassesDescList.contains(desc))
                    modelClassesDescList.add(desc);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
    }

    public String[] getPackages() {
        return Arrays.copyOf(packages, packages.length);
    }

    public List<ModelDescription> getModelClassesDesc() {
        return Lists.newArrayList(modelClassesDescList);
    }

    private ModelDescription enhanceModel(InputStream in) {
        try {
            classpool.importPackage(BASE_MODEL_PKG);
            CtClass clazz = classpool.makeClass(in);

            if (clazz.isInterface()) {
                return null;
            }
            if (clazz.getName().endsWith(".package")) {
                return null;
            }

            ModelDescription cache = descCache.get(clazz.getURL().toExternalForm());
            if (cache != null) {
                return cache;
            }
//            if (!clazz.hasAnnotation(Entity.class)) {
//                return null;
//            }
            logger.debug("增强模型类[{}]", clazz.getName());

            cache = new ModelDescription();
            cache.classFile = clazz.getURL().toExternalForm();
            cache.classSimpleName = clazz.getSimpleName();
            cache.className = clazz.getName();

            boolean idGetSetFixed = false;

            // Add a default constructor if needed
            try {
                boolean hasDefaultConstructor = false;
                for (CtConstructor constructor : clazz.getDeclaredConstructors()) {
                    if (constructor.getParameterTypes().length == 0) {
                        hasDefaultConstructor = true;
                        break;
                    }
                }
                if (!hasDefaultConstructor) {
                    CtConstructor defaultConstructor = CtNewConstructor.defaultConstructor(clazz);
                    clazz.addConstructor(defaultConstructor);
                }
            } catch (Exception e) {
                logger.error("Error in ModelManager", e);
                throw new UnexpectedException("Error in PropertiesEnhancer", e);
            }

            for (CtField field : clazz.getDeclaredFields()) {
                if (!isProperty(field)) {
                    continue;
                }
                //add getter method
                String fieldName = StringUtils.capitalize(field.getName());
                String getterName = "get" + fieldName;
                CtMethod getter = null;
                CtClass fieldType = classpool.get(field.getType().getName());
                try {
                    getter = clazz.getDeclaredMethod(getterName);
                } catch (NotFoundException e) {
                    //noop
                }
                if (getter == null && (fieldType.getName().equals(Boolean.class.getName())
                        || fieldType.getName().equals(boolean.class.getName()))) {
                    getterName = "is" + fieldName;
                    try {
                        getter = clazz.getDeclaredMethod(getterName);
                    } catch (NotFoundException e) {
                        //noop
                    }
                }
                if (getter == null) {
                    createGetter(clazz, getterName, fieldType, field);
                }
                String setterName = "set" + fieldName;
                CtClass[] args = new CtClass[]{fieldType};
                if (!isFinal(field)) {
                    try {
                        CtMethod ctMethod = clazz.getDeclaredMethod(setterName, args);
                        if (ctMethod.getParameterTypes().length != 1 || !ctMethod.getParameterTypes()[0].equals(field.getType())
                                || Modifier.isStatic(ctMethod.getModifiers())) {
                            throw new NotFoundException("it's not a setter !");
                        }
                    } catch (NotFoundException e) {
                        //add setter method
                        createSetter(clazz, setterName, args, field);
                    }
                }
                // 查找作为id的字段
                if (!idGetSetFixed) {
                    if (field.getAnnotation(javax.persistence.Id.class) != null) {
                        try {
                            clazz.getDeclaredMethod(ID_GETTER_NAME);
                        } catch (NotFoundException e) {
                            createIdGetter(clazz, getterName, fieldType);
                        }

                        try {
                            clazz.getDeclaredMethod(ID_SETTER_NAME);
                        } catch (NotFoundException e) {
                            createIdSetter(clazz, setterName, args);
                        }

                        classpool.importPackage(fieldType.getPackageName());
                        classpool.importPackage(clazz.getName());

                        CtMethod _getFinder = new CtMethod(classpool.get(FINDER_C_NAME),
                                GET_FINDER_M_NAME,
                                new CtClass[]{classpool.get("java.lang.String")},
                                clazz);
                        _getFinder.setModifiers(Modifier.setPublic(Modifier.STATIC));
                        try {
                            _getFinder.setBody("{Finder finder = getFinderCache(" + clazz.getSimpleName() + ".class);" +
                                    "if(finder == null)" +
                                    "try {" +
                                    "   finder = (Finder) getFinderConstructor().newInstance(new Object[]{$1," +
                                    fieldType.getSimpleName() + ".class," + clazz.getSimpleName() + ".class});" +
                                    "   putFinderCache(" + clazz.getSimpleName() + ".class , finder);" +
                                    "} catch (Exception e) {" +
                                    "    throw new ameba.exceptions.AmebaException(e);" +
                                    "}" +
                                    "if (finder == null) {\n" +
                                    "    throw new ameba.db.model.Model.NotFinderFindException();\n" +
                                    "}" +
                                    "return finder;}");
                        } catch (CannotCompileException e) {
                            throw new CannotCompileException("Entity Model must be extends ameba.db.model.Model", e);
                        }
                        clazz.addMethod(_getFinder);
                        _getFinder = new CtMethod(classpool.get(FINDER_C_NAME),
                                GET_FINDER_M_NAME,
                                null,
                                clazz);

                        _getFinder.setModifiers(Modifier.setPublic(Modifier.STATIC));
                        _getFinder.setBody("{return (Finder) " + GET_FINDER_M_NAME + "(\"" + Model.DB_DEFAULT_SERVER_NAME + "\");}");
                        clazz.addMethod(_getFinder);
                        idGetSetFixed = true;
                    }
                }
            }

            cache.classBytecode = clazz.toBytecode();

            fireModelEnhancing(cache);

            try {
                descCache.put(cache.classFile, cache);
                return cache;
            } finally {
                clazz.detach();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CtMethod createSetter(CtClass clazz, String methodName, CtClass[] args, CtField field) throws CannotCompileException {
        CtMethod setter = new CtMethod(CtClass.voidType,
                methodName,
                args,
                clazz);
        setter.setModifiers(Modifier.PUBLIC);
        setter.setBody("{this." + field.getName() + "=$1;}");
        clazz.addMethod(setter);
        return setter;
    }

    private CtMethod createGetter(CtClass clazz, String methodName, CtClass fieldType, CtField field) throws CannotCompileException {
        CtMethod getter = new CtMethod(fieldType,
                methodName, null, clazz);
        getter.setModifiers(Modifier.PUBLIC); //访问权限
        getter.setBody("{ return this." + field.getName() + "; }");
        clazz.addMethod(getter);
        return getter;
    }

    private CtMethod createIdSetter(CtClass clazz, String methodName, CtClass[] args) throws CannotCompileException {
        CtMethod setter = new CtMethod(CtClass.voidType,
                ID_SETTER_NAME,
                args,
                clazz);
        setter.setModifiers(Modifier.PUBLIC);
        setter.setBody("{this." + methodName + "($1);}");
        clazz.addMethod(setter);
        return setter;
    }

    private CtMethod createIdGetter(CtClass clazz, String methodName, CtClass fieldType) throws CannotCompileException {
        CtMethod getter = new CtMethod(fieldType,
                ID_GETTER_NAME, null, clazz);
        getter.setModifiers(Modifier.PUBLIC); //访问权限
        getter.setBody("{ return this." + methodName + "(); }");
        clazz.addMethod(getter);
        return getter;
    }

    public static abstract class ModelEventListener {
        protected abstract byte[] enhancing(ModelDescription desc);

        protected abstract void loaded(Class clazz, ModelDescription desc, int index, int size);
    }


}
