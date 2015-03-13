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
    
    public <M extends T> Finder<ID, M> on(String server) {
        return new EbeanFinder<ID, M>(server, getIdType(), (Class<M>) getModelType());
    }

    private EbeanServer server() {
        return server;
    }

    /**
     * Retrieves an entity by ID.
     */
    
    public <M extends T> M byId(ID id) {
        return (M) server().find(getModelType(), id);
    }

    /**
     * Retrieves an entity reference for this ID.
     */
    
    public <M extends T> M ref(ID id) {
        return (M) server().getReference(getModelType(), id);
    }

    /**
     * Creates a filter for sorting and filtering lists of entities locally without going back to the database.
     */
    
    public Filter<T> filter() {
        return server().filter(getModelType());
    }

    /**
     * Creates a query.
     */
    
    public Query<T> query() {
        return server().createQuery(getModelType());
    }

    /**
     * Returns the next identity value.
     */
    
    public <I extends ID> I nextId() {
        return (I) server().nextId(getModelType());
    }

    
    public Query<T> setPersistenceContextScope(PersistenceContextScope persistenceContextScope) {
        return _query().setPersistenceContextScope(persistenceContextScope);
    }

    /**
     * Specifies a path to load including all its properties.
     */
    
    public Query<T> fetch(String path) {
        return _query().fetch(path);
    }

    /**
     * Additionally specifies a <code>JoinConfig</code> to specify a 'query join' and/or define the lazy loading query.
     */
    
    public Query<T> fetch(String path, FetchConfig joinConfig) {
        return _query().fetch(path, joinConfig);
    }

    
    public Query<T> apply(PathProperties pathProperties) {
        return _query().apply(pathProperties);
    }

    /**
     * Specifies a path to fetch with a specific list properties to include, to load a partial object.
     */
    
    public Query<T> fetch(String path, String fetchProperties) {
        return _query().fetch(path, fetchProperties);
    }

    /**
     * Additionally specifies a <code>FetchConfig</code> to use a separate query or lazy loading to load this path.
     */
    
    public Query<T> fetch(String assocProperty, String fetchProperties, FetchConfig fetchConfig) {
        return _query().fetch(assocProperty, fetchProperties, fetchConfig);
    }

    /**
     * Applies a filter on the 'many' property list rather than the root level objects.
     */
    
    public ExpressionList<T> filterMany(String propertyName) {
        return _query().filterMany(propertyName);
    }

    /**
     * Executes a find IDs query in a background thread.
     */
    
    public FutureIds<T> findFutureIds() {
        return _query().findFutureIds();
    }

    /**
     * Executes a find list query in a background thread.
     */
    
    public FutureList<T> findFutureList() {
        return _query().findFutureList();
    }

    /**
     * Executes a find row count query in a background thread.
     */
    
    public FutureRowCount<T> findFutureRowCount() {
        return _query().findFutureRowCount();
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
    
    public <M extends T> List<M> findList() {
        return (List<M>) _query().findList();
    }

    /**
     * Executes the query and returns the results as a map of objects.
     */
    
    public <M extends T> Map<?, M> findMap() {
        return (Map<?, M>) _query().findMap();
    }

    /**
     * Executes the query and returns the results as a map of the objects.
     */
    
    public <K, M extends T> Map<K, M> findMap(String a, Class<K> b) {
        return (Map<K, M>) _query().findMap(a, b);
    }

    
    public PagedList<T> findPagedList(int i, int i2) {
        return _query().findPagedList(i, i2);
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
    
    public <M extends T> Set<M> findSet() {
        return (Set<M>) _query().findSet();
    }

    /**
     * Executes the query and returns the results as either a single bean or <code>null</code>, if no matching bean is found.
     */
    
    public <M extends T> M findUnique() {
        return (M) _query().findUnique();
    }

    public void findEach(QueryEachConsumer<T> consumer) {
        _query().findEach(consumer);
    }

    public void findEachWhile(QueryEachWhileConsumer<T> consumer) {
        _query().findEachWhile(consumer);
    }

    
    public QueryIterator<T> findIterate() {
        return _query().findIterate();
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
    
    public ExpressionList<T> having() {
        return _query().having();
    }

    /**
     * Adds an expression to the <code>having</code> clause and returns the query.
     */
    
    public Query<T> having(com.avaje.ebean.Expression addExpressionToHaving) {
        return _query().having(addExpressionToHaving);
    }

    /**
     * Adds clauses to the <code>having</code> clause and returns the query.
     */
    
    public Query<T> having(String addToHavingClause) {
        return _query().having(addToHavingClause);
    }

    /**
     * Returns the <code>order by</code> clause so that you can append an ascending or descending property to the <code>order by</code> clause.
     * <p/>
     * This is exactly the same as {@link #orderBy}.
     */
    
    public OrderBy<T> order() {
        return _query().order();
    }

    /**
     * Sets the <code>order by</code> clause, replacing the existing <code>order by</code> clause if there is one.
     * <p/>
     * This is exactly the same as {@link #orderBy(String)}.
     */
    
    public Query<T> order(String orderByClause) {
        return _query().order(orderByClause);
    }

    /**
     * Returns the <code>order by</code> clause so that you can append an ascending or descending property to the <code>order by</code> clause.
     * <p/>
     * This is exactly the same as {@link #order}.
     */
    
    public OrderBy<T> orderBy() {
        return _query().orderBy();
    }

    /**
     * Set the <code>order by</code> clause replacing the existing <code>order by</code> clause if there is one.
     * <p/>
     * This is exactly the same as {@link #order(String)}.
     */
    
    public Query<T> orderBy(String orderByClause) {
        return _query().orderBy(orderByClause);
    }

    /**
     * Explicitly sets a comma delimited list of the properties to fetch on the 'main' entity bean, to load a partial object.
     */
    
    public Query<T> select(String fetchProperties) {
        return _query().select(fetchProperties);
    }

    /**
     * Explicitly specifies whether to use 'Autofetch' for this query.
     */
    
    public Query<T> setAutofetch(boolean autofetch) {
        return _query().setAutofetch(autofetch);
    }

    /**
     * Set the default lazy loading batch size to use.
     * <p/>
     * When lazy loading is invoked on beans loaded by this query then this sets the
     * batch size used to load those beans.
     *
     * @param lazyLoadBatchSize the number of beans to lazy load in a single batch
     */
    
    public Query<T> setLazyLoadBatchSize(int lazyLoadBatchSize) {
        return _query().setLazyLoadBatchSize(lazyLoadBatchSize);
    }

    /**
     * Sets a hint, which for JDBC translates to <code>Statement.fetchSize()</code>.
     */
    
    public Query<T> setBufferFetchSizeHint(int fetchSize) {
        return _query().setBufferFetchSizeHint(fetchSize);
    }

    /**
     * Sets whether this query uses <code>DISTINCT</code>.
     */
    
    public Query<T> setDistinct(boolean isDistinct) {
        return _query().setDistinct(isDistinct);
    }

    /**
     * Sets the first row to return for this query.
     */
    
    public Query<T> setFirstRow(int firstRow) {
        return _query().setFirstRow(firstRow);
    }

    /**
     * Sets the ID value to query.
     */
    
    public Query<T> setId(Object id) {
        return _query().setId(id);
    }

    /**
     * When set to <code>true</code>, all the beans from this query are loaded into the bean cache.
     */
    
    public Query<T> setLoadBeanCache(boolean loadBeanCache) {
        return _query().setLoadBeanCache(loadBeanCache);
    }

    /**
     * Sets the property to use as keys for a map.
     */
    
    public Query<T> setMapKey(String mapKey) {
        return _query().setMapKey(mapKey);
    }

    /**
     * Sets the maximum number of rows to return in the query.
     */
    
    public Query<T> setMaxRows(int maxRows) {
        return _query().setMaxRows(maxRows);
    }

    /**
     * Replaces any existing <code>order by</code> clause using an <code>OrderBy</code> object.
     * <p/>
     * This is exactly the same as {@link #setOrderBy(com.avaje.ebean.OrderBy)}.
     */
    
    public Query<T> setOrder(OrderBy<T> orderBy) {
        return _query().setOrder(orderBy);
    }

    /**
     * Set an OrderBy object to replace any existing <code>order by</code> clause.
     * <p/>
     * This is exactly the same as {@link #setOrder(com.avaje.ebean.OrderBy)}.
     */
    
    public Query<T> setOrderBy(OrderBy<T> orderBy) {
        return _query().setOrderBy(orderBy);
    }

    /**
     * Sets an ordered bind parameter according to its position.
     */
    
    public Query<T> setParameter(int position, Object value) {
        return _query().setParameter(position, value);
    }

    /**
     * Sets a named bind parameter.
     */
    
    public Query<T> setParameter(String name, Object value) {
        return _query().setParameter(name, value);
    }

    /**
     * Sets the OQL query to run
     */
    
    public Query<T> setQuery(String oql) {
        return (query = server().createQuery(getModelType(), oql));
    }

    /**
     * Sets <code>RawSql</code> to use for this query.
     */
    
    public Query<T> setRawSql(RawSql rawSql) {
        return _query().setRawSql(rawSql);
    }

    /**
     * Sets whether the returned beans will be read-only.
     */
    
    public Query<T> setReadOnly(boolean readOnly) {
        return _query().setReadOnly(readOnly);
    }

    /**
     * Sets a timeout on this query.
     */
    
    public Query<T> setTimeout(int secs) {
        return _query().setTimeout(secs);
    }

    /**
     * Sets whether to use the bean cache.
     */
    
    public Query<T> setUseCache(boolean useBeanCache) {
        return _query().setUseCache(useBeanCache);
    }

    /**
     * Sets whether to use the query cache.
     */
    
    public Query<T> setUseQueryCache(boolean useQueryCache) {
        return _query().setUseQueryCache(useQueryCache);
    }

    /**
     * Adds expressions to the <code>where</code> clause with the ability to chain on the <code>ExpressionList</code>.
     */
    
    public ExpressionList<T> where() {
        return _query().where();
    }

    /**
     * Adds a single <code>Expression</code> to the <code>where</code> clause and returns the query.
     */
    
    public Query<T> where(com.avaje.ebean.Expression expression) {
        return _query().where(expression);
    }

    /**
     * Adds additional clauses to the <code>where</code> clause.
     */
    
    public Query<T> where(String addToWhereClause) {
        return _query().where(addToWhereClause);
    }

    /**
     * Execute the select with "for update" which should lock the record "on read"
     */
    
    public Query<T> setForUpdate(boolean forUpdate) {
        return _query().setForUpdate(forUpdate);
    }

    @Override
    public void deleteById(ID id) {
        server().delete(id);
    }

}