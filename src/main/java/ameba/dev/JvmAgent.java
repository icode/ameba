package ameba.dev;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.jar.JarFile;

/**
 * @author icode
 */
public class JvmAgent {
    public static boolean enabled = false;
    static Instrumentation instrumentation;

    //jvm特性，不能调用其他的方法
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        JvmAgent.instrumentation = instrumentation;
        JvmAgent.enabled = true;
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) throws Exception {
        JvmAgent.instrumentation = instrumentation;
        JvmAgent.enabled = true;
    }

    public static synchronized void initialize() {
        if (instrumentation == null) {
            String file = JvmAgent.class.getResource("").getPath();
            file = file.substring(0, file.lastIndexOf("!"));
            file = file.substring(file.lastIndexOf(":") + 1);
            AgentLoader.loadAgent(file);
        }
    }

    public static void addTransformer(ClassFileTransformer transformer, boolean canRetransform) {
        instrumentation.addTransformer(transformer, canRetransform);
    }

    public static long getObjectSize(Object objectToSize) {
        return instrumentation.getObjectSize(objectToSize);
    }

    public static boolean isRedefineClassesSupported() {
        return instrumentation.isRedefineClassesSupported();
    }

    public static void addTransformer(ClassFileTransformer transformer) {
        instrumentation.addTransformer(transformer);
    }

    public static void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix) {
        instrumentation.setNativeMethodPrefix(transformer, prefix);
    }

    public static Class[] getInitiatedClasses(ClassLoader loader) {
        return instrumentation.getInitiatedClasses(loader);
    }

    public static boolean isNativeMethodPrefixSupported() {
        return instrumentation.isNativeMethodPrefixSupported();
    }

    public static void appendToSystemClassLoaderSearch(JarFile jarfile) {
        instrumentation.appendToSystemClassLoaderSearch(jarfile);
    }

    public static Class[] getAllLoadedClasses() {
        return instrumentation.getAllLoadedClasses();
    }

    public static void appendToBootstrapClassLoaderSearch(JarFile jarfile) {
        instrumentation.appendToBootstrapClassLoaderSearch(jarfile);
    }

    public static boolean isRetransformClassesSupported() {
        return instrumentation.isRetransformClassesSupported();
    }

    public static boolean removeTransformer(ClassFileTransformer transformer) {
        return instrumentation.removeTransformer(transformer);
    }

    public static void redefineClasses(ClassDefinition... definitions) throws ClassNotFoundException, UnmodifiableClassException {
        instrumentation.redefineClasses(definitions);
    }

    public static void retransformClasses(Class<?>... classes) throws UnmodifiableClassException {
        instrumentation.retransformClasses(classes);
    }

    public static boolean isModifiableClass(Class<?> theClass) {
        return instrumentation.isModifiableClass(theClass);
    }

    public static void reload(ClassDefinition... definitions) throws UnmodifiableClassException, ClassNotFoundException {
        instrumentation.redefineClasses(definitions);
    }
}
