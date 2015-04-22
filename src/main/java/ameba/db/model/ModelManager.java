package ameba.db.model;

import ameba.core.AddOn;
import ameba.core.Application;
import ameba.db.DataSourceManager;
import ameba.event.Listener;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.ws.rs.core.Configuration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * <p>ModelManager class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public class ModelManager extends AddOn {

    /**
     * Constant <code>MODULE_MODELS_KEY_PREFIX="db.default.models."</code>
     */
    public static final String MODULE_MODELS_KEY_PREFIX = "db.default.models.";
    private static Logger logger = LoggerFactory.getLogger(ModelManager.class);
    private static Map<String, Set<Class>> modelMap;

    /**
     * <p>getModels.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.util.Set} object.
     */
    public static Set<Class> getModels(String name) {
        return modelMap.get(name);
    }

    /** {@inheritDoc} */
    @Override
    public void setup(final Application application) {
        Configuration config = application.getConfiguration();
        modelMap = Maps.newLinkedHashMap();

        Set<String> defaultModelsPkg = Sets.newLinkedHashSet();
        //db.default.models.pkg=
        for (String key : config.getPropertyNames()) {
            if (key.startsWith(MODULE_MODELS_KEY_PREFIX)) {
                String modelPackages = (String) config.getProperty(key);
                if (StringUtils.isNotBlank(modelPackages)) {
                    Collections.addAll(defaultModelsPkg, StringUtils.deleteWhitespace(modelPackages).split(","));
                }
            }
        }

        for (String name : DataSourceManager.getDataSourceNames()) {
            String modelPackages = (String) config.getProperty("db." + name + ".models");
            if (StringUtils.isNotBlank(modelPackages)) {
                final Set<String> pkgs = Sets.newHashSet(StringUtils.deleteWhitespace(modelPackages).split(","));

                //db.default.models.pkg=
                //db.default.models+=
                if (DataSourceManager.getDefaultDataSourceName().equalsIgnoreCase(name)) {
                    pkgs.addAll(defaultModelsPkg);
                }

                final String[] startsPackages = pkgs.toArray(new String[pkgs.size()]);
                application.packages(startsPackages);

                final Set<Class> classes = Sets.newHashSet();

                subscribeSystemEvent(Application.ClassFoundEvent.class, new Listener<Application.ClassFoundEvent>() {
                    @Override
                    public void onReceive(Application.ClassFoundEvent event) {
                        event.accept(new Application.ClassFoundEvent.ClassAccept() {
                            @Override
                            @SuppressWarnings("unchecked")
                            public boolean accept(Application.ClassFoundEvent.ClassInfo info) {
                                if (info.startsWithPackage(startsPackages)) {
                                    logger.trace("load class : {}", info.getClassName());
                                    Class clazz = info.toClass();
                                    if (info.containsAnnotations(Entity.class, Embeddable.class)
                                            || Model.class.isAssignableFrom(clazz)) {
                                        classes.add(clazz);
                                    }
                                    return true;
                                }
                                return false;
                            }
                        });
                    }
                });

                modelMap.put(name, classes);
            }
        }

        defaultModelsPkg.clear();
    }

}
