package ameba.db.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javassist.*;
import org.glassfish.jersey.server.ResourceFinder;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Locale.ENGLISH;

/**
 * 模型管理器
 *
 * @author: ICode
 * @since: 13-8-18 上午10:39
 */
public class ModelManager {
    public static final Logger                    logger         = LoggerFactory.getLogger(ModelManager.class);
    public static final Map<String, ModelManager> managerMap     = Maps.newHashMap();
    private             List<Class>               modelClassList = Lists.newArrayList();
    private String[] packages;

    private ModelManager(String[] packages) {
        this.packages = packages;
        ResourceFinder scanner = new PackageNamesScanner(packages, true);
        while (scanner.hasNext()) {
            scanner.next();
            Class clazz = enhanceModel(scanner.open());
            if (clazz != null)
                modelClassList.add(clazz);
        }
    }

    public static ModelManager create(String name, String[] packages) {
        ModelManager manager = getManager(name);
        if (manager == null) {
            synchronized (managerMap) {
                if (manager == null) {
                    manager = new ModelManager(packages);
                    managerMap.put(name, manager);
                }
            }
        }
        return manager;
    }

    public static ModelManager getManager(String name) {
        return managerMap.get(name);
    }

    private static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
    }

    public String[] getPackages() {
        return Arrays.copyOf(packages, packages.length);
    }

    public List<Class> getModelClasses() {
        return Lists.newArrayList(modelClassList);
    }

    private Class<?> enhanceModel(InputStream in) {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass clazz = pool.makeClass(in);
            if (!clazz.hasAnnotation(Entity.class)) {
                return null;
            }
            CtField[] fields = clazz.getDeclaredFields();
            logger.info("增强模型类[{}]", clazz.getName());
            for (CtField field : fields) {
                if (javassist.Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                //add getter method
                String fieldName = capitalize(field.getName());
                String methodName = "get" + fieldName;
                CtMethod getter = null;
                CtClass fieldType = pool.get(field.getType().getName());
                try {
                    getter = clazz.getDeclaredMethod(methodName);
                    if (getter == null) {
                        methodName = "is" + fieldName;
                        getter = clazz.getDeclaredMethod(methodName);
                    }
                } catch (NotFoundException e) {
                }
                if (getter == null) {
                    getter = new CtMethod(fieldType,
                            methodName, null, clazz);
                    getter.setModifiers(Modifier.PUBLIC); //访问权限
                    getter.setBody("{ return this." + field.getName() + "; }");
                    clazz.addMethod(getter);
                }

                methodName = "set" + fieldName;
                CtMethod setter = null;
                CtClass[] args = new CtClass[]{fieldType};
                try {
                    setter = clazz.getDeclaredMethod(methodName, args);
                } catch (NotFoundException e) {
                }
                if (setter == null) {
                    //add setter method
                    setter = new CtMethod(CtClass.voidType,
                            methodName,
                            args,
                            clazz);
                    setter.setModifiers(Modifier.PUBLIC);
                    setter.setBody("{this." + field.getName() + "=$1;}");
                    clazz.addMethod(setter);
                }
            }
            return clazz.toClass();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
