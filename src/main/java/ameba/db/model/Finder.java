package ameba.db.model;

import com.avaje.ebean.*;
import com.avaje.ebean.text.PathProperties;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author icode
 */
public abstract class Finder<ID, T> {

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
     *
     * @param <M> model
     */
    @SuppressWarnings("unchecked")
    public abstract <M extends T> Finder<ID, M> on(String server);

    /**
     * Retrieves an entity by ID.
     *
     * @param <M> model
     */
    public abstract <M extends T> M byId(ID id);

    /**
     * Retrieves an entity reference for this ID.
     *
     * @param <M> model
     */
    public abstract <M extends T> M ref(ID id);

    /**
     * Creates a filter for sorting and filtering lists of entities locally without going back to the database.
     *
     * @return Filter<T>
     */
    public abstract Filter<T> filter();

    public abstract Query<T> query();

    /**
     * Creates a query.
     *
     * @return Query<T>
     */
    public abstract Query<T> createQuery();

    public abstract Query<T> createNamedQuery(String name);

    public abstract SqlQuery createNamedSqlQuery(String name);

    public abstract SqlQuery createSqlQuery(String sql);

    /**
     * Returns the next identity value.
     */
    public abstract <I extends ID> I nextId();

    /**
     * Sets the OQL query to run
     *
     * @param oql oql
     */
    public abstract Query<T> setQuery(String oql);

    public abstract Query<T> setRawSql(RawSql rawSql);

    public abstract Query<T> setPersistenceContextScope(PersistenceContextScope scope);

    public abstract Query<T> setAutofetch(boolean autofetch);

    public abstract Query<T> setLazyLoadBatchSize(int size);

    public abstract Query<T> select(String fetchProperties);

    public abstract Query<T> fetch(String path, String fetchProperties);

    public abstract Query<T> fetch(String assocProperty, String fetchProperties, FetchConfig fetchConfig);

    public abstract Query<T> fetch(String path);

    public abstract Query<T> fetch(String path, FetchConfig joinConfig);

    public abstract Query<T> apply(PathProperties pathProperties);

    public abstract List<Object> findIds();

    public abstract QueryIterator<T> findIterate();

    public abstract void findEach(QueryEachConsumer<T> consumer);

    public abstract void findEachWhile(QueryEachWhileConsumer<T> consumer);

    public abstract <M extends T> List<M> findList();

    public abstract <M extends T> Set<M> findSet();

    public abstract <M extends T> Map<?, M> findMap();

    public abstract <K, M extends T> Map<K, M> findMap(String keyProperty, Class<K> keyType);

    public abstract <M extends T> M findUnique();

    public abstract int findRowCount();

    public abstract FutureRowCount<T> findFutureRowCount();

    public abstract FutureIds<T> findFutureIds();

    public abstract FutureList<T> findFutureList();

    public abstract PagedList<T> findPagedList(int pageIndex, int pageSize);

    public abstract Query<T> setParameter(String name, Object value);

    public abstract Query<T> setParameter(int position, Object value);

    public abstract Query<T> setId(Object id);

    public abstract Query<T> where(String addToWhereClause);

    public abstract Query<T> where(Expression expression);

    public abstract ExpressionList<T> where();

    public abstract ExpressionList<T> filterMany(String propertyName);

    public abstract ExpressionList<T> having();

    public abstract Query<T> having(String addToHavingClause);

    public abstract Query<T> having(Expression addExpressionToHaving);

    public abstract Query<T> orderBy(String orderByClause);

    public abstract Query<T> order(String orderByClause);

    public abstract OrderBy<T> order();

    public abstract OrderBy<T> orderBy();

    public abstract Query<T> setOrder(OrderBy<T> orderBy);

    public abstract Query<T> setOrderBy(OrderBy<T> orderBy);

    public abstract Query<T> setDistinct(boolean isDistinct);

    public abstract ExpressionFactory getExpressionFactory();

    public abstract int getFirstRow();

    public abstract String getGeneratedSql();

    public abstract Query<T> setFirstRow(int firstRow);

    public abstract int getMaxRows();

    public abstract Query<T> setMaxRows(int maxRows);

    public abstract Query<T> setMapKey(String mapKey);

    public abstract Query<T> setUseCache(boolean useBeanCache);

    public abstract Query<T> setUseQueryCache(boolean useQueryCache);

    public abstract Query<T> setReadOnly(boolean readOnly);

    public abstract Query<T> setLoadBeanCache(boolean loadBeanCache);

    public abstract Query<T> setTimeout(int secs);

    public abstract Query<T> setBufferFetchSizeHint(int fetchSize);

    public abstract Query<T> setForUpdate(boolean forUpdate);

    public abstract void deleteById(ID id);

}