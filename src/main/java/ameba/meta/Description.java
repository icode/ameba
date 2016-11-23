package ameba.meta;

import java.lang.annotation.*;

/**
 * <p>Description class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {
    String value();
}
