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

    EbeanServer server;

    Query<T> query;

    public EbeanFinder(String serverName, Class<ID> idType, Class<T> type) {
        super(serverName, idType, type);
        server = Ebean.getServer(getServerName());
    }

    private EbeanServer server() {
        return server;
    }

    /**
     * Retrieves all entities of the given type.
     */
    public List<T> all() {
        return findList();
    }

    /**
     * Retrieves an entity by ID.
     */
    public T byId(ID id) {
        return server().find(getModelType(), id);
    }

    /**
     * Retrieves an entity reference for this ID.
     */
    public T ref(ID id) {
        return server().getReference(getModelType(), id);
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
        return server().find(getModelType());
    }

    private Query<T> getQuery() {
        if (query == null) {
            synchronized (this) {
                if (query == null) {
                    query = query();
                }
            }
        }
        return query;
    }

    /**
     * Returns the next identity value.
     */
    @SuppressWarnings("unchecked")
    public ID nextId() {
        return (ID) server().nextId(getModelType());
    }

    /**
     * Cancels query execution, if supported by the underlying database and driver.
     */
    public void cancel() {
        getQuery().cancel();
    }

    /**
     * Copies this query.
     */
    public Query<T> copy() {
        return getQuery().copy();
    }

    public Query<T> setPersistenceContextScope(PersistenceContextScope persistenceContextScope) {
        return getQuery().setPersistenceContextScope(persistenceContextScope);
    }

    /**
     * Specifies a path to load including all its properties.
     */
    public Query<T> fetch(String path) {
        return getQuery().fetch(path);
    }

    /**
     * Additionally specifies a <code>JoinConfig</code> to specify a 'query join' and/or define the lazy loading query.
     */
    public Query<T> fetch(String path, FetchConfig joinConfig) {
        return getQuery().fetch(path, joinConfig);
    }

    @Override
    public Query<T> apply(PathProperties pathProperties) {
        return getQuery().apply(pathProperties);
    }

    /**
     * Specifies a path to fetch with a specific list properties to include, to load a partial object.
     */
    public Query<T> fetch(String path, String fetchProperties) {
        return getQuery().fetch(path, fetchProperties);
    }

    /**
     * Additionally specifies a <code>FetchConfig</code> to use a separate query or lazy loading to load this path.
     */
    public Query<T> fetch(String assocProperty, String fetchProperties, FetchConfig fetchConfig) {
        return getQuery().fetch(assocProperty, fetchProperties, fetchConfig);
    }

    /**
     * Applies a filter on the 'many' property list rather than the root level objects.
     */
    public ExpressionList<T> filterMany(String propertyName) {
        return getQuery().filterMany(propertyName);
    }

    /**
     * Executes a find IDs query in a background thread.
     */
    public FutureIds<T> findFutureIds() {
        return getQuery().findFutureIds();
    }

    /**
     * Executes a find list query in a background thread.
     */
    public FutureList<T> findFutureList() {
        return getQuery().findFutureList();
    }

    /**
     * Executes a find row count query in a background thread.
     */
    public FutureRowCount<T> findFutureRowCount() {
        return getQuery().findFutureRowCount();
    }

    /**
     * Executes a query and returns the results as a list of IDs.
     */
    public List<Object> findIds() {
        return getQuery().findIds();
    }

    /**
     * Executes the query and returns the results as a list of objects.
     */
    public List<T> findList() {
        return getQuery().findList();
    }

    /**
     * Executes the query and returns the results as a map of objects.
     */
    public Map<?, T> findMap() {
        return getQuery().findMap();
    }

    /**
     * Executes the query and returns the results as a map of the objects.
     */
    public <K> Map<K, T> findMap(String a, Class<K> b) {
        return getQuery().findMap(a, b);
    }

    @Override
    public PagedList<T> findPagedList(int i, int i2) {
        return getQuery().findPagedList(i, i2);
    }

    /**
     * Returns the number of entities this query should return.
     */
    public int findRowCount() {
        return getQuery().findRowCount();
    }

    /**
     * Executes the query and returns the results as a set of objects.
     */
    public Set<T> findSet() {
        return getQuery().findSet();
    }

    /**
     * Executes the query and returns the results as either a single bean or <code>null</code>, if no matching bean is found.
     */
    public T findUnique() {
        return getQuery().findUnique();
    }

    @Override
    public void findEach(QueryEachConsumer<T> consumer) {
        getQuery().findEach(consumer);
    }

    @Override
    public void findEachWhile(QueryEachWhileConsumer<T> consumer) {
        getQuery().findEachWhile(consumer);
    }

    public QueryIterator<T> findIterate() {
        return getQuery().findIterate();
    }

    /**
     * This is deprecated in favor of #findEachWhile.
     * <p>
     * This is functionally exactly the same as #findEachWhile.  It is
     * replaced by findEachWhile because the method name is much better.
     * </p>
     *
     * @param visitor
     *          the visitor used to process the queried beans.
     *
     * @deprecated
     */
    public void findVisit(QueryResultVisitor<T> visitor) {
        getQuery().findVisit(visitor);
    }

    /**
     * Returns the <code>ExpressionFactory</code> used by this query.
     */
    public ExpressionFactory getExpressionFactory() {
        return getQuery().getExpressionFactory();
    }

    /**
     * Returns the first row value.
     */
    public int getFirstRow() {
        return getQuery().getFirstRow();
    }

    /**
     * Returns the SQL that was generated for executing this query.
     */
    public String getGeneratedSql() {
        return getQuery().getGeneratedSql();
    }

    /**
     * Returns the maximum of rows for this query.
     */
    public int getMaxRows() {
        return getQuery().getMaxRows();
    }

    /**
     * Returns the <code>RawSql</code> that was set to use for this query.
     */
    public RawSql getRawSql() {
        return getQuery().getRawSql();
    }

    /**
     * Returns the query's <code>having</code> clause.
     */
    public ExpressionList<T> having() {
        return getQuery().having();
    }

    /**
     * Adds an expression to the <code>having</code> clause and returns the query.
     */
    public Query<T> having(com.avaje.ebean.Expression addExpressionToHaving) {
        return getQuery().having(addExpressionToHaving);
    }

    /**
     * Adds clauses to the <code>having</code> clause and returns the query.
     */
    public Query<T> having(String addToHavingClause) {
        return getQuery().having(addToHavingClause);
    }

    /**
     * Returns <code>true</code> if this query was tuned by <code>autoFetch</code>.
     */
    public boolean isAutofetchTuned() {
        return getQuery().isAutofetchTuned();
    }

    /**
     * Returns the <code>order by</code> clause so that you can append an ascending or descending property to the <code>order by</code> clause.
     * <p/>
     * This is exactly the same as {@link #orderBy}.
     */
    public OrderBy<T> order() {
        return getQuery().order();
    }

    /**
     * Sets the <code>order by</code> clause, replacing the existing <code>order by</code> clause if there is one.
     * <p/>
     * This is exactly the same as {@link #orderBy(String)}.
     */
    public Query<T> order(String orderByClause) {
        return getQuery().order(orderByClause);
    }

    /**
     * Returns the <code>order by</code> clause so that you can append an ascending or descending property to the <code>order by</code> clause.
     * <p/>
     * This is exactly the same as {@link #order}.
     */
    public OrderBy<T> orderBy() {
        return getQuery().orderBy();
    }

    /**
     * Set the <code>order by</code> clause replacing the existing <code>order by</code> clause if there is one.
     * <p/>
     * This is exactly the same as {@link #order(String)}.
     */
    public Query<T> orderBy(String orderByClause) {
        return getQuery().orderBy(orderByClause);
    }

    /**
     * Explicitly sets a comma delimited list of the properties to fetch on the 'main' entity bean, to load a partial object.
     */
    public Query<T> select(String fetchProperties) {
        return getQuery().select(fetchProperties);
    }

    /**
     * Explicitly specifies whether to use 'Autofetch' for this query.
     */
    public Query<T> setAutofetch(boolean autofetch) {
        return getQuery().setAutofetch(autofetch);
    }

    /**
     * Set the default lazy loading batch size to use.
     * <p>
     * When lazy loading is invoked on beans loaded by this query then this sets the
     * batch size used to load those beans.
     *
     * @param lazyLoadBatchSize the number of beans to lazy load in a single batch
     */
    @Override
    public Query<T> setLazyLoadBatchSize(int lazyLoadBatchSize) {
        return getQuery().setLazyLoadBatchSize(lazyLoadBatchSize);
    }

    /**
     * Sets a hint, which for JDBC translates to <code>Statement.fetchSize()</code>.
     */
    public Query<T> setBufferFetchSizeHint(int fetchSize) {
        return getQuery().setBufferFetchSizeHint(fetchSize);
    }

    /**
     * Sets whether this query uses <code>DISTINCT</code>.
     */
    public Query<T> setDistinct(boolean isDistinct) {
        return getQuery().setDistinct(isDistinct);
    }

    /**
     * Sets the first row to return for this query.
     */
    public Query<T> setFirstRow(int firstRow) {
        return getQuery().setFirstRow(firstRow);
    }

    /**
     * Sets the ID value to query.
     */
    public Query<T> setId(Object id) {
        return getQuery().setId(id);
    }

    /**
     * When set to <code>true</code>, all the beans from this query are loaded into the bean cache.
     */
    public Query<T> setLoadBeanCache(boolean loadBeanCache) {
        return getQuery().setLoadBeanCache(loadBeanCache);
    }

    /**
     * Sets the property to use as keys for a map.
     */
    public Query<T> setMapKey(String mapKey) {
        return getQuery().setMapKey(mapKey);
    }

    /**
     * Sets the maximum number of rows to return in the query.
     */
    public Query<T> setMaxRows(int maxRows) {
        return getQuery().setMaxRows(maxRows);
    }

    /**
     * Replaces any existing <code>order by</code> clause using an <code>OrderBy</code> object.
     * <p/>
     * This is exactly the same as {@link #setOrderBy(com.avaje.ebean.OrderBy)}.
     */
    public Query<T> setOrder(OrderBy<T> orderBy) {
        return getQuery().setOrder(orderBy);
    }

    /**
     * Set an OrderBy object to replace any existing <code>order by</code> clause.
     * <p/>
     * This is exactly the same as {@link #setOrder(com.avaje.ebean.OrderBy)}.
     */
    public Query<T> setOrderBy(OrderBy<T> orderBy) {
        return getQuery().setOrderBy(orderBy);
    }

    /**
     * Sets an ordered bind parameter according to its position.
     */
    public Query<T> setParameter(int position, Object value) {
        return getQuery().setParameter(position, value);
    }

    /**
     * Sets a named bind parameter.
     */
    public Query<T> setParameter(String name, Object value) {
        return getQuery().setParameter(name, value);
    }

    /**
     * Sets the OQL query to run
     */
    public Query<T> setQuery(String oql) {
        return server().createQuery(getModelType(), oql);
    }

    @Override
    public Finder<ID, T> newFinder() {
        return new EbeanFinder<ID, T>(getServerName(), getIdType(), getModelType());
    }

    /**
     * reset old query
     * @return this
     */
    @Override
    public Finder<ID, T> reset() {
        synchronized (this) {
            query = null;
        }
        return this;
    }

    /**
     * Sets <code>RawSql</code> to use for this query.
     */
    public Query<T> setRawSql(RawSql rawSql) {
        return getQuery().setRawSql(rawSql);
    }

    /**
     * Sets whether the returned beans will be read-only.
     */
    public Query<T> setReadOnly(boolean readOnly) {
        return getQuery().setReadOnly(readOnly);
    }

    /**
     * Sets a timeout on this query.
     */
    public Query<T> setTimeout(int secs) {
        return getQuery().setTimeout(secs);
    }

    /**
     * Sets whether to use the bean cache.
     */
    public Query<T> setUseCache(boolean useBeanCache) {
        return getQuery().setUseCache(useBeanCache);
    }

    /**
     * Sets whether to use the query cache.
     */
    public Query<T> setUseQueryCache(boolean useQueryCache) {
        return getQuery().setUseQueryCache(useQueryCache);
    }

    /**
     * Adds expressions to the <code>where</code> clause with the ability to chain on the <code>ExpressionList</code>.
     */
    public ExpressionList<T> where() {
        return getQuery().where();
    }

    /**
     * Adds a single <code>Expression</code> to the <code>where</code> clause and returns the query.
     */
    public Query<T> where(com.avaje.ebean.Expression expression) {
        return getQuery().where(expression);
    }

    /**
     * Adds additional clauses to the <code>where</code> clause.
     */
    public Query<T> where(String addToWhereClause) {
        return getQuery().where(addToWhereClause);
    }

    /**
     * Execute the select with "for update" which should lock the record "on read"
     */
    @Override
    public Query<T> setForUpdate(boolean forUpdate) {
        return getQuery().setForUpdate(forUpdate);
    }

    /**
     * Whether this query is for update
     */
    @Override
    public boolean isForUpdate() {
        return getQuery().isForUpdate();
    }
}