package ameba.db.model;

import ameba.db.DataSourceFeature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * @author 张立鑫 IntelligentCode
 * @since 2013-08-07
 */
@Singleton
@ConstrainedTo(RuntimeType.SERVER)
public class EnhanceModelFeature implements Feature {

    private static final Logger logger = LoggerFactory.getLogger(EnhanceModelFeature.class);

    @Override
    public boolean configure(final FeatureContext context) {
        Configuration config = context.getConfiguration();
        for (String name : DataSourceFeature.getDataSourceNames()) {
            //db.default.models=reward.models
            String modelPackages = (String) config.getProperty("db." + name + ".models");
            if (StringUtils.isNotBlank(modelPackages)) {
                logger.info("创建ModelManager，[{}:{}]", name, modelPackages);
                ModelManager.create(name, modelPackages.split(","));
            }
        }
        return true;
    }
}