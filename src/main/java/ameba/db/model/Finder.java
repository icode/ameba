package ameba.db.model;

import com.avaje.ebean.*;
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
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            logger.error("Finder.on(server) error", e);
        }
        return null;
    }

    /**
     * Retrieves all entities of the given modelType.
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
    public abstract <T> Filter<T> filter();

    /**
     * Creates a query.
     */
    public abstract Query<T> query();

    /**
     * Returns the next identity value.
     */
    public abstract ID nextId();

    /**
     * Cancels query execution, if supported by the underlying database and driver.
     */
    public abstract void cancel();

    /**
     * Copies this query.
     */
    public abstract Query<T> copy();

    /**
     * Specifies a path to load including all its properties.
     */
    public abstract Query<T> fetch(String path);

    /**
     * Additionally specifies a <code>JoinConfig</code> to specify a 'query join' and/or define the lazy loading query.
     */
    public abstract Query<T> fetch(String path, FetchConfig joinConfig);

    /**
     * Specifies a path to fetch with a specific list properties to include, to load a partial object.
     */
    public abstract Query<T> fetch(String path, String fetchProperties);

    /**
     * Additionally specifies a <code>FetchConfig</code> to use a separate query or lazy loading to load this path.
     */
    public abstract Query<T> fetch(String assocProperty, String fetchProperties, FetchConfig fetchConfig);

    /**
     * Applies a filter on the 'many' property list rather than the root level objects.
     */
    public abstract ExpressionList<T> filterMany(String propertyName);

    /**
     * Executes a find IDs query in a background thread.
     */
    public abstract FutureIds<T> findFutureIds();

    /**
     * Executes a find list query in a background thread.
     */
    public abstract FutureList<T> findFutureList();

    /**
     * Executes a find row count query in a background thread.
     */
    public abstract FutureRowCount<T> findFutureRowCount();

    /**
     * Executes a query and returns the results as a list of IDs.
     */
    public abstract List<Object> findIds();

    /**
     * Executes the query and returns the results as a list of objects.
     */
    public abstract List<T> findList();

    /**
     * Executes the query and returns the results as a map of objects.
     */
    public abstract Map<?, T> findMap();

    /**
     * Executes the query and returns the results as a map of the objects.
     */
    public abstract <K> Map<K, T> findMap(String a, Class<K> b);

    /**
     * Returns a <code>PagingList</code> for this query.
     */
    public abstract PagingList<T> findPagingList(int pageSize);

    /**
     * Returns the number of entities this query should return.
     */
    public abstract int findRowCount();

    /**
     * Executes the query and returns the results as a set of objects.
     */
    public abstract Set<T> findSet();

    /**
     * Executes the query and returns the results as either a single bean or <code>null</code>, if no matching bean is found.
     */
    public abstract T findUnique();

    public abstract void findVisit(QueryResultVisitor<T> visitor);

    public abstract QueryIterator<T> findIterate();

    /**
     * Returns the <code>ExpressionFactory</code> used by this query.
     */
    public abstract ExpressionFactory getExpressionFactory();

    /**
     * Returns the first row value.
     */
    public abstract int getFirstRow();

    /**
     * Returns the SQL that was generated for executing this query.
     */
    public abstract String getGeneratedSql();

    /**
     * Returns the maximum of rows for this query.
     */
    public abstract int getMaxRows();

    /**
     * Returns the <code>RawSql</code> that was set to use for this query.
     */
    public abstract RawSql getRawSql();

    public abstract UseIndex getUseIndex();

    /**
     * Returns the query's <code>having</code> clause.
     */
    public abstract ExpressionList<T> having();

    /**
     * Adds an expression to the <code>having</code> clause and returns the query.
     */
    public abstract Query<T> having(Expression addExpressionToHaving);

    /**
     * Adds clauses to the <code>having</code> clause and returns the query.
     */
    public abstract Query<T> having(String addToHavingClause);

    /**
     * Returns <code>true</code> if this query was tuned by <code>autoFetch</code>.
     */
    public abstract boolean isAutofetchTuned();

    /**
     * Returns the <code>order by</code> clause so that you can append an ascending or descending property to the <code>order by</code> clause.
     * <p/>
     * This is exactly the same as {@link #orderBy}.
     */
    public abstract OrderBy<T> order();

    /**
     * Sets the <code>order by</code> clause, replacing the existing <code>order by</code> clause if there is one.
     * <p/>
     * This is exactly the same as {@link #orderBy(String)}.
     */
    public abstract Query<T> order(String orderByClause);

    /**
     * Returns the <code>order by</code> clause so that you can append an ascending or descending property to the <code>order by</code> clause.
     * <p/>
     * This is exactly the same as {@link #order}.
     */
    public abstract OrderBy<T> orderBy();

    /**
     * Set the <code>order by</code> clause replacing the existing <code>order by</code> clause if there is one.
     * <p/>
     * This is exactly the same as {@link #order(String)}.
     */
    public abstract Query<T> orderBy(String orderByClause);

    /**
     * Explicitly sets a comma delimited list of the properties to fetch on the 'main' entity bean, to load a partial object.
     */
    public abstract Query<T> select(String fetchProperties);

    /**
     * Explicitly specifies whether to use 'Autofetch' for this query.
     */
    public abstract Query<T> setAutofetch(boolean autofetch);

    /**
     * Sets the rows after which fetching should continue in a background thread.
     */
    public abstract Query<T> setBackgroundFetchAfter(int backgroundFetchAfter);

    /**
     * Sets a hint, which for JDBC translates to <code>Statement.fetchSize()</code>.
     */
    public abstract Query<T> setBufferFetchSizeHint(int fetchSize);

    /**
     * Sets whether this query uses <code>DISTINCT</code>.
     */
    public abstract Query<T> setDistinct(boolean isDistinct);

    /**
     * Sets the first row to return for this query.
     */
    public abstract Query<T> setFirstRow(int firstRow);

    /**
     * Sets the ID value to query.
     */
    public abstract Query<T> setId(Object id);

    /**
     * Sets a listener to process the query on a row-by-row basis.
     */
    public abstract Query<T> setListener(QueryListener<T> queryListener);

    /**
     * When set to <code>true</code>, all the beans from this query are loaded into the bean cache.
     */
    public abstract Query<T> setLoadBeanCache(boolean loadBeanCache);

    /**
     * Sets the property to use as keys for a map.
     */
    public abstract Query<T> setMapKey(String mapKey);

    /**
     * Sets the maximum number of rows to return in the query.
     */
    public abstract Query<T> setMaxRows(int maxRows);

    /**
     * Replaces any existing <code>order by</code> clause using an <code>OrderBy</code> object.
     * <p/>
     * This is exactly the same as {@link Query#setOrderBy(OrderBy)}.
     */
    public abstract Query<T> setOrder(OrderBy<T> orderBy);

    /**
     * Set an OrderBy object to replace any existing <code>order by</code> clause.
     * <p/>
     * This is exactly the same as {@link Query#setOrder(OrderBy)}.
     */
    public abstract Query<T> setOrderBy(OrderBy<T> orderBy);

    /**
     * Sets an ordered bind parameter according to its position.
     */
    public abstract Query<T> setParameter(int position, Object value);

    /**
     * Sets a named bind parameter.
     */
    public abstract Query<T> setParameter(String name, Object value);

    /**
     * Sets the OQL query to run
     */
    public abstract Query<T> setQuery(String oql);

    /**
     * Sets <code>RawSql</code> to use for this query.
     */
    public abstract Query<T> setRawSql(RawSql rawSql);

    /**
     * Sets whether the returned beans will be read-only.
     */
    public abstract Query<T> setReadOnly(boolean readOnly);

    /**
     * Sets a timeout on this query.
     */
    public abstract Query<T> setTimeout(int secs);

    /**
     * Sets whether to use the bean cache.
     */
    public abstract Query<T> setUseCache(boolean useBeanCache);

    /**
     * Sets whether to use the query cache.
     */
    public abstract Query<T> setUseQueryCache(boolean useQueryCache);

    public abstract Query<T> setUseIndex(UseIndex useIndex);

    /**
     * Sets whether to use 'vanilla mode', in which the returned beans and collections will be plain classes rather than Ebean-generated dynamic subclasses etc.
     */
    public abstract Query<T> setVanillaMode(boolean vanillaMode);

    /**
     * Adds expressions to the <code>where</code> clause with the ability to chain on the <code>ExpressionList</code>.
     */
    public abstract ExpressionList<T> where();

    /**
     * Adds a single <code>Expression</code> to the <code>where</code> clause and returns the query.
     */
    public abstract Query<T> where(Expression expression);

    /**
     * Adds additional clauses to the <code>where</code> clause.
     */
    public abstract Query<T> where(String addToWhereClause);

    /**
     * Return the total hits matched for a lucene text search query.
     */
    @Override
    public abstract int getTotalHits();

    /**
     * Execute the select with "for update" which should lock the record "on read"
     */
    @Override
    public abstract Query<T> setForUpdate(boolean forUpdate);
}