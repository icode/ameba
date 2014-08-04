package ameba.mvc.template.internal;

import ameba.mvc.ErrorPageGenerator;
import ameba.mvc.template.TemplateException;
import ameba.util.IOUtils;
import com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.base.Function;
import jersey.repackaged.com.google.common.collect.Collections2;
import jersey.repackaged.com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.spi.AbstractTemplateProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author icode
 */
@Provider
@Singleton
public abstract class AmebaTemplateProcessor<T> extends AbstractTemplateProcessor<T> {
    public static final String PROTECTED_DIR = "_protected";
    private static final Logger logger = LoggerFactory.getLogger(AmebaTemplateProcessor.class);
    private static final ThreadLocal<Viewable> ERROR_VIEWBLE_THREAD_LOCAL = new ThreadLocal<Viewable>();
    /**
     * Create an instance of the processor with injected {@link javax.ws.rs.core.Configuration config} and
     * (optional) {@link javax.servlet.ServletContext servlet context}.
     *
     * @param config              configuration to configure this processor from.
     * @param servletContext      (optional) servlet context to obtain template resources from.
     * @param propertySuffix      suffix to distinguish properties for current template processor.
     * @param supportedExtensions supported template file extensions.
     */

    Set<String> supportedExtensions;
    @Inject
    private ServiceLocator serviceLocator;
    private ErrorPageGenerator errorPageGenerator;
    private Charset charset;

    public AmebaTemplateProcessor(Configuration config, ServletContext servletContext, String propertySuffix, String... supportedExtensions) {
        super(config, servletContext, propertySuffix, supportedExtensions);
        String charsetStr = (String) config.getProperty("app.charset");
        charset = Charset.forName(StringUtils.isBlank(charsetStr) ? "utf-8" : charsetStr);
        this.supportedExtensions = Sets.newHashSet(Collections2.transform(
                Arrays.asList(supportedExtensions), new Function<String, String>() {
            @Override
            public String apply(String input) {
                input = input.toLowerCase();
                return input.startsWith(".") ? input : "." + input;
            }
        }));
    }

    protected Viewable getExceptionViewable() {
        return ERROR_VIEWBLE_THREAD_LOCAL.get();
    }

    protected void setExceptionViewable(Viewable e) {
        ERROR_VIEWBLE_THREAD_LOCAL.set(e);
    }

    protected void clearExceptionViewable() {
        ERROR_VIEWBLE_THREAD_LOCAL.remove();
    }

    protected ErrorPageGenerator getErrorPageGenerator() {
        if (errorPageGenerator == null) {
            final Set<ExceptionMapper> exceptionMappers = Sets.newLinkedHashSet();
            exceptionMappers.addAll(Providers.getCustomProviders(serviceLocator, ExceptionMapper.class));
            exceptionMappers.addAll(Providers.getProviders(serviceLocator, ExceptionMapper.class));
            for (ExceptionMapper t : exceptionMappers) {
                if (t instanceof ErrorPageGenerator) {
                    this.errorPageGenerator = (ErrorPageGenerator) t;
                    return this.errorPageGenerator;
                }
            }
        }
        return errorPageGenerator;
    }

    @Override
    public T resolve(String name, MediaType mediaType) {
        T t = super.resolve(name, mediaType);

        if (t == null && name != null && name.startsWith("/")) {
            t = super.resolve("/" + PROTECTED_DIR + name, mediaType);
        }

        if (t == null && name != null && name.startsWith("/")) {
            for (String ex : supportedExtensions) {
                String file = name.endsWith(ex) ? name : name + ex;
                InputStream in = IOUtils.getResourceAsStream(file);
                try {
                    if (in != null) {
                        t = resolve(file.startsWith("/__views/ameba/") ? null : file, new InputStreamReader(in, charset));
                        if (t != null)
                            return t;
                    }
                } catch (TemplateException e) {
                    resolveError(e);
                } catch (Exception e) {
                    if (e instanceof ParseException) {
                        e = createException((ParseException) e);
                    } else {
                        e = new TemplateException("Parse template error", e, e.getStackTrace()[0].getLineNumber());
                    }
                    resolveError(e);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            logger.warn("close template input stream has error", e);
                        }
                    }
                }
            }
        }

        return t;
    }


    protected TemplateException createException(ParseException e) {
        List<String> msgSource = Lists.newArrayList(e.getMessage().split("\n"));
        File file = new File(getBasePath() + msgSource.get(2));
        List<String> source = Lists.newArrayList();
        source.add(msgSource.get(4));
        source.add(msgSource.get(5));
        Integer line;
        try {
            line = Integer.valueOf(msgSource.get(1).split(",")[1].split(":")[1].trim());
        } catch (Exception ex) {
            line = 0;
        }
        return new TemplateException(msgSource.get(0) + "\n" + msgSource.get(1).replace(", in:", ""), e, line, file, source, 0);
    }

    @Override
    protected T resolve(String templatePath, Reader reader) throws Exception {
        try {
            if (templatePath == null)
                return resolve(reader);
            else
                return resolve(templatePath);
        } catch (Exception e) {
            if (e instanceof ParseException) {
                e = createException((ParseException) e);
            } else {
                e = new TemplateException("Parse template error", e, e.getStackTrace()[0].getLineNumber());
            }
            return resolveError(e);
        } finally {
            reader.close();
        }
    }

    protected abstract T resolve(String templatePath) throws Exception;

    protected abstract T resolve(Reader reader) throws Exception;

    @Override
    public void writeTo(T templateReference, Viewable viewable, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws IOException {
        try {
            if (getExceptionViewable() != null) {
                viewable = getExceptionViewable();
                clearExceptionViewable();
            }
            writeTemplate(templateReference, viewable, mediaType, httpHeaders, out);
        } catch (Exception e) {
            if (e instanceof ParseException) {
                e = createException((ParseException) e);
            } else {
                e = new TemplateException("Write template error", e, e.getStackTrace()[0].getLineNumber());
            }
            writeTo(resolveError(e), (Viewable) null, MediaType.TEXT_HTML_TYPE, httpHeaders, out);
        }
    }

    T resolveError(Exception e) {
        Viewable v = (Viewable) getErrorPageGenerator().toResponse(e).getEntity();
        setExceptionViewable(v);
        return resolve(v.getTemplateName(), MediaType.TEXT_HTML_TYPE);
    }

    public abstract void writeTemplate(T templateReference, Viewable viewable, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws Exception;
}
