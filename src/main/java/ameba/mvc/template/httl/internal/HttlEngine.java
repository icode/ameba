package ameba.mvc.template.httl.internal;

import httl.spi.Loader;
import httl.spi.Logger;
import httl.spi.engines.DefaultEngine;

import java.util.Arrays;
import java.util.List;

/**
 * @author icode
 */
public class HttlEngine extends DefaultEngine {
    private Loader loader;
    private boolean preload;
    private String[] templateSuffix;
    private Logger logger;
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

    public void setLogger(Logger logger) {
        this.logger = logger;
        super.setLogger(logger);
    }

    public void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory = templateDirectory;
        super.setTemplateDirectory(templateDirectory);
    }

    @Override
    public void inited() {
        if (preload) {
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
                            if (logger != null && logger.isDebugEnabled()) {
                                logger.debug("Preload the template: " + name);
                            }
                            getTemplate(name, getDefaultEncoding());
                        } catch (Exception e) {
                            if (logger != null && logger.isErrorEnabled()) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                }
                if (logger != null && logger.isInfoEnabled()) {
                    logger.info("Preload " + count + " templates from directory " + (templateDirectory == null ? "/" : templateDirectory) + " with suffix " + Arrays.toString(templateSuffix));
                }
            } catch (Exception e) {
                if (logger != null && logger.isErrorEnabled()) {
                    logger.error(e.getMessage(), e);
                }
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
