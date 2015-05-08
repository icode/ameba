package ameba.mvc.template.httl.internal;

import ameba.core.Requests;
import httl.spi.Resolver;
import httl.util.ClassUtils;
import httl.util.MapSupport;

/**
 * @author icode
 */
public class RequestResolver implements Resolver {

    private static final String REQUEST_KEY = "request";

    private static final String COOKIE_KEY = "cookies";

    private static final String PARAMETER_KEY = "parameters";

    private static final String HEADER_KEY = "headers";


    public Object get(String key) {
        if (REQUEST_KEY.equals(key)) {
            return Requests.getRequest();
        }
        if (COOKIE_KEY.equals(key)) {
            return new MapSupport<String, Object>() {
                public Object get(Object key) {
                    return Requests.getCookies().get(key);
                }
            };
        }
        if (PARAMETER_KEY.equals(key)) {
            return new MapSupport<String, Object>() {
                public Object get(Object key) {
                    return Requests.getUriInfo().getQueryParameters().get(key);
                }
            };
        }
        if (HEADER_KEY.equals(key)) {
            return new MapSupport<String, Object>() {
                public Object get(Object key) {
                    return Requests.getHeaders().get(key);
                }
            };
        }
        Object value = ClassUtils.getProperty(Requests.getRequest(), key);
        if (value != null) {
            return value;
        }
        value = Requests.getProperty(key);
        if (value != null) {
            return value;
        }
        value = Requests.getUriInfo().getQueryParameters().getFirst(key);
        if (value != null) {
            return value;
        }
        value = Requests.getHeaders().getFirst(key);
        if (value != null) {
            return value;
        }
        value = Requests.getCookies().get(key);
        if (value != null) {
            return value;
        }
        return null;
    }
}