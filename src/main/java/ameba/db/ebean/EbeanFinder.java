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

    public Query<T> query() {
        if (query == null) {
            query = createQuery();
        }
        return query;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <M extends T> Finder<ID, M> on(String server) {
        return new EbeanFinder(server, getIdType(), (Class<M>) getModelType());
    }

    private EbeanServer server() {
        return server;
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
    public Filter<T> filter() {
        return server().filter(getModelType());
    }

    /**
     * Creates a query.
     */
    public Query<T> createQuery() {
        return server().createQuery(getModelType());
    }

    @Override
    public Query<T> createNamedQuery(String name) {
        return server().createNamedQuery(getModelType(), name);
    }

    @Override
    public SqlQuery createNamedSqlQuery(String name) {
        return server().createNamedSqlQuery(name);
    }

    public SqlQuery createSqlQuery(String sql) {
        return server().createSqlQuery(sql);
    }

    /**
     * Returns the next identity value.
     */
    @SuppressWarnings("unchecked")
    public <I extends ID> I nextId() {
        return (I) server().nextId(getModelType());
    }


    public Query<T> setPersistenceContextScope(PersistenceContextScope persistenceContextScope) {
        return query().setPersistenceContextScope(persistenceContextScope);
    }

    /**
     * Specifies a path to load including all its properties.
     */
    public Query<T> fetch(String path) {
        return query().fetch(path);
    }

    /**
     * Additionally specifies a <code>JoinConfig</code> to specify a 'query join' and/or define the lazy loading query.
     */
    public Query<T> fetch(String path, FetchConfig joinConfig) {
        return query().fetch(path, joinConfig);
    }


    public Query<T> apply(PathProperties pathProperties) {
        return query().apply(pathProperties);
    }

    /**
     * Specifies a path to fetch with a specific list properties to include, to load a partial object.
     */
    public Query<T> fetch(String path, String fetchProperties) {
        return query().fetch(path, fetchProperties);
    }

    /**
     * Additionally specifies a <code>FetchConfig</code> to use a separate query or lazy loading to load this path.
     */
    public Query<T> fetch(String assocProperty, String fetchProperties, FetchConfig fetchConfig) {
        return query().fetch(assocProperty, fetchProperties, fetchConfig);
    }

    /**
     * Applies a filter on the 'many' property list rather than the root level objects.
     */
    public ExpressionList<T> filterMany(String propertyName) {
        return query().filterMany(propertyName);
    }

    /**
     * Executes a find IDs query in a background thread.
     */
    public FutureIds<T> findFutureIds() {
        return query().findFutureIds();
    }

    /**
     * Executes a find list query in a background thread.
     */
    public FutureList<T> findFutureList() {
        return query().findFutureList();
    }

    /**
     * Executes a find row count query in a background thread.
     */
    public FutureRowCount<T> findFutureRowCount() {
        return query().findFutureRowCount();
    }

    /**
     * Executes a query and returns the results as a list of IDs.
     */
    public List<Object> findIds() {
        return query().findIds();
    }

    /**
     * Executes the query and returns the results as a list of objects.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> List<M> findList() {
        return (List<M>) query().findList();
    }

    /**
     * Executes the query and returns the results as a map of objects.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Map<?, M> findMap() {
        return (Map<?, M>) query().findMap();
    }

    /**
     * Executes the query and returns the results as a map of the objects.
     */
    @SuppressWarnings("unchecked")
    public <K, M extends T> Map<K, M> findMap(String a, Class<K> b) {
        return (Map<K, M>) query().findMap(a, b);
    }


    public PagedList<T> findPagedList(int i, int i2) {
        return query().findPagedList(i, i2);
    }

    /**
     * Returns the number of entities this query should return.
     */
    public int findRowCount() {
        return query().findRowCount();
    }

    /**
     * Executes the query and returns the results as a set of objects.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Set<M> findSet() {
        return (Set<M>) query().findSet();
    }

    /**
     * Executes the query and returns the results as either a single bean or <code>null</code>, if no matching bean is found.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> M findUnique() {
        return (M) query().findUnique();
    }

    public void findEach(QueryEachConsumer<T> consumer) {
        query().findEach(consumer);
    }

    public void findEachWhile(QueryEachWhileConsumer<T> consumer) {
        query().findEachWhile(consumer);
    }


    public QueryIterator<T> findIterate() {
        return query().findIterate();
    }

    /**
     * Returns the <code>ExpressionFactory</code> used by this query.
     */
    public ExpressionFactory getExpressionFactory() {
        return query().getExpressionFactory();
    }

    /**
     * Returns the first row value.
     */
    public int getFirstRow() {
        return query().getFirstRow();
    }

    /**
     * Returns the SQL that was generated for executing this query.
     */
    public String getGeneratedSql() {
        return query().getGeneratedSql();
    }

    /**
     * Returns the maximum of rows for this query.
     */
    public int getMaxRows() {
        return query().getMaxRows();
    }

    /**
     * Returns the query's <code>having</code> clause.
     */
    public ExpressionList<T> having() {
        return query().having();
    }

    /**
     * Adds an expression to the <code>having</code> clause and returns the query.
     */
    public Query<T> having(com.avaje.ebean.Expression addExpressionToHaving) {
        return query().having(addExpressionToHaving);
    }

    /**
     * Adds clauses to the <code>having</code> clause and returns the query.
     */
    public Query<T> having(String addToHavingClause) {
        return query().having(addToHavingClause);
    }

    /**
     * Returns the <code>order by</code> clause so that you can append an ascending or descending property to the <code>order by</code> clause.
     * This is exactly the same as {@link #orderBy}.
     *
     * @return OrderBy
     */
    public OrderBy<T> order() {
        return query().order();
    }

    /**
     * Sets the <code>order by</code> clause, replacing the existing <code>order by</code> clause if there is one.
     * This is exactly the same as {@link #orderBy(String)}.
     *
     * @param orderByClause order by oql
     * @return Query
     */
    public Query<T> order(String orderByClause) {
        return query().order(orderByClause);
    }

    /**
     * Returns the <code>order by</code> clause so that you can append an ascending or descending property to the <code>order by</code> clause.
     * This is exactly the same as {@link #order}.
     */
    public OrderBy<T> orderBy() {
        return query().orderBy();
    }

    /**
     * Set the <code>order by</code> clause replacing the existing <code>order by</code> clause if there is one.
     * <p/>
     * This is exactly the same as {@link #order(String)}.
     */
    public Query<T> orderBy(String orderByClause) {
        return query().orderBy(orderByClause);
    }

    /**
     * Explicitly sets a comma delimited list of the properties to fetch on the 'main' entity bean, to load a partial object.
     */
    public Query<T> select(String fetchProperties) {
        return query().select(fetchProperties);
    }

    /**
     * Explicitly specifies whether to use 'Autofetch' for this query.
     */
    public Query<T> setAutofetch(boolean autofetch) {
        return query().setAutofetch(autofetch);
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
        return query().setLazyLoadBatchSize(lazyLoadBatchSize);
    }

    /**
     * Sets a hint, which for JDBC translates to <code>Statement.fetchSize()</code>.
     */
    public Query<T> setBufferFetchSizeHint(int fetchSize) {
        return query().setBufferFetchSizeHint(fetchSize);
    }

    /**
     * Sets whether this query uses <code>DISTINCT</code>.
     */
    public Query<T> setDistinct(boolean isDistinct) {
        return query().setDistinct(isDistinct);
    }

    /**
     * Sets the first row to return for this query.
     */
    public Query<T> setFirstRow(int firstRow) {
        return query().setFirstRow(firstRow);
    }

    /**
     * Sets the ID value to query.
     */
    public Query<T> setId(Object id) {
        return query().setId(id);
    }

    /**
     * When set to <code>true</code>, all the beans from this query are loaded into the bean cache.
     */
    public Query<T> setLoadBeanCache(boolean loadBeanCache) {
        return query().setLoadBeanCache(loadBeanCache);
    }

    /**
     * Sets the property to use as keys for a map.
     */
    public Query<T> setMapKey(String mapKey) {
        return query().setMapKey(mapKey);
    }

    /**
     * Sets the maximum number of rows to return in the query.
     */
    public Query<T> setMaxRows(int maxRows) {
        return query().setMaxRows(maxRows);
    }

    /**
     * Replaces any existing <code>order by</code> clause using an <code>OrderBy</code> object.
     * <p/>
     * This is exactly the same as {@link #setOrderBy(com.avaje.ebean.OrderBy)}.
     */
    public Query<T> setOrder(OrderBy<T> orderBy) {
        return query().setOrder(orderBy);
    }

    /**
     * Set an OrderBy object to replace any existing <code>order by</code> clause.
     * <p/>
     * This is exactly the same as {@link #setOrder(com.avaje.ebean.OrderBy)}.
     */
    public Query<T> setOrderBy(OrderBy<T> orderBy) {
        return query().setOrderBy(orderBy);
    }

    /**
     * Sets an ordered bind parameter according to its position.
     */
    public Query<T> setParameter(int position, Object value) {
        return query().setParameter(position, value);
    }

    /**
     * Sets a named bind parameter.
     */
    public Query<T> setParameter(String name, Object value) {
        return query().setParameter(name, value);
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
        return query().setRawSql(rawSql);
    }

    /**
     * Sets whether the returned beans will be read-only.
     */
    public Query<T> setReadOnly(boolean readOnly) {
        return query().setReadOnly(readOnly);
    }

    /**
     * Sets a timeout on this query.
     */
    public Query<T> setTimeout(int secs) {
        return query().setTimeout(secs);
    }

    /**
     * Sets whether to use the bean cache.
     */
    public Query<T> setUseCache(boolean useBeanCache) {
        return query().setUseCache(useBeanCache);
    }

    /**
     * Sets whether to use the query cache.
     */
    public Query<T> setUseQueryCache(boolean useQueryCache) {
        return query().setUseQueryCache(useQueryCache);
    }

    /**
     * Adds expressions to the <code>where</code> clause with the ability to chain on the <code>ExpressionList</code>.
     */
    public ExpressionList<T> where() {
        return query().where();
    }

    /**
     * Adds a single <code>Expression</code> to the <code>where</code> clause and returns the query.
     */
    public Query<T> where(com.avaje.ebean.Expression expression) {
        return query().where(expression);
    }

    /**
     * Adds additional clauses to the <code>where</code> clause.
     */
    public Query<T> where(String addToWhereClause) {
        return query().where(addToWhereClause);
    }

    /**
     * Execute the select with "for update" which should lock the record "on read"
     */
    public Query<T> setForUpdate(boolean forUpdate) {
        return query().setForUpdate(forUpdate);
    }

    @Override
    public void deleteById(ID id) {
        server().delete(id);
    }

}