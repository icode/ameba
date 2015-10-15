package ameba.core;

import ameba.container.server.Request;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.internal.*;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.glassfish.jersey.server.spi.RequestScopedInitializer;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.*;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.*;

/**
 * <p>Requests class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public class Requests {

    private static Provider<ContainerRequest> requestProvider;

    private Requests() {
    }

    public static Request getRequest() {
        return (Request) requestProvider.get();
    }

    /**
     * <p>getRemoteAddr.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getRemoteAddr() {
        return getRequest().getRemoteAddr();
    }

    /**
     * <p>readEntity.</p>
     *
     * @param rawType            a {@link java.lang.Class} object.
     * @param type               a {@link java.lang.reflect.Type} object.
     * @param annotations        an array of {@link java.lang.annotation.Annotation} objects.
     * @param propertiesDelegate a {@link org.glassfish.jersey.internal.PropertiesDelegate} object.
     * @param <T>                a T object.
     * @return a T object.
     */
    public static <T> T readEntity(Class<T> rawType, Type type, Annotation[] annotations, PropertiesDelegate propertiesDelegate) {
        return getRequest().readEntity(rawType, type, annotations, propertiesDelegate);
    }

    /**
     * <p>getRequestUri.</p>
     *
     * @return a {@link java.net.URI} object.
     */
    public static URI getRequestUri() {
        return getRequest().getRequestUri();
    }

    /**
     * <p>setRequestUri.</p>
     *
     * @param requestUri a {@link java.net.URI} object.
     * @throws java.lang.IllegalStateException if any.
     */
    public static void setRequestUri(URI requestUri) throws IllegalStateException {
        getRequest().setRequestUri(requestUri);
    }

    /**
     * <p>getMediaType.</p>
     *
     * @return a {@link javax.ws.rs.core.MediaType} object.
     */
    public static MediaType getMediaType() {
        return getRequest().getMediaType();
    }

    /**
     * <p>getAbsolutePath.</p>
     *
     * @return a {@link java.net.URI} object.
     */
    public static URI getAbsolutePath() {
        return getRequest().getAbsolutePath();
    }

    /**
     * <p>readEntity.</p>
     *
     * @param rawType     a {@link java.lang.Class} object.
     * @param type        a {@link java.lang.reflect.Type} object.
     * @param annotations an array of {@link java.lang.annotation.Annotation} objects.
     * @param <T>         a T object.
     * @return a T object.
     */
    public static <T> T readEntity(Class<T> rawType, Type type, Annotation[] annotations) {
        return getRequest().readEntity(rawType, type, annotations);
    }

    /**
     * <p>getQualifiedAcceptableMediaTypes.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public static List<AcceptableMediaType> getQualifiedAcceptableMediaTypes() {
        return getRequest().getQualifiedAcceptableMediaTypes();
    }

    /**
     * <p>headers.</p>
     *
     * @param newHeaders a {@link javax.ws.rs.core.MultivaluedMap} object.
     * @return a {@link org.glassfish.jersey.message.internal.InboundMessageContext} object.
     */
    public static InboundMessageContext headers(MultivaluedMap<String, String> newHeaders) {
        return getRequest().headers(newHeaders);
    }

    /**
     * <p>bufferEntity.</p>
     *
     * @return a boolean.
     * @throws javax.ws.rs.ProcessingException if any.
     */
    public static boolean bufferEntity() throws ProcessingException {
        return getRequest().bufferEntity();
    }

    /**
     * <p>getIfNoneMatch.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public static Set<MatchingEntityTag> getIfNoneMatch() {
        return getRequest().getIfNoneMatch();
    }

    /**
     * <p>getPath.</p>
     *
     * @param decode a boolean.
     * @return a {@link java.lang.String} object.
     */
    public static String getPath(boolean decode) {
        return getRequest().getPath(decode);
    }

    /**
     * <p>close.</p>
     */
    public static void close() {
        getRequest().close();
    }

    /**
     * <p>getProperty.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     */
    public static Object getProperty(String name) {
        return getRequest().getProperty(name);
    }

    /**
     * <p>getAbortResponse.</p>
     *
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    public static Response getAbortResponse() {
        return getRequest().getAbortResponse();
    }

    /**
     * <p>getCookies.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public static Map<String, Cookie> getCookies() {
        return getRequest().getCookies();
    }

    /**
     * <p>hasEntity.</p>
     *
     * @return a boolean.
     */
    public static boolean hasEntity() {
        return getRequest().hasEntity();
    }

    /**
     * <p>hasLink.</p>
     *
     * @param relation a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean hasLink(String relation) {
        return getRequest().hasLink(relation);
    }

    /**
     * <p>getLastModified.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public static Date getLastModified() {
        return getRequest().getLastModified();
    }

    /**
     * <p>readEntity.</p>
     *
     * @param rawType a {@link java.lang.Class} object.
     * @param type    a {@link java.lang.reflect.Type} object.
     * @param <T>     a T object.
     * @return a T object.
     */
    public static <T> T readEntity(Class<T> rawType, Type type) {
        return getRequest().readEntity(rawType, type);
    }

    /**
     * <p>remove.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.glassfish.jersey.message.internal.InboundMessageContext} object.
     */
    public static InboundMessageContext remove(String name) {
        return getRequest().remove(name);
    }

    /**
     * <p>setWriter.</p>
     *
     * @param responseWriter a {@link org.glassfish.jersey.server.spi.ContainerResponseWriter} object.
     */
    public static void setWriter(ContainerResponseWriter responseWriter) {
        getRequest().setWriter(responseWriter);
    }

    /**
     * <p>readEntity.</p>
     *
     * @param rawType a {@link java.lang.Class} object.
     * @param <T>     a T object.
     * @return a T object.
     */
    public static <T> T readEntity(Class<T> rawType) {
        return getRequest().readEntity(rawType);
    }

    /**
     * <p>evaluatePreconditions.</p>
     *
     * @param lastModified a {@link java.util.Date} object.
     * @return a {@link javax.ws.rs.core.Response.ResponseBuilder} object.
     */
    public static Response.ResponseBuilder evaluatePreconditions(Date lastModified) {
        return getRequest().evaluatePreconditions(lastModified);
    }

    /**
     * <p>getSecurityContext.</p>
     *
     * @return a {@link javax.ws.rs.core.SecurityContext} object.
     */
    public static SecurityContext getSecurityContext() {
        return getRequest().getSecurityContext();
    }

    /**
     * <p>setSecurityContext.</p>
     *
     * @param context a {@link javax.ws.rs.core.SecurityContext} object.
     */
    public static void setSecurityContext(SecurityContext context) {
        getRequest().setSecurityContext(context);
    }

    /**
     * <p>getEntityTag.</p>
     *
     * @return a {@link javax.ws.rs.core.EntityTag} object.
     */
    public static EntityTag getEntityTag() {
        return getRequest().getEntityTag();
    }

    /**
     * <p>getAllowedMethods.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public static Set<String> getAllowedMethods() {
        return getRequest().getAllowedMethods();
    }

    /**
     * <p>getLinkBuilder.</p>
     *
     * @param relation a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Link.Builder} object.
     */
    public static Link.Builder getLinkBuilder(String relation) {
        return getRequest().getLinkBuilder(relation);
    }

    /**
     * <p>getPropertiesDelegate.</p>
     *
     * @return a {@link org.glassfish.jersey.internal.PropertiesDelegate} object.
     */
    public static PropertiesDelegate getPropertiesDelegate() {
        return getRequest().getPropertiesDelegate();
    }

    /**
     * <p>setProperty.</p>
     *
     * @param name   a {@link java.lang.String} object.
     * @param object a {@link java.lang.Object} object.
     */
    public static void setProperty(String name, Object object) {
        getRequest().setProperty(name, object);
    }

    /**
     * <p>getQualifiedAcceptEncoding.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public static List<AcceptableToken> getQualifiedAcceptEncoding() {
        return getRequest().getQualifiedAcceptEncoding();
    }

    /**
     * <p>getRequestHeader.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public static List<String> getRequestHeader(String name) {
        return getRequest().getRequestHeader(name);
    }

    /**
     * <p>evaluatePreconditions.</p>
     *
     * @param eTag a {@link javax.ws.rs.core.EntityTag} object.
     * @return a {@link javax.ws.rs.core.Response.ResponseBuilder} object.
     */
    public static Response.ResponseBuilder evaluatePreconditions(EntityTag eTag) {
        return getRequest().evaluatePreconditions(eTag);
    }

    /**
     * <p>removeProperty.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public static void removeProperty(String name) {
        getRequest().removeProperty(name);
    }

    /**
     * <p>getQualifiedAcceptableLanguages.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public static List<AcceptableLanguageTag> getQualifiedAcceptableLanguages() {
        return getRequest().getQualifiedAcceptableLanguages();
    }

    /**
     * <p>readEntity.</p>
     *
     * @param rawType            a {@link java.lang.Class} object.
     * @param type               a {@link java.lang.reflect.Type} object.
     * @param propertiesDelegate a {@link org.glassfish.jersey.internal.PropertiesDelegate} object.
     * @param <T>                a T object.
     * @return a T object.
     */
    public static <T> T readEntity(Class<T> rawType, Type type, PropertiesDelegate propertiesDelegate) {
        return getRequest().readEntity(rawType, type, propertiesDelegate);
    }

    /**
     * <p>readEntity.</p>
     *
     * @param rawType            a {@link java.lang.Class} object.
     * @param propertiesDelegate a {@link org.glassfish.jersey.internal.PropertiesDelegate} object.
     * @param <T>                a T object.
     * @return a T object.
     */
    public static <T> T readEntity(Class<T> rawType, PropertiesDelegate propertiesDelegate) {
        return getRequest().readEntity(rawType, propertiesDelegate);
    }

    /**
     * <p>abortWith.</p>
     *
     * @param response a {@link javax.ws.rs.core.Response} object.
     */
    public static void abortWith(Response response) {
        getRequest().abortWith(response);
    }

    /**
     * <p>headers.</p>
     *
     * @param name   a {@link java.lang.String} object.
     * @param values a {@link java.lang.Iterable} object.
     * @return a {@link org.glassfish.jersey.message.internal.InboundMessageContext} object.
     */
    public static InboundMessageContext headers(String name, Iterable<?> values) {
        return getRequest().headers(name, values);
    }

    /**
     * <p>getDate.</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public static Date getDate() {
        return getRequest().getDate();
    }

    /**
     * <p>getMethod.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getMethod() {
        return getRequest().getMethod();
    }

    /**
     * <p>setMethod.</p>
     *
     * @param method a {@link java.lang.String} object.
     * @throws java.lang.IllegalStateException if any.
     */
    public static void setMethod(String method) throws IllegalStateException {
        getRequest().setMethod(method);
    }

    /**
     * <p>getBaseUri.</p>
     *
     * @return a {@link java.net.URI} object.
     */
    public static URI getBaseUri() {
        return getRequest().getBaseUri();
    }

    /**
     * <p>getUriInfo.</p>
     *
     * @return a {@link org.glassfish.jersey.server.ExtendedUriInfo} object.
     */
    public static ExtendedUriInfo getUriInfo() {
        return getRequest().getUriInfo();
    }

    /**
     * <p>headers.</p>
     *
     * @param newHeaders a {@link java.util.Map} object.
     * @return a {@link org.glassfish.jersey.message.internal.InboundMessageContext} object.
     */
    public static InboundMessageContext headers(Map<String, List<String>> newHeaders) {
        return getRequest().headers(newHeaders);
    }

    /**
     * <p>getRequestCookies.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public static Map<String, Cookie> getRequestCookies() {
        return getRequest().getRequestCookies();
    }

    /**
     * <p>getQualifiedAcceptCharset.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public static List<AcceptableToken> getQualifiedAcceptCharset() {
        return getRequest().getQualifiedAcceptCharset();
    }

    /**
     * <p>getAcceptableMediaTypes.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public static List<MediaType> getAcceptableMediaTypes() {
        return getRequest().getAcceptableMediaTypes();
    }

    /**
     * <p>getLocation.</p>
     *
     * @return a {@link java.net.URI} object.
     */
    public static URI getLocation() {
        return getRequest().getLocation();
    }

    /**
     * <p>getIfMatch.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public static Set<MatchingEntityTag> getIfMatch() {
        return getRequest().getIfMatch();
    }

    /**
     * <p>selectVariant.</p>
     *
     * @param variants a {@link java.util.List} object.
     * @return a {@link javax.ws.rs.core.Variant} object.
     * @throws java.lang.IllegalArgumentException if any.
     */
    public static Variant selectVariant(List<Variant> variants) throws IllegalArgumentException {
        return getRequest().selectVariant(variants);
    }

    /**
     * <p>getPropertyNames.</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public static Collection<String> getPropertyNames() {
        return getRequest().getPropertyNames();
    }

    /**
     * <p>evaluatePreconditions.</p>
     *
     * @return a {@link javax.ws.rs.core.Response.ResponseBuilder} object.
     */
    public static Response.ResponseBuilder evaluatePreconditions() {
        return getRequest().evaluatePreconditions();
    }

    /**
     * <p>getRequestHeaders.</p>
     *
     * @return a {@link javax.ws.rs.core.MultivaluedMap} object.
     */
    public static MultivaluedMap<String, String> getRequestHeaders() {
        return getRequest().getRequestHeaders();
    }

    /**
     * <p>getRequestScopedInitializer.</p>
     *
     * @return a {@link org.glassfish.jersey.server.spi.RequestScopedInitializer} object.
     */
    public static RequestScopedInitializer getRequestScopedInitializer() {
        return getRequest().getRequestScopedInitializer();
    }

    /**
     * <p>setRequestScopedInitializer.</p>
     *
     * @param requestScopedInitializer a {@link org.glassfish.jersey.server.spi.RequestScopedInitializer} object.
     */
    public static void setRequestScopedInitializer(RequestScopedInitializer requestScopedInitializer) {
        getRequest().setRequestScopedInitializer(requestScopedInitializer);
    }

    /**
     * <p>header.</p>
     *
     * @param name  a {@link java.lang.String} object.
     * @param value a {@link java.lang.Object} object.
     * @return a {@link org.glassfish.jersey.message.internal.InboundMessageContext} object.
     */
    public static InboundMessageContext header(String name, Object value) {
        return getRequest().header(name, value);
    }

    /**
     * <p>getHeaderString.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getHeaderString(String name) {
        return getRequest().getHeaderString(name);
    }

    /**
     * <p>readEntity.</p>
     *
     * @param rawType     a {@link java.lang.Class} object.
     * @param annotations an array of {@link java.lang.annotation.Annotation} objects.
     * @param <T>         a T object.
     * @return a T object.
     */
    public static <T> T readEntity(Class<T> rawType, Annotation[] annotations) {
        return getRequest().readEntity(rawType, annotations);
    }

    /**
     * <p>headers.</p>
     *
     * @param name   a {@link java.lang.String} object.
     * @param values a {@link java.lang.Object} object.
     * @return a {@link org.glassfish.jersey.message.internal.InboundMessageContext} object.
     */
    public static InboundMessageContext headers(String name, Object... values) {
        return getRequest().headers(name, values);
    }

    /**
     * <p>getEntityStream.</p>
     *
     * @return a {@link java.io.InputStream} object.
     */
    public static InputStream getEntityStream() {
        return getRequest().getEntityStream();
    }

    /**
     * <p>setEntityStream.</p>
     *
     * @param input a {@link java.io.InputStream} object.
     */
    public static void setEntityStream(InputStream input) {
        getRequest().setEntityStream(input);
    }

    /**
     * <p>setMethodWithoutException.</p>
     *
     * @param method a {@link java.lang.String} object.
     */
    public static void setMethodWithoutException(String method) {
        getRequest().setMethodWithoutException(method);
    }

    /**
     * <p>inResponseProcessing.</p>
     */
    public static void inResponseProcessing() {
        getRequest().inResponseProcessing();
    }

    /**
     * <p>getRemoteRealAddr.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getRemoteRealAddr() {
        return getRequest().getRemoteRealAddr();
    }

    /**
     * <p>readEntity.</p>
     *
     * @param rawType            a {@link java.lang.Class} object.
     * @param annotations        an array of {@link java.lang.annotation.Annotation} objects.
     * @param propertiesDelegate a {@link org.glassfish.jersey.internal.PropertiesDelegate} object.
     * @param <T>                a T object.
     * @return a T object.
     */
    public static <T> T readEntity(Class<T> rawType, Annotation[] annotations, PropertiesDelegate propertiesDelegate) {
        return getRequest().readEntity(rawType, annotations, propertiesDelegate);
    }

    /**
     * <p>getWorkers.</p>
     *
     * @return a {@link org.glassfish.jersey.message.MessageBodyWorkers} object.
     */
    public static MessageBodyWorkers getWorkers() {
        return getRequest().getWorkers();
    }

    /**
     * <p>setWorkers.</p>
     *
     * @param workers a {@link org.glassfish.jersey.message.MessageBodyWorkers} object.
     */
    public static void setWorkers(MessageBodyWorkers workers) {
        getRequest().setWorkers(workers);
    }

    /**
     * <p>getAcceptableLanguages.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public static List<Locale> getAcceptableLanguages() {
        return getRequest().getAcceptableLanguages();
    }

    /**
     * <p>getLength.</p>
     *
     * @return a int.
     */
    public static int getLength() {
        return getRequest().getLength();
    }

    /**
     * <p>evaluatePreconditions.</p>
     *
     * @param lastModified a {@link java.util.Date} object.
     * @param eTag         a {@link javax.ws.rs.core.EntityTag} object.
     * @return a {@link javax.ws.rs.core.Response.ResponseBuilder} object.
     */
    public static Response.ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag eTag) {
        return getRequest().evaluatePreconditions(lastModified, eTag);
    }

    /**
     * <p>getLink.</p>
     *
     * @param relation a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Link} object.
     */
    public static Link getLink(String relation) {
        return getRequest().getLink(relation);
    }

    /**
     * <p>getLanguage.</p>
     *
     * @return a {@link java.util.Locale} object.
     */
    public static Locale getLanguage() {
        return getRequest().getLanguage();
    }

    /**
     * <p>getRemoteRealAddr.</p>
     *
     * @param realIpHeader a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getRemoteRealAddr(String realIpHeader) {
        return getRequest().getRemoteRealAddr(realIpHeader);
    }

    /**
     * <p>getResponseCookies.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public static Map<String, NewCookie> getResponseCookies() {
        return getRequest().getResponseCookies();
    }

    /**
     * <p>getVaryValue.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getVaryValue() {
        return getRequest().getVaryValue();
    }

    /**
     * <p>setRequestUri.</p>
     *
     * @param baseUri    a {@link java.net.URI} object.
     * @param requestUri a {@link java.net.URI} object.
     * @throws java.lang.IllegalStateException if any.
     */
    public static void setRequestUri(URI baseUri, URI requestUri) throws IllegalStateException {
        getRequest().setRequestUri(baseUri, requestUri);
    }

    /**
     * <p>getLinks.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public static Set<Link> getLinks() {
        return getRequest().getLinks();
    }

    /**
     * <p>getHeaders.</p>
     *
     * @return a {@link javax.ws.rs.core.MultivaluedMap} object.
     */
    public static MultivaluedMap<String, String> getHeaders() {
        return getRequest().getHeaders();
    }

    /**
     * <p>getResponseWriter.</p>
     *
     * @return a {@link org.glassfish.jersey.server.spi.ContainerResponseWriter} object.
     */
    public static ContainerResponseWriter getResponseWriter() {
        return getRequest().getResponseWriter();
    }

    static class BindRequest implements Feature {

        @Inject
        public BindRequest(Provider<ContainerRequest> reqProvider) {
            requestProvider = reqProvider;
        }

        @Override
        public boolean configure(FeatureContext context) {
            return true;
        }
    }
}
