package ameba.core.ws.rs;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.NameBinding;
import java.lang.annotation.*;

/**
 * HTTP PATCH Method
 * <p>
 * jsonPatch must be have @GET resource/{id} method and none arguments
 *
 * @author icode
 * @since 0.1.6e
 *
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@HttpMethod(HttpPatchProperties.METHOD_NAME)
@Documented
@NameBinding
public @interface PATCH {

}
