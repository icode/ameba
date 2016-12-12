package ameba.db.ebean;

import ameba.db.model.Model;
import ameba.db.model.Updater;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.SqlUpdate;
import io.ebean.Update;

/**
 * <p>EbeanUpdater class.</p>
 *
 * @author icode
 * @since 0.1.6e
 *
 */
public class EbeanUpdater<M extends Model> extends Updater<M> {

    private Update<M> update;
    private EbeanServer server;

    /**
     * <p>Constructor for EbeanUpdater.</p>
     *
     * @param serverName a {@link java.lang.String} object.
     * @param modelType  a {@link java.lang.Class} object.
     * @param sql        a {@link java.lang.String} object.
     */
    public EbeanUpdater(String serverName, Class<M> modelType, String sql) {
        super(serverName, modelType, sql);
        server = Ebean.getServer(getServerName());
    }

    /**
     * <p>Getter for the field <code>update</code>.</p>
     *
     * @return a {@link io.ebean.Update} object.
     */
    public Update<M> getUpdate() {
        if (update == null)
            update = createUpdate();
        return update;
    }

    /**
     * <p>createUpdate.</p>
     *
     * @return a {@link io.ebean.Update} object.
     */
    public Update<M> createUpdate() {
        return server.createUpdate(getModelType(), getSqlOrName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <E extends M> Updater<E> on(String server) {
        return new EbeanUpdater(server, getModelType(), getSqlOrName());
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return getUpdate().getName();
    }

    /** {@inheritDoc} */
    @Override
    public SqlUpdate sqlUpdate() {
        return server.createSqlUpdate(getSqlOrName());
    }

    /** {@inheritDoc} */
    @Override
    public Update<M> setNotifyCache(boolean notifyCache) {
        return getUpdate().setNotifyCache(notifyCache);
    }

    /** {@inheritDoc} */
    @Override
    public Update<M> setTimeout(int secs) {
        return getUpdate().setTimeout(secs);
    }

    /** {@inheritDoc} */
    @Override
    public int execute() {
        return getUpdate().execute();
    }

    /** {@inheritDoc} */
    @Override
    public Update<M> set(int position, Object value) {
        return getUpdate().set(position, value);
    }

    /** {@inheritDoc} */
    @Override
    public Update<M> setParameter(int position, Object value) {
        return getUpdate().setParameter(position, value);
    }

    /** {@inheritDoc} */
    @Override
    public Update<M> setNull(int position, int jdbcType) {
        return getUpdate().setNull(position, jdbcType);
    }

    /** {@inheritDoc} */
    @Override
    public Update<M> setNullParameter(int position, int jdbcType) {
        return getUpdate().setNullParameter(position, jdbcType);
    }

    /** {@inheritDoc} */
    @Override
    public Update<M> set(String name, Object value) {
        return getUpdate().set(name, value);
    }

    /** {@inheritDoc} */
    @Override
    public Update<M> setParameter(String name, Object param) {
        return getUpdate().setParameter(name, param);
    }

    /** {@inheritDoc} */
    @Override
    public Update<M> setNull(String name, int jdbcType) {
        return getUpdate().setNull(name, jdbcType);
    }

    /** {@inheritDoc} */
    @Override
    public Update<M> setNullParameter(String name, int jdbcType) {
        return getUpdate().setNullParameter(name, jdbcType);
    }

    /** {@inheritDoc} */
    @Override
    public String getGeneratedSql() {
        return getUpdate().getGeneratedSql();
    }
}
