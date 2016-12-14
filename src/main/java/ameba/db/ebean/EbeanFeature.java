package ameba.db.ebean;

import ameba.core.Application;
import ameba.db.DataSourceManager;
import ameba.db.PersistenceExceptionMapper;
import ameba.db.ebean.internal.ModelInterceptor;
import ameba.db.ebean.jackson.JacksonEbeanModule;
import ameba.db.ebean.jackson.JsonIOExceptionMapper;
import ameba.db.ebean.migration.EbeanMigration;
import ameba.db.migration.Migration;
import ameba.db.migration.models.ScriptInfo;
import ameba.db.model.ModelManager;
import ameba.i18n.Messages;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.Lists;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.PersistBatch;
import io.ebean.config.ContainerConfig;
import io.ebean.config.JsonConfig;
import io.ebean.config.PropertiesWrapper;
import io.ebean.config.ServerConfig;
import io.ebeaninternal.api.SpiEbeanServer;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.hk2.utilities.binding.ScopedBindingBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * <p>EbeanFeature class.</p>
 *
 * @author icode
 * @since 2013-08-07
 */
public class EbeanFeature implements Feature {

    /**
     * Constant <code>SORT_PARAM_NAME="model.query.param.sort"</code>
     */
    public static final String SORT_PARAM_NAME = "model.query.param.sort";
    /**
     * Constant <code>PAGE_PARAM_NAME="model.query.param.page"</code>
     */
    public static final String PAGE_PARAM_NAME = "model.query.param.page";
    /**
     * Constant <code>PER_PAGE_PARAM_NAME="model.query.param.prePage"</code>
     */
    public static final String PER_PAGE_PARAM_NAME = "model.query.param.prePage";
    /**
     * Constant <code>REQ_TOTAL_COUNT_PARAM_NAME="model.query.param.requireTotalCount"</code>
     */
    public static final String REQ_TOTAL_COUNT_PARAM_NAME = "model.query.param.requireTotalCount";
    /**
     * Constant <code>REQ_TOTAL_COUNT_HEADER_NAME="model.query.requireTotalCount.header"</code>
     */
    public static final String REQ_TOTAL_COUNT_HEADER_NAME = "model.query.param.requireTotalCount.header";
    /**
     * Constant <code>DEFAULT_PER_PAGE_PARAM_NAME="model.query.param.perPage.default"</code>
     */
    public static final String DEFAULT_PER_PAGE_PARAM_NAME = "model.query.param.perPage.default";
    /**
     * Constant <code>DEFAULT_PER_PAGE_PARAM_NAME="model.query.param.perPage.max"</code>
     */
    public static final String MAX_PER_PAGE_PARAM_NAME = "model.query.param.perPage.max";
    /**
     * Constant <code>FILTER_PARAM_NAME="model.query.param.filter"</code>
     */
    public static final String FILTER_PARAM_NAME = "model.query.param.filter";
    private static final Logger logger = LoggerFactory.getLogger(EbeanFeature.class);
    private static final List<EbeanServer> SERVERS = Lists.newArrayList();
    @Inject
    private ServiceLocator locator;
    @Context
    private ObjectMapper objectMapper;
    @Context
    private XmlMapper xmlMapper;
    @Inject
    private Application application;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(final FeatureContext context) {
        if (!context.getConfiguration().isRegistered(PersistenceExceptionMapper.class)) {
            context.register(PersistenceExceptionMapper.class);
        }

        if (context.getConfiguration().isRegistered(ModelInterceptor.class)) {
            return false;
        }

        context.register(ModelInterceptor.class)
                .register(JsonIOExceptionMapper.class);

        for (EbeanServer server : SERVERS) {
            try {
                server.shutdown(true, true);
            } catch (Exception e) {
                logger.warn("shutdown old Ebean server has a error", e);
            }
        }
        SERVERS.clear();

        final Configuration appConfig = context.getConfiguration();

        final JsonFactory jsonFactory = objectMapper.getFactory();

        ContainerConfig containerConfig = new ContainerConfig();
        Properties cp = new Properties();
        cp.putAll(appConfig.getProperties());
        containerConfig.loadFromProperties(cp);
        for (final String name : DataSourceManager.getDataSourceNames()) {
            final ServerConfig config = new ServerConfig() {
                @Override
                public void loadFromProperties(Properties properties) {
                    loadSettings(new PropertiesWrapper("db", name, properties));
                }
            };
            config.setName(name);
            config.setJsonInclude(JsonConfig.Include.NON_EMPTY);
            config.setPersistBatch(PersistBatch.ALL);
            config.setUpdateAllPropertiesInBatch(false);
            config.loadFromProperties(cp);
            config.setPackages(null);
            config.setDataSourceJndiName(null);
            config.setDataSource(DataSourceManager.getDataSource(name));//设置为druid数据源
            config.setJsonFactory(jsonFactory);
            config.setContainerConfig(containerConfig);
            config.setDisableClasspathSearch(true);

            if (name.equals(DataSourceManager.getDefaultDataSourceName())) {
                config.setDefaultServer(true);
            } else {
                config.setDefaultServer(false);
            }

            Set<Class> classes = ModelManager.getModels(name);
            if (classes != null) {
                classes.forEach(config::addClass);
            }
            config.addClass(ScriptInfo.class);

            logger.debug(Messages.get("info.db.connect", name));

            final EbeanServer server = EbeanServerFactory.create(config);

            logger.info(Messages.get("info.db.connected", name, appConfig.getProperty("db." + name + ".url")));

            JacksonEbeanModule module = new JacksonEbeanModule(server, locator);

            objectMapper.registerModule(module);
            xmlMapper.registerModule(module);

            SERVERS.add(server);
        }

        ServiceLocatorUtilities.bind(locator, new AbstractBinder() {
            @Override
            protected void configure() {
                for (EbeanServer server : SERVERS) {
                    String name = server.getName();
                    createBuilder(server).named(name);

                    if (name.equals(DataSourceManager.getDefaultDataSourceName())) {
                        createBuilder(server);
                    }

                    bind(new EbeanMigration(application, (SpiEbeanServer) server))
                            .to(Migration.class)
                            .named(name)
                            .proxy(false);
                }
            }

            private ScopedBindingBuilder<SpiEbeanServer> createBuilder(EbeanServer server) {
                return bind((SpiEbeanServer) server)
                        .to(SpiEbeanServer.class)
                        .to(EbeanServer.class)
                        .proxy(false);
            }
        });
        return true;
    }
}
