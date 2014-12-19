package ameba.db.model;

import com.avaje.ebean.*;
import com.avaje.ebean.text.PathProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author icode
 */
public abstract class Finder<ID, T> {

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

    public abstract Query<T> setRawSql(RawSql var1);

    public abstract Query<T> setPersistenceContextScope(PersistenceContextScope var1);

    public abstract Query<T> setAutofetch(boolean var1);

    public abstract Query<T> setLazyLoadBatchSize(int var1);

    public abstract Query<T> select(String var1);

    public abstract Query<T> fetch(String var1, String var2);

    public abstract Query<T> fetch(String var1, String var2, FetchConfig var3);

    public abstract Query<T> fetch(String var1);

    public abstract Query<T> fetch(String var1, FetchConfig var2);

    public abstract Query<T> apply(PathProperties var1);

    public abstract List<Object> findIds();

    public abstract QueryIterator<T> findIterate();

    public abstract void findEach(QueryEachConsumer<T> var1);

    public abstract void findEachWhile(QueryEachWhileConsumer<T> var1);

    public abstract List<T> findList();

    public abstract Set<T> findSet();

    public abstract Map<?, T> findMap();

    public abstract <K> Map<K, T> findMap(String var1, Class<K> var2);

    public abstract T findUnique();

    public abstract int findRowCount();

    public abstract FutureRowCount<T> findFutureRowCount();

    public abstract FutureIds<T> findFutureIds();

    public abstract FutureList<T> findFutureList();

    public abstract PagedList<T> findPagedList(int var1, int var2);

    public abstract Query<T> setParameter(String var1, Object var2);

    public abstract Query<T> setParameter(int var1, Object var2);

    public abstract Query<T> setId(Object var1);

    public abstract Query<T> where(String var1);

    public abstract  Query<T> where(Expression var1);

    public abstract ExpressionList<T> where();

    public abstract ExpressionList<T> filterMany(String var1);

    public abstract ExpressionList<T> having();

    public abstract Query<T> having(String var1);

    public abstract Query<T> having(Expression var1);

    public abstract Query<T> orderBy(String var1);

    public abstract Query<T> order(String var1);

    public abstract OrderBy<T> order();

    public abstract OrderBy<T> orderBy();

    public abstract Query<T> setOrder(OrderBy<T> var1);

    public abstract Query<T> setOrderBy(OrderBy<T> var1);

    public abstract Query<T> setDistinct(boolean var1);

    public abstract ExpressionFactory getExpressionFactory();

    public abstract int getFirstRow();

    public abstract Query<T> setFirstRow(int var1);

    public abstract int getMaxRows();

    public abstract Query<T> setMaxRows(int var1);

    public abstract Query<T> setMapKey(String var1);

    public abstract Query<T> setUseCache(boolean var1);

    public abstract Query<T> setUseQueryCache(boolean var1);

    public abstract Query<T> setReadOnly(boolean var1);

    public abstract Query<T> setLoadBeanCache(boolean var1);

    public abstract Query<T> setTimeout(int var1);

    public abstract Query<T> setBufferFetchSizeHint(int var1);

    public abstract Query<T> setForUpdate(boolean var1);

}