package ameba.mvc.template.internal;

import ameba.Ameba;
import ameba.core.Application;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.ArrayUtils;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.uri.UriTemplate;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.Produces;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author icode
 */
@ConstrainedTo(RuntimeType.SERVER)
@Produces({"text/html", "application/xhtml+xml", "application/x-ms-application"})
final class DataViewMessageBodyWriter implements MessageBodyWriter<Object> {
    public static final String DISABLE_DATA_VIEW = "data.view.disabled";
    public static final String DISABLE_DEFAULT_DATA_VIEW = "data.view.default.disabled";
    private static final MediaType LOW_IE_DEFAULT_REQ_TYPE = new MediaType("application", "x-ms-application");
    public static final List<MediaType> TEMPLATE_PRODUCES = Lists.newArrayList(
            MediaType.TEXT_HTML_TYPE,
            MediaType.APPLICATION_XHTML_XML_TYPE,
            LOW_IE_DEFAULT_REQ_TYPE,
            MediaType.WILDCARD_TYPE
    );
    private static final String DATA_VIEW_DEFAULT_KEY_PRE = "data.view.default.";
    public static final String DATA_VIEW_LIST_KEY = DATA_VIEW_DEFAULT_KEY_PRE + "list";
    public static final String DATA_VIEW_ITEM_KEY = DATA_VIEW_DEFAULT_KEY_PRE + "item";
    public static final String DATA_VIEW_NULL_KEY = DATA_VIEW_DEFAULT_KEY_PRE + "empty";
    private static final String DEFAULT_DATA_VIEW_PAGE_DIR = Viewables.PROTECTED_DIR_PATH + "/default/";
    public static final String DEFAULT_DATA_LIST = DEFAULT_DATA_VIEW_PAGE_DIR + "list";
    public static final String DEFAULT_DATA_ITEM = DEFAULT_DATA_VIEW_PAGE_DIR + "item";
    public static final String DEFAULT_DATA_NULL = DEFAULT_DATA_VIEW_PAGE_DIR + "empty";
    private final boolean dataViewDisabled;
    private final boolean defaultDataViewDisabled;
    private final String dataViewList;
    private final String dataViewItem;
    private final String dataViewNull;

    @Context
    private Provider<ContainerRequestContext> requestProvider;
    @Context
    private Provider<ResourceInfo> resourceInfoProvider;
    @Context
    private Provider<ExtendedUriInfo> uriInfoProvider;
    @Context
    private Provider<MessageBodyWorkers> workersProvider;

    @Inject
    public DataViewMessageBodyWriter(Application application) {
        dataViewDisabled = "true".equals(application.getProperty(DISABLE_DATA_VIEW));
        defaultDataViewDisabled = "true".equals(application.getProperty(DISABLE_DEFAULT_DATA_VIEW));
        Map<String, Object> properties = application.getProperties();
        dataViewList = PropertiesHelper.getValue(properties, DATA_VIEW_LIST_KEY, DEFAULT_DATA_LIST, null);
        dataViewItem = PropertiesHelper.getValue(properties, DATA_VIEW_ITEM_KEY, DEFAULT_DATA_ITEM, null);
        dataViewNull = PropertiesHelper.getValue(properties, DATA_VIEW_NULL_KEY, DEFAULT_DATA_NULL, null);
    }

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations,
                               final MediaType mediaType) {
        String[] p;
        return !dataViewDisabled
                && -1 != ListUtils.indexOf(requestProvider.get().getAcceptableMediaTypes(),
                new Predicate<MediaType>() {
                    @Override
                    public boolean evaluate(MediaType mediaType) {
                        return isSupportMediaType(mediaType);
                    }
                })
                && ((p = TemplateHelper.getProduces(annotations)) == null
                || -1 != ArrayUtils.indexOf(p,
                new Predicate<String>() {
                    @Override
                    public boolean evaluate(String stringType) {
                        if (stringType.equals(MediaType.WILDCARD)) return true;

                        MediaType mediaType = MediaType.valueOf(stringType);
                        return isSupportMediaType(mediaType);
                    }
                }));
    }

    @Override
    public long getSize(final Object viewable, final Class<?> type, final Type genericType,
                        final Annotation[] annotations, final MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(final Object entity,
                        final Class<?> type,
                        final Type genericType,
                        final Annotation[] annotations,
                        MediaType mediaType,
                        final MultivaluedMap<String, Object> httpHeaders,
                        final OutputStream entityStream) throws IOException, WebApplicationException {
        if (mediaType == null
                || MediaTypes.isWildcard(mediaType)
                || (mediaType.getType().equals(LOW_IE_DEFAULT_REQ_TYPE.getType()) &&
                mediaType.getSubtype().equals(LOW_IE_DEFAULT_REQ_TYPE.getSubtype()))) {
            mediaType = MediaType.TEXT_HTML_TYPE;
        }

        List<String> templates = Lists.newArrayList();
        ResourceInfo resourceInfo = resourceInfoProvider.get();

        if (resourceInfo != null && resourceInfo.getResourceMethod() != null) {
            templates.add(resourceInfo.getResourceMethod().getName());
        }
        templates.add("index");
        String path = getTemplatePath(uriInfoProvider.get());
        templates.add(Viewables.PROTECTED_DIR_PATH + path);
        templates.add(path);
        if (!defaultDataViewDisabled) {
            if (entity == null
                    || (entity instanceof Collection
                    && ((Collection) entity).size() == 0)
                    || (entity.getClass().isArray()
                    && ((Object[]) entity).length == 0)) {
                templates.add(dataViewNull);
            } else if (isItem(entity)) {
                templates.add(dataViewItem);
            } else {
                templates.add(dataViewList);
            }
        }

        Class clazz = Ameba.class;

        if (resourceInfo != null) {
            clazz = resourceInfo.getResourceClass();
        } else {
            List<Object> res = uriInfoProvider.get().getMatchedResources();
            if (res != null && res.size() > 0) {
                clazz = res.get(0).getClass();
            }
        }
        workersProvider.get().getMessageBodyWriter(
                ImplicitViewable.class,
                ImplicitViewable.class,
                annotations,
                mediaType)

                .writeTo(new ImplicitViewable(templates, entity, clazz),
                        ImplicitViewable.class,
                        ImplicitViewable.class,
                        annotations, mediaType,
                        httpHeaders, entityStream);
    }


    private boolean isItem(Object entity) {
        return !(entity instanceof Collection)
                && !entity.getClass().isArray();
    }

    private String getTemplatePath(ExtendedUriInfo uriInfo) {
        StringBuilder builder = new StringBuilder();

        for (UriTemplate template : uriInfo.getMatchedTemplates()) {
            List<String> variables = template.getTemplateVariables();
            String[] args = new String[variables.size()];
            for (int i = 0; i < args.length; i++) {
                args[i] = "{" + variables.get(i) + "}";
            }
            String uri = template.createURI(args);
            if (!uri.equals("/") && !uri.equals(""))
                builder.insert(0, uri);
        }

        return builder.toString().toLowerCase();
    }

    private boolean isSupportMediaType(MediaType mediaType) {
        for (MediaType type : TEMPLATE_PRODUCES) {
            if (mediaType.getType().equalsIgnoreCase(type.getType())
                    && mediaType.getSubtype().equalsIgnoreCase(type.getSubtype())) {
                return true;
            }
        }
        return false;
    }
}