package ameba.container;

import ameba.Application;
import ameba.util.ClassUtils;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.server.ServerContainer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author icode
 */
public abstract class Container {
    public static final Logger logger = LoggerFactory.getLogger(Container.class);

    protected Application application;

    protected Container(Application application) {
        this.application = application;
    }

    @SuppressWarnings("unchecked")
    public static Container create(Application application) throws IllegalAccessException, InstantiationException {

        String provider = (String) application.getProperty("app.container.provider");

        try {
            Class<Container> ContainerClass = (Class<Container>) ClassUtils.forName(provider);
            Constructor<Container> constructor = ContainerClass.<Container>getDeclaredConstructor(Application.class);
            constructor.setAccessible(true);
            Container container = constructor.newInstance(application);
            constructor.setAccessible(false);
            application.register(container.getWebSocketContainerProvider());
            return container;
        } catch (InvocationTargetException e) {
            throw new ContainerException(e);
        } catch (NoSuchMethodException e) {
            throw new ContainerException(e);
        } finally {
            logger.info("HTTP容器为 {}", provider);
        }
    }

    public Application getApplication() {
        return application;
    }

    public abstract ServiceLocator getServiceLocator();

    public abstract ServerContainer getWebSocketContainer();

    protected abstract WebSocketContainerProvider getWebSocketContainerProvider();

    public abstract void start() throws Exception;

    public abstract void shutdown() throws Exception;


    public abstract class WebSocketContainerProvider implements Factory<ServerContainer> {

        @Override
        public ServerContainer provide() {
            return getWebSocketContainer();
        }

        @Override
        public abstract void dispose(ServerContainer serverContainer);
    }

}
