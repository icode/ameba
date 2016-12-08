package ameba.meta;

import java.lang.annotation.*;

/**
 * <p>Tags class.</p>
 *
 * @author icode
 *
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Tags {
    Tag[] value();
}
