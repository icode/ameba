package ameba.container;

import ameba.Application;
import ameba.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * @author icode
 */
public abstract class Container {

    protected Application application;

    public Container(Application application) {
        this.application = application;
    }

    public static Container create(Application application) throws IllegalAccessException, InstantiationException {
        try {
            return (Container) ClassUtils.forName((String) application.getProperty("app.container.provider"))
                    .getConstructor(Application.class)
                    .newInstance(application);
        } catch (InvocationTargetException e) {
            //noop
        } catch (NoSuchMethodException e) {
            //noop
        }
        return null;
    }

    public Application getApplication() {
        return application;
    }

    public abstract void start() throws Exception;

    public abstract void shutdown() throws Exception;
}
