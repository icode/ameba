package ameba.mvc.template.internal;

import ameba.exception.AmebaException;
import ameba.mvc.template.TemplateException;
import ameba.util.IOUtils;
import com.google.common.base.Charsets;
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
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.internal.LocalizationMessages;
import org.glassfish.jersey.server.mvc.spi.TemplateProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>Abstract AbstractTemplateProcessor class.</p>
 *
 * @author icode
 */
@Singleton
public abstract class AbstractTemplateProcessor<T> implements TemplateProcessor<T> {
    public static final String TEMPLATE_CONF_PREFIX = "template.";
    private static Logger logger = LoggerFactory.getLogger(AbstractTemplateProcessor.class);
    private final ConcurrentMap<String, T> cache;
    private final String suffix;
    private final Configuration config;
    private final String[] basePath;
    private final Charset encoding;
    private final Set<String> supportedExtensions;
    @Context
    private ResourceInfo resourceInfo;

    /**
     * <p>Constructor for AbstractTemplateProcessor.</p>
     *
     * @param config              a {@link javax.ws.rs.core.Configuration} object.
     * @param propertySuffix      a {@link java.lang.String} object.
     * @param supportedExtensions a {@link java.lang.String} object.
     */
    public AbstractTemplateProcessor(Configuration config, String propertySuffix, String... supportedExtensions) {
        this.config = config;
        this.suffix = '.' + propertySuffix;
        Map<String, Object> properties = config.getProperties();
        String basePath = TemplateHelper.getBasePath(properties, propertySuffix);

        Collection<String> basePaths = TemplateHelper.getBasePaths(basePath);

        this.basePath = basePaths.toArray(new String[basePaths.size()]);

        Boolean cacheEnabled = PropertiesHelper.getValue(properties,
                MvcFeature.CACHE_TEMPLATES + this.suffix, Boolean.class, null);
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

    private Collection<String> getTemplatePaths(String name) {

        Set<String> paths = Sets.newLinkedHashSet();

        for (String path : basePath) {
            paths.addAll(getTemplatePaths(name, path));
        }

        return paths;
    }

    private Collection<String> getTemplatePaths(String name, String basePath) {
        String lowerName = name.toLowerCase();
        final String templatePath = basePath.endsWith("/") ? basePath + name.substring(1) : basePath + name;

        // Check whether the given name ends with supported suffix.
        for (final String extension : supportedExtensions) {
            if (lowerName.endsWith(extension)) {
                return Collections.singleton(templatePath);
            }
        }

        return Collections2.transform(this.supportedExtensions, new Function<String, String>() {
            public String apply(String input) {
                return templatePath + input;
            }
        });
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
        Object objectFactoryProperty = this.config.getProperty(MvcFeature.TEMPLATE_OBJECT_FACTORY + this.suffix);
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
            try {
                encoding = Charset.forName(charset);
            } catch (Exception e) {
                encoding = Charsets.UTF_8;
            }
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

    protected String resolveJarFile() {
        Class resourceClass = resourceInfo.getResourceClass();
        if (resourceClass == null) return null;
        String classFile = resourceClass.getName().replace(".", "/") + ".class";
        URL classUrl = IOUtils.getResource(classFile);
        String urlProtocol = classUrl.getProtocol();
        if (urlProtocol.equals("jar")) {
            String path = classUrl.getPath();
            return path.substring(0, path.length() - classFile.length() - 1);
        }
        return null;
    }

    protected URL getNearTemplateURL(String jarFile, String template) {
        if (jarFile != null) {
            Enumeration<URL> urls = IOUtils.getResources(template);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (url.getPath().startsWith(jarFile)) {
                    return url;
                }
            }
        }
        return null;
    }


    private T resolve(String name) {
        Collection<String> tpls = this.getTemplatePaths(name);
        Iterator iterator = tpls.iterator();
        String jarFile = resolveJarFile();
        String template;
        InputStreamReader reader = null;
        URL url = null;
        do {
            if (!iterator.hasNext()) {
                break;
            }

            template = (String) iterator.next();

            url = getNearTemplateURL(jarFile, template);

            reader = resolveReader(url);
        } while (reader == null);

        if (reader == null) {
            iterator = tpls.iterator();
            do {
                if (!iterator.hasNext()) {
                    return null;
                }

                template = (String) iterator.next();

                url = IOUtils.getResource(template);

                reader = resolveReader(url);
            } while (reader == null);
        }

        try {
            return this.resolve(url, reader);
        } catch (Exception e) {
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

    protected InputStreamReader resolveReader(URL url) {

        if (url != null) {
            try {
                InputStream in = url.openStream();
                if (in != null)
                    return new InputStreamReader(in, this.encoding);
            } catch (IOException e) {
                logger.warn("resolve template error", e);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T resolve(String name, MediaType mediaType) {
        if (this.cache != null) {
            if (!this.cache.containsKey(name)) {
                T t = this.resolve(name);
                if (t != null)
                    this.cache.putIfAbsent(name, t);
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
     * @param templateURL a {@link java.lang.String} object.
     * @param reader      a {@link java.io.Reader} object.
     * @return a T object.
     * @throws java.lang.Exception if any.
     */
    protected abstract T resolve(URL templateURL, Reader reader) throws Exception;

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeTo(T templateReference, Viewable viewable, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws IOException {
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
            throw r;
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
