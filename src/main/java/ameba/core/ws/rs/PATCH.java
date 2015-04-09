package ameba.core.ws.rs;

import com.google.common.collect.Lists;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.NameBinding;
import java.lang.annotation.*;
import java.util.List;

import static ameba.message.internal.MediaType.APPLICATION_JSON_PATCH;

/**
 * HTTP PATCH Method
 * <p/>
 * jsonPatch must be have @GET resource/{id} method and none arguments
 *
 * @author icode
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@HttpMethod(PATCH.NAME)
@Documented
@NameBinding
public @interface PATCH {
    String NAME = "PATCH";
    String ACCEPT_PATCH_HEADER = "Accept-Patch";
    List<String> SUPPORT_PATCH_MEDIA_TYPES =
            Lists.newArrayList(
                    APPLICATION_JSON_PATCH
            );
}
