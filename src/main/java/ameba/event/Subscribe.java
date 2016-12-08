package ameba.event;

import java.lang.annotation.*;

/**
 * <p>Subscribe class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface Subscribe {
    Class<? extends Event>[] value() default {};

    boolean async() default false;
}
