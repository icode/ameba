package ameba.compiler;

import javax.tools.ToolProvider;

/**
 * @author icode
 */
public class Config {
    private JavaCompiler compiler;

    public JavaCompiler getCompiler() {
        if (compiler == null)
            compiler = ToolProvider.getSystemJavaCompiler() == null ? new JdtCompiler() : new JdkCompiler();
        return compiler;
    }

    public void setCompiler(JavaCompiler compiler) {
        this.compiler = compiler;
    }
}
