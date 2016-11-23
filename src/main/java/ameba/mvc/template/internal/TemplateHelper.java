package ameba.mvc.template.internal;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.internal.util.collection.Refs;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.message.internal.VariantSelector;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>TemplateHelper class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class TemplateHelper {
    /**
     * Constant <code>TPL_ENGINE_DIR_PR="template.directory.engine."</code>
     */
    public static final String TPL_ENGINE_DIR_PR = "template.directory.engine.";
    /** Constant <code>TPL_DIR="template.directory"</code> */
    public static final String TPL_DIR = "template.directory";
    private static final Charset DEFAULT_ENCODING = Charset.forName("UTF-8");


    private TemplateHelper() {
    }

    /**
     * Return an absolute path to the given class where segments are separated using {@code delim} character and {@code path}
     * is appended to this path.
     *
     * @param resourceClass class for which an absolute path should be obtained.
     * @param path          segment to be appended to the resulting path.
     * @param delim         character used for separating path segments.
     * @return an absolute path to the resource class.
     */
    public static String getAbsolutePath(Class<?> resourceClass, String path, char delim) {
        return '/' + resourceClass.getName().replace('.', '/').replace('$', delim) + delim + path;
    }

    /**
     * Get media types for which the {@link org.glassfish.jersey.server.mvc.spi.ResolvedViewable resolved viewable} could be
     * produced.
     *
     * @param containerRequest request to obtain acceptable media types.
     * @param extendedUriInfo  uri info to obtain resource method from and its producible media types.
     * @param varyHeaderValue  Vary header reference.
     * @return list of producible media types.
     */
    public static List<MediaType> getProducibleMediaTypes(final ContainerRequest containerRequest,
                                                          final ExtendedUriInfo extendedUriInfo,
                                                          final Ref<String> varyHeaderValue) {
        final List<MediaType> producedTypes = getResourceMethodProducibleTypes(extendedUriInfo);
        final MediaType[] mediaTypes = producedTypes.toArray(new MediaType[producedTypes.size()]);

        final List<Variant> variants = VariantSelector.selectVariants(containerRequest, Variant.mediaTypes(mediaTypes)
                .build(), varyHeaderValue == null ? Refs.emptyRef() : varyHeaderValue);

        return Lists.transform(variants, new Function<Variant, MediaType>() {
            @Override
            public MediaType apply(final Variant variant) {
                return MediaTypes.stripQualityParams(variant.getMediaType());
            }
        });
    }

    /**
     * Get template name from given {@link org.glassfish.jersey.server.mvc.Viewable viewable} or return {@code index} if the given
     * viewable doesn't contain a valid template name.
     *
     * @param viewable viewable to obtain template name from.
     * @return {@code non-null}, {@code non-empty} template name.
     */
    public static String getTemplateName(final Viewable viewable) {
        return viewable.getTemplateName() == null || viewable.getTemplateName().isEmpty() ? "index" : viewable.getTemplateName();
    }

    /**
     * Return a list of producible media types of the last matched resource method.
     *
     * @param extendedUriInfo uri info to obtain resource method from.
     * @return list of producible media types of the last matched resource method.
     */
    private static List<MediaType> getResourceMethodProducibleTypes(final ExtendedUriInfo extendedUriInfo) {
        if (extendedUriInfo.getMatchedResourceMethod() != null
                && !extendedUriInfo.getMatchedResourceMethod().getProducedTypes().isEmpty()) {
            return extendedUriInfo.getMatchedResourceMethod().getProducedTypes();
        }
        return Arrays.asList(MediaType.WILDCARD_TYPE);
    }

    /**
     * Extract {@link org.glassfish.jersey.server.mvc.Template template} annotation from given list.
     *
     * @param annotations list of annotations.
     * @return {@link org.glassfish.jersey.server.mvc.Template template} annotation or {@code null} if this annotation is not present.
     */
    public static Template getTemplateAnnotation(final Annotation[] annotations) {
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof Template) {
                    return (Template) annotation;
                }
            }
        }

        return null;
    }

    /**
     * <p>getProduces.</p>
     *
     * @param annotations an array of {@link java.lang.annotation.Annotation} objects.
     * @return an array of {@link java.lang.String} objects.
     */
    public static String[] getProduces(final Annotation[] annotations) {
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof Produces) {
                    return ((Produces) annotation).value();
                }
            }
        }

        return null;
    }

    /**
     * Get output encoding from configuration.
     *
     * @param configuration Configuration.
     * @param suffix        Template processor suffix of the
     *                      to configuration property {@link org.glassfish.jersey.server.mvc.MvcFeature#ENCODING}.
     * @return Encoding read from configuration properties or a default encoding if no encoding is configured.
     */
    public static Charset getTemplateOutputEncoding(Configuration configuration, String suffix) {
        final String enc = PropertiesHelper.getValue(configuration.getProperties(), MvcFeature.ENCODING + suffix,
                String.class, null);
        if (enc == null) {
            return DEFAULT_ENCODING;
        } else {
            return Charset.forName(enc);
        }
    }


    /**
     * <p>getBasePaths.</p>
     *
     * @param basePath a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    public static Collection<String> getBasePaths(String basePath) {
        return Collections2.transform(Lists.newArrayList(basePath.split(",")),
                new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return s.startsWith("/") ? s.substring(1) : s;
                    }
                });
    }

    /**
     * <p>getExtends.</p>
     *
     * @param config a {@link javax.ws.rs.core.Configuration} object.
     * @param module a {@link java.lang.String} object.
     * @param defaultExtensions an array of {@link java.lang.String} objects.
     * @return an array of {@link java.lang.String} objects.
     */
    public static String[] getExtends(Configuration config, String module, String[] defaultExtensions) {
        String extension = getStringExtends(config, module);

        if (StringUtils.isBlank(extension)) {
            return defaultExtensions;
        } else {
            extension = extension.toLowerCase();
        }
        String[] extensions = extension.split(",");
        for (String ext : defaultExtensions) {
            if (!ArrayUtils.contains(extensions, ext.substring(1))
                    && !ArrayUtils.contains(extensions, ext)) {
                extensions = ArrayUtils.add(extensions, ext);
            }
        }
        return extensions;
    }

    /**
     * <p>getStringExtends.</p>
     *
     * @param config a {@link javax.ws.rs.core.Configuration} object.
     * @param module a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getStringExtends(Configuration config, String module) {
        Map<String, Object> map = config.getProperties();
        String extension = (String) map.get(AbstractTemplateProcessor.TEMPLATE_CONF_PREFIX + module + ".suffix");
        if (StringUtils.isNotBlank(extension)) {
            extension = StringUtils.deleteWhitespace(extension);
        }

        if (StringUtils.isBlank(extension)) {
            extension = (String) map.get(AbstractTemplateProcessor.TEMPLATE_CONF_PREFIX + "suffix");
            if (StringUtils.isNotBlank(extension)) {
                extension = StringUtils.deleteWhitespace(extension);
            }
        }

        return StringUtils.isBlank(extension) ? null : extension.toLowerCase();
    }


    /**
     * <p>getTemplateEngineDirConfig.</p>
     *
     * @param value a {@link java.lang.String} object.
     * @param engine a {@link java.lang.String} object.
     * @param context a {@link javax.ws.rs.core.FeatureContext} object.
     * @param tempConf a {@link java.util.Map} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getTemplateEngineDirConfig(String value,
                                                    String engine,
                                                    FeatureContext context,
                                                    Map<String, String> tempConf) {
        if (StringUtils.isBlank(value)) {
            value = tempConf.get(TPL_ENGINE_DIR_PR + engine);
        }

        if (StringUtils.isBlank(value)) {
            value = (String) context.getConfiguration().getProperty(TPL_ENGINE_DIR_PR + engine);
        }

        if (StringUtils.isBlank(value)) {
            value = (String) context.getConfiguration().getProperty(TPL_DIR);
        }
        return value;
    }

    /**
     * <p>getBasePath.</p>
     *
     * @param properties a {@link java.util.Map} object.
     * @param cfgSuffix a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getBasePath(Map<String, Object> properties, String cfgSuffix) {
        String basePath = PropertiesHelper.getValue(properties,
                MvcFeature.TEMPLATE_BASE_PATH + "." + cfgSuffix, String.class, null);
        if (basePath == null) {
            basePath = PropertiesHelper.getValue(properties, MvcFeature.TEMPLATE_BASE_PATH, "", null);
        }
        return basePath;
    }
}
