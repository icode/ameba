package ameba.meta;

import java.lang.annotation.*;

/**
 * <p>Display class.</p>
 *
 * @author icode
 *
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Display {
    String value();
}
