package ameba.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Subscribe class.</p>
 *
 * @author icode
 * @since 0.1.6e
 * @version $Id: $Id
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {
    Class<? extends Event>[] value() default {};

    boolean async() default false;
}
