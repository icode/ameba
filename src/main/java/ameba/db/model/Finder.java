package ameba.db.model;

import com.avaje.ebean.*;
import com.avaje.ebean.text.PathProperties;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Abstract Finder class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public abstract class Finder<ID, T> {

    private final Class<ID> idType;
    private final Class<T> modelType;
    private final String serverName;


    /**
     * Creates a finder for entity of modelType <code>T</code> with <code>ID</code> I of modelType <code>ID</code>, using a specific Ebean server.
     *
     * @param serverName a {@link java.lang.String} object.
     * @param idType     a {@link java.lang.Class} object.
     * @param modelType  a {@link java.lang.Class} object.
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

    /**
     * <p>Getter for the field <code>idType</code>.</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    public Class<ID> getIdType() {
        return idType;
    }

    /**
     * <p>Getter for the field <code>modelType</code>.</p>
     *
     * @return a {@link java.lang.Class} object.
     * @param <M> a M object.
     */
    @SuppressWarnings("unchecked")
    public <M extends T> Class<M> getModelType() {
        return (Class<M>) modelType;
    }

    /**
     * <p>Getter for the field <code>serverName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Changes the model server.
     *
     * @param server a {@link java.lang.String} object.
     * @return a {@link ameba.db.model.Finder} object.
     * @param <M> a M object.
     */
    @SuppressWarnings("unchecked")
    public abstract <M extends T> Finder<ID, M> on(String server);

    /**
     * Retrieves an entity by ID.
     *
     * @param id  a ID object.
     * @return a M object.
     * @param <M> a M object.
     */
    public abstract <M extends T> M byId(ID id);

    /**
     * Retrieves an entity reference for this ID.
     *
     * @param id  a ID object.
     * @return a M object.
     * @param <M> a M object.
     */
    public abstract <M extends T> M ref(ID id);

    /**
     * Creates a filter for sorting and filtering lists of entities locally without going back to the database.
     *
     * @return Filter
     */
    public abstract Filter<T> filter();

    /**
     * <p>query.</p>
     *
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> query();

    /**
     * Creates a query.
     *
     * @return Query
     */
    public abstract Query<T> createQuery();

    /**
     * <p>createUpdateQuery.</p>
     *
     * @return a {@link com.avaje.ebean.UpdateQuery} object.
     */
    public abstract UpdateQuery<T> createUpdateQuery();

    /**
     * <p>createSqlQuery.</p>
     *
     * @param sql a {@link java.lang.String} object.
     * @return a {@link com.avaje.ebean.SqlQuery} object.
     */
    public abstract SqlQuery createSqlQuery(String sql);

    /**
     * Returns the next identity value.
     *
     * @return a I object.
     * @param <I> a I object.
     */
    public abstract <I extends ID> I nextId();

    /**
     * <p>setRawSql.</p>
     *
     * @param rawSql a {@link com.avaje.ebean.RawSql} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setRawSql(RawSql rawSql);

    /**
     * <p>setPersistenceContextScope.</p>
     *
     * @param scope a {@link com.avaje.ebean.PersistenceContextScope} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setPersistenceContextScope(PersistenceContextScope scope);

    /**
     * <p>setLazyLoadBatchSize.</p>
     *
     * @param size a int.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setLazyLoadBatchSize(int size);

    /**
     * <p>setDisableReadAuditing.</p>
     *
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query setDisableReadAuditing();

    /**
     * <p>select.</p>
     *
     * @param fetchProperties a {@link java.lang.String} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> select(String fetchProperties);

    /**
     * <p>setAutoTune.</p>
     *
     * @param autoTune a boolean.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setAutoTune(boolean autoTune);

    /**
     * <p>fetch.</p>
     *
     * @param path            a {@link java.lang.String} object.
     * @param fetchProperties a {@link java.lang.String} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> fetch(String path, String fetchProperties);

    /**
     * <p>fetch.</p>
     *
     * @param assocProperty   a {@link java.lang.String} object.
     * @param fetchProperties a {@link java.lang.String} object.
     * @param fetchConfig     a {@link com.avaje.ebean.FetchConfig} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> fetch(String assocProperty, String fetchProperties, FetchConfig fetchConfig);

    /**
     * <p>fetch.</p>
     *
     * @param path a {@link java.lang.String} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> fetch(String path);

    /**
     * <p>fetch.</p>
     *
     * @param path       a {@link java.lang.String} object.
     * @param joinConfig a {@link com.avaje.ebean.FetchConfig} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> fetch(String path, FetchConfig joinConfig);

    /**
     * <p>apply.</p>
     *
     * @param pathProperties a {@link com.avaje.ebean.text.PathProperties} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> apply(PathProperties pathProperties);

    /**
     * <p>findIds.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public abstract List<Object> findIds();

    /**
     * <p>findVersions.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public abstract List<Version<T>> findVersions();

    /**
     * <p>findVersionsBetween.</p>
     *
     * @param start a {@link java.sql.Timestamp} object.
     * @param end   a {@link java.sql.Timestamp} object.
     * @return a {@link java.util.List} object.
     */
    public abstract List<Version<T>> findVersionsBetween(Timestamp start, Timestamp end);

    /**
     * <p>text.</p>
     *
     * @return a {@link com.avaje.ebean.ExpressionList} object.
     */
    public abstract ExpressionList<T> text();

    /**
     * <p>findEach.</p>
     *
     * @param consumer a {@link com.avaje.ebean.QueryEachConsumer} object.
     */
    public abstract void findEach(QueryEachConsumer<T> consumer);

    /**
     * <p>findEachWhile.</p>
     *
     * @param consumer a {@link com.avaje.ebean.QueryEachWhileConsumer} object.
     */
    public abstract void findEachWhile(QueryEachWhileConsumer<T> consumer);

    /**
     * <p>findList.</p>
     *
     * @return a {@link java.util.List} object.
     * @param <M> a M object.
     */
    public abstract <M extends T> List<M> findList();

    /**
     * <p>findSet.</p>
     *
     * @return a {@link java.util.Set} object.
     * @param <M> a M object.
     */
    public abstract <M extends T> Set<M> findSet();

    /**
     * <p>findMap.</p>
     *
     * @return a {@link java.util.Map} object.
     * @param <M> a M object.
     */
    public abstract <M extends T> Map<?, M> findMap();

    /**
     * <p>findUnique.</p>
     *
     * @return a M object.
     * @param <M> a M object.
     */
    public abstract <M extends T> M findUnique();

    /**
     * <p>findCount.</p>
     *
     * @return a int.
     */
    public abstract int findCount();

    /**
     * <p>findFutureCount.</p>
     *
     * @return a {@link com.avaje.ebean.FutureRowCount} object.
     */
    public abstract FutureRowCount<T> findFutureCount();

    /**
     * <p>findFutureIds.</p>
     *
     * @return a {@link com.avaje.ebean.FutureIds} object.
     */
    public abstract FutureIds<T> findFutureIds();

    /**
     * <p>findFutureList.</p>
     *
     * @return a {@link com.avaje.ebean.FutureList} object.
     */
    public abstract FutureList<T> findFutureList();

    /**
     * <p>findPagedList.</p>
     *
     * @return a {@link com.avaje.ebean.PagedList} object.
     */
    public abstract PagedList<T> findPagedList();

    /**
     * <p>setParameter.</p>
     *
     * @param name  a {@link java.lang.String} object.
     * @param value a {@link java.lang.Object} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setParameter(String name, Object value);

    /**
     * <p>setParameter.</p>
     *
     * @param position a int.
     * @param value    a {@link java.lang.Object} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setParameter(int position, Object value);

    /**
     * <p>setId.</p>
     *
     * @param id a {@link java.lang.Object} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setId(Object id);

    /**
     * <p>getId.</p>
     *
     * @return a {@link java.lang.Object} object.
     */
    public abstract Object getId();

    /**
     * <p>where.</p>
     *
     * @param expression a {@link com.avaje.ebean.Expression} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> where(Expression expression);

    /**
     * <p>where.</p>
     *
     * @return a {@link com.avaje.ebean.ExpressionList} object.
     */
    public abstract ExpressionList<T> where();

    /**
     * <p>filterMany.</p>
     *
     * @param propertyName a {@link java.lang.String} object.
     * @return a {@link com.avaje.ebean.ExpressionList} object.
     */
    public abstract ExpressionList<T> filterMany(String propertyName);

    /**
     * <p>having.</p>
     *
     * @return a {@link com.avaje.ebean.ExpressionList} object.
     */
    public abstract ExpressionList<T> having();

    /**
     * <p>having.</p>
     *
     * @param addExpressionToHaving a {@link com.avaje.ebean.Expression} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> having(Expression addExpressionToHaving);

    /**
     * <p>orderBy.</p>
     *
     * @param orderByClause a {@link java.lang.String} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> orderBy(String orderByClause);

    /**
     * <p>order.</p>
     *
     * @param orderByClause a {@link java.lang.String} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> order(String orderByClause);

    /**
     * <p>order.</p>
     *
     * @return a {@link com.avaje.ebean.OrderBy} object.
     */
    public abstract OrderBy<T> order();

    /**
     * <p>orderBy.</p>
     *
     * @return a {@link com.avaje.ebean.OrderBy} object.
     */
    public abstract OrderBy<T> orderBy();

    /**
     * <p>setOrder.</p>
     *
     * @param orderBy a {@link com.avaje.ebean.OrderBy} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setOrder(OrderBy<T> orderBy);

    /**
     * <p>setOrderBy.</p>
     *
     * @param orderBy a {@link com.avaje.ebean.OrderBy} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setOrderBy(OrderBy<T> orderBy);

    /**
     * <p>setDistinct.</p>
     *
     * @param isDistinct a boolean.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setDistinct(boolean isDistinct);

    /**
     * <p>getExpressionFactory.</p>
     *
     * @return a {@link com.avaje.ebean.ExpressionFactory} object.
     */
    public abstract ExpressionFactory getExpressionFactory();

    /**
     * <p>isAutoTuned.</p>
     *
     * @return a boolean.
     */
    public abstract boolean isAutoTuned();

    /**
     * <p>getFirstRow.</p>
     *
     * @return a int.
     */
    public abstract int getFirstRow();

    /**
     * <p>getGeneratedSql.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getGeneratedSql();

    /**
     * <p>setFirstRow.</p>
     *
     * @param firstRow a int.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setFirstRow(int firstRow);

    /**
     * <p>getMaxRows.</p>
     *
     * @return a int.
     */
    public abstract int getMaxRows();

    /**
     * <p>setMaxRows.</p>
     *
     * @param maxRows a int.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setMaxRows(int maxRows);

    /**
     * <p>setMapKey.</p>
     *
     * @param mapKey a {@link java.lang.String} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setMapKey(String mapKey);

    /**
     * <p>setUseCache.</p>
     *
     * @param useBeanCache a boolean.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setUseCache(boolean useBeanCache);

    /**
     * <p>setUseQueryCache.</p>
     *
     * @param useQueryCache a boolean.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setUseQueryCache(boolean useQueryCache);

    /**
     * <p>setReadOnly.</p>
     *
     * @param readOnly a boolean.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setReadOnly(boolean readOnly);

    /**
     * <p>setLoadBeanCache.</p>
     *
     * @param loadBeanCache a boolean.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setLoadBeanCache(boolean loadBeanCache);

    /**
     * <p>setTimeout.</p>
     *
     * @param secs a int.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setTimeout(int secs);

    /**
     * <p>setBufferFetchSizeHint.</p>
     *
     * @param fetchSize a int.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setBufferFetchSizeHint(int fetchSize);

    /**
     * <p>setForUpdate.</p>
     *
     * @param forUpdate a boolean.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setForUpdate(boolean forUpdate);

    /**
     * <p>isForUpdate.</p>
     *
     * @return a boolean.
     */
    public abstract boolean isForUpdate();

    /**
     * <p>alias.</p>
     *
     * @param alias a {@link java.lang.String} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> alias(String alias);

    /**
     * <p>getBeanType.</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    public abstract Class<T> getBeanType();

    /**
     * <p>setDisableLazyLoading.</p>
     *
     * @param disableLazyLoading a boolean.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setDisableLazyLoading(boolean disableLazyLoading);

    /**
     * <p>getRawSql.</p>
     *
     * @return a {@link com.avaje.ebean.RawSql} object.
     */
    public abstract RawSql getRawSql();

    /**
     * <p>asOf.</p>
     *
     * @param asOf a {@link java.sql.Timestamp} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> asOf(Timestamp asOf);

    /**
     * <p>cancel.</p>
     */
    public abstract void cancel();

    /**
     * <p>copyQuery.</p>
     *
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> copyQuery();

    /**
     * <p>setUseDocStore.</p>
     *
     * @param use a boolean.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public abstract Query<T> setUseDocStore(boolean use);
}
