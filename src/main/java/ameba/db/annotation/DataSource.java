package ameba.db.annotation;

import javax.validation.constraints.NotNull;
import java.lang.annotation.*;

/**
 * change default data source name
 *
 * @author icode
 * @since 0.1.6e
 *
 */
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSource {
    @NotNull String value();
}
