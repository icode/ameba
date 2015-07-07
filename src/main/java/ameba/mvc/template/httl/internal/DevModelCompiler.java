package ameba.mvc.template.httl.internal;

import ameba.Ameba;
import ameba.exception.AmebaException;
import httl.spi.compilers.JavassistCompiler;
import httl.spi.compilers.JdkCompiler;
import httl.util.ClassUtils;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.LoaderClassPath;

/**
 * @author icode
 */
public class DevModelCompiler extends JavassistCompiler {

    @Override
    @SuppressWarnings("unchecked")
    protected void init() {
        pool = new ClassPool();
        if (Ameba.getApp().getMode().isDev()) {
            try {
                Class<ClassPath> pathClass =
                        (Class<ClassPath>) ClassUtils.forName("ameba.dev.classloading.enhancers.Enhancer$AppClassPath");
                ClassPath classPath = pathClass.getConstructor(ClassLoader.class)
                        .newInstance(ClassUtils.getContextClassLoader());
                super.pool.insertClassPath(classPath);
            } catch (Exception e) {
                throw new AmebaException("DevModelCompiler must be use for dev model and has dev module", e);
            }
        } else {
            ClassLoader contextLoader = ClassUtils.getContextClassLoader();
            try {
                contextLoader.loadClass(JdkCompiler.class.getName());
            } catch (ClassNotFoundException e) { // 如果线程上下文的ClassLoader不能加载当前httl.jar包中的类，则切换回httl.jar所在的ClassLoader
                contextLoader = JdkCompiler.class.getClassLoader();
            }

            pool.appendClassPath(new LoaderClassPath(contextLoader));
        }
        pool.appendSystemPath();
    }
}
