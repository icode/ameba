package ameba.dev;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * @author icode
 */
public class JvmAgent {
    public static boolean enabled = false;
    static Instrumentation instrumentation;

    //jvm特性
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        JvmAgent.instrumentation = instrumentation;
        JvmAgent.enabled = true;
    }

    //jvm特性
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

    public static void reload(ClassDefinition... definitions) throws UnmodifiableClassException, ClassNotFoundException {
        instrumentation.redefineClasses(definitions);
    }
}
