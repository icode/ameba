package ameba.message.error;

import ameba.core.Requests;
import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author icode
 */
public class ExceptionMapperUtils {
    private ExceptionMapperUtils() {
    }

    public static MediaType getResponseType() {
        return getResponseType(Requests.getRequest());
    }

    public static MediaType getResponseType(ContainerRequest request) {
        List<MediaType> accepts = request.getAcceptableMediaTypes();
        MediaType m;
        if (accepts != null && accepts.size() > 0) {
            m = accepts.get(0);
        } else {
            m = Requests.getMediaType();
        }
        if (m.isWildcardType()) {
            m = MediaType.APPLICATION_JSON_TYPE;
        }
        return m;
    }
}
