package ameba.mvc.template.httl.internal;

import ameba.i18n.Messages;
import httl.spi.Loader;
import httl.spi.engines.DefaultEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * @author icode
 */
public class HttlEngine extends DefaultEngine {
    private static final Logger logger = LoggerFactory.getLogger(HttlEngine.class);
    private Loader loader;
    private boolean preload;
    private String[] templateSuffix;
    private String templateDirectory;
    private String defaultEncoding;

    public void setLoader(Loader loader) {
        this.loader = loader;
        super.setLoader(loader);
    }

    public void setPreload(boolean preload) {
        this.preload = preload;
        super.setPreload(preload);
    }

    public void setTemplateSuffix(String[] suffix) {
        this.templateSuffix = suffix;
        super.setTemplateSuffix(suffix);
    }

    public void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory = templateDirectory;
        super.setTemplateDirectory(templateDirectory);
    }

    @Override
    public void inited() {
        if (preload) {
            logger.info(Messages.get("info.template.httl.precompile"));
            try {
                int count = 0;
                for (String suffix : templateSuffix) {
                    List<String> list = loader.list(suffix);
                    if (list == null) {
                        continue;
                    }
                    count += list.size();
                    for (String name : list) {
                        try {
                            logger.debug("Preload the template: " + name);
                            getTemplate(name, getDefaultEncoding());
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
                logger.info("Preload " + count + " templates from directory " + (templateDirectory == null ? "/" : templateDirectory) + " with suffix " + Arrays.toString(templateSuffix));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public String getDefaultEncoding() {
        if (defaultEncoding == null) {
            defaultEncoding = getProperty("input.encoding", String.class);
        }
        return defaultEncoding;
    }
}
