package ameba.container.internal;

import com.google.common.collect.Iterables;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.AbstractContainerLifecycleListener;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

import javax.ws.rs.core.Application;
import java.util.LinkedList;

/**
 * @author icode
 */
public class ConfigHelper {

    /**
     * Prevents instantiation.
     */
    private ConfigHelper() {
    }

    /**
     * Provides a single ContainerLifecycleListener instance based on the {@link ApplicationHandler application} configuration.
     * This method looks for providers implementing {@link ContainerLifecycleListener} interface and aggregates them into
     * a single umbrella listener instance that is returned.
     *
     * @param applicationHandler actual application from where to get the listener.
     * @return a single instance of a ContainerLifecycleListener, can not be null.
     */
    public static LifecycleListener getContainerLifecycleListener(final ApplicationHandler applicationHandler) {

        final Iterable<ContainerLifecycleListener> listeners = Iterables.concat(
                Providers.getAllProviders(applicationHandler.getServiceLocator(), ContainerLifecycleListener.class),
                new LinkedList<ContainerLifecycleListener>() {{
                    add(new ServiceLocatorShutdownListener());
                }});

        return new LifecycleListener() {

            @Override
            public void onReloadShutdown(Container container, Runnable tryScope) {
                for (final ContainerLifecycleListener listener : listeners) {
                    if (listener instanceof ServiceLocatorShutdownListener) {
                        container = new ContainerDelegate(container);
                        tryScope.run();
                    }
                    listener.onShutdown(container);
                }
            }

            @Override
            public void onStartup(final Container container) {
                for (final ContainerLifecycleListener listener : listeners) {
                    listener.onStartup(container);
                }
            }

            @Override
            public void onReload(final Container container) {
                for (final ContainerLifecycleListener listener : listeners) {
                    listener.onReload(container);
                }
            }

            @Override
            public void onShutdown(final Container container) {
                for (final ContainerLifecycleListener listener : listeners) {
                    listener.onShutdown(container);
                }
            }
        };
    }

    /**
     * Gets the most internal wrapped {@link Application application} class. This method is similar to
     * {@link ResourceConfig#getApplication()} except if provided application was created by wrapping multiple
     * {@link ResourceConfig} instances this method returns the original application and not a resource config wrapper.
     *
     * @param app jax-rs application
     * @return the original {@link Application} subclass.
     */
    public static Application getWrappedApplication(Application app) {
        while (app instanceof ResourceConfig) {
            final Application wrappedApplication = ((ResourceConfig) app).getApplication();
            if (wrappedApplication == app) {
                break;
            }
            app = wrappedApplication;
        }
        return app;
    }

    public static class ServiceLocatorShutdownListener extends AbstractContainerLifecycleListener {

        @Override
        public void onShutdown(final Container container) {
            final ApplicationHandler handler = container.getApplicationHandler();
            final ServiceLocator locator = handler.getServiceLocator();

            // Call @PreDestroy method on Application.
            locator.preDestroy(getWrappedApplication(handler.getConfiguration()));
            // Shutdown ServiceLocator.
            Injections.shutdownLocator(locator);
        }
    }

    private static class ContainerDelegate implements Container {

        private Container container;
        private ResourceConfig config;
        private ApplicationHandler handler;

        public ContainerDelegate(Container container) {
            this.container = container;
            this.config = container.getConfiguration();
            this.handler = container.getApplicationHandler();
        }

        @Override
        public ResourceConfig getConfiguration() {
            return config;
        }

        @Override
        public ApplicationHandler getApplicationHandler() {
            return handler;
        }

        @Override
        public void reload() {
            container.reload();
        }

        @Override
        public void reload(ResourceConfig configuration) {
            container.reload(configuration);
        }
    }

    public static abstract class LifecycleListener implements ContainerLifecycleListener {
        /**
         * use for reload Container.
         * <p>
         * the tryScope must run success then shutdown ServiceLocator
         * <p>
         * otherwise not shut down ServiceLocator
         *
         * @param container {@link Container}
         * @param tryScope try scope
         */
        public abstract void onReloadShutdown(final Container container, Runnable tryScope);
    }

}
