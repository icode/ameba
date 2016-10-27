package ameba.container.server;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.core.SecurityContext;
import java.net.URI;

/**
 * <p>Abstract Request class.</p>
 *
 * @author icode
 * @since 0.1.6e
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

    public abstract String getRemoteHost();

    public abstract int getRemotePort();

    public abstract String getLocalAddr();

    public abstract String getLocalName();

    public abstract int getLocalPort();

    public abstract URI getRawReqeustUri();


    /**
     * <p>getProxyRemoteAddr.</p>
     *
     * @param realIpHeader a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getProxyRemoteAddr(String realIpHeader) {
        String ip = null;
        if (realIpHeader != null && realIpHeader.length() != 0) {
            ip = getHeaderString(realIpHeader);
        }
        if (StringUtils.isBlank(ip)) {
            ip = getHeaderString("X-Real-IP");
            if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = getHeaderString("X-Forwarded-For");
                if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
                    ip = ip.split(",")[0];
                }
            }
            if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = getHeaderString("Proxy-Client-IP");
            }
            if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = getHeaderString("WL-Proxy-Client-IP");
            }
            if (StringUtils.isNotBlank(ip)) {
                ip = ip.toLowerCase();
            }
        }
        return ip;
    }

    /**
     * <p>getRemoteRealAddr.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getProxyRemoteAddr() {
        return getProxyRemoteAddr(null);
    }

    public String getRemoteRealAddr(String realIpHeader) {
        String host = getProxyRemoteAddr(realIpHeader);
        if (host == null || host.equals("unknown")) {
            host = getRemoteAddr();
        }

        return host;
    }

    public String getRemoteRealAddr() {
        return getRemoteRealAddr(null);
    }
}
