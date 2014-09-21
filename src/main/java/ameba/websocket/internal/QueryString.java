package ameba.websocket.internal;

import java.lang.annotation.*;

/**
 * @author icode
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Documented
public @interface QueryString {
}
