package ameba.lib;

import akka.actor.ActorSystem;
import ameba.container.event.ShutdownEvent;
import ameba.core.Application;
import ameba.event.Listener;
import ameba.event.SystemEventBus;
import ameba.util.LinkedProperties;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Properties;

/**
 * <p>Akka class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public class Akka {
    private static ActorSystem system;

    private Akka() {
    }

    /**
     * <p>system.</p>
     *
     * @return a {@link akka.actor.ActorSystem} object.
     */
    public static ActorSystem system() {
        return system;
    }

    public static class Addon extends ameba.core.Addon {
        @Override
        public void setup(Application application) {
            String name = StringUtils.defaultString(application.getApplicationName(), "ameba");
            Properties properties = new LinkedProperties();

            for (Map.Entry<String, Object> entry : application.getSrcProperties().entrySet()) {
                if (!entry.getKey().contains("*")) {
                    properties.put(entry.getKey(), entry.getValue());
                }
            }

            Config config = ConfigFactory.parseProperties(properties).withFallback(ConfigFactory.load());
            system = ActorSystem.create(name, config, application.getClassLoader());
            SystemEventBus.subscribe(ShutdownEvent.class, new Listener<ShutdownEvent>() {
                @Override
                public void onReceive(ShutdownEvent event) {
                    system.shutdown();
                }
            });
        }
    }
}
