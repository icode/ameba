package ameba.db.model;

import com.avaje.ebean.*;
import com.avaje.ebean.text.PathProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Creates a finder for entity of modelType <code>T</code> with <I extends ID> I of modelType <code>ID</code>, using a specific Ebean server.
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

    @SuppressWarnings("unchecked")
    public <M extends T> Class<M> getModelType() {
        return (Class<M>) modelType;
    }

    public String getServerName() {
        return serverName;
    }

    /**
     * Changes the model server.
     */
    @SuppressWarnings("unchecked")
    public abstract <M extends T> Finder<ID, M> on(String server);

    /**
     * Retrieves all entities of the given type.
     */
    public abstract <M extends T> List<M> all();

    /**
     * Retrieves an entity by ID.
     */
    public abstract <M extends T> M byId(ID id);

    /**
     * Retrieves an entity reference for this ID.
     */
    public abstract <M extends T> M ref(ID id);

    /**
     * Creates a filter for sorting and filtering lists of entities locally without going back to the database.
     */
    public abstract <M extends T> Filter<M> filter();

    /**
     * Creates a query.
     */
    public abstract <M extends T> Query<M> query();

    /**
     * Returns the next identity value.
     */
    public abstract <I extends ID> I nextId();

    /**
     * Sets the OQL query to run
     */
    public abstract <M extends T> Query<M> setQuery(String oql);

    public abstract <M extends T> Query<M> setRawSql(RawSql rawSql);

    public abstract <M extends T> Query<M> setPersistenceContextScope(PersistenceContextScope scope);

    public abstract <M extends T> Query<M> setAutofetch(boolean autofetch);

    public abstract <M extends T> Query<M> setLazyLoadBatchSize(int size);

    public abstract <M extends T> Query<M> select(String fetchProperties);

    public abstract <M extends T> Query<M> fetch(String path, String fetchProperties);

    public abstract <M extends T> Query<M> fetch(String assocProperty, String fetchProperties, FetchConfig fetchConfig);

    public abstract <M extends T> Query<M> fetch(String path);

    public abstract <M extends T> Query<M> fetch(String path, FetchConfig joinConfig);

    public abstract <M extends T> Query<M> apply(PathProperties pathProperties);

    public abstract List<Object> findIds();

    public abstract <M extends T> QueryIterator<M> findIterate();

    public abstract void findEach(QueryEachConsumer<T> consumer);

    public abstract void findEachWhile(QueryEachWhileConsumer<T> consumer);

    public abstract <M extends T> List<M> findList();

    public abstract <M extends T> Set<M> findSet();

    public abstract <M extends T> Map<?, M> findMap();

    public abstract <K, M extends T> Map<K, M> findMap(String keyProperty, Class<K> keyType);

    public abstract <M extends T> M findUnique();

    public abstract int findRowCount();

    public abstract <M extends T> FutureRowCount<M> findFutureRowCount();

    public abstract <M extends T> FutureIds<M> findFutureIds();

    public abstract <M extends T> FutureList<M> findFutureList();

    public abstract <M extends T> PagedList<M> findPagedList(int pageIndex, int pageSize);

    public abstract <M extends T> Query<M> setParameter(String name, Object value);

    public abstract <M extends T> Query<M> setParameter(int position, Object value);

    public abstract <M extends T> Query<M> setId(Object id);

    public abstract <M extends T> Query<M> where(String addToWhereClause);

    public abstract <M extends T> Query<M> where(Expression expression);

    public abstract <M extends T> ExpressionList<M> where();

    public abstract <M extends T> ExpressionList<M> filterMany(String propertyName);

    public abstract <M extends T> ExpressionList<M> having();

    public abstract <M extends T> Query<M> having(String addToHavingClause);

    public abstract <M extends T> Query<M> having(Expression addExpressionToHaving);

    public abstract <M extends T> Query<M> orderBy(String orderByClause);

    public abstract <M extends T> Query<M> order(String orderByClause);

    public abstract <M extends T> OrderBy<M> order();

    public abstract <M extends T> OrderBy<M> orderBy();

    public abstract <M extends T> Query<M> setOrder(OrderBy<T> orderBy);

    public abstract <M extends T> Query<M> setOrderBy(OrderBy<T> orderBy);

    public abstract <M extends T> Query<M> setDistinct(boolean isDistinct);

    public abstract ExpressionFactory getExpressionFactory();

    public abstract int getFirstRow();

    public abstract <M extends T> Query<M> setFirstRow(int firstRow);

    public abstract int getMaxRows();

    public abstract <M extends T> Query<M> setMaxRows(int maxRows);

    public abstract <M extends T> Query<M> setMapKey(String mapKey);

    public abstract <M extends T> Query<M> setUseCache(boolean useBeanCache);

    public abstract <M extends T> Query<M> setUseQueryCache(boolean useQueryCache);

    public abstract <M extends T> Query<M> setReadOnly(boolean readOnly);

    public abstract <M extends T> Query<M> setLoadBeanCache(boolean loadBeanCache);

    public abstract <M extends T> Query<M> setTimeout(int secs);

    public abstract <M extends T> Query<M> setBufferFetchSizeHint(int fetchSize);

    public abstract <M extends T> Query<M> setForUpdate(boolean forUpdate);

    public abstract void deleteById(ID id);

}