package ameba.db.ebean.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Filter class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Filter {
    String[] blacklist() default {};

    String[] whitelist() default {};
}
