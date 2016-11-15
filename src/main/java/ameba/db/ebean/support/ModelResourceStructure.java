package ameba.db.ebean.support;

import ameba.db.ebean.EbeanUtils;
import ameba.db.ebean.internal.ModelInterceptor;
import ameba.exception.UnprocessableEntityException;
import ameba.i18n.Messages;
import ameba.lib.LoggerOwner;
import com.avaje.ebean.*;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.internal.util.collection.Refs;

import javax.inject.Inject;
import javax.persistence.OptimisticLockException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import java.net.URI;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * <p>Abstract ModelResourceStructure class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public abstract class ModelResourceStructure<URI_ID, MODEL_ID, MODEL> extends LoggerOwner {

    protected final SpiEbeanServer server;
    protected Class<MODEL> modelType;
    protected String defaultFindOrderBy;
    @Context
    protected UriInfo uriInfo;
    @Inject
    protected ServiceLocator locator;
    private TxScope txScope = null;
    private BeanDescriptor descriptor;

    /**
     * <p>Constructor for ModelResourceStructure.</p>
     *
     * @param modelType a {@link java.lang.Class} object.
     */
    public ModelResourceStructure(Class<MODEL> modelType) {
        this(modelType, (SpiEbeanServer) Ebean.getServer(null));
    }

    /**
     * <p>Constructor for ModelResourceStructure.</p>
     *
     * @param modelType a {@link java.lang.Class} object.
     * @param server    a {@link com.avaje.ebeaninternal.api.SpiEbeanServer} object.
     */
    public ModelResourceStructure(Class<MODEL> modelType, SpiEbeanServer server) {
        this.modelType = modelType;
        this.server = server;
    }

    public void setTxScope(TxScope txScope) {
        this.txScope = txScope;
    }

    public void switchToSupportsScope() {
        txScope = TxScope.supports();
    }

    public void switchToNotSupportedScope() {
        txScope = TxScope.notSupported();
    }

    public void switchToRequiredScope() {
        txScope = TxScope.required();
    }

    public void switchToRequiredNewScope() {
        txScope = TxScope.requiresNew();
    }

    public void switchToMandatoryScope() {
        txScope = TxScope.mandatory();
    }

    /**
     * <p>getModelBeanDescriptor.</p>
     *
     * @return a {@link com.avaje.ebeaninternal.server.deploy.BeanDescriptor} object.
     */
    protected BeanDescriptor getModelBeanDescriptor() {
        if (descriptor == null) {
            descriptor = server.getBeanDescriptor(modelType);
        }
        return descriptor;
    }

    /**
     * convert to id
     *
     * @param id string id
     * @return MODEL_ID
     * @throws NotFoundException response status 404
     */
    @SuppressWarnings("unchecked")
    protected final MODEL_ID tryConvertId(Object id) {
        try {
            return (MODEL_ID) getModelBeanDescriptor().convertId(id);
        } catch (Exception e) {
            throw new UnprocessableEntityException(Messages.get("info.query.id.unprocessable.entity"), e);
        }
    }

    /**
     * convert id to string for insert
     *
     * @param id id object
     * @return id string
     */
    protected String idToString(@NotNull MODEL_ID id) {
        return id.toString();
    }

    /**
     * <p>setForInsertId.</p>
     *
     * @param model a MODEL object.
     */
    protected void setForInsertId(final MODEL model) {
        BeanDescriptor descriptor = getModelBeanDescriptor();
        EntityBeanIntercept intercept = ((EntityBean) model)._ebean_getIntercept();
        BeanProperty idProp = descriptor.getIdProperty();
        if (idProp != null) {
            idProp.setValue((EntityBean) model, null);
            intercept.setPropertyUnloaded(idProp.getPropertyIndex());
        }
    }

    protected void flushBatch() {
        Transaction t = server.currentTransaction();
        if (t != null)
            t.flushBatch();
    }

    /**
     * Insert a model.
     * <p>
     * success status 201
     * </p>
     *
     * @param model the model to insert
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     * @see javax.ws.rs.POST
     * @see AbstractModelResource#insert(MODEL)
     */
    @SuppressWarnings("unchecked")
    public Response insert(@NotNull @Valid final MODEL model) throws Exception {
        matchedInsert(model);
        setForInsertId(model);

        executeTx(new TxRunnable() {
            @Override
            public void run(Transaction t) throws Exception {
                preInsertModel(model);
                insertModel(model);
                postInsertModel(model);
            }
        });
        MODEL_ID id = (MODEL_ID) this.server.getBeanId(model);

        return Response.created(buildLocationUri(id)).build();
    }

    protected void matchedInsert(final MODEL model) throws Exception {

    }

    /**
     * <p>preInsertModel.</p>
     *
     * @param model a MODEL object.
     * @throws java.lang.Exception if any.
     */
    protected void preInsertModel(final MODEL model) throws Exception {

    }

    /**
     * <p>insertModel.</p>
     *
     * @param model a MODEL object.
     * @throws java.lang.Exception if any.
     */
    protected void insertModel(final MODEL model) throws Exception {
        server.insert(model);
    }

    /**
     * <p>postInsertModel.</p>
     *
     * @param model a MODEL object.
     * @throws java.lang.Exception if any.
     */
    protected void postInsertModel(final MODEL model) throws Exception {

    }


    /**
     * replace or insert a model.
     * <br>
     * success replace status 204
     * <br>
     * fail replace but inserted status 201
     *
     * @param id    the unique id of the model
     * @param model the model to update
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     * @see javax.ws.rs.PUT
     * @see AbstractModelResource#replace(URI_ID, MODEL)
     */
    public Response replace(@PathParam("id") final URI_ID id, @NotNull @Valid final MODEL model) throws Exception {
        final MODEL_ID mId = tryConvertId(id);
        matchedReplace(mId, model);
        BeanDescriptor descriptor = getModelBeanDescriptor();
        descriptor.convertSetId(mId, (EntityBean) model);
        EbeanUtils.forceUpdateAllProperties(server, model);

        final Response.ResponseBuilder builder = Response.noContent();
        executeTx(new TxRunnable() {
            @Override
            public void run(Transaction t) throws Exception {
                preReplaceModel(model);
                replaceModel(model);
                postReplaceModel(model);
            }
        }, new TxRunnable() {
            @Override
            public void run(Transaction t) throws Exception {
                logger().debug("not found model record, insert a model record.");
                preInsertModel(model);
                insertModel(model);
                postInsertModel(model);
                builder.status(Response.Status.CREATED).location(buildLocationUri(mId, true));
            }
        });
        return builder.build();
    }

    protected void matchedReplace(MODEL_ID id, final MODEL model) throws Exception {

    }

    /**
     * <p>preReplaceModel.</p>
     *
     * @param model a MODEL object.
     * @throws java.lang.Exception if any.
     */
    protected void preReplaceModel(final MODEL model) throws Exception {

    }

    /**
     * <p>replaceModel.</p>
     *
     * @param model a MODEL object.
     * @throws java.lang.Exception if any.
     */
    protected void replaceModel(final MODEL model) throws Exception {
        server.update(model, null, true);
    }

    /**
     * <p>postReplaceModel.</p>
     *
     * @param model a MODEL object.
     * @throws java.lang.Exception if any.
     */
    protected void postReplaceModel(final MODEL model) throws Exception {

    }

    /**
     * Update a model items.
     * <br>
     * success status 204
     * <br>
     * fail status 422
     *
     * @param id    the unique id of the model
     * @param model the model to update
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     * @see ameba.core.ws.rs.PATCH
     * @see AbstractModelResource#patch(URI_ID, MODEL)
     */
    public Response patch(@PathParam("id") final URI_ID id, @NotNull final MODEL model) throws Exception {
        MODEL_ID mId = tryConvertId(id);
        matchedPatch(mId, model);
        BeanDescriptor descriptor = getModelBeanDescriptor();
        descriptor.convertSetId(mId, (EntityBean) model);
        final Response.ResponseBuilder builder = Response.noContent()
                .contentLocation(uriInfo.getAbsolutePath());
        return executeTx(new TxCallable<Response>() {
            @Override
            public Response call(Transaction t) throws Exception {
                prePatchModel(model);
                patchModel(model);
                postPatchModel(model);
                return builder.build();
            }
        }, new TxCallable<Response>() {
            @Override
            public Response call(Transaction t) {
                // id 无法对应数据。实体对象和补丁都正确，但无法处理请求，所以返回422
                return builder.status(422).build();
            }
        });
    }

    protected void matchedPatch(final MODEL_ID id, final MODEL model) throws Exception {

    }

    /**
     * <p>prePatchModel.</p>
     *
     * @param model a MODEL object.
     * @throws java.lang.Exception if any.
     */
    protected void prePatchModel(final MODEL model) throws Exception {

    }

    /**
     * <p>patchModel.</p>
     *
     * @param model a MODEL object.
     * @throws java.lang.Exception if any.
     */
    protected void patchModel(final MODEL model) throws Exception {
        server.update(model, null, false);
    }

    /**
     * <p>postPatchModel.</p>
     *
     * @param model a MODEL object.
     * @throws java.lang.Exception if any.
     */
    protected void postPatchModel(final MODEL model) throws Exception {

    }

    /**
     * Delete multiple model using Id's from the Matrix.
     * <br>
     * success status 200
     * <br>
     * fail status 404
     * <br>
     * logical delete status 202
     *
     * @param id  The id use for path matching type
     * @param ids The ids in the form "/resource/id1" or "/resource/id1;id2;id3"
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     * @see javax.ws.rs.DELETE
     * @see AbstractModelResource#deleteMultiple(URI_ID, PathSegment, boolean)
     */
    public Response deleteMultiple(@NotNull @PathParam("ids") URI_ID id,
                                   @NotNull @PathParam("ids") final PathSegment ids,
                                   @QueryParam("permanent") final boolean permanent) throws Exception {
        Set<String> idSet = ids.getMatrixParameters().keySet();
        final Response.ResponseBuilder builder = Response.noContent();
        final TxRunnable failProcess = new TxRunnable() {
            @Override
            public void run(Transaction t) {
                builder.status(Response.Status.NOT_FOUND);
            }
        };
        final MODEL_ID firstId = tryConvertId(ids.getPath());
        final Set<MODEL_ID> idCollection = Sets.newLinkedHashSet();
        idCollection.add(firstId);

        if (!idSet.isEmpty()) {
            idCollection.addAll(Collections2.transform(idSet, new Function<String, MODEL_ID>() {
                @Override
                public MODEL_ID apply(String input) {
                    return tryConvertId(input);
                }
            }));
        }
        matchedDelete(firstId, idCollection, permanent);
        if (!idSet.isEmpty()) {
            executeTx(new TxRunnable() {
                @Override
                public void run(Transaction t) throws Exception {
                    preDeleteMultipleModel(idCollection, permanent);
                    boolean p = deleteMultipleModel(idCollection, permanent);
                    if (!p) {
                        builder.status(Response.Status.ACCEPTED);
                    }
                    postDeleteMultipleModel(idCollection, p);
                }
            }, failProcess);
        } else {
            executeTx(new TxRunnable() {
                @Override
                public void run(Transaction t) throws Exception {
                    preDeleteModel(firstId, permanent);
                    boolean p = deleteModel(firstId, permanent);
                    if (!p) {
                        builder.status(Response.Status.ACCEPTED);
                    }
                    postDeleteModel(firstId, p);
                }
            }, failProcess);
        }
        return builder.build();
    }

    protected void matchedDelete(MODEL_ID id, Set<MODEL_ID> idSet, boolean permanent) throws Exception {

    }

    /**
     * <p>preDeleteMultipleModel.</p>
     *
     * @param idCollection a {@link java.util.Set} object.
     * @throws java.lang.Exception if any.
     */
    protected void preDeleteMultipleModel(Set<MODEL_ID> idCollection, boolean permanent) throws Exception {

    }

    /**
     * delete multiple Model
     *
     * @param idCollection model id collection
     * @return if true delete from physical device, if logical delete return false, response status 202
     * @throws java.lang.Exception if any.
     */
    protected boolean deleteMultipleModel(Set<MODEL_ID> idCollection, boolean permanent) throws Exception {
        if (permanent) {
            server.deleteAllPermanent(modelType, idCollection);
        } else {
            server.deleteAll(modelType, idCollection);
        }
        return permanent;
    }

    /**
     * <p>postDeleteMultipleModel.</p>
     *
     * @param idCollection a {@link java.util.Set} object.
     * @throws java.lang.Exception if any.
     */
    protected void postDeleteMultipleModel(Set<MODEL_ID> idCollection, boolean permanent) throws Exception {

    }

    /**
     * <p>preDeleteModel.</p>
     *
     * @param id a MODEL_ID object.
     * @throws java.lang.Exception if any.
     */
    protected void preDeleteModel(MODEL_ID id, boolean permanent) throws Exception {

    }

    /**
     * delete a model
     *
     * @param id model id
     * @return if true delete from physical device, if logical delete return false, response status 202
     * @throws java.lang.Exception if any.
     */
    protected boolean deleteModel(MODEL_ID id, boolean permanent) throws Exception {
        if (permanent) {
            server.deletePermanent(modelType, id);
        } else {
            server.delete(modelType, id);
        }
        return permanent;
    }

    /**
     * <p>postDeleteModel.</p>
     *
     * @param id a MODEL_ID object.
     * @throws java.lang.Exception if any.
     */
    protected void postDeleteModel(MODEL_ID id, boolean permanent) throws Exception {

    }

    /**
     * Find a model or model list given its Ids.
     *
     * @param id  The id use for path matching type
     * @param ids the id of the model.
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     * @see javax.ws.rs.GET
     * @see AbstractModelResource#findByIds
     */
    public Response findByIds(@NotNull @PathParam("ids") URI_ID id,
                              @NotNull @PathParam("ids") final PathSegment ids,
                              @QueryParam("include_deleted") final boolean includeDeleted) throws Exception {
        final Query<MODEL> query = server.find(modelType);
        final MODEL_ID firstId = tryConvertId(ids.getPath());
        Set<String> idSet = ids.getMatrixParameters().keySet();
        final Set<MODEL_ID> idCollection = Sets.newLinkedHashSet();
        idCollection.add(firstId);
        if (!idSet.isEmpty()) {
            idCollection.addAll(Collections2.transform(idSet, new Function<String, MODEL_ID>() {
                @Override
                public MODEL_ID apply(String input) {
                    return tryConvertId(input);
                }
            }));
        }
        matchedFindByIds(firstId, idCollection, includeDeleted);
        Object model;
        if (includeDeleted) {
            query.setIncludeSoftDeletes();
        }
        final TxRunnable configureQuery = new TxRunnable() {
            @Override
            public void run(Transaction t) throws Exception {
                configDefaultQuery(query);
                configFindByIdsQuery(query, includeDeleted);
                applyUriQuery(query, false);
            }
        };
        if (!idSet.isEmpty()) {
            model = executeTx(new TxCallable() {
                @Override
                public Object call(Transaction t) throws Exception {
                    configureQuery.run(t);
                    List<MODEL> m = query.where().idIn(idCollection.toArray()).findList();
                    return processFoundByIdsModelList(m, includeDeleted);
                }
            });
        } else {
            model = executeTx(new TxCallable<Object>() {
                @Override
                public Object call(Transaction t) throws Exception {
                    configureQuery.run(t);
                    MODEL m = query.setId(firstId).findUnique();
                    return processFoundByIdModel(m, includeDeleted);
                }
            });
        }

        if (isEmptyEntity(model)) {
            throw new NotFoundException();
        }
        return Response.ok(model).build();
    }

    protected void matchedFindByIds(MODEL_ID id, Set<MODEL_ID> ids, boolean includeDeleted) throws Exception {
        switchToSupportsScope();
    }

    /**
     * Configure the "Find By Ids" query.
     * <p>
     * This is only used when no BeanPathProperties where set via UriOptions.
     * </p>
     * <p>
     * This effectively controls the "default" query used to render this model.
     * </p>
     *
     * @param query a {@link com.avaje.ebean.Query} object.
     * @throws java.lang.Exception if any.
     */
    protected void configFindByIdsQuery(final Query<MODEL> query, boolean includeDeleted) throws Exception {

    }

    /**
     * <p>processFoundModel.</p>
     *
     * @param model a MODEL object.
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
    protected Object processFoundByIdModel(final MODEL model, boolean includeDeleted) throws Exception {
        return model;
    }

    protected Object processFoundByIdsModelList(final List<MODEL> models, boolean includeDeleted) throws Exception {
        return models;
    }

    /**
     * Find the beans for this beanType.
     * <p>
     * This can use URL query parameters such as order and maxrows to configure
     * the query.
     * </p>
     *
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     * @see javax.ws.rs.GET
     * @see AbstractModelResource#find()
     */
    public Response find(@QueryParam("include_deleted") final boolean includeDeleted) throws Exception {
        matchedFind(includeDeleted);
        final Query<MODEL> query = server.find(modelType);

        if (includeDeleted) {
            query.setIncludeSoftDeletes();
        }

        defaultFindOrderBy(query);

        final Ref<FutureRowCount> rowCount = Refs.emptyRef();

        Object entity = executeTx(new TxCallable<Object>() {
            @Override
            public Object call(Transaction t) throws Exception {
                configDefaultQuery(query);
                configFindQuery(query, includeDeleted);
                rowCount.set(applyUriQuery(query));
                List<MODEL> list = query.findList();
                return processFoundModelList(list, includeDeleted);
            }
        });

        if (isEmptyEntity(entity)) {
            return Response.noContent().build();
        }
        Response response = Response.ok(entity).build();
        applyRowCountHeader(response.getHeaders(), query, rowCount.get());
        return response;
    }

    protected void matchedFind(boolean includeDeleted) throws Exception {
        switchToSupportsScope();
    }

    /**
     * Configure the "Find" query.
     * <p>
     * This is only used when no BeanPathProperties where set via UriOptions.
     * </p>
     * <p>
     * This effectively controls the "default" query used with the find all
     * query.
     * </p>
     *
     * @param query a {@link com.avaje.ebean.Query} object.
     * @throws java.lang.Exception if any.
     */
    protected void configFindQuery(final Query<MODEL> query, boolean includeDeleted) throws Exception {

    }

    /**
     * <p>processFoundModelList.</p>
     *
     * @param list a {@link java.util.List} object.
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
    protected Object processFoundModelList(final List<MODEL> list, boolean includeDeleted) throws Exception {
        return list;
    }

    protected void defaultFindOrderBy(Query<MODEL> query) {
        if (StringUtils.isNotBlank(defaultFindOrderBy)) {
            // see if we should use the default orderBy clause
            OrderBy<MODEL> orderBy = query.orderBy();
            if (orderBy.isEmpty()) {
                query.orderBy(defaultFindOrderBy);
            }
        }
    }

    /***
     *  find model history between start to end timestamp versions
     *
     *  need model mark {@History}
     *
     * @param id model id
     * @param start start timestamp
     * @param end end timestamp
     * @return history versions
     * @throws Exception any error
     */
    public Response fetchHistory(@PathParam("id") URI_ID id,
                                 @PathParam("start") final Timestamp start,
                                 @PathParam("end") final Timestamp end) throws Exception {
        final MODEL_ID mId = tryConvertId(id);
        matchedFetchHistory(mId, start, end);
        final Query<MODEL> query = server.find(modelType);

        defaultFindOrderBy(query);

        final Ref<FutureRowCount> rowCount = Refs.emptyRef();
        Object entity = executeTx(new TxCallable<Object>() {
            @Override
            public Object call(Transaction t) throws Exception {
                configDefaultQuery(query);
                configFetchHistoryQuery(query, mId, start, end);
                applyUriQuery(query, false);
                applyPageConfig(query);
                List<Version<MODEL>> list = query.findVersionsBetween(start, end);
                rowCount.set(fetchRowCount(query));
                return processFetchedHistoryModelList(list, mId, start, end);
            }
        });

        if (isEmptyEntity(entity)) {
            return Response.noContent().build();
        }
        Response response = Response.ok(entity).build();
        applyRowCountHeader(response.getHeaders(), query, rowCount.get());
        return response;
    }

    protected void matchedFetchHistory(final MODEL_ID id,
                                       final Timestamp start,
                                       final Timestamp end) throws Exception {
        switchToSupportsScope();
    }

    protected void configFetchHistoryQuery(final Query<MODEL> query,
                                           final MODEL_ID id,
                                           final Timestamp start,
                                           final Timestamp end) throws Exception {

    }

    protected Object processFetchedHistoryModelList(final List<Version<MODEL>> list,
                                                    final MODEL_ID id,
                                                    final Timestamp start,
                                                    final Timestamp end) throws Exception {
        return list;
    }

    public Response fetchHistory(@PathParam("id") URI_ID id) throws Exception {
        final MODEL_ID mId = tryConvertId(id);
        matchedFetchHistory(mId);
        final Query<MODEL> query = server.find(modelType);

        defaultFindOrderBy(query);

        final Ref<FutureRowCount> rowCount = Refs.emptyRef();
        Object entity = executeTx(new TxCallable<Object>() {
            @Override
            public Object call(Transaction t) throws Exception {
                configDefaultQuery(query);
                configFetchHistoryQuery(query, mId);
                applyUriQuery(query, false);
                applyPageConfig(query);
                List<Version<MODEL>> list = query.findVersions();
                rowCount.set(fetchRowCount(query));
                return processFetchedHistoryModelList(list, mId);
            }
        });

        if (isEmptyEntity(entity)) {
            return Response.noContent().build();
        }
        Response response = Response.ok(entity).build();
        applyRowCountHeader(response.getHeaders(), query, rowCount.get());
        return response;
    }

    protected void matchedFetchHistory(final MODEL_ID id) throws Exception {
        switchToSupportsScope();
    }

    protected void configFetchHistoryQuery(final Query<MODEL> query,
                                           final MODEL_ID id) throws Exception {

    }

    protected Object processFetchedHistoryModelList(final List<Version<MODEL>> list,
                                                    final MODEL_ID id) throws Exception {
        return list;
    }

    /**
     * find history as of timestamp
     *
     * @param id   model id
     * @param asOf Timestamp
     * @return history model
     * @throws Exception any error
     */
    public Response fetchHistoryAsOf(@PathParam("id") URI_ID id,
                                     @PathParam("asOf") final Timestamp asOf) throws Exception {
        final MODEL_ID mId = tryConvertId(id);
        matchedFetchHistoryAsOf(mId, asOf);
        final Query<MODEL> query = server.find(modelType);

        defaultFindOrderBy(query);

        Object entity = executeTx(new TxCallable<Object>() {
            @Override
            public Object call(Transaction t) throws Exception {
                configDefaultQuery(query);
                configFetchHistoryAsOfQuery(query, mId, asOf);
                applyUriQuery(query, false);
                MODEL model = query.asOf(asOf).setId(mId).findUnique();
                return processFetchedHistoryAsOfModel(mId, model, asOf);
            }
        });

        if (isEmptyEntity(entity)) {
            return Response.noContent().build();
        }
        return Response.ok(entity).build();
    }

    protected void matchedFetchHistoryAsOf(final MODEL_ID id, final Timestamp asOf) throws Exception {
        switchToSupportsScope();
    }

    protected void configFetchHistoryAsOfQuery(final Query<MODEL> query,
                                               final MODEL_ID id,
                                               final Timestamp asOf) throws Exception {

    }

    protected Object processFetchedHistoryAsOfModel(final MODEL_ID id,
                                                    final MODEL model,
                                                    final Timestamp asOf) throws Exception {
        return model;
    }

    /**
     * all query config default query
     *
     * @param query query
     * @throws java.lang.Exception if any.
     */
    protected void configDefaultQuery(final Query<MODEL> query) throws Exception {

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////                                   ///////////////////////////////////
    ///////////////////////////////////        useful method block        ///////////////////////////////////
    ///////////////////////////////////                                   ///////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected boolean isEmptyEntity(Object entity) {
        return entity == null || (entity instanceof Collection && ((Collection) entity).isEmpty());
    }

    /**
     * apply uri query parameter on query
     *
     * @param query        Query
     * @param needPageList need page list
     * @return page list count or null
     * @see ModelInterceptor#applyUriQuery(MultivaluedMap, SpiQuery, ServiceLocator, boolean)
     */
    protected FutureRowCount applyUriQuery(final Query<MODEL> query, boolean needPageList) {
        return ModelInterceptor.applyUriQuery(uriInfo.getQueryParameters(), (SpiQuery) query, locator, needPageList);
    }

    /**
     * <p>applyUriQuery.</p>
     *
     * @param query a {@link com.avaje.ebean.Query} object.
     * @return a {@link com.avaje.ebean.FutureRowCount} object.
     */
    protected FutureRowCount applyUriQuery(final Query<MODEL> query) {
        return applyUriQuery(query, true);
    }

    protected void applyPageConfig(Query query) {
        ModelInterceptor.applyPageConfig(uriInfo.getQueryParameters(), query);
    }

    protected FutureRowCount fetchRowCount(Query query) {
        return ModelInterceptor.fetchRowCount(uriInfo.getQueryParameters(), query);
    }

    /**
     * <p>applyRowCountHeader.</p>
     *
     * @param headerParams a {@link javax.ws.rs.core.MultivaluedMap} object.
     * @param query        a {@link com.avaje.ebean.Query} object.
     * @param rowCount     a {@link com.avaje.ebean.FutureRowCount} object.
     */
    protected void applyRowCountHeader(MultivaluedMap<String, Object> headerParams, Query query, FutureRowCount rowCount) {
        ModelInterceptor.applyRowCountHeader(headerParams, query, rowCount);
    }

    /**
     * <p>processTransactionError.</p>
     *
     * @param t        Transaction
     * @param callable a {@link ModelResourceStructure.TxCallable} object.
     * @param process  a {@link ModelResourceStructure.TxCallable} object.
     * @param <T>      model
     * @return model
     * @throws java.lang.Exception if any.
     */
    protected <T> T processTransactionError(Transaction t, TxCallable<T> callable, TxCallable<T> process) throws Exception {
        try {
            return callable.call(t);
        } catch (Exception e) {
            return processCheckRowCountError(t, e, e, process);
        }
    }


    /**
     * <p>processCheckRowCountError.</p>
     *
     * @param t       Transaction
     * @param root    root exception
     * @param e       exception
     * @param process process method
     * @param <T>     model
     * @return model
     * @throws java.lang.Exception if any.
     */
    protected <T> T processCheckRowCountError(Transaction t, Exception root, Throwable e, TxCallable<T> process) throws Exception {
        if (e == null) {
            throw root;
        }
        if (e instanceof OptimisticLockException) {
            if ("checkRowCount".equals(e.getStackTrace()[0].getMethodName())) {
                if (process != null)
                    return process.call(t);
            }
        }
        return processCheckRowCountError(t, root, e.getCause(), process);
    }

    /**
     * <p>executeTx.</p>
     *
     * @param r            a {@link ModelResourceStructure.TxRunnable} object.
     * @param errorHandler error handler
     * @throws java.lang.Exception if any.
     */
    protected void executeTx(final TxRunnable r, final TxRunnable errorHandler) throws Exception {
        Transaction transaction = beginTransaction();
        configureTransDefault(transaction);
        processTransactionError(transaction, new TxCallable() {
            @Override
            public Object call(Transaction t) throws Exception {
                try {
                    r.run(t);
                    t.commit();
                } catch (Throwable e) {
                    t.rollback(e);
                    throw e;
                } finally {
                    t.end();
                }
                return null;
            }
        }, errorHandler != null ? new TxCallable() {
            @Override
            public Object call(Transaction t) throws Exception {
                errorHandler.run(t);
                return null;
            }
        } : null);
    }

    protected Transaction beginTransaction() {
        return server.beginTransaction(txScope);
    }

    /**
     * <p>executeTx.</p>
     *
     * @param r a {@link ModelResourceStructure.TxRunnable} object.
     * @throws java.lang.Exception if any.
     */
    protected void executeTx(TxRunnable r) throws Exception {
        executeTx(r, null);
    }

    /**
     * <p>executeTx.</p>
     *
     * @param c   a {@link ModelResourceStructure.TxCallable} object.
     * @param <O> a O object.
     * @return a O object.
     * @throws java.lang.Exception if any.
     */
    protected <O> O executeTx(TxCallable<O> c) throws Exception {
        return executeTx(c, null);
    }


    @SuppressWarnings("unchecked")
    protected <O> O executeTx(final TxCallable<O> c, final TxCallable<O> errorHandler) throws Exception {
        Transaction transaction = beginTransaction();
        configureTransDefault(transaction);
        return processTransactionError(transaction, new TxCallable<O>() {
            @Override
            public O call(Transaction t) throws Exception {
                Object o = null;
                try {
                    o = c.call(t);
                    t.commit();
                } catch (Throwable e) {
                    t.rollback(e);
                    throw e;
                } finally {
                    t.end();
                }
                return (O) o;
            }
        }, errorHandler);
    }

    protected void configureTransDefault(Transaction transaction) {

    }

    /**
     * <p>buildLocationUri.</p>
     *
     * @param id          a MODEL_ID object.
     * @param useTemplate use current template
     * @return a {@link java.net.URI} object.
     */
    protected URI buildLocationUri(MODEL_ID id, boolean useTemplate) {
        if (id == null) {
            throw new NotFoundException();
        }
        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        if (useTemplate) {
            return ub.build(id);
        } else {
            return ub.path(idToString(id)).build();
        }
    }

    protected URI buildLocationUri(MODEL_ID id) {
        return buildLocationUri(id, false);
    }

    protected interface TxRunnable {
        void run(Transaction transaction) throws Exception;
    }

    protected interface TxCallable<O> {
        O call(Transaction transaction) throws Exception;
    }
}
