package ameba.db.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javassist.*;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.ResourceFinder;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 模型管理器
 *
 * @author: ICode
 * @since: 13-8-18 上午10:39
 */
public class ModelManager {
    public static final Logger logger = LoggerFactory.getLogger(ModelManager.class);
    public static final Map<String, ModelManager> managerMap = Maps.newHashMap();
    public static final String ID_SETTER_NAME = "__setId__";
    public static final String ID_GETTER_NAME = "__getId__";
    public static final String GET_FINDER_M_NAME = "getFinder";
    public final static String BASE_MODEL_PKG = "ameba.db.model";
    public final static String BASE_MODEL_SIMPLE_NAME = "Model";
    public final static String BASE_MODEL_NAME = BASE_MODEL_PKG + "." + BASE_MODEL_SIMPLE_NAME;
    private static final Map<String, ModelDescription> descCache = Maps.newHashMap();
    private static final ClassPool pool = ClassPool.getDefault();
    private List<ModelDescription> modelClassesDescList = Lists.newArrayList();
    private List<ModelEventListener> listeners = Lists.newArrayList();
    private String[] packages;

    private ModelManager(String[] packages) {
        this.packages = packages;
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
                        logger.debug("load {} model manager class {}", name, desc.classFile);
                        desc.clazz = pool.makeClass(in).toClass();
                        manager.fireModelLoaded(desc.clazz, desc, index, size);
                    } catch (IOException e) {
                        logger.warn("load model class file [" + desc.classFile + "] error", e);
                    } catch (CannotCompileException e) {
                        logger.warn("load model class file [" + desc.classFile + "] error", e);
                    } finally {
                        index++;
                        try {
                            in.close();
                        } catch (IOException e) {
                            logger.warn("close class bytecode stream has a error", e);
                        }
                    }
                } else {
                    manager.fireModelLoaded(desc.clazz, desc, index, size);
                    index++;
                }
                logger.debug("clear {} model manager class {} desc", name, desc.classFile);
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
            desc.classBytecode = listener.enhancing(desc);
        }
    }

    private void loadClass() {
        ResourceFinder scanner = new PackageNamesScanner(packages, true);
        while (scanner.hasNext()) {
            scanner.next();
            InputStream in = scanner.open();
            try {
                ModelDescription desc = enhanceModel(in);
                if (desc != null)
                    modelClassesDescList.add(desc);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("close class file input stream error", e);
                }
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
            pool.importPackage(BASE_MODEL_PKG);
            CtClass clazz = pool.makeClass(in);
            ModelDescription cache = descCache.get(clazz.getURL().toExternalForm());
            if (cache != null) {
                return cache;
            }
            if (!clazz.hasAnnotation(Entity.class)) {
                return null;
            }
            logger.debug("增强模型类[{}]", clazz.getName());
            CtClass mClazz = clazz;

            cache = new ModelDescription();
            cache.classFile = mClazz.getURL().toExternalForm();
            cache.classSimpleName = mClazz.getSimpleName();
            cache.className = mClazz.getName();

            boolean idGetSetFixed = false;
            while (clazz != null) {
                for (CtField field : clazz.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    //add getter method
                    String fieldName = StringUtils.capitalize(field.getName());
                    String getterName = "get" + fieldName;
                    CtMethod getter = null;
                    CtClass fieldType = pool.get(field.getType().getName());
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
                    CtMethod setter = null;
                    CtClass[] args = new CtClass[]{fieldType};
                    try {
                        setter = clazz.getDeclaredMethod(setterName, args);
                    } catch (NotFoundException e) {
                        //noop
                    }
                    if (setter == null) {
                        //add setter method
                        createSetter(clazz, setterName, args, field);
                    }
                    // 查找作为id的字段
                    if (!idGetSetFixed) {
                        if (field.getAnnotation(javax.persistence.Id.class) != null) {
                            CtMethod idGetter = null;
                            CtMethod idSetter = null;
                            try {
                                idGetter = clazz.getDeclaredMethod(ID_GETTER_NAME);
                            } catch (NotFoundException e) {
                                //noop
                            }
                            if (idGetter == null)
                                createIdGetter(clazz, getterName, fieldType);

                            try {
                                idSetter = clazz.getDeclaredMethod(ID_SETTER_NAME);
                            } catch (NotFoundException e) {
                                //noop
                            }
                            if (idSetter == null)
                                createIdSetter(clazz, setterName, args);

                            pool.importPackage(fieldType.getPackageName());
                            pool.importPackage(mClazz.getName());

                            CtMethod _getFinder = new CtMethod(pool.get(Finder.class.getName()),
                                    GET_FINDER_M_NAME,
                                    new CtClass[]{pool.get(String.class.getName())},
                                    clazz);
                            _getFinder.setModifiers(Modifier.setPublic(Modifier.STATIC));
                            try {
                                _getFinder.setBody("{Finder finder = getFinderCache(" + mClazz.getSimpleName() + ".class);" +
                                        "if(finder == null)" +
                                        "try {" +
                                        "   finder = (Finder) getFinderConstructor().newInstance(new Object[]{$1," + fieldType.getSimpleName() + ".class," + mClazz.getSimpleName() + ".class});" +
                                        "   putFinderCache(" + mClazz.getSimpleName() + ".class , finder);" +
                                        "} catch (Exception e) {" +
                                        "    throw new RuntimeException(e);" +
                                        "}" +
                                        "if (finder == null) {\n" +
                                        "    throw new ameba.db.model.Model.NotFinderFindException();\n" +
                                        "}" +
                                        "return finder;}");
                            } catch (CannotCompileException e) {
                                throw new CannotCompileException("Entity Model must be extends ameba.db.model.Model", e);
                            }
                            clazz.addMethod(_getFinder);
                            _getFinder = new CtMethod(pool.get(Finder.class.getName()),
                                    GET_FINDER_M_NAME,
                                    null,
                                    clazz);

                            _getFinder.setModifiers(Modifier.setPublic(Modifier.STATIC));
                            _getFinder.setBody("{return (Finder) getFinder(DefaultProperties.DB_DEFAULT_SERVER_NAME);}");
                            clazz.addMethod(_getFinder);
//                            CtField finderField = new CtField(pool.get(Finder.class.getName()), ID_TYPE_FIELD_NAME, clazz);
//                            finderField.setModifiers(Modifier.STATIC | Modifier.PUBLIC | Modifier.FINAL);
//                            clazz.addField(finderField, "_getFinder();");
                            idGetSetFixed = true;
                        }
                    }
                }
                clazz = clazz.getSuperclass();
                if (clazz != null && clazz.getName().equals(BASE_MODEL_NAME)) {
                    break;
                }
            }

            cache.classBytecode = mClazz.toBytecode();

            fireModelEnhancing(cache);

            try {
                descCache.put(cache.classFile, cache);
                return cache;
            } finally {
                mClazz.detach();
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
