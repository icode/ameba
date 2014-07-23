package ameba.compiler;

import java.io.File;
import java.io.IOException;

public abstract class JavaCompiler {
    protected ClassLoader classloader;
    protected boolean debugEnabled;

    public static JavaCompiler create(ClassLoader classloader, Config config) {
        try {
            JavaCompiler jc = (JavaCompiler) config.getCompileTool().newInstance();
            jc.classloader = classloader;
            jc.debugEnabled = config.isCompileDebug();
            jc.initialize();
            return jc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void initialize() {
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public File getOutputdir() {
        return classloader.getOutputdir();
    }

    public Class<?> compile(JavaSource source) {
        try {
            source.clean();
            source.saveJavaFile();

            generateJavaClass(source);

            return classloader.loadClass(source.getQualifiedClassName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void generateJavaClass(JavaSource source) throws IOException;
}
