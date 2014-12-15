package ameba.db.model;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.OptimisticLockException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author icode
 */
public abstract class Persister<M extends Model> {

    private static final Logger logger = LoggerFactory.getLogger(Persister.class);
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

    protected M getModel() {
        return model;
    }

    protected String getServerName() {
        return serverName;
    }


    /**
     * Changes the model server.
     */
    @SuppressWarnings("unchecked")
    public Persister<M> on(String server) {
        try {
            return (Persister<M>) this.getClass().getConstructor(String.class, Model.class).newInstance(server, model);
        } catch (InstantiationException e) {
            logger.error("Persister.on(server) error", e);
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            logger.error("Persister.on(server) error", e);
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            logger.error("Persister.on(server) error", e);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            logger.error("Persister.on(server) error", e);
            throw new RuntimeException(e);
        }
    }

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
        getModel()._setId(id);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || other.getClass() != this.getClass()) return false;
        Object id = getModel()._getId();
        Object otherId = ((Model) other)._getId();
        if (id == null) return false;
        if (otherId == null) return false;
        return id.equals(otherId);
    }

    @Override
    public int hashCode() {
        Object id = getModel()._getId();
        return id == null ? super.hashCode() : id.hashCode();
    }


    /**
     * Deletes this entity.
     */
    public abstract void delete();

    /**
     * Delete the bean given its type and id.
     */
    public abstract int delete(Class<?> beanType, Object id);

    /**
     * Delete several beans given their type and id values.
     */
    public abstract void delete(Class<?> beanType, Collection<?> ids);

    /**
     * Delete all the beans from an Iterator.
     */
    public abstract int delete(Iterator<?> it) throws OptimisticLockException;

    /**
     * Delete all the beans from a Collection.
     */
    public abstract int delete(Collection<?> c) throws OptimisticLockException;

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
