package ameba.db.ebean;

import ameba.db.model.Model;
import ameba.db.model.Persister;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;

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

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public <E extends M> Persister<E> on(String server) {
        return new EbeanPersister<E>(server, (E) getModel());
    }

    /** {@inheritDoc} */
    @Override
    public void save() {
        server().save(getModel());
    }

    /** {@inheritDoc} */
    @Override
    public void saveManyToManyAssociations(String path) {
        server().saveManyToManyAssociations(getModel(), path);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteManyToManyAssociations(String path) {
        server().deleteManyToManyAssociations(getModel(), path);
    }

    /** {@inheritDoc} */
    @Override
    public void update() {
        server().update(getModel());
    }

    /** {@inheritDoc} */
    @Override
    public void delete() {
        server().delete(getModel());
    }

    /** {@inheritDoc} */
    @Override
    public void refresh() {
        server().refresh(getModel());
    }

    /** {@inheritDoc} */
    @Override
    public void markAsDirty() {
        server().markAsDirty(getModel());
    }

    /** {@inheritDoc} */
    @Override
    public void insert() {
        server().insert(getModel());
    }
}
