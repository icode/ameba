package ameba.container;

import ameba.container.server.Connector;
import ameba.core.Application;
import ameba.event.Event;
import ameba.event.SystemEventBus;
import ameba.feature.AmebaFeature;
import ameba.util.ClassUtils;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.server.ServerContainer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author icode
 */
public abstract class Container {
    public static final Logger logger = LoggerFactory.getLogger(Container.class);

    private Application application;

    public Container(final Application application) {
        this.application = application;
        prepare();
        configureWebSocketContainerProvider();
        configureHttpServer();
        registerBinder(application.getConfig());
        configureHttpContainer();
    }

    protected static void publishEvent(Event event) {
        SystemEventBus.publish(event);
        AmebaFeature.publishEvent(event);
    }

    @SuppressWarnings("unchecked")
    public static Container create(Application application) throws IllegalAccessException, InstantiationException {

        String provider = (String) application.getProperty("app.container.provider");

        try {
            Class<Container> ContainerClass = (Class<Container>) ClassUtils.getClass(provider);
            Constructor<Container> constructor = ContainerClass.<Container>getDeclaredConstructor(Application.class);
            return constructor.newInstance(application);
        } catch (InvocationTargetException e) {
            throw new ContainerException(e);
        } catch (NoSuchMethodException e) {
            throw new ContainerException(e);
        } catch (ClassNotFoundException e) {
            throw new ContainerException(e);
        } finally {
            logger.debug("HTTP容器为 {}", provider);
        }
    }

    public void registerBinder(ResourceConfig configuration) {
        configuration.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(getWebSocketContainerProvider()).to(ServerContainer.class).proxy(false);
                bindFactory(new Factory<Container>() {
                    @Override
                    public Container provide() {
                        return Container.this;
                    }

                    @Override
                    public void dispose(Container instance) {

                    }
                }).to(Container.class).proxy(false);
            }
        });
        configuration.registerInstances(new ContainerLifecycleListener() {
            @Override
            public void onStartup(org.glassfish.jersey.server.spi.Container container) {
                publishEvent(new StartupEvent(Container.this, application));
                logger.trace("应用容器已经启动");
            }

            @Override
            public void onReload(org.glassfish.jersey.server.spi.Container container) {
                publishEvent(new ReloadEvent(Container.this, application));
                logger.trace("应用容器已重新加载");
            }

            @Override
            public void onShutdown(org.glassfish.jersey.server.spi.Container container) {
                publishEvent(new ShutdownEvent(Container.this, application));
                logger.trace("应用容器已关闭");
            }
        });
    }

    public Application getApplication() {
        return application;
    }

    public abstract ServiceLocator getServiceLocator();

    protected abstract void prepare();

    protected abstract void configureHttpServer();

    protected abstract void configureHttpContainer();

    public abstract ServerContainer getWebSocketContainer();

    protected abstract void configureWebSocketContainerProvider();

    protected abstract WebSocketContainerProvider getWebSocketContainerProvider();

    public void start() throws Exception {
        logger.trace("应用容器启动中...");
        publishEvent(new StartEvent(this, application));
        doStart();
    }

    public void reload() {
        publishEvent(new BeginReloadEvent(this, application, application.getConfig()));
        doReload(application.getConfig());
    }

    public void reload(ResourceConfig configuration) {
        publishEvent(new BeginReloadEvent(this, application, configuration));
        registerBinder(configuration);
        doReload(configuration);
    }

    protected abstract void doReload(ResourceConfig configuration);

    protected abstract void doStart() throws Exception;

    public abstract void shutdown() throws Exception;

    public abstract List<Connector> getConnectors();

    public abstract String getType();

    public static class StartEvent extends ContainerEvent {

        public StartEvent(Container container, Application app) {
            super(container, app);
        }
    }

    private static class ContainerEvent implements Event {
        private Container container;
        private Application app;

        public ContainerEvent(Container container, Application app) {
            this.container = container;
            this.app = app;
        }

        public Container getContainer() {
            return container;
        }

        public Application getApp() {
            return app;
        }
    }

    public static class StartupEvent extends ContainerEvent {
        public StartupEvent(Container container, Application app) {
            super(container, app);
        }
    }

    public static class ReloadEvent extends ContainerEvent {
        public ReloadEvent(Container container, Application app) {
            super(container, app);
        }
    }

    public static class BeginReloadEvent extends ContainerEvent {
        ResourceConfig newConfig;

        public BeginReloadEvent(Container container, Application app, ResourceConfig newConfig) {
            super(container, app);
            this.newConfig = newConfig;
        }

        public ResourceConfig getNewConfig() {
            return newConfig;
        }
    }

    public static class ShutdownEvent extends ContainerEvent {
        public ShutdownEvent(Container container, Application app) {
            super(container, app);
        }
    }

    public abstract class WebSocketContainerProvider implements Factory<ServerContainer> {

        @Override
        public ServerContainer provide() {
            return getWebSocketContainer();
        }

        @Override
        public abstract void dispose(ServerContainer serverContainer);
    }

}
