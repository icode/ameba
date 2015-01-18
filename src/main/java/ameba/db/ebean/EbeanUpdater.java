package ameba.db.ebean;

import ameba.db.model.Model;
import ameba.db.model.Updater;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Update;

/**
 * @author icode
 */
public class EbeanUpdater<M extends Model> extends Updater<M> {

    private Update<M> update;

    public EbeanUpdater(String serverName, Class<M> modelType, String sql) {
        super(serverName, modelType, sql);
        EbeanServer server = Ebean.getServer(getServerName());
        update = server.createUpdate(getModelType(), getSql());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends M> Updater<E> on(String server) {
        return new EbeanUpdater<E>(server, (Class<E>) getModelType(), getSql());
    }

    @Override
    public String getName() {
        return update.getName();
    }

    @Override
    public Update<M> setNotifyCache(boolean notifyCache) {
        return update.setNotifyCache(notifyCache);
    }

    @Override
    public Update<M> setTimeout(int secs) {
        return update.setTimeout(secs);
    }

    @Override
    public int execute() {
        return update.execute();
    }

    @Override
    public Update<M> set(int position, Object value) {
        return update.set(position, value);
    }

    @Override
    public Update<M> setParameter(int position, Object value) {
        return update.setParameter(position, value);
    }

    @Override
    public Update<M> setNull(int position, int jdbcType) {
        return update.setNull(position, jdbcType);
    }

    @Override
    public Update<M> setNullParameter(int position, int jdbcType) {
        return update.setNullParameter(position, jdbcType);
    }

    @Override
    public Update<M> set(String name, Object value) {
        return update.set(name, value);
    }

    @Override
    public Update<M> setParameter(String name, Object param) {
        return update.setParameter(name, param);
    }

    @Override
    public Update<M> setNull(String name, int jdbcType) {
        return update.setNull(name, jdbcType);
    }

    @Override
    public Update<M> setNullParameter(String name, int jdbcType) {
        return update.setNullParameter(name, jdbcType);
    }

    @Override
    public String getGeneratedSql() {
        return update.getGeneratedSql();
    }
}
