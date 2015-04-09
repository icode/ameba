package ameba.core.ws.rs;

import jersey.repackaged.com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.ModelProcessor;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.model.internal.ModelProcessorUtil;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static ameba.message.internal.MediaType.WILDCARD_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;

/**
 * @author icode
 */
@Priority(Integer.MAX_VALUE)
public class OptionsMethodProcessor implements ModelProcessor {

    private final List<ModelProcessorUtil.Method> methodList;

    private static String SUPPORT_PATCH_MEDIA_TYPES = null;

    public static String getSupportPatchMediaTypes() {
        if (SUPPORT_PATCH_MEDIA_TYPES == null) {
            synchronized (PATCH.SUPPORT_PATCH_MEDIA_TYPES) {
                if (SUPPORT_PATCH_MEDIA_TYPES == null) {
                    SUPPORT_PATCH_MEDIA_TYPES = StringUtils.join(PATCH.SUPPORT_PATCH_MEDIA_TYPES, ",");
                }
            }
        }
        return SUPPORT_PATCH_MEDIA_TYPES;
    }

    /**
     * Creates new instance.
     */
    public OptionsMethodProcessor() {
        methodList = Lists.newArrayList();

        methodList.add(new ModelProcessorUtil.Method(HttpMethod.OPTIONS, WILDCARD_TYPE, WILDCARD_TYPE,
                GenericOptionsInflector.class));
    }

    @XmlRootElement
    protected static class AllowedMethods implements Serializable {
        protected Set<String> allow;

        public AllowedMethods(Set<String> allow) {
            this.allow = allow;
        }

        public Set<String> getAllow() {
            return allow;
        }
    }

    protected static MediaType getSupportProduceMediaType(ContainerRequestContext containerRequestContext) {
        for (MediaType mediaType : containerRequestContext.getAcceptableMediaTypes()) {
            if (((mediaType.getType().equalsIgnoreCase("application") || mediaType.getType().equalsIgnoreCase("text"))
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

    protected static class GenericOptionsInflector implements Inflector<ContainerRequestContext, Response> {
        @Inject
        private ExtendedUriInfo extendedUriInfo;

        @Override
        public Response apply(ContainerRequestContext containerRequestContext) {

            final MediaType foundMediaType = getSupportProduceMediaType(containerRequestContext);

            if (foundMediaType != null) {
                return generateRespBuilder(extendedUriInfo, new RespEntityGenerator() {
                    @Override
                    public Object generate(Set<String> allowedMethods, ExtendedUriInfo extendedUriInfo) {
                        if (foundMediaType.equals(TEXT_PLAIN_TYPE)) {
                            final String allowedList = allowedMethods.toString();
                            return allowedList.substring(1, allowedList.length() - 1);
                        }
                        return new AllowedMethods(allowedMethods);
                    }
                }).type(foundMediaType).build();
            }

            return generateRespBuilder(extendedUriInfo)
                    .header(HttpHeaders.CONTENT_LENGTH, 0)
                    .type(containerRequestContext.getAcceptableMediaTypes().get(0))
                    .build();
        }
    }

    protected static Response.ResponseBuilder generateRespBuilder(ExtendedUriInfo extendedUriInfo, RespEntityGenerator respEntityGenerator) {

        final Set<String> allowedMethods = ModelProcessorUtil.getAllowedMethods(
                (extendedUriInfo.getMatchedRuntimeResources().get(0)));

        Response.ResponseBuilder builder = Response.ok().allow(allowedMethods);
        if (allowedMethods.contains(PATCH.NAME)) {
            builder.header(PATCH.ACCEPT_PATCH_HEADER, getSupportPatchMediaTypes());
        }
        if (respEntityGenerator != null) {
            builder.entity(respEntityGenerator.generate(allowedMethods, extendedUriInfo));
        }
        return builder;
    }

    protected static abstract class RespEntityGenerator {
        public abstract Object generate(Set<String> allowedMethods, ExtendedUriInfo extendedUriInfo);
    }

    protected static Response.ResponseBuilder generateRespBuilder(ExtendedUriInfo extendedUriInfo) {
        return generateRespBuilder(extendedUriInfo, null);
    }

    @Override
    public ResourceModel processResourceModel(ResourceModel resourceModel, Configuration configuration) {
        return ModelProcessorUtil.enhanceResourceModel(resourceModel, false, methodList, true).build();
    }

    @Override
    public ResourceModel processSubResource(ResourceModel subResourceModel, Configuration configuration) {
        return ModelProcessorUtil.enhanceResourceModel(subResourceModel, true, methodList, true).build();
    }
}