package ameba.mvc.template.internal;

import ameba.core.Frameworks;
import ameba.exception.AmebaException;
import ameba.mvc.ErrorPageGenerator;
import ameba.mvc.template.TemplateException;
import ameba.util.IOUtils;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.DataStructures;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.internal.LocalizationMessages;
import org.glassfish.jersey.server.mvc.internal.TemplateHelper;
import org.glassfish.jersey.server.mvc.spi.TemplateProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>Abstract AmebaTemplateProcessor class.</p>
 *
 * @author icode
 */
@Provider
@Singleton
public abstract class AmebaTemplateProcessor<T> implements TemplateProcessor<T> {
    private static Logger logger = LoggerFactory.getLogger(AmebaTemplateProcessor.class);
    private final ConcurrentMap<String, T> cache;
    private final String suffix;
    private final Configuration config;
    private final String[] basePath;
    private final Charset encoding;
    Set<String> supportedExtensions;
    @Context
    private MessageBodyWorkers workers;
    @Inject
    private ServiceLocator serviceLocator;
    private MessageBodyWriter<Viewable> viewableMessageBodyWriter;
    private ErrorPageGenerator errorPageGenerator;
    @Inject
    private javax.inject.Provider<ContainerRequest> request;

    /**
     * <p>Constructor for AmebaTemplateProcessor.</p>
     *
     * @param config              a {@link javax.ws.rs.core.Configuration} object.
     * @param propertySuffix      a {@link java.lang.String} object.
     * @param supportedExtensions a {@link java.lang.String} object.
     */
    public AmebaTemplateProcessor(Configuration config, String propertySuffix, String... supportedExtensions) {
        this.config = config;
        this.suffix = '.' + propertySuffix;
        Map<String, Object> properties = config.getProperties();
        String basePath = PropertiesHelper.getValue(properties, MvcFeature.TEMPLATE_BASE_PATH + this.suffix, String.class, null);
        if (basePath == null) {
            basePath = PropertiesHelper.getValue(properties, MvcFeature.TEMPLATE_BASE_PATH, "", null);
        }

        Collection<String> basePaths = Collections2.transform(Lists.newArrayList(basePath.split(",")), new Function<String, String>() {
            @Override
            public String apply(String s) {
                return s.startsWith("/") ? s.substring(1) : s;
            }
        });

        this.basePath = basePaths.toArray(new String[basePaths.size()]);

        Boolean cacheEnabled = PropertiesHelper.getValue(properties, MvcFeature.CACHE_TEMPLATES + this.suffix, Boolean.class, null);
        if (cacheEnabled == null) {
            cacheEnabled = PropertiesHelper.getValue(properties, MvcFeature.CACHE_TEMPLATES, false, null);
        }

        this.cache = cacheEnabled ? DataStructures.<String, T>createConcurrentMap() : null;
        this.encoding = TemplateHelper.getTemplateOutputEncoding(config, this.suffix);

        this.supportedExtensions = Sets.newHashSet(Collections2.transform(
                Arrays.asList(supportedExtensions), new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        input = input.toLowerCase();
                        return input.startsWith(".") ? input : "." + input;
                    }
                }));

    }

    /**
     * <p>Getter for the field <code>basePath</code>.</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    protected String[] getBasePath() {
        return this.basePath;
    }

    /**
     * <p>Getter for the field <code>viewableMessageBodyWriter</code>.</p>
     *
     * @return a {@link javax.ws.rs.ext.MessageBodyWriter} object.
     */
    public MessageBodyWriter<Viewable> getViewableMessageBodyWriter() {
        if (viewableMessageBodyWriter == null)
            synchronized (this) {
                if (viewableMessageBodyWriter == null) {
                    viewableMessageBodyWriter = Frameworks.getViewableMessageBodyWriter(workers);
                }
            }
        return viewableMessageBodyWriter;
    }

    /**
     * <p>Getter for the field <code>errorPageGenerator</code>.</p>
     *
     * @return a {@link ameba.mvc.ErrorPageGenerator} object.
     */
    protected ErrorPageGenerator getErrorPageGenerator() {
        if (errorPageGenerator == null)
            synchronized (this) {
                if (errorPageGenerator == null) {
                    this.errorPageGenerator = Frameworks.getErrorPageGenerator(serviceLocator);
                }
            }
        return errorPageGenerator;
    }

    private Collection<String> getTemplatePaths(String name) {

        Set<String> paths = Sets.newLinkedHashSet();

        for (String path : basePath) {
            paths.addAll(getTemplatePaths(name, path));
        }

        return paths;
    }

    private Collection<String> getTemplatePaths(String name, String basePath) {
        String lowerName = name.toLowerCase();
        String templatePath = basePath.endsWith("/") ? basePath + name.substring(1) : basePath + name;
        Iterator var4 = this.supportedExtensions.iterator();

        String extension;
        do {
            if (!var4.hasNext()) {
                final String finalTemplatePath = templatePath;
                return Collections2.transform(this.supportedExtensions, new Function<String, String>() {
                    public String apply(String input) {
                        return finalTemplatePath + input;
                    }
                });
            }

            extension = (String) var4.next();
        } while (!lowerName.endsWith(extension));

        return Collections.singleton(templatePath);
    }

    /**
     * <p>getTemplateObjectFactory.</p>
     *
     * @param serviceLocator a {@link org.glassfish.hk2.api.ServiceLocator} object.
     * @param type           a {@link java.lang.Class} object.
     * @param defaultValue   a {@link org.glassfish.jersey.internal.util.collection.Value} object.
     * @param <F>            a F object.
     * @return a F object.
     */
    protected <F> F getTemplateObjectFactory(ServiceLocator serviceLocator, Class<F> type, Value<F> defaultValue) {
        Object objectFactoryProperty = this.config.getProperty("jersey.config.server.mvc.factory" + this.suffix);
        if (objectFactoryProperty != null) {
            if (type.isAssignableFrom(objectFactoryProperty.getClass())) {
                return type.cast(objectFactoryProperty);
            }

            Class factoryClass = null;
            if (objectFactoryProperty instanceof String) {
                factoryClass = (Class) ReflectionHelper.classForNamePA((String) objectFactoryProperty).run();
            } else if (objectFactoryProperty instanceof Class) {
                factoryClass = (Class) objectFactoryProperty;
            }

            if (factoryClass != null) {
                if (type.isAssignableFrom(factoryClass)) {
                    return type.cast(serviceLocator.create(factoryClass));
                }

                logger.warn(LocalizationMessages.WRONG_TEMPLATE_OBJECT_FACTORY(factoryClass, type));
            }
        }

        return defaultValue.get();
    }

    /**
     * <p>setContentType.</p>
     *
     * @param mediaType   a {@link javax.ws.rs.core.MediaType} object.
     * @param httpHeaders a {@link javax.ws.rs.core.MultivaluedMap} object.
     * @return a {@link java.nio.charset.Charset} object.
     * @since 0.1.6e
     */
    protected Charset setContentType(MediaType mediaType, MultivaluedMap<String, Object> httpHeaders) {
        String charset = mediaType.getParameters().get("charset");
        Charset encoding;
        MediaType finalMediaType;
        if (charset == null) {
            encoding = this.getEncoding();
            HashMap<String, String> typeList = Maps.newHashMap(mediaType.getParameters());
            typeList.put("charset", encoding.name());
            finalMediaType = new MediaType(mediaType.getType(), mediaType.getSubtype(), typeList);
        } else {
            encoding = Charset.forName(charset);
            finalMediaType = mediaType;
        }

        List<Object> typeList = Lists.newArrayListWithCapacity(1);
        typeList.add(finalMediaType.toString());
        httpHeaders.put("Content-Type", typeList);
        return encoding;
    }

    /**
     * <p>Getter for the field <code>encoding</code>.</p>
     *
     * @return a {@link java.nio.charset.Charset} object.
     * @since 0.1.6e
     */
    protected Charset getEncoding() {
        return this.encoding;
    }

    private T resolve(String name) {
        Iterator var2 = this.getTemplatePaths(name).iterator();

        String template;
        InputStreamReader reader;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            template = (String) var2.next();
            InputStream e;
            e = Thread.currentThread().getStackTrace()[0].getClass().getResourceAsStream(template);
            if (e == null) {
                e = IOUtils.getResourceAsStream(template);
            }

            reader = e != null ? new InputStreamReader(e) : null;

            if (reader == null) {
                try {
                    reader = new InputStreamReader(new FileInputStream(template), this.encoding);
                } catch (FileNotFoundException var16) {
                    //no op
                }
            }
        } while (reader == null);

        try {
            return this.resolve(template, reader);
        } catch (Exception e) {
            logger.warn(LocalizationMessages.TEMPLATE_RESOLVE_ERROR(template), e);
            RuntimeException r;
            try {
                r = createException(e, null);
            } catch (Exception ex) {
                if (ex instanceof AmebaException) {
                    r = (RuntimeException) ex;
                } else {
                    r = new TemplateException("create resolve Exception error", ex, -1);
                }
            }
            throw r;
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T resolve(String name, MediaType mediaType) {
        if (this.cache != null) {
            if (!this.cache.containsKey(name)) {
                this.cache.putIfAbsent(name, this.resolve(name));
            }

            return this.cache.get(name);
        } else {
            return this.resolve(name);
        }
    }

    /**
     * <p>createException.</p>
     *
     * @param e        a {@link java.lang.Exception} object.
     * @param template a T object.
     * @return a {@link ameba.mvc.template.TemplateException} object.
     */
    protected abstract TemplateException createException(Exception e, T template);

    /**
     * <p>resolve.</p>
     *
     * @param templatePath a {@link java.lang.String} object.
     * @param reader       a {@link java.io.Reader} object.
     * @return a T object.
     * @throws java.lang.Exception if any.
     */
    protected abstract T resolve(String templatePath, Reader reader) throws Exception;

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeTo(T templateReference, Viewable viewable, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws IOException {
        try {
            writeTemplate(templateReference, viewable, mediaType, httpHeaders, out);
        } catch (Exception e) {
            RuntimeException r;
            try {
                r = createException(e, templateReference);
            } catch (Exception ex) {
                if (ex instanceof AmebaException) {
                    r = (RuntimeException) ex;
                } else {
                    r = new TemplateException("create writeTo Exception error", ex, -1);
                }
            }

            try {
                viewable = (Viewable) getErrorPageGenerator().toResponse(r).getEntity();
                getViewableMessageBodyWriter().writeTo(viewable,
                        Viewable.class, Viewable.class, new Annotation[]{},
                        mediaType, httpHeaders,
                        out);
                request.get().getResponseWriter()
                        .writeResponseStatusAndHeaders(
                                -1,
                                new ContainerResponse(
                                        request.get(),
                                        Response.serverError().build()
                                )
                        ).flush();
            } catch (Exception ex) {
                logger.error("send error message error", ex);
            }
        }
    }

    /**
     * <p>writeTemplate.</p>
     *
     * @param templateReference a T object.
     * @param viewable          a {@link org.glassfish.jersey.server.mvc.Viewable} object.
     * @param mediaType         a {@link javax.ws.rs.core.MediaType} object.
     * @param httpHeaders       a {@link javax.ws.rs.core.MultivaluedMap} object.
     * @param out               a {@link java.io.OutputStream} object.
     * @throws java.lang.Exception if any.
     */
    public abstract void writeTemplate(T templateReference, Viewable viewable, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws Exception;
}
