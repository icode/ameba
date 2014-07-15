package ameba.db.model;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.OptimisticLockException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by ICode on 14-3-6.
 */
public abstract class Persister<M extends Model> {

    private static final Logger logger = LoggerFactory.getLogger(Persister.class);
    private M model;
    private String server;

    public Persister(String server, M model) {
        if (StringUtils.isBlank(server)) {
            throw new IllegalArgumentException("server name is blank");
        }
        if (model == null) {
            throw new IllegalArgumentException("model is null");
        }
        this.model = model;
        this.server = server;
    }

    protected M getModel() {
        return model;
    }

    protected String getServer() {
        return server;
    }


    /**
     * Changes the model server.
     */
    @SuppressWarnings("unchecked")
    public Persister<M> on(String server) {
        try {
            return (Persister<M>) this.getClass().getConstructor(String.class, Object.class).newInstance(server, model);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            logger.error("Finder.on(server) error", e);
        }
        return null;
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
        ((Model) getModel())._setId(id);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || other.getClass() != this.getClass()) return false;
        Object id = ((Model) getModel())._getId();
        Object otherId = ((Model) other)._getId();
        if (id == null) return false;
        if (otherId == null) return false;
        return id.equals(otherId);
    }

    @Override
    public int hashCode() {
        Object id = ((Model) getModel())._getId();
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

}
