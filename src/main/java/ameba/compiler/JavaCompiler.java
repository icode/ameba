package ameba.compiler;

import java.io.IOException;
import java.util.List;

public abstract class JavaCompiler {
    protected ClassLoader classLoader;
    protected Config config;

    public static JavaCompiler create(ClassLoader classloader, Config config) {
        try {
            JavaCompiler jc = config.getCompiler();
            jc.classLoader = classloader;
            jc.initialize();
            return jc;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void initialize(){}

    public Class<?> compile(JavaSource source) {
        try {
            source.clean();
            source.saveJavaFile();

            generateJavaClass(source);

            return classLoader.loadClass(source.getClassName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void generateJavaClass(JavaSource... source) throws IOException;
    public abstract void generateJavaClass(List<JavaSource> sources) throws IOException;
}
