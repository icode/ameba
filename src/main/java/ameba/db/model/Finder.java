package ameba.db.model;

import com.avaje.ebean.Filter;
import com.avaje.ebean.Query;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author icode
 */
public abstract class Finder<ID, T> implements Query<T> {

    private static final Logger logger = LoggerFactory.getLogger(Finder.class);
    private final Class<ID> idType;
    private final Class<T> modelType;
    private final String serverName;


    /**
     * Creates a finder for entity of modelType <code>T</code> with ID of modelType <code>ID</code>, using a specific Ebean server.
     */
    public Finder(String serverName, Class<ID> idType, Class<T> modelType) {
        if (StringUtils.isBlank(serverName)) {
            throw new IllegalArgumentException("server name is blank");
        }
        if (idType == null) {
            throw new IllegalArgumentException("id model type is null");
        }
        if (modelType == null) {
            throw new IllegalArgumentException("model model type is null");
        }

        this.modelType = modelType;
        this.idType = idType;
        this.serverName = serverName;
    }

    public Class<ID> getIdType() {
        return idType;
    }

    public Class<T> getModelType() {
        return modelType;
    }

    public String getServerName() {
        return serverName;
    }

    /**
     * Changes the model server.
     */
    @SuppressWarnings("unchecked")
    public Finder<ID, T> on(String server) {
        try {
            return this.getClass().getConstructor(String.class, Class.class, Class.class).newInstance(server, idType, modelType);
        } catch (InstantiationException e) {
            logger.error("Finder.on(server) error", e);
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            logger.error("Finder.on(server) error", e);
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            logger.error("Finder.on(server) error", e);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            logger.error("Finder.on(server) error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves all entities of the given type.
     */
    public abstract List<T> all();

    /**
     * Retrieves an entity by ID.
     */
    public abstract T byId(ID id);

    /**
     * Retrieves an entity reference for this ID.
     */
    public abstract T ref(ID id);

    /**
     * Creates a filter for sorting and filtering lists of entities locally without going back to the database.
     */
    public abstract Filter<T> filter();

    /**
     * Creates a query.
     */
    public abstract Query<T> query();

    /**
     * Returns the next identity value.
     */
    public abstract ID nextId();

    /**
     * Sets the OQL query to run
     */
    public abstract Query<T> setQuery(String oql);
}