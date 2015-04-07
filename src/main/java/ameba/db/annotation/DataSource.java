package ameba.db.annotation;

import javax.validation.constraints.NotNull;
import java.lang.annotation.*;

/**
 * change default data source name
 *
 * @author icode
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface DataSource {
    @NotNull String value();
}