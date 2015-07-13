package ameba.message.error;

import ameba.core.Requests;
import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.NotFoundException;
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
        return getResponseType(Requests.getRequest());
    }

    public static MediaType getResponseType(ContainerRequest request) {
        Exception e = (Exception) Requests.getProperty(DefaultExceptionMapper.BEFORE_EXCEPTION_KEY);
        if (e != null && e instanceof NotFoundException) {
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
