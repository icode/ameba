package ameba.db.ebean;

import ameba.db.model.Model;
import ameba.db.model.Persister;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;

import javax.persistence.OptimisticLockException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Base-class for model-mapped models that provides convenience methods.
 */
public class EbeanPersister<M extends Model> extends Persister<M> {

    private EbeanServer server;

    public EbeanPersister(String server, M model) {
        super(server, model);
        this.server = Ebean.getServer(server);
    }


    @Override
    public void save() {
        server.save(getModel());
    }

    @Override
    public void saveManyToManyAssociations(String path) {
        server.saveManyToManyAssociations(getModel(), path);
    }

    @Override
    public void deleteManyToManyAssociations(String path) {
        server.deleteManyToManyAssociations(getModel(), path);
    }

    @Override
    public void update() {
        server.update(getModel());
    }

    @Override
    public void update(Object id) {
        super.update(id);
        server.update(getModel());
    }

    @Override
    public void delete() {
        server.delete(getModel());
    }

    @Override
    public int delete(Class<?> beanType, Object id) {
        return server.delete(beanType, id);
    }

    @Override
    public void delete(Class<?> beanType, Collection<?> ids) {
        server.delete(beanType, ids);
    }

    @Override
    public int delete(Iterator<?> it) throws OptimisticLockException {
        return server.delete(it);
    }

    @Override
    public int delete(Collection<?> c) throws OptimisticLockException {
        return server.delete(c);
    }

    @Override
    public void refresh() {
        server.refresh(getModel());
    }

    @Override
    public void markAsDirty() {
        server.markAsDirty(getModel());
    }

    @Override
    public void insert() {
        server.insert(getModel());
    }
}