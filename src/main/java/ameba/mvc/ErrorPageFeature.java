package ameba.mvc;

import ameba.mvc.template.internal.HttlViewProcessor;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * 错误处理页面配置
 * Created by icode on 14-3-25.
 */
@ConstrainedTo(RuntimeType.SERVER)
public class ErrorPageFeature implements Feature {
    private static final Logger logger = LoggerFactory.getLogger(ErrorPageFeature.class);

    private HttlViewProcessor httlViewProcessor;

    @Inject
    public ErrorPageFeature(ServiceLocator locator) {
        httlViewProcessor = locator.create(HttlViewProcessor.class);
    }

    @Override
    public boolean configure(FeatureContext featureContext) {
        String generatorClass = (String) featureContext.getConfiguration().getProperty("http.error.page.generator");
        if (StringUtils.isNotBlank(generatorClass)) {
            try {
                Class generatorClazz = Class.forName(generatorClass);
                ErrorPageGenerator.setTemplateProcessor(httlViewProcessor);
                featureContext.register(generatorClazz);

            } catch (ClassNotFoundException e) {
                logger.error("获取 http.error.page.generator 类失败", e);
            }
        }
        return true;
    }
}
