package ameba.container;

import ameba.container.server.Connector;
import ameba.core.Application;
import ameba.event.Event;
import ameba.event.SystemEventBus;
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
 * <p>Abstract Container class.</p>
 *
 * @author icode
 */
public abstract class Container {
    /**
     * Constant <code>logger</code>
     */
    public static final Logger logger = LoggerFactory.getLogger(Container.class);

    private Application application;

    /**
     * <p>Constructor for Container.</p>
     *
     * @param application a {@link ameba.core.Application} object.
     */
    public Container(final Application application) {
        this.application = application;
        prepare();
        configureWebSocketContainerProvider();
        configureHttpServer();
        registerBinder(application.getConfig());
        configureHttpContainer();
    }

    /**
     * <p>create.</p>
     *
     * @param application a {@link ameba.core.Application} object.
     * @return a {@link ameba.container.Container} object.
     * @throws java.lang.IllegalAccessException if any.
     * @throws java.lang.InstantiationException if any.
     */
    @SuppressWarnings("unchecked")
    public static Container create(Application application) throws IllegalAccessException, InstantiationException {

        String provider = (String) application.getProperty("app.container.provider");
        logger.debug("HTTP容器实现 {}", provider);
        try {
            Class<Container> ContainerClass = (Class<Container>) ClassUtils.getClass(provider);
            Constructor<Container> constructor = ContainerClass.<Container>getDeclaredConstructor(Application.class);
            return constructor.newInstance(application);
        } catch (InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new ContainerException(e);
        }
    }

    /**
     * <p>registerBinder.</p>
     *
     * @param configuration a {@link org.glassfish.jersey.server.ResourceConfig} object.
     * @since 0.1.6e
     */
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
                SystemEventBus.publish(new StartupEvent(Container.this, application));
                logger.trace("应用容器已经启动");
            }

            @Override
            public void onReload(org.glassfish.jersey.server.spi.Container container) {
                SystemEventBus.publish(new ReloadedEvent(Container.this, application));
                logger.trace("应用容器已重新加载");
            }

            @Override
            public void onShutdown(org.glassfish.jersey.server.spi.Container container) {
                SystemEventBus.publish(new ShutdownEvent(Container.this, application));
                logger.trace("应用容器已关闭");
            }
        });
    }

    /**
     * <p>Getter for the field <code>application</code>.</p>
     *
     * @return a {@link ameba.core.Application} object.
     */
    public Application getApplication() {
        return application;
    }

    /**
     * <p>getServiceLocator.</p>
     *
     * @return a {@link org.glassfish.hk2.api.ServiceLocator} object.
     */
    public abstract ServiceLocator getServiceLocator();

    /**
     * <p>prepare.</p>
     *
     * @since 0.1.6e
     */
    protected void prepare() {
    }

    /**
     * <p>configureHttpServer.</p>
     */
    protected abstract void configureHttpServer();

    /**
     * <p>configureHttpContainer.</p>
     */
    protected abstract void configureHttpContainer();

    /**
     * <p>getWebSocketContainer.</p>
     *
     * @return a {@link javax.websocket.server.ServerContainer} object.
     */
    public abstract ServerContainer getWebSocketContainer();

    /**
     * <p>configureWebSocketContainerProvider.</p>
     */
    protected abstract void configureWebSocketContainerProvider();

    /**
     * <p>getWebSocketContainerProvider.</p>
     *
     * @return a {@link ameba.container.Container.WebSocketContainerProvider} object.
     */
    protected abstract WebSocketContainerProvider getWebSocketContainerProvider();

    /**
     * <p>start.</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void start() throws Exception {
        logger.trace("应用容器启动中...");
        SystemEventBus.publish(new StartEvent(this, application));
        doStart();
    }

    /**
     * <p>reload.</p>
     *
     * @since 0.1.6e
     */
    public void reload() {
        SystemEventBus.publish(new BeginReloadEvent(this, application, application.getConfig()));
        doReload(application.getConfig());
    }

    /**
     * <p>reload.</p>
     *
     * @param configuration a {@link org.glassfish.jersey.server.ResourceConfig} object.
     * @since 0.1.6e
     */
    public void reload(ResourceConfig configuration) {
        SystemEventBus.publish(new BeginReloadEvent(this, application, configuration));
        registerBinder(configuration);
        doReload(configuration);
    }

    /**
     * <p>doReload.</p>
     *
     * @param configuration a {@link org.glassfish.jersey.server.ResourceConfig} object.
     */
    protected abstract void doReload(ResourceConfig configuration);

    /**
     * <p>doStart.</p>
     *
     * @throws java.lang.Exception if any.
     */
    protected abstract void doStart() throws Exception;

    /**
     * <p>shutdown.</p>
     *
     * @throws java.lang.Exception if any.
     */
    public abstract void shutdown() throws Exception;

    /**
     * <p>getConnectors.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public abstract List<Connector> getConnectors();

    /**
     * <p>getType.</p>
     *
     * @return a {@link java.lang.String} object.
     */
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

    public static class ReloadedEvent extends ContainerEvent {
        public ReloadedEvent(Container container, Application app) {
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
