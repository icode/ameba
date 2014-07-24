package ameba.compiler;

import java.io.IOException;
import java.util.Arrays;
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

    public void compile(JavaSource... sources) {
        compile(Arrays.asList(sources));
    }

    public void compile(List<JavaSource> sources) {
        try {
            generateJavaClass(sources);

            for(JavaSource source : sources){
                source.saveClassFile();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void generateJavaClass(JavaSource... source) throws IOException;
    public abstract void generateJavaClass(List<JavaSource> sources) throws IOException;
}
