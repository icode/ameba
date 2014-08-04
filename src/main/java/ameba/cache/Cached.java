package ameba.cache;

import javax.ws.rs.NameBinding;
import java.lang.annotation.*;

/**
 * @author icode
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@NameBinding
public @interface Cached {

}