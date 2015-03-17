package ameba.db.model;

import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Update;
import org.apache.commons.lang3.StringUtils;

/**
 * @author icode
 */
public abstract class Updater<M extends Model> {

    private final Class<M> modelType;
    private String serverName;
    private String sql;

    public Updater(String serverName, Class<M> modelType, String sql) {
        if (StringUtils.isBlank(serverName)) {
            throw new IllegalArgumentException("server name is blank");
        }

        this.modelType = modelType;
        this.serverName = serverName;
        this.sql = sql;
    }

    public Class<M> getModelType() {
        return modelType;
    }

    public String getServerName() {
        return serverName;
    }

    public String getSql() {
        return sql;
    }

    /**
     * Changes the model server.
     */
    public abstract <E extends M> Updater<E> on(String server);

    /**
     * Return the name if it is a named update.
     */
    public abstract String getName();

    public abstract SqlUpdate sqlUpdate();

    /**
     * Set this to false if you do not want the cache to invalidate related
     * objects.
     * <p>
     * If you don't set this Ebean will automatically invalidate the appropriate
     * parts of the "L2" server cache.
     * </p>
     */
    public abstract Update<M> setNotifyCache(boolean notifyCache);

    /**
     * Set a timeout for statement execution.
     * <p>
     * This will typically result in a call to setQueryTimeout() on a
     * preparedStatement. If the timeout occurs an exception will be thrown - this
     * will be a SQLException wrapped up in a PersistenceException.
     * </p>
     *
     * @param secs the timeout in seconds. Zero implies unlimited.
     */
    public abstract Update<M> setTimeout(int secs);

    /**
     * Execute the statement returning the number of rows modified.
     */
    public abstract int execute();

    /**
     * Set an ordered bind parameter.
     * <p>
     * position starts at value 1 (not 0) to be consistent with PreparedStatement.
     * </p>
     * <p>
     * Set a value for each ? you have in the sql.
     * </p>
     *
     * @param position the index position of the parameter starting with 1.
     * @param value    the parameter value to bind.
     */
    public abstract Update<M> set(int position, Object value);

    /**
     * Set and ordered bind parameter (same as bind).
     *
     * @param position the index position of the parameter starting with 1.
     * @param value    the parameter value to bind.
     */
    public abstract Update<M> setParameter(int position, Object value);

    /**
     * Set an ordered parameter that is null. The JDBC type of the null must be
     * specified.
     * <p>
     * position starts at value 1 (not 0) to be consistent with PreparedStatement.
     * </p>
     */
    public abstract Update<M> setNull(int position, int jdbcType);

    /**
     * Set an ordered parameter that is null (same as bind).
     */
    public abstract Update<M> setNullParameter(int position, int jdbcType);

    /**
     * Set a named parameter. Named parameters have a colon to prefix the name.
     * <p>
     * A more succinct version of setParameter() to be consistent with Query.
     * </p>
     *
     * @param name  the parameter name.
     * @param value the parameter value.
     */
    public abstract Update<M> set(String name, Object value);

    /**
     * Bind a named parameter (same as bind).
     */
    public abstract Update<M> setParameter(String name, Object param);

    /**
     * Set a named parameter that is null. The JDBC type of the null must be
     * specified.
     * <p>
     * A more succinct version of setNullParameter().
     * </p>
     *
     * @param name     the parameter name.
     * @param jdbcType the type of the property being bound.
     */
    public abstract Update<M> setNull(String name, int jdbcType);

    /**
     * Bind a named parameter that is null (same as bind).
     */
    public abstract Update<M> setNullParameter(String name, int jdbcType);

    /**
     * Return the sql that is actually executed.
     */
    public abstract String getGeneratedSql();
}
