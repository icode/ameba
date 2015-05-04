package ameba.message.error;

import ameba.core.Requests;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author icode
 */
public class ExceptionMapperUtils {
    private ExceptionMapperUtils() {
    }

    public static MediaType getResponseType() {
        List<MediaType> accepts = Requests.getAcceptableMediaTypes();
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
