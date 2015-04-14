package ameba.websocket.internal;

import java.lang.annotation.*;

/**
 * <p>QueryString class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
public @interface QueryString {
}
