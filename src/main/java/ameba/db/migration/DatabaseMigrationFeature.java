package ameba.db.migration;

import ameba.core.Application;
import ameba.db.DataSourceManager;
import ameba.exception.AmebaException;
import ameba.lib.LoggerOwner;
import ameba.util.IOUtils;
import com.google.common.collect.Maps;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.io.File;
import java.io.Writer;
import java.util.Map;

/**
 * Database Migration Feature
 *
 * @author icode
 * @since 0.1.6e
 */
public class DatabaseMigrationFeature extends LoggerOwner implements Feature {

    public static final String EVOLUTIONS_SUB_PATH = "conf/evolutions/";
    private static String evolutionsBasePath;
    @Inject
    private Application app;

    /**
     * <p>Getter for the field <code>evolutionsBasePath</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getEvolutionsBasePath() {
        if (evolutionsBasePath == null) {
            evolutionsBasePath = IOUtils.getResource("").getPath() + EVOLUTIONS_SUB_PATH;
        }
        return evolutionsBasePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(FeatureContext context) {
        boolean isEnabled = false;
        Configuration configuration = context.getConfiguration();
        for (String name : DataSourceManager.getDataSourceNames()) {
            if (!"false".equals(configuration.getProperty("db." + name + ".migration.enabled"))) {
                isEnabled = true;
                String ddlFile = getEvolutionsBasePath() + name + "/create.sql";
                File f = new File(ddlFile);
                if (f.exists() && f.isFile()) {
                    String paramKey = "db." + name + ".migration.parameter.";
                    int preIndex = paramKey.length() - 1;
                    Map<String, Object> params = Maps.newHashMap();
                    for (String key : configuration.getPropertyNames()) {
                        if (key.startsWith(paramKey)) {
                            params.put(paramKey.substring(preIndex), configuration.getProperty(key));
                        }
                    }
                    String schemaDefault = (String) configuration.getProperty("db." + name + ".schema.default");
                    String contexts = (String) configuration.getProperty("db." + name + ".contexts");
                    if (StringUtils.isBlank(contexts)) {
                        contexts = app.getMode().name();
                    }
                    String labels = (String) configuration.getProperty("db." + name + ".labels");
                    DatabaseMigrant migrant = new DatabaseMigrant(name, ddlFile, schemaDefault, contexts, labels, params);
                    Writer sqlWriter = new StringBuilderWriter();
                    try {
                        migrant.update();
                    } catch (Exception e) {
                        throw new AmebaException(e);
                    }

//                    try {
//                        migrant.getDataSourceConnection().createStatement().execute(sqlWriter.toString());
//                    } catch (SQLException e) {
//                        logger().error("update database version error", e);
//                    }

                    logger().debug(sqlWriter.toString());
                }
            }
        }
        return isEnabled;
    }


}
