package ameba.message.error;

import ameba.core.Requests;
import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author icode
 */
public class ExceptionMapperUtils {
    private static final MediaType LOW_IE_DEFAULT_REQ_TYPE = new MediaType("application", "x-ms-application");

    private ExceptionMapperUtils() {
    }

    public static MediaType getResponseType() {
        return getResponseType(Requests.getRequest(), null);
    }

    public static MediaType getResponseType(Integer status) {
        return getResponseType(Requests.getRequest(), status);
    }

    public static MediaType getResponseType(ContainerRequest request) {
        return getResponseType(request, null);
    }

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
