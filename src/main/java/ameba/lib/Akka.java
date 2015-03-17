package ameba.lib;

import akka.actor.ActorSystem;
import ameba.core.Application;
import org.apache.commons.lang3.StringUtils;

/**
 * @author icode
 */
public class Akka {
    private Akka() {
    }

    private static ActorSystem system;

    public static class AddOn extends ameba.core.AddOn {
        @Override
        public void setup(Application application) {
            String name = StringUtils.defaultString(application.getApplicationName(), "ameba");
            system = ActorSystem.create(name);
        }
    }

    public static ActorSystem system() {
        return system;
    }
}
