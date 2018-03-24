package ameba.inject;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author icode
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {
    String value();
}