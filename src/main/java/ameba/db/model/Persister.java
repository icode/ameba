package ameba.db.model;

import org.apache.commons.lang3.StringUtils;

/**
 * @author icode
 */
public abstract class Persister<M extends Model> {

    private M model;
    private String serverName;

    public Persister(String serverName, M model) {
        if (StringUtils.isBlank(serverName)) {
            throw new IllegalArgumentException("server name is blank");
        }
        if (model == null) {
            throw new IllegalArgumentException("model is null");
        }
        this.model = model;
        this.serverName = serverName;
    }

    public M getModel() {
        return model;
    }

    public String getServerName() {
        return serverName;
    }

    /**
     * Changes the model server.
     */
    public abstract <E extends M> Persister<E> on(String server);

    /**
     * Saves (inserts) this entity.
     */
    public abstract void save();

    /**
     * Persist a many-to-many association.
     */
    public abstract void saveManyToManyAssociations(String path);

    /**
     * Deletes a many-to-many association
     *
     * @parama path name of the many-to-many association we want to delete
     */
    public abstract void deleteManyToManyAssociations(String path);

    /**
     * Updates this entity.
     */
    public abstract void update();

    /**
     * Updates this entity, by specifying the entity ID.
     */
    public void update(Object id) {
        getModel()._set_model_id(id);
    }

    public void update(String server) {
        on(server).update();
    }

    public void insert(String server) {
        on(server).insert();
    }

    public void delete(String server) {
        on(server).delete();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || other.getClass() != this.getClass()) return false;
        Object id = getModel()._get_model_id();
        Object otherId = ((Model) other)._get_model_id();
        return id != null && otherId != null && id.equals(otherId);
    }

    @Override
    public int hashCode() {
        Object id = getModel()._get_model_id();
        return id == null ? super.hashCode() : id.hashCode();
    }

    /**
     * Deletes this entity.
     */
    public abstract void delete();

    /**
     * Refreshes this entity from the database.
     */
    public abstract void refresh();

    /**
     * Marks the entity bean as dirty.
     * <p/>
     * This is used so that when a bean that is otherwise unmodified is updated the version
     * property is updated.
     * <p/>
     * An unmodified bean that is saved or updated is normally skipped and this marks the bean as
     * dirty so that it is not skipped.
     * <p/>
     * <pre class="code">
     * <p/>
     * Customer customer = Customer.find.byId(id);
     * <p/>
     * // mark the bean as dirty so that a save() or update() will
     * // increment the version property
     * customer.markAsDirty();
     * customer.save();
     * <p/>
     * </pre>
     */
    public abstract void markAsDirty();

    /**
     * Insert this entity.
     */
    public abstract void insert();

}
