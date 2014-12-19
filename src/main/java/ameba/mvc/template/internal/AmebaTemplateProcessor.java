package ameba.mvc.template.internal;

import ameba.Ameba;
import ameba.core.Frameworks;
import ameba.mvc.ErrorPageGenerator;
import ameba.mvc.template.TemplateException;
import ameba.mvc.template.TemplateNotFoundException;
import ameba.util.IOUtils;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.spi.AbstractTemplateProcessor;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
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
    public static final String INNER_TPL_DIR = "/__views/ameba/";
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

    @Context
    private MessageBodyWorkers workers;
    @Inject
    private ServiceLocator serviceLocator;

    private MessageBodyWriter<Viewable> viewableMessageBodyWriter;
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

    public MessageBodyWriter<Viewable> getViewableMessageBodyWriter() {
        if (viewableMessageBodyWriter == null)
            synchronized (this) {
                if (viewableMessageBodyWriter == null) {
                    viewableMessageBodyWriter = Frameworks.getViewableMessageBodyWriter(workers);
                }
            }
        return viewableMessageBodyWriter;
    }

    protected ErrorPageGenerator getErrorPageGenerator() {
        if (errorPageGenerator == null)
            synchronized (this) {
                if (errorPageGenerator == null) {
                    this.errorPageGenerator = Frameworks.getErrorPageGenerator(serviceLocator);
                }
            }
        return errorPageGenerator;
    }

    @Override
    public T resolve(String name, MediaType mediaType) {
        T t = super.resolve(name, mediaType);

        /*if (t == null && name != null && name.startsWith("/")) {
            t = super.resolve("/" + PROTECTED_DIR + name, mediaType);
        }*/

        if (t == null && name != null && name.startsWith(INNER_TPL_DIR)) {
            for (String ex : supportedExtensions) {
                String file = name.endsWith(ex) ? name : name + ex;
                InputStream in = IOUtils.getResourceAsStream(file);
                try {
                    if (in != null) {
                        try {
                            t = resolve(new InputStreamReader(in, charset));
                            if (t != null)
                                return t;
                        } finally {
                            IOUtils.closeQuietly(in);
                        }
                    } else if (resolve(file, (Reader) null) == null && Ameba.getApp().getMode().isDev()) {
                        throw new TemplateNotFoundException("未找到模板:" + getBasePath() + file);
                    }
                } catch (TemplateNotFoundException e) {
                    throw e;
                } catch (TemplateException e) {
                    throw e;
                } catch (Exception e) {
                    RuntimeException r;
                    if (e instanceof ParseException) {
                        r = createException((ParseException) e);
                    } else {
                        r = new TemplateException("Parse template error: " + getBasePath() + file, e, e.getStackTrace()[0].getLineNumber());
                    }
                    throw r;
                } finally {
                    IOUtils.closeQuietly(in);
                }
            }
        }

        return t;
    }


    protected abstract TemplateException createException(ParseException e);

    @Override
    protected T resolve(String templatePath, Reader reader) throws Exception {
        try {
            if (templatePath == null)
                return resolve(reader);
            else
                return resolve(templatePath);
        } catch (Exception e) {
            RuntimeException r;
            if (e instanceof ParseException) {
                r = createException((ParseException) e);
            } else if (e instanceof IllegalStateException) {
                return null;
            } else {
                r = new TemplateException("Parse template error: " + templatePath, e, e.getStackTrace()[0].getLineNumber());
            }
            throw r;
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    protected abstract T resolve(String templatePath) throws Exception;

    protected abstract T resolve(Reader reader) throws Exception;

    @Override
    public void writeTo(T templateReference, Viewable viewable, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws IOException {
        try {
            writeTemplate(templateReference, viewable, mediaType, httpHeaders, out);
        } catch (Exception e) {
            RuntimeException r;
            if (e instanceof ParseException) {
                r = createException((ParseException) e);
            } else {
                String file = getTemplateFile(templateReference);
                file = getBasePath() + file;
                File tFile = new File(file);
                String source = IOUtils.readFromResource(file);

                List<String> sources;

                if (!"".equals(source)) {
                    sources = Lists.newArrayList(source.split("\n"));
                } else {
                    sources = Lists.newArrayList();
                }

                if (e instanceof FileNotFoundException || e.getCause() instanceof FileNotFoundException) {
                    r = new TemplateNotFoundException(e.getMessage(),
                            e, -1, tFile, sources, -1);
                } else {
                    r = new TemplateException("Write template error in  " + file + ". " + e.getMessage(),
                            e, -1, tFile, sources, -1);
                }
            }

            viewable = (Viewable) getErrorPageGenerator().toResponse(r).getEntity();

            getViewableMessageBodyWriter().writeTo(viewable,
                    Viewable.class, Viewable.class, new Annotation[]{},
                    mediaType, httpHeaders,
                    out);
        }
    }

    public abstract String getTemplateFile(T templateReference);

    public abstract void writeTemplate(T templateReference, Viewable viewable, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws Exception;
}
