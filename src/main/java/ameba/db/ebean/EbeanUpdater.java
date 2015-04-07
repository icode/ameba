package ameba.db.ebean;

import ameba.db.model.Model;
import ameba.db.model.Updater;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlUpdate;
import com.avaje.ebean.Update;

/**
 * @author icode
 */
public class EbeanUpdater<M extends Model> extends Updater<M> {

    private Update<M> update;
    private EbeanServer server;

    public EbeanUpdater(String serverName, Class<M> modelType, String sql) {
        super(serverName, modelType, sql);
        server = Ebean.getServer(getServerName());
    }

    protected Update<M> getUpdate() {
        if (update == null)
            update = server.createUpdate(getModelType(), getSqlOrName());
        return update;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends M> Updater<E> on(String server) {
        return new EbeanUpdater(server, getModelType(), getSqlOrName());
    }

    @Override
    public String getName() {
        return getUpdate().getName();
    }

    @Override
    public SqlUpdate sqlUpdate() {
        return server.createSqlUpdate(getSqlOrName());
    }

    @Override
    public Update namedUpdate() {
        return server.createNamedUpdate(getModelType(), getSqlOrName());
    }

    @Override
    public SqlUpdate namedSqlUpdate() {
        return server.createNamedSqlUpdate(getSqlOrName());
    }

    @Override
    public Update<M> setNotifyCache(boolean notifyCache) {
        return getUpdate().setNotifyCache(notifyCache);
    }

    @Override
    public Update<M> setTimeout(int secs) {
        return getUpdate().setTimeout(secs);
    }

    @Override
    public int execute() {
        return getUpdate().execute();
    }

    @Override
    public Update<M> set(int position, Object value) {
        return getUpdate().set(position, value);
    }

    @Override
    public Update<M> setParameter(int position, Object value) {
        return getUpdate().setParameter(position, value);
    }

    @Override
    public Update<M> setNull(int position, int jdbcType) {
        return getUpdate().setNull(position, jdbcType);
    }

    @Override
    public Update<M> setNullParameter(int position, int jdbcType) {
        return getUpdate().setNullParameter(position, jdbcType);
    }

    @Override
    public Update<M> set(String name, Object value) {
        return getUpdate().set(name, value);
    }

    @Override
    public Update<M> setParameter(String name, Object param) {
        return getUpdate().setParameter(name, param);
    }

    @Override
    public Update<M> setNull(String name, int jdbcType) {
        return getUpdate().setNull(name, jdbcType);
    }

    @Override
    public Update<M> setNullParameter(String name, int jdbcType) {
        return getUpdate().setNullParameter(name, jdbcType);
    }

    @Override
    public String getGeneratedSql() {
        return getUpdate().getGeneratedSql();
    }
}
