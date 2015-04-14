package ameba.db.annotation;

import javax.validation.constraints.NotNull;
import java.lang.annotation.*;

/**
 * change default data source name
 *
 * @author icode
 * @since 0.1.6e
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface DataSource {
    @NotNull String value();
}
