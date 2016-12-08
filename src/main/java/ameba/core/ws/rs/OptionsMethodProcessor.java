package ameba.core.ws.rs;

import ameba.core.ServiceLocators;
import jersey.repackaged.com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.model.internal.RankedComparator;
import org.glassfish.jersey.model.internal.RankedProvider;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.model.internal.ModelProcessorUtil;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static ameba.message.internal.MediaType.WILDCARD_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;

/**
 * <p>OptionsMethodProcessor class.</p>
 *
 * @author icode
 * @since 0.1.6e
 *
 */
@Priority(Integer.MAX_VALUE)
@Singleton
public class OptionsMethodProcessor implements ModelProcessor {

    private static String SUPPORT_PATCH_MEDIA_TYPES = null;
    private static Iterable<OptionsResponseGenerator> generators;
    private final List<ModelProcessorUtil.Method> methodList;

    /**
     * Creates new instance.
     *
     * @param locator a {@link org.glassfish.hk2.api.ServiceLocator} object.
     */
    @Inject
    public OptionsMethodProcessor(ServiceLocator locator) {
        methodList = Lists.newArrayList();

        methodList.add(new ModelProcessorUtil.Method(HttpMethod.OPTIONS, WILDCARD_TYPE, WILDCARD_TYPE,
                GenericOptionsInflector.class));

        final Iterable<RankedProvider<OptionsResponseGenerator>> rankedProviders =
                ServiceLocators.getRankedProviders(locator, OptionsResponseGenerator.class);

        generators = ServiceLocators
                .sortRankedProviders(new RankedComparator<>(), rankedProviders);
    }

    /**
     * <p>getSupportPatchMediaTypes.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getSupportPatchMediaTypes() {
        if (SUPPORT_PATCH_MEDIA_TYPES == null) {
            synchronized (HttpPatchProperties.SUPPORT_PATCH_MEDIA_TYPES) {
                if (SUPPORT_PATCH_MEDIA_TYPES == null) {
                    SUPPORT_PATCH_MEDIA_TYPES = StringUtils.join(HttpPatchProperties.SUPPORT_PATCH_MEDIA_TYPES, ",");
                }
            }
        }
        return SUPPORT_PATCH_MEDIA_TYPES;
    }

    /**
     * <p>getSupportProduceMediaType.</p>
     *
     * @param containerRequestContext a {@link javax.ws.rs.container.ContainerRequestContext} object.
     * @return a {@link javax.ws.rs.core.MediaType} object.
     */
    protected static MediaType getSupportProduceMediaType(ContainerRequestContext containerRequestContext) {
        for (MediaType mediaType : containerRequestContext.getAcceptableMediaTypes()) {
            if (mediaType.isCompatible(TEXT_PLAIN_TYPE) ||
                    (mediaType.getType().equalsIgnoreCase("application")
                            && !mediaType.getSubtype().toLowerCase().contains("html"))
                    || mediaType.getSubtype().equalsIgnoreCase("json")
                    || mediaType.getSubtype().equalsIgnoreCase("xml")
                    || mediaType.getSubtype().toLowerCase().endsWith("+json")
                    || mediaType.getSubtype().toLowerCase().endsWith("+xml")) {
                return mediaType;
            }
        }
        return null;
    }

    /**
     * <p>generateRespBuilder.</p>
     *
     * @param containerRequestContext a {@link javax.ws.rs.container.ContainerRequestContext} object.
     * @param extendedUriInfo         a {@link org.glassfish.jersey.server.ExtendedUriInfo} object.
     * @param mediaType               a {@link javax.ws.rs.core.MediaType} object.
     * @param respEntityGenerators    a {@link java.lang.Iterable} object.
     * @return a {@link javax.ws.rs.core.Response.ResponseBuilder} object.
     */
    protected static Response.ResponseBuilder generateRespBuilder(
            ContainerRequestContext containerRequestContext,
            ExtendedUriInfo extendedUriInfo,
            MediaType mediaType,
            Iterable<OptionsResponseGenerator> respEntityGenerators) {

        final Set<String> allowedMethods = ModelProcessorUtil.getAllowedMethods(
                (extendedUriInfo.getMatchedRuntimeResources().get(0)));

        Response.ResponseBuilder builder = Response.ok().allow(allowedMethods);
        if (allowedMethods.contains(HttpPatchProperties.METHOD_NAME)) {
            builder.header(HttpPatchProperties.ACCEPT_PATCH_HEADER, getSupportPatchMediaTypes());
        }
        if (mediaType != null) {
            builder.type(mediaType);
        }
        if (respEntityGenerators != null) {
            Response response = builder.build();
            for (OptionsResponseGenerator generator : respEntityGenerators) {
                response = generator.generate(allowedMethods, mediaType, extendedUriInfo,
                        containerRequestContext, response);
            }
            builder = Response.fromResponse(response);
        }
        return builder;
    }

    /**
     * <p>generateRespBuilder.</p>
     *
     * @param extendedUriInfo a {@link org.glassfish.jersey.server.ExtendedUriInfo} object.
     * @param mediaType       a {@link javax.ws.rs.core.MediaType} object.
     * @return a {@link javax.ws.rs.core.Response.ResponseBuilder} object.
     */
    protected static Response.ResponseBuilder generateRespBuilder(ExtendedUriInfo extendedUriInfo, MediaType mediaType) {
        return generateRespBuilder(null, extendedUriInfo, mediaType, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceModel processResourceModel(ResourceModel resourceModel, Configuration configuration) {
        return ModelProcessorUtil.enhanceResourceModel(resourceModel, false, methodList, true).build();
    }

    /** {@inheritDoc} */
    @Override
    public ResourceModel processSubResource(ResourceModel subResourceModel, Configuration configuration) {
        return ModelProcessorUtil.enhanceResourceModel(subResourceModel, true, methodList, true).build();
    }

    @XmlRootElement
    protected static class AllowedMethods implements Serializable {
        protected Set<String> allow;
        protected Set<AllowedMethods> children;
        protected String uri;

        public AllowedMethods(String uri, Set<String> allow) {
            this.allow = allow;
            this.uri = uri;
        }

        public AllowedMethods(String uri, Set<String> allow, Set<AllowedMethods> children) {
            this.allow = allow;
            this.children = children;
            this.uri = uri;
        }

        public Set<AllowedMethods> getChildren() {
            return children;
        }

        public String getUri() {
            return uri;
        }

        public Set<String> getAllow() {
            return allow;
        }
    }

    protected static class GenericOptionsInflector implements Inflector<ContainerRequestContext, Response> {
        @Context
        private ExtendedUriInfo extendedUriInfo;

        @Override
        public Response apply(ContainerRequestContext containerRequestContext) {

            final MediaType foundMediaType = getSupportProduceMediaType(containerRequestContext);

            if (foundMediaType != null) {
                return generateRespBuilder(containerRequestContext, extendedUriInfo, foundMediaType, generators).build();
            }

            return generateRespBuilder(extendedUriInfo, containerRequestContext.getAcceptableMediaTypes().get(0))
                    .header(HttpHeaders.CONTENT_LENGTH, 0)
                    .build();
        }
    }

    @Priority(Priorities.HEADER_DECORATOR)
    static class DefaultOptionsResponseGenerator implements OptionsResponseGenerator {
        @Override
        public Response generate(Set<String> allowedMethods, MediaType mediaType, ExtendedUriInfo extendedUriInfo,
                                 ContainerRequestContext containerRequestContext, Response response) {
            Response.ResponseBuilder builder = Response.fromResponse(response);
            if (mediaType.isCompatible(TEXT_PLAIN_TYPE)) {
                return builder.entity(StringUtils.join(allowedMethods, ",")).build();
            }
            String uri = extendedUriInfo.getMatchedModelResource().getPathPattern().getTemplate().getTemplate();
            return builder.entity(new AllowedMethods(uri, allowedMethods)).build();
        }
    }
}
