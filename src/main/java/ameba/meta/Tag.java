package ameba.meta;

import java.lang.annotation.*;

/**
 * <p>Tag class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Tag {
    String name();

    String value();
}
