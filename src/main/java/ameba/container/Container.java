package ameba.container;

import ameba.Application;
import ameba.util.ClassUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * @author icode
 */
public abstract class Container {
    public static final Logger logger = LoggerFactory.getLogger(Container.class);

    protected Application application;

    public Container(Application application) {
        this.application = application;
    }

    public static Container create(Application application) throws IllegalAccessException, InstantiationException {

        String provider = (String) application.getProperty("app.container.provider");

        try {
            return (Container) ClassUtils.forName(provider)
                    .getConstructor(Application.class)
                    .newInstance(application);
        } catch (InvocationTargetException e) {
            //noop
        } catch (NoSuchMethodException e) {
            //noop
        } finally {
            logger.info("HTTP容器为 {}", provider);
        }
        return null;
    }

    public Application getApplication() {
        return application;
    }

    public abstract ServiceLocator getServiceLocator();

    public abstract void start() throws Exception;

    public abstract void shutdown() throws Exception;
}
