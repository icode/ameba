package ameba.db.ebean;

import ameba.db.model.Model;
import ameba.db.model.Persister;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;

/**
 * Base-class for model-mapped models that provides convenience methods.
 */
public class EbeanPersister<M extends Model> extends Persister<M> {

    private EbeanServer server;

    public EbeanPersister(String serverName, M model) {
        super(serverName, model);
        server = Ebean.getServer(getServerName());
    }

    private EbeanServer server() {
        return server;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends M> Persister<E> on(String server) {
        return new EbeanPersister<E>(server, (E) getModel());
    }

    @Override
    public void save() {
        server().save(getModel());
    }

    @Override
    public void saveManyToManyAssociations(String path) {
        server().saveManyToManyAssociations(getModel(), path);
    }

    @Override
    public void deleteManyToManyAssociations(String path) {
        server().deleteManyToManyAssociations(getModel(), path);
    }

    @Override
    public void update() {
        server().update(getModel());
    }

    @Override
    public void delete() {
        server().delete(getModel());
    }

    @Override
    public void refresh() {
        server().refresh(getModel());
    }

    @Override
    public void markAsDirty() {
        server().markAsDirty(getModel());
    }

    @Override
    public void insert() {
        server().insert(getModel());
    }
}