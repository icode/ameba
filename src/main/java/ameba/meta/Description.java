package ameba.meta;

import java.lang.annotation.*;

/**
 * <p>Description class.</p>
 *
 * @author icode
 *
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {
    String value();
}
