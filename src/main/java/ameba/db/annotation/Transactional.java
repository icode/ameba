package ameba.db.annotation;

import javax.ws.rs.NameBinding;
import java.lang.annotation.*;

/**
 * Wraps the annotated action in an Ebean transaction.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@NameBinding
public @interface Transactional {

}