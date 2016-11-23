package ameba.db.model;

import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Update;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>Abstract Updater class.</p>
 *
 * @author icode
 * @since 0.1.6e
 * @version $Id: $Id
 */
public abstract class Updater<M extends Model> {

    private final Class<M> modelType;
    private String serverName;
    private String sqlOrName;

    /**
     * <p>Constructor for Updater.</p>
     *
     * @param serverName a {@link java.lang.String} object.
     * @param modelType  a {@link java.lang.Class} object.
     * @param sqlOrName  a {@link java.lang.String} object.
     */
    public Updater(String serverName, Class<M> modelType, String sqlOrName) {
        if (StringUtils.isBlank(serverName)) {
            throw new IllegalArgumentException("server name is blank");
        }

        this.modelType = modelType;
        this.serverName = serverName;
        this.sqlOrName = sqlOrName;
    }

    /**
     * <p>Getter for the field <code>modelType</code>.</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    public Class<M> getModelType() {
        return modelType;
    }

    /**
     * <p>Getter for the field <code>serverName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * <p>Getter for the field <code>sqlOrName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSqlOrName() {
        return sqlOrName;
    }

    /**
     * Changes the model server.
     *
     * @param server a {@link java.lang.String} object.
     * @return a {@link ameba.db.model.Updater} object.
     * @param <E> a E object.
     */
    public abstract <E extends M> Updater<E> on(String server);

    /**
     * Return the name if it is a named update.
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getName();

    /**
     * <p>sqlUpdate.</p>
     *
     * @return a {@link com.avaje.ebean.SqlUpdate} object.
     */
    public abstract SqlUpdate sqlUpdate();

    /**
     * <p>getUpdate.</p>
     *
     * @return a {@link com.avaje.ebean.Update} object.
     */
    public abstract Update<M> getUpdate();

    /**
     * <p>createUpdate.</p>
     *
     * @return a {@link com.avaje.ebean.Update} object.
     */
    public abstract Update<M> createUpdate();

    /**
     * Set this to false if you do not want the cache to invalidate related
     * objects.
     * <p>
     * If you don't set this Ebean will automatically invalidate the appropriate
     * parts of the "L2" server cache.
     * </p>
     *
     * @param notifyCache a boolean.
     * @return a {@link com.avaje.ebean.Update} object.
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
     * @return a {@link com.avaje.ebean.Update} object.
     */
    public abstract Update<M> setTimeout(int secs);

    /**
     * Execute the statement returning the number of rows modified.
     *
     * @return a int.
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
     * @return a {@link com.avaje.ebean.Update} object.
     */
    public abstract Update<M> set(int position, Object value);

    /**
     * Set and ordered bind parameter (same as bind).
     *
     * @param position the index position of the parameter starting with 1.
     * @param value    the parameter value to bind.
     * @return a {@link com.avaje.ebean.Update} object.
     */
    public abstract Update<M> setParameter(int position, Object value);

    /**
     * Set an ordered parameter that is null. The JDBC type of the null must be
     * specified.
     * <p>
     * position starts at value 1 (not 0) to be consistent with PreparedStatement.
     * </p>
     *
     * @param position a int.
     * @param jdbcType a int.
     * @return a {@link com.avaje.ebean.Update} object.
     */
    public abstract Update<M> setNull(int position, int jdbcType);

    /**
     * Set an ordered parameter that is null (same as bind).
     *
     * @param position a int.
     * @param jdbcType a int.
     * @return a {@link com.avaje.ebean.Update} object.
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
     * @return a {@link com.avaje.ebean.Update} object.
     */
    public abstract Update<M> set(String name, Object value);

    /**
     * Bind a named parameter (same as bind).
     *
     * @param name  a {@link java.lang.String} object.
     * @param param a {@link java.lang.Object} object.
     * @return a {@link com.avaje.ebean.Update} object.
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
     * @return a {@link com.avaje.ebean.Update} object.
     */
    public abstract Update<M> setNull(String name, int jdbcType);

    /**
     * Bind a named parameter that is null (same as bind).
     *
     * @param name     a {@link java.lang.String} object.
     * @param jdbcType a int.
     * @return a {@link com.avaje.ebean.Update} object.
     */
    public abstract Update<M> setNullParameter(String name, int jdbcType);

    /**
     * Return the sql that is actually executed.
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getGeneratedSql();
}
