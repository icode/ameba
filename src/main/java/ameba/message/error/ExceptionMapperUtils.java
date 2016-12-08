package ameba.message.error;

import ameba.core.Requests;
import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * <p>ExceptionMapperUtils class.</p>
 *
 * @author icode
 *
 */
public class ExceptionMapperUtils {
    private static final MediaType LOW_IE_DEFAULT_REQ_TYPE = new MediaType("application", "x-ms-application");

    private ExceptionMapperUtils() {
    }

    /**
     * <p>getResponseType.</p>
     *
     * @return a {@link javax.ws.rs.core.MediaType} object.
     */
    public static MediaType getResponseType() {
        return getResponseType(Requests.getRequest(), null);
    }

    /**
     * <p>getResponseType.</p>
     *
     * @param status a {@link java.lang.Integer} object.
     * @return a {@link javax.ws.rs.core.MediaType} object.
     */
    public static MediaType getResponseType(Integer status) {
        return getResponseType(Requests.getRequest(), status);
    }

    /**
     * <p>getResponseType.</p>
     *
     * @param request a {@link org.glassfish.jersey.server.ContainerRequest} object.
     * @return a {@link javax.ws.rs.core.MediaType} object.
     */
    public static MediaType getResponseType(ContainerRequest request) {
        return getResponseType(request, null);
    }

    /**
     * <p>getResponseType.</p>
     *
     * @param request a {@link org.glassfish.jersey.server.ContainerRequest} object.
     * @param status  a {@link java.lang.Integer} object.
     * @return a {@link javax.ws.rs.core.MediaType} object.
     */
    public static MediaType getResponseType(ContainerRequest request, Integer status) {
        if (status != null && status == 406) {
            return MediaType.TEXT_HTML_TYPE;
        }
        List<MediaType> accepts = request.getAcceptableMediaTypes();
        MediaType m;
        if (accepts != null && accepts.size() > 0) {
            m = accepts.get(0);
        } else {
            m = Requests.getMediaType();
        }
        if (m.isWildcardType() || m.equals(LOW_IE_DEFAULT_REQ_TYPE)) {
            m = MediaType.TEXT_HTML_TYPE;
        }
        return m;
    }
}
