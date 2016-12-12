package ameba.db.ebean;

import ameba.db.model.Model;
import ameba.db.model.Persister;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;

/**
 * Base-class for model-mapped models that provides convenience methods.
 *
 * @author sulijuan
 *
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
        return new EbeanPersister<>(server, (E) getModel());
    }

    /** {@inheritDoc} */
    @Override
    public void save() {
        server().save(getModel());
    }

    /**
     * <p>save.</p>
     *
     * @param transaction a {@link io.ebean.Transaction} object.
     */
    public void save(Transaction transaction) {
        server().save(getModel(), transaction);
    }

    /** {@inheritDoc} */
    @Override
    public void update() {
        server().update(getModel());
    }

    /**
     * <p>update.</p>
     *
     * @param t a {@link io.ebean.Transaction} object.
     */
    public void update(Transaction t) {
        server().update(getModel(), t);
    }


    /** {@inheritDoc} */
    public void update(Transaction t, boolean deleteMissingChildren) {
        server().update(getModel(), t, deleteMissingChildren);
    }

    /** {@inheritDoc} */
    @Override
    public void update(boolean deleteMissingChildren) {
        Transaction t = server().currentTransaction();
        t = t == null ? server().beginTransaction() : t;
        server().update(getModel(), t, deleteMissingChildren);
    }

    /** {@inheritDoc} */
    @Override
    public void delete() {
        server().delete(getModel());
    }

    /** {@inheritDoc} */
    @Override
    public void delete(Transaction t) {
        server().delete(getModel(), t);
    }

    /** {@inheritDoc} */
    @Override
    public void refresh() {
        server().refresh(getModel());
    }

    /** {@inheritDoc} */
    @Override
    public Persister<M> markAsDirty() {
        server().markAsDirty(getModel());
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void insert() {
        server().insert(getModel());
    }

    /**
     * <p>insert.</p>
     *
     * @param t a {@link io.ebean.Transaction} object.
     */
    public void insert(Transaction t) {
        server().insert(getModel(), t);
    }
}
