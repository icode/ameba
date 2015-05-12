package ameba.mvc.template.internal;

import jersey.repackaged.com.google.common.collect.Sets;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.internal.LocalizationMessages;
import org.glassfish.jersey.server.mvc.spi.ResolvedViewable;
import org.glassfish.jersey.server.mvc.spi.TemplateProcessor;
import org.glassfish.jersey.server.mvc.spi.ViewableContext;
import org.glassfish.jersey.server.mvc.spi.ViewableContextException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

/**
 * {@link javax.ws.rs.ext.MessageBodyWriter Message body writer}
 * for {@link org.glassfish.jersey.server.mvc.Viewable viewable}
 * entities.
 *
 * @author Paul Sandoz
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 * @author icode
 */
@Singleton
@ConstrainedTo(RuntimeType.SERVER)
final class TemplateMessageBodyWriter implements MessageBodyWriter<Viewable> {

    @Inject
    private ServiceLocator serviceLocator;
    @Context
    private Provider<ExtendedUriInfo> extendedUriInfoProvider;
    @Context
    private Provider<ContainerRequest> requestProvider;
    @Context
    private Provider<ResourceInfo> resourceInfoProvider;

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                               final MediaType mediaType) {
        return Viewable.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final Viewable viewable, final Class<?> type, final Type genericType,
                        final Annotation[] annotations, final MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(final Viewable viewable,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        final MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            final ResolvedViewable resolvedViewable = resolve(viewable);
            if (resolvedViewable == null) {
                final String message = LocalizationMessages
                        .TEMPLATE_NAME_COULD_NOT_BE_RESOLVED(viewable.getTemplateName());
                throw new WebApplicationException(new ProcessingException(message), Response.Status.NOT_FOUND);
            }

            MediaType mType = resolvedViewable.getMediaType();

            if (mType == null
                    || mType.isWildcardType()
                    || mType.equals(MediaType.APPLICATION_OCTET_STREAM_TYPE)) {
                mType = mediaType;
            }

            httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, mType);
            resolvedViewable.writeTo(entityStream, httpHeaders);
        } catch (ViewableContextException vce) {
            throw new NotFoundException(vce);
        }
    }

    /**
     * Resolve the given {@link org.glassfish.jersey.server.mvc.Viewable viewable} using
     * {@link org.glassfish.jersey.server.mvc.spi.ViewableContext}.
     *
     * @param viewable viewable to be resolved.
     * @return resolved viewable or {@code null}, if the viewable cannot be resolved.
     */
    private ResolvedViewable resolve(final Viewable viewable) {
        if (viewable instanceof ResolvedViewable) {
            return (ResolvedViewable) viewable;
        } else {
            final ViewableContext viewableContext = getViewableContext();
            final Set<TemplateProcessor> templateProcessors = getTemplateProcessors();

            List<MediaType> producibleMediaTypes = TemplateHelper
                    .getProducibleMediaTypes(requestProvider.get(), extendedUriInfoProvider.get(), null);

            if (viewable instanceof ImplicitViewable) {
                // Template Names.
                final ImplicitViewable implicitViewable = (ImplicitViewable) viewable;

                for (final String templateName : implicitViewable.getTemplateNames()) {
                    final Viewable simpleViewable = new Viewable(templateName, viewable.getModel());

                    final ResolvedViewable resolvedViewable = resolve(simpleViewable, producibleMediaTypes,
                            implicitViewable.getResolvingClass(), viewableContext, templateProcessors);

                    if (resolvedViewable != null) {
                        return resolvedViewable;
                    }
                }
            } else {
                final Class<?> resourceClass = resourceInfoProvider.get().getResourceClass();
                return resolve(viewable, producibleMediaTypes, resourceClass, viewableContext, templateProcessors);
            }

            return null;
        }
    }

    /**
     * Resolve given {@link org.glassfish.jersey.server.mvc.Viewable viewable}
     * for a list of {@link javax.ws.rs.core.MediaType mediaTypes} and a {@link Class resolvingClass}
     * using given {@link org.glassfish.jersey.server.mvc.spi.ViewableContext viewableContext}
     * and a set of {@link org.glassfish.jersey.server.mvc.spi.TemplateProcessor templateProcessors}
     *
     * @param viewable           viewable to be resolved.
     * @param mediaTypes         producible media types.
     * @param resolvingClass     non-null resolving class.
     * @param viewableContext    viewable context.
     * @param templateProcessors collection of available template processors.
     * @return resolved viewable or {@code null}, if the viewable cannot be resolved.
     */
    private ResolvedViewable resolve(final Viewable viewable,
                                     final List<MediaType> mediaTypes,
                                     final Class<?> resolvingClass,
                                     final ViewableContext viewableContext,
                                     final Set<TemplateProcessor> templateProcessors) {
        for (TemplateProcessor templateProcessor : templateProcessors) {
            for (final MediaType mediaType : mediaTypes) {
                final ResolvedViewable resolvedViewable = viewableContext
                        .resolveViewable(viewable, mediaType, resolvingClass, templateProcessor);

                if (resolvedViewable != null) {
                    return resolvedViewable;
                }
            }
        }

        return null;
    }

    /**
     * Get a {@link java.util.LinkedHashSet collection} of available template processors.
     *
     * @return set of template processors.
     */
    private Set<TemplateProcessor> getTemplateProcessors() {
        final Set<TemplateProcessor> templateProcessors = Sets.newLinkedHashSet();

        templateProcessors.addAll(Providers.getCustomProviders(serviceLocator, TemplateProcessor.class));
        templateProcessors.addAll(Providers.getProviders(serviceLocator, TemplateProcessor.class));

        return templateProcessors;
    }

    /**
     * Get {@link org.glassfish.jersey.server.mvc.spi.ViewableContext viewable context}.
     * User defined (custom) contexts have higher priority than the default ones
     * (i.e. {@link ResolvingViewableContext}).
     *
     * @return {@code non-null} viewable context.
     */
    private ViewableContext getViewableContext() {
        final Set<ViewableContext> customProviders =
                Providers.getCustomProviders(serviceLocator, ViewableContext.class);
        if (!customProviders.isEmpty()) {
            return customProviders.iterator().next();
        }
        return Providers.getProviders(serviceLocator, ViewableContext.class).iterator().next();
    }
}