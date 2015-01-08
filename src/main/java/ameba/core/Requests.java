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
 * @author icode
 */
public class Requests {

    private static Provider<ContainerRequest> requestProvider;

    private Requests() {
    }

    private static Request getRequest() {
        return (Request) requestProvider.get();
    }

    public static String getRealAddress(String realIpHeader) {
        return getRequest().getRemoteRealAddr(realIpHeader);
    }

    public static String getRealAddress() {
        return getRequest().getRemoteRealAddr();
    }

    public static String getRemoteAddr() {
        return getRequest().getRemoteAddr();
    }

    public static <T> T readEntity(Class<T> rawType, Type type, Annotation[] annotations, PropertiesDelegate propertiesDelegate) {
        return getRequest().readEntity(rawType, type, annotations, propertiesDelegate);
    }

    public static URI getRequestUri() {
        return getRequest().getRequestUri();
    }

    public static void setRequestUri(URI requestUri) throws IllegalStateException {
        getRequest().setRequestUri(requestUri);
    }

    public static MediaType getMediaType() {
        return getRequest().getMediaType();
    }

    public static URI getAbsolutePath() {
        return getRequest().getAbsolutePath();
    }

    public static <T> T readEntity(Class<T> rawType, Type type, Annotation[] annotations) {
        return getRequest().readEntity(rawType, type, annotations);
    }

    public static List<AcceptableMediaType> getQualifiedAcceptableMediaTypes() {
        return getRequest().getQualifiedAcceptableMediaTypes();
    }

    public static InboundMessageContext headers(MultivaluedMap<String, String> newHeaders) {
        return getRequest().headers(newHeaders);
    }

    public static boolean bufferEntity() throws ProcessingException {
        return getRequest().bufferEntity();
    }

    public static Set<MatchingEntityTag> getIfNoneMatch() {
        return getRequest().getIfNoneMatch();
    }

    public static String getPath(boolean decode) {
        return getRequest().getPath(decode);
    }

    public static void close() {
        getRequest().close();
    }

    public static Object getProperty(String name) {
        return getRequest().getProperty(name);
    }

    public static Response getAbortResponse() {
        return getRequest().getAbortResponse();
    }

    public static Map<String, Cookie> getCookies() {
        return getRequest().getCookies();
    }

    public static boolean hasEntity() {
        return getRequest().hasEntity();
    }

    public static boolean hasLink(String relation) {
        return getRequest().hasLink(relation);
    }

    public static Date getLastModified() {
        return getRequest().getLastModified();
    }

    public static <T> T readEntity(Class<T> rawType, Type type) {
        return getRequest().readEntity(rawType, type);
    }

    public static InboundMessageContext remove(String name) {
        return getRequest().remove(name);
    }

    public static void setWriter(ContainerResponseWriter responseWriter) {
        getRequest().setWriter(responseWriter);
    }

    public static <T> T readEntity(Class<T> rawType) {
        return getRequest().readEntity(rawType);
    }

    public static Response.ResponseBuilder evaluatePreconditions(Date lastModified) {
        return getRequest().evaluatePreconditions(lastModified);
    }

    public static SecurityContext getSecurityContext() {
        return getRequest().getSecurityContext();
    }

    public static void setSecurityContext(SecurityContext context) {
        getRequest().setSecurityContext(context);
    }

    public static EntityTag getEntityTag() {
        return getRequest().getEntityTag();
    }

    public static Set<String> getAllowedMethods() {
        return getRequest().getAllowedMethods();
    }

    public static Link.Builder getLinkBuilder(String relation) {
        return getRequest().getLinkBuilder(relation);
    }

    public static PropertiesDelegate getPropertiesDelegate() {
        return getRequest().getPropertiesDelegate();
    }

    public static void setProperty(String name, Object object) {
        getRequest().setProperty(name, object);
    }

    public static List<AcceptableToken> getQualifiedAcceptEncoding() {
        return getRequest().getQualifiedAcceptEncoding();
    }

    public static List<String> getRequestHeader(String name) {
        return getRequest().getRequestHeader(name);
    }

    public static Response.ResponseBuilder evaluatePreconditions(EntityTag eTag) {
        return getRequest().evaluatePreconditions(eTag);
    }

    public static void removeProperty(String name) {
        getRequest().removeProperty(name);
    }

    public static List<AcceptableLanguageTag> getQualifiedAcceptableLanguages() {
        return getRequest().getQualifiedAcceptableLanguages();
    }

    public static <T> T readEntity(Class<T> rawType, Type type, PropertiesDelegate propertiesDelegate) {
        return getRequest().readEntity(rawType, type, propertiesDelegate);
    }

    public static <T> T readEntity(Class<T> rawType, PropertiesDelegate propertiesDelegate) {
        return getRequest().readEntity(rawType, propertiesDelegate);
    }

    public static void abortWith(Response response) {
        getRequest().abortWith(response);
    }

    public static InboundMessageContext headers(String name, Iterable<?> values) {
        return getRequest().headers(name, values);
    }

    public static Date getDate() {
        return getRequest().getDate();
    }

    public static String getMethod() {
        return getRequest().getMethod();
    }

    public static void setMethod(String method) throws IllegalStateException {
        getRequest().setMethod(method);
    }

    public static URI getBaseUri() {
        return getRequest().getBaseUri();
    }

    public static ExtendedUriInfo getUriInfo() {
        return getRequest().getUriInfo();
    }

    public static InboundMessageContext headers(Map<String, List<String>> newHeaders) {
        return getRequest().headers(newHeaders);
    }

    public static Map<String, Cookie> getRequestCookies() {
        return getRequest().getRequestCookies();
    }

    public static List<AcceptableToken> getQualifiedAcceptCharset() {
        return getRequest().getQualifiedAcceptCharset();
    }

    public static List<MediaType> getAcceptableMediaTypes() {
        return getRequest().getAcceptableMediaTypes();
    }

    public static URI getLocation() {
        return getRequest().getLocation();
    }

    public static Set<MatchingEntityTag> getIfMatch() {
        return getRequest().getIfMatch();
    }

    public static Variant selectVariant(List<Variant> variants) throws IllegalArgumentException {
        return getRequest().selectVariant(variants);
    }

    public static Collection<String> getPropertyNames() {
        return getRequest().getPropertyNames();
    }

    public static Response.ResponseBuilder evaluatePreconditions() {
        return getRequest().evaluatePreconditions();
    }

    public static MultivaluedMap<String, String> getRequestHeaders() {
        return getRequest().getRequestHeaders();
    }

    public static RequestScopedInitializer getRequestScopedInitializer() {
        return getRequest().getRequestScopedInitializer();
    }

    public static void setRequestScopedInitializer(RequestScopedInitializer requestScopedInitializer) {
        getRequest().setRequestScopedInitializer(requestScopedInitializer);
    }

    public static InboundMessageContext header(String name, Object value) {
        return getRequest().header(name, value);
    }

    public static String getHeaderString(String name) {
        return getRequest().getHeaderString(name);
    }

    public static <T> T readEntity(Class<T> rawType, Annotation[] annotations) {
        return getRequest().readEntity(rawType, annotations);
    }

    public static InboundMessageContext headers(String name, Object... values) {
        return getRequest().headers(name, values);
    }

    public static InputStream getEntityStream() {
        return getRequest().getEntityStream();
    }

    public static void setEntityStream(InputStream input) {
        getRequest().setEntityStream(input);
    }

    public static void setMethodWithoutException(String method) {
        getRequest().setMethodWithoutException(method);
    }

    public static void inResponseProcessing() {
        getRequest().inResponseProcessing();
    }

    public static String getRemoteRealAddr() {
        return getRequest().getRemoteRealAddr();
    }

    public static <T> T readEntity(Class<T> rawType, Annotation[] annotations, PropertiesDelegate propertiesDelegate) {
        return getRequest().readEntity(rawType, annotations, propertiesDelegate);
    }

    public static MessageBodyWorkers getWorkers() {
        return getRequest().getWorkers();
    }

    public static void setWorkers(MessageBodyWorkers workers) {
        getRequest().setWorkers(workers);
    }

    public static List<Locale> getAcceptableLanguages() {
        return getRequest().getAcceptableLanguages();
    }

    public static int getLength() {
        return getRequest().getLength();
    }

    public static Response.ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag eTag) {
        return getRequest().evaluatePreconditions(lastModified, eTag);
    }

    public static Link getLink(String relation) {
        return getRequest().getLink(relation);
    }

    public static Locale getLanguage() {
        return getRequest().getLanguage();
    }

    public static String getRemoteRealAddr(String realIpHeader) {
        return getRequest().getRemoteRealAddr(realIpHeader);
    }

    public static Map<String, NewCookie> getResponseCookies() {
        return getRequest().getResponseCookies();
    }

    public static String getVaryValue() {
        return getRequest().getVaryValue();
    }

    public static void setRequestUri(URI baseUri, URI requestUri) throws IllegalStateException {
        getRequest().setRequestUri(baseUri, requestUri);
    }

    public static Set<Link> getLinks() {
        return getRequest().getLinks();
    }

    public static MultivaluedMap<String, String> getHeaders() {
        return getRequest().getHeaders();
    }

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
