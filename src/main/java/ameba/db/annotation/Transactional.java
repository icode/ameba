package ameba.db.annotation;

import javax.ws.rs.NameBinding;
import java.lang.annotation.*;
import java.sql.Connection;

/**
 * Wraps the annotated action in an Ebean transaction.
 *
 * @author sulijuan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Inherited
@NameBinding
public @interface Transactional {
    String[] servers() default {};

    Isolation[] isolations() default {};

    public enum Isolation {

        /**
         * Read Committed Isolation level. This is typically the default for most
         * configurations.
         */
        READ_COMMITED(Connection.TRANSACTION_READ_COMMITTED),

        /**
         * Read uncommitted Isolation level.
         */
        READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),

        /**
         * Repeatable Read Isolation level.
         */
        REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),

        /**
         * Serializable Isolation level.
         */
        SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE),

        /**
         * No Isolation level.
         */
        NONE(Connection.TRANSACTION_NONE),

        /**
         * The default isolation level. This typically means the default that the
         * DataSource is using or configured to use.
         */
        DEFAULT(-1);

        final int level;

        private Isolation(int level) {
            this.level = level;
        }

        /**
         * Return the level as per java.sql.Connection.
         * <p>
         * Note that -1 denotes the default isolation level.
         * </p>
         *
         * @return level
         */
        public int getLevel() {
            return level;
        }
    }
}
