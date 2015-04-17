package ameba.db.ebean;

import ameba.db.model.Model;
import ameba.db.model.Persister;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Transaction;

/**
 * Base-class for model-mapped models that provides convenience methods.
 *
 * @author sulijuan
 */
public class EbeanPersister<M extends Model> extends Persister<M> {

    private EbeanServer server;

    /**
     * <p>Constructor for EbeanPersister.</p>
     *
     * @param serverName a {@link java.lang.String} object.
     * @param model      a M object.
     */
    public EbeanPersister(String serverName, M model) {
        super(serverName, model);
        server = Ebean.getServer(getServerName());
    }

    private EbeanServer server() {
        return server;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <E extends M> Persister<E> on(String server) {
        return new EbeanPersister<E>(server, (E) getModel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save() {
        server().save(getModel());
    }

    public void save(Transaction transaction) {
        server().save(getModel(), transaction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveManyToManyAssociations(String path) {
        server().saveManyToManyAssociations(getModel(), path);
    }

    public void saveManyToManyAssociations(String path, Transaction t) {
        server().saveManyToManyAssociations(getModel(), path, t);
    }

    @Override
    public void saveAssociation(String propertyName) {
        server().saveAssociation(getModel(), propertyName);
    }

    public void saveAssociation(String propertyName, Transaction t) {
        server().saveAssociation(getModel(), propertyName, t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteManyToManyAssociations(String path) {
        server().deleteManyToManyAssociations(getModel(), path);
    }

    public void deleteManyToManyAssociations(String path, Transaction t) {
        server().deleteManyToManyAssociations(getModel(), path, t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update() {
        server().update(getModel());
    }

    public void update(Transaction t) {
        server().update(getModel(), t);
    }


    @Override
    public void update(boolean deleteMissingChildren) {
        server().update(getModel(), null, deleteMissingChildren);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete() {
        server().delete(getModel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() {
        server().refresh(getModel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markAsDirty() {
        server().markAsDirty(getModel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insert() {
        server().insert(getModel());
    }

    public void insert(Transaction t) {
        server().insert(getModel(), t);
    }
}
