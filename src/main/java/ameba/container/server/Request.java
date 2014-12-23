package ameba.container.server;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.core.SecurityContext;
import java.net.URI;

/**
 * @author icode
 */
public abstract class Request extends ContainerRequest {
    /**
     * Create new Jersey container request context.
     *
     * @param baseUri            base application URI.
     * @param requestUri         request URI.
     * @param httpMethod         request HTTP method name.
     * @param securityContext    security context of the current request. Must not be {@code null}.
     *                           The {@link javax.ws.rs.core.SecurityContext#getUserPrincipal()} must return
     *                           {@code null} if the current request has not been authenticated
     *                           by the container.
     * @param propertiesDelegate custom {@link org.glassfish.jersey.internal.PropertiesDelegate properties delegate}
     */
    public Request(URI baseUri, URI requestUri, String httpMethod, SecurityContext securityContext, PropertiesDelegate propertiesDelegate) {
        super(baseUri, requestUri, httpMethod, securityContext, propertiesDelegate);
    }

    public abstract String getRemoteAddr();


    public String getRemoteRealAddr(String realIpHeader) {
        String ip = null;
        if (realIpHeader != null && realIpHeader.length() != 0) {
            ip = getHeaderString(realIpHeader);
        }
        if (StringUtils.isBlank(ip)) {
            ip = getHeaderString("x-forwarded-for");
            if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = getHeaderString("Proxy-Client-IP");
            }
            if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = getHeaderString("WL-Proxy-Client-IP");
            }
            if ("unknown".equalsIgnoreCase(ip)) {
                ip = "unknown";
            }
        }
        return ip;
    }

    public String getRemoteRealAddr() {
        return getRemoteRealAddr(null);
    }
}
