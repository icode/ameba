package ameba.db.ebean;

import ameba.db.model.Finder;
import com.avaje.ebean.*;
import com.avaje.ebean.text.PathProperties;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base-class for model-mapped models that provides convenience methods.
 */
public class EbeanFinder<ID, T> extends Finder<ID, T> {

    private EbeanServer server;

    private Query<T> query;

    public EbeanFinder(String serverName, Class<ID> idType, Class<T> type) {
        super(serverName, idType, type);
        server = Ebean.getServer(getServerName());
    }

    private Query<T> _query() {
        if (query == null) {
            query = query();
        }
        return query;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M extends T> Finder<ID, M> on(String server) {
        return new EbeanFinder<ID, M>(server, getIdType(), (Class<M>) getModelType());
    }

    private EbeanServer server() {
        return server;
    }

    /**
     * Retrieves all entities of the given type.
     */
    public <M extends T> List<M> all() {
        return findList();
    }

    /**
     * Retrieves an entity by ID.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> M byId(ID id) {
        return (M) server().find(getModelType(), id);
    }

    /**
     * Retrieves an entity reference for this ID.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> M ref(ID id) {
        return (M) server().getReference(getModelType(), id);
    }

    /**
     * Creates a filter for sorting and filtering lists of entities locally without going back to the database.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Filter<M> filter() {
        return (Filter<M>) server().filter(getModelType());
    }

    /**
     * Creates a query.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> query() {
        return (Query<M>) server().createQuery(getModelType());
    }

    /**
     * Returns the next identity value.
     */
    @SuppressWarnings("unchecked")
    public <I extends ID> I nextId() {
        return (I) server().nextId(getModelType());
    }

    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setPersistenceContextScope(PersistenceContextScope persistenceContextScope) {
        return (Query<M>) _query().setPersistenceContextScope(persistenceContextScope);
    }

    /**
     * Specifies a path to load including all its properties.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> fetch(String path) {
        return (Query<M>) _query().fetch(path);
    }

    /**
     * Additionally specifies a <code>JoinConfig</code> to specify a 'query join' and/or define the lazy loading query.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> fetch(String path, FetchConfig joinConfig) {
        return (Query<M>) _query().fetch(path, joinConfig);
    }

    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> apply(PathProperties pathProperties) {
        return (Query<M>) _query().apply(pathProperties);
    }

    /**
     * Specifies a path to fetch with a specific list properties to include, to load a partial object.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> fetch(String path, String fetchProperties) {
        return (Query<M>) _query().fetch(path, fetchProperties);
    }

    /**
     * Additionally specifies a <code>FetchConfig</code> to use a separate query or lazy loading to load this path.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> fetch(String assocProperty, String fetchProperties, FetchConfig fetchConfig) {
        return (Query<M>) _query().fetch(assocProperty, fetchProperties, fetchConfig);
    }

    /**
     * Applies a filter on the 'many' property list rather than the root level objects.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> ExpressionList<M> filterMany(String propertyName) {
        return (ExpressionList<M>) _query().filterMany(propertyName);
    }

    /**
     * Executes a find IDs query in a background thread.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> FutureIds<M> findFutureIds() {
        return (FutureIds<M>) _query().findFutureIds();
    }

    /**
     * Executes a find list query in a background thread.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> FutureList<M> findFutureList() {
        return (FutureList<M>) _query().findFutureList();
    }

    /**
     * Executes a find row count query in a background thread.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> FutureRowCount<M> findFutureRowCount() {
        return (FutureRowCount<M>) _query().findFutureRowCount();
    }

    /**
     * Executes a query and returns the results as a list of IDs.
     */
    public List<Object> findIds() {
        return _query().findIds();
    }

    /**
     * Executes the query and returns the results as a list of objects.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> List<M> findList() {
        return (List<M>) _query().findList();
    }

    /**
     * Executes the query and returns the results as a map of objects.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Map<?, M> findMap() {
        return (Map<?, M>) _query().findMap();
    }

    /**
     * Executes the query and returns the results as a map of the objects.
     */
    @SuppressWarnings("unchecked")
    public <K, M extends T> Map<K, M> findMap(String a, Class<K> b) {
        return (Map<K, M>) _query().findMap(a, b);
    }

    @SuppressWarnings("unchecked")
    public <M extends T> PagedList<M> findPagedList(int i, int i2) {
        return (PagedList<M>) _query().findPagedList(i, i2);
    }

    /**
     * Returns the number of entities this query should return.
     */
    public int findRowCount() {
        return _query().findRowCount();
    }

    /**
     * Executes the query and returns the results as a set of objects.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Set<M> findSet() {
        return (Set<M>) _query().findSet();
    }

    /**
     * Executes the query and returns the results as either a single bean or <code>null</code>, if no matching bean is found.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> M findUnique() {
        return (M) _query().findUnique();
    }

    public void findEach(QueryEachConsumer<T> consumer) {
        _query().findEach(consumer);
    }

    public void findEachWhile(QueryEachWhileConsumer<T> consumer) {
        _query().findEachWhile(consumer);
    }

    @SuppressWarnings("unchecked")
    public <M extends T> QueryIterator<M> findIterate() {
        return (QueryIterator<M>) _query().findIterate();
    }

    /**
     * Returns the <code>ExpressionFactory</code> used by this query.
     */
    public ExpressionFactory getExpressionFactory() {
        return _query().getExpressionFactory();
    }

    /**
     * Returns the first row value.
     */
    public int getFirstRow() {
        return _query().getFirstRow();
    }

    /**
     * Returns the SQL that was generated for executing this query.
     */
    public String getGeneratedSql() {
        return _query().getGeneratedSql();
    }

    /**
     * Returns the maximum of rows for this query.
     */
    public int getMaxRows() {
        return _query().getMaxRows();
    }

    /**
     * Returns the query's <code>having</code> clause.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> ExpressionList<M> having() {
        return (ExpressionList<M>) _query().having();
    }

    /**
     * Adds an expression to the <code>having</code> clause and returns the query.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> having(com.avaje.ebean.Expression addExpressionToHaving) {
        return (Query<M>) _query().having(addExpressionToHaving);
    }

    /**
     * Adds clauses to the <code>having</code> clause and returns the query.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> having(String addToHavingClause) {
        return (Query<M>) _query().having(addToHavingClause);
    }

    /**
     * Returns the <code>order by</code> clause so that you can append an ascending or descending property to the <code>order by</code> clause.
     * <p/>
     * This is exactly the same as {@link #orderBy}.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> OrderBy<M> order() {
        return (OrderBy<M>) _query().order();
    }

    /**
     * Sets the <code>order by</code> clause, replacing the existing <code>order by</code> clause if there is one.
     * <p/>
     * This is exactly the same as {@link #orderBy(String)}.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> order(String orderByClause) {
        return (Query<M>) _query().order(orderByClause);
    }

    /**
     * Returns the <code>order by</code> clause so that you can append an ascending or descending property to the <code>order by</code> clause.
     * <p/>
     * This is exactly the same as {@link #order}.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> OrderBy<M> orderBy() {
        return (OrderBy<M>) _query().orderBy();
    }

    /**
     * Set the <code>order by</code> clause replacing the existing <code>order by</code> clause if there is one.
     * <p/>
     * This is exactly the same as {@link #order(String)}.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> orderBy(String orderByClause) {
        return (Query<M>) _query().orderBy(orderByClause);
    }

    /**
     * Explicitly sets a comma delimited list of the properties to fetch on the 'main' entity bean, to load a partial object.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> select(String fetchProperties) {
        return (Query<M>) _query().select(fetchProperties);
    }

    /**
     * Explicitly specifies whether to use 'Autofetch' for this query.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setAutofetch(boolean autofetch) {
        return (Query<M>) _query().setAutofetch(autofetch);
    }

    /**
     * Set the default lazy loading batch size to use.
     * <p/>
     * When lazy loading is invoked on beans loaded by this query then this sets the
     * batch size used to load those beans.
     *
     * @param lazyLoadBatchSize the number of beans to lazy load in a single batch
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setLazyLoadBatchSize(int lazyLoadBatchSize) {
        return (Query<M>) _query().setLazyLoadBatchSize(lazyLoadBatchSize);
    }

    /**
     * Sets a hint, which for JDBC translates to <code>Statement.fetchSize()</code>.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setBufferFetchSizeHint(int fetchSize) {
        return (Query<M>) _query().setBufferFetchSizeHint(fetchSize);
    }

    /**
     * Sets whether this query uses <code>DISTINCT</code>.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setDistinct(boolean isDistinct) {
        return (Query<M>) _query().setDistinct(isDistinct);
    }

    /**
     * Sets the first row to return for this query.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setFirstRow(int firstRow) {
        return (Query<M>) _query().setFirstRow(firstRow);
    }

    /**
     * Sets the ID value to query.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setId(Object id) {
        return (Query<M>) _query().setId(id);
    }

    /**
     * When set to <code>true</code>, all the beans from this query are loaded into the bean cache.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setLoadBeanCache(boolean loadBeanCache) {
        return (Query<M>) _query().setLoadBeanCache(loadBeanCache);
    }

    /**
     * Sets the property to use as keys for a map.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setMapKey(String mapKey) {
        return (Query<M>) _query().setMapKey(mapKey);
    }

    /**
     * Sets the maximum number of rows to return in the query.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setMaxRows(int maxRows) {
        return (Query<M>) _query().setMaxRows(maxRows);
    }

    /**
     * Replaces any existing <code>order by</code> clause using an <code>OrderBy</code> object.
     * <p/>
     * This is exactly the same as {@link #setOrderBy(com.avaje.ebean.OrderBy)}.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setOrder(OrderBy<T> orderBy) {
        return (Query<M>) _query().setOrder(orderBy);
    }

    /**
     * Set an OrderBy object to replace any existing <code>order by</code> clause.
     * <p/>
     * This is exactly the same as {@link #setOrder(com.avaje.ebean.OrderBy)}.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setOrderBy(OrderBy<T> orderBy) {
        return (Query<M>) _query().setOrderBy(orderBy);
    }

    /**
     * Sets an ordered bind parameter according to its position.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setParameter(int position, Object value) {
        return (Query<M>) _query().setParameter(position, value);
    }

    /**
     * Sets a named bind parameter.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setParameter(String name, Object value) {
        return (Query<M>) _query().setParameter(name, value);
    }

    /**
     * Sets the OQL query to run
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setQuery(String oql) {
        return (Query<M>) (query = server().createQuery(getModelType(), oql));
    }

    /**
     * Sets <code>RawSql</code> to use for this query.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setRawSql(RawSql rawSql) {
        return (Query<M>) _query().setRawSql(rawSql);
    }

    /**
     * Sets whether the returned beans will be read-only.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setReadOnly(boolean readOnly) {
        return (Query<M>) _query().setReadOnly(readOnly);
    }

    /**
     * Sets a timeout on this query.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setTimeout(int secs) {
        return (Query<M>) _query().setTimeout(secs);
    }

    /**
     * Sets whether to use the bean cache.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setUseCache(boolean useBeanCache) {
        return (Query<M>) _query().setUseCache(useBeanCache);
    }

    /**
     * Sets whether to use the query cache.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setUseQueryCache(boolean useQueryCache) {
        return (Query<M>) _query().setUseQueryCache(useQueryCache);
    }

    /**
     * Adds expressions to the <code>where</code> clause with the ability to chain on the <code>ExpressionList</code>.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> ExpressionList<M> where() {
        return (ExpressionList<M>) _query().where();
    }

    /**
     * Adds a single <code>Expression</code> to the <code>where</code> clause and returns the query.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> where(com.avaje.ebean.Expression expression) {
        return (Query<M>) _query().where(expression);
    }

    /**
     * Adds additional clauses to the <code>where</code> clause.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> where(String addToWhereClause) {
        return (Query<M>) _query().where(addToWhereClause);
    }

    /**
     * Execute the select with "for update" which should lock the record "on read"
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Query<M> setForUpdate(boolean forUpdate) {
        return (Query<M>) _query().setForUpdate(forUpdate);
    }

    @Override
    public void deleteById(ID id) {
        server().delete(id);
    }

}