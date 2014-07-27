package ameba.util;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

/**
 * @author ICode
 * @since 13-8-21 上午7:48
 */
public class Cookies {
    private Cookies() {
    }

    public static NewCookie newDeletedCookie(String name) {
        /**
         * Create a new instance.
         *
         * @param name     the name of the cookie
         * @param value    the value of the cookie
         * @param path     the URI path for which the cookie is valid
         * @param domain   the host domain for which the cookie is valid
         * @param version  the version of the specification to which the cookie complies
         * @param comment  the comment
         * @param maxAge   the maximum age of the cookie in seconds
         * @param expiry   the cookie expiry date.
         * @param secure   specifies whether the cookie will only be sent over a secure connection
         * @param httpOnly if {@code true} make the cookie HTTP only, i.e. only visible as part of an HTTP request.
         * @throws IllegalArgumentException if name is {@code null}.
         * @since 2.0
         */
        return new NewCookie(name, null, null, null, Cookie.DEFAULT_VERSION + 999, null, 0, null, false, true);
    }

    public static NewCookie newHttpOnlyCookie(String name, String value) {
        /**
         * Create a new instance.
         *
         * @param name     the name of the cookie
         * @param value    the value of the cookie
         * @param path     the URI path for which the cookie is valid
         * @param domain   the host domain for which the cookie is valid
         * @param version  the version of the specification to which the cookie complies
         * @param comment  the comment
         * @param maxAge   the maximum age of the cookie in seconds
         * @param expiry   the cookie expiry date.
         * @param secure   specifies whether the cookie will only be sent over a secure connection
         * @param httpOnly if {@code true} make the cookie HTTP only, i.e. only visible as part of an HTTP request.
         * @throws IllegalArgumentException if name is {@code null}.
         * @since 2.0
         */
        return new NewCookie(name, value, null, null, Cookie.DEFAULT_VERSION, null, -1, null, false, true);
    }

    public static NewCookie newHttpOnlyCookie(String name, String value, int maxAge) {
        /**
         * Create a new instance.
         *
         * @param name     the name of the cookie
         * @param value    the value of the cookie
         * @param path     the URI path for which the cookie is valid
         * @param domain   the host domain for which the cookie is valid
         * @param version  the version of the specification to which the cookie complies
         * @param comment  the comment
         * @param maxAge   the maximum age of the cookie in seconds
         * @param expiry   the cookie expiry date.
         * @param secure   specifies whether the cookie will only be sent over a secure connection
         * @param httpOnly if {@code true} make the cookie HTTP only, i.e. only visible as part of an HTTP request.
         * @throws IllegalArgumentException if name is {@code null}.
         * @since 2.0
         */
        return new NewCookie(name, value, null, null, Cookie.DEFAULT_VERSION, null, maxAge, null, false, true);
    }
}
