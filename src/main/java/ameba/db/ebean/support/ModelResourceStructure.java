package ameba.db.ebean.support;

import ameba.db.ebean.EbeanUtils;
import ameba.db.ebean.internal.ModelInterceptor;
import ameba.exception.UnprocessableEntityException;
import ameba.i18n.Messages;
import ameba.lib.LoggerOwner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import io.ebean.*;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
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
     * @param server    a {@link io.ebeaninternal.api.SpiEbeanServer} object.
     */
    public ModelResourceStructure(Class<MODEL> modelType, SpiEbeanServer server) {
        this.modelType = modelType;
        this.server = server;
    }

    /**
     * <p>Setter for the field <code>txScope</code>.</p>
     *
     * @param txScope a {@link io.ebean.TxScope} object.
     */
    public void setTxScope(TxScope txScope) {
        this.txScope = txScope;
    }

    /**
     * <p>switchToSupportsScope.</p>
     */
    public void switchToSupportsScope() {
        txScope = TxScope.supports();
    }

    /**
     * <p>switchToNotSupportedScope.</p>
     */
    public void switchToNotSupportedScope() {
        txScope = TxScope.notSupported();
    }

    /**
     * <p>switchToRequiredScope.</p>
     */
    public void switchToRequiredScope() {
        txScope = TxScope.required();
    }

    /**
     * <p>switchToRequiredNewScope.</p>
     */
    public void switchToRequiredNewScope() {
        txScope = TxScope.requiresNew();
    }

    /**
     * <p>switchToMandatoryScope.</p>
     */
    public void switchToMandatoryScope() {
        txScope = TxScope.mandatory();
    }

    /**
     * <p>getModelBeanDescriptor.</p>
     *
     * @return a {@link io.ebeaninternal.server.deploy.BeanDescriptor} object.
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

    /**
     * <p>flushBatch.</p>
     */
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
     * @see AbstractModelResource#insert
     */
    @SuppressWarnings("unchecked")
    public Response insert(@NotNull @Valid final MODEL model) throws Exception {
        matchedInsert(model);
        setForInsertId(model);

        executeTx(t -> {
            preInsertModel(model);
            insertModel(model);
            postInsertModel(model);
        });
        MODEL_ID id = (MODEL_ID) this.server.getBeanId(model);

        return Response.created(buildLocationUri(id)).build();
    }

    /**
     * <p>matchedInsert.</p>
     *
     * @param model a MODEL object.
     * @throws java.lang.Exception if any.
     */
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
     * @see AbstractModelResource#replace
     */
    public Response replace(@PathParam("id") final URI_ID id, @NotNull @Valid final MODEL model) throws Exception {
        final MODEL_ID mId = tryConvertId(id);
        matchedReplace(mId, model);
        BeanDescriptor descriptor = getModelBeanDescriptor();
        descriptor.convertSetId(mId, (EntityBean) model);
        EbeanUtils.forceUpdateAllProperties(server, model);

        final Response.ResponseBuilder builder = Response.noContent();
        executeTx(t -> {
            preReplaceModel(model);
            replaceModel(model);
            postReplaceModel(model);
        }, t -> {
            logger().debug("not found model record, insert a model record.");
            preInsertModel(model);
            insertModel(model);
            postInsertModel(model);
            builder.status(Response.Status.CREATED).location(buildLocationUri(mId, true));
        });
        return builder.build();
    }

    /**
     * <p>matchedReplace.</p>
     *
     * @param id    a MODEL_ID object.
     * @param model a MODEL object.
     * @throws java.lang.Exception if any.
     */
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
     * @see AbstractModelResource#patch
     */
    public Response patch(@PathParam("id") final URI_ID id, @NotNull final MODEL model) throws Exception {
        MODEL_ID mId = tryConvertId(id);
        matchedPatch(mId, model);
        BeanDescriptor descriptor = getModelBeanDescriptor();
        descriptor.convertSetId(mId, (EntityBean) model);
        final Response.ResponseBuilder builder = Response.noContent()
                .contentLocation(uriInfo.getAbsolutePath());
        return executeTx(t -> {
            prePatchModel(model);
            patchModel(model);
            postPatchModel(model);
            return builder.build();
        }, t -> {
            // id 无法对应数据。实体对象和补丁都正确，但无法处理请求，所以返回422
            return builder.status(422).build();
        });
    }

    /**
     * <p>matchedPatch.</p>
     *
     * @param id    a MODEL_ID object.
     * @param model a MODEL object.
     * @throws java.lang.Exception if any.
     */
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
     * @param id        The id use for path matching type
     * @param ids       The ids in the form "/resource/id1" or "/resource/id1;id2;id3"
     * @param ids       The ids in the form "/resource/id1" or "/resource/id1;id2;id3"
     * @param ids       The ids in the form "/resource/id1" or "/resource/id1;id2;id3"
     * @param ids       The ids in the form "/resource/id1" or "/resource/id1;id2;id3"
     * @param ids       The ids in the form "/resource/id1" or "/resource/id1;id2;id3"
     * @param ids       The ids in the form "/resource/id1" or "/resource/id1;id2;id3"
     * @param permanent a boolean.
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     * @see javax.ws.rs.DELETE
     * @see AbstractModelResource#deleteMultiple
     */
    public Response deleteMultiple(@NotNull @PathParam("ids") URI_ID id,
                                   @NotNull @PathParam("ids") final PathSegment ids,
                                   @QueryParam("permanent") final boolean permanent) throws Exception {
        Set<String> idSet = ids.getMatrixParameters().keySet();
        final Response.ResponseBuilder builder = Response.noContent();
        final TxRunnable failProcess = t -> builder.status(Response.Status.NOT_FOUND);
        final MODEL_ID firstId = tryConvertId(ids.getPath());
        final Set<MODEL_ID> idCollection = Sets.newLinkedHashSet();
        idCollection.add(firstId);

        if (!idSet.isEmpty()) {
            idCollection.addAll(Collections2.transform(idSet, this::tryConvertId));
        }
        matchedDelete(firstId, idCollection, permanent);
        if (!idSet.isEmpty()) {
            executeTx(t -> {
                preDeleteMultipleModel(idCollection, permanent);
                boolean p = deleteMultipleModel(idCollection, permanent);
                if (!p) {
                    builder.status(Response.Status.ACCEPTED);
                }
                postDeleteMultipleModel(idCollection, p);
            }, failProcess);
        } else {
            executeTx(t -> {
                preDeleteModel(firstId, permanent);
                boolean p = deleteModel(firstId, permanent);
                if (!p) {
                    builder.status(Response.Status.ACCEPTED);
                }
                postDeleteModel(firstId, p);
            }, failProcess);
        }
        return builder.build();
    }

    /**
     * <p>matchedDelete.</p>
     *
     * @param id        a MODEL_ID object.
     * @param idSet     a {@link java.util.Set} object.
     * @param idSet     a {@link java.util.Set} object.
     * @param permanent a boolean.
     * @throws java.lang.Exception if any.
     */
    protected void matchedDelete(MODEL_ID id, Set<MODEL_ID> idSet, boolean permanent) throws Exception {

    }

    /**
     * <p>preDeleteMultipleModel.</p>
     *
     * @param idCollection a {@link java.util.Set} object.
     * @param permanent    a boolean.
     * @throws java.lang.Exception if any.
     */
    protected void preDeleteMultipleModel(Set<MODEL_ID> idCollection, boolean permanent) throws Exception {

    }

    /**
     * delete multiple Model
     *
     * @param idCollection model id collection
     * @param permanent    a boolean.
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
     * @param permanent    a boolean.
     * @throws java.lang.Exception if any.
     */
    protected void postDeleteMultipleModel(Set<MODEL_ID> idCollection, boolean permanent) throws Exception {

    }

    /**
     * <p>preDeleteModel.</p>
     *
     * @param id        a MODEL_ID object.
     * @param permanent a boolean.
     * @throws java.lang.Exception if any.
     */
    protected void preDeleteModel(MODEL_ID id, boolean permanent) throws Exception {

    }

    /**
     * delete a model
     *
     * @param id        model id
     * @param permanent a boolean.
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
     * @param id        a MODEL_ID object.
     * @param permanent a boolean.
     * @throws java.lang.Exception if any.
     */
    protected void postDeleteModel(MODEL_ID id, boolean permanent) throws Exception {

    }

    /**
     * Find a model or model list given its Ids.
     *
     * @param id             The id use for path matching type
     * @param ids            the id of the model.
     * @param ids            the id of the model.
     * @param ids            the id of the model.
     * @param ids            the id of the model.
     * @param ids            the id of the model.
     * @param ids            the id of the model.
     * @param includeDeleted a boolean.
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
            idCollection.addAll(Collections2.transform(idSet, this::tryConvertId));
        }
        matchedFindByIds(firstId, idCollection, includeDeleted);
        Object model;
        if (includeDeleted) {
            query.setIncludeSoftDeletes();
        }
        final TxRunnable configureQuery = t -> {
            configDefaultQuery(query);
            configFindByIdsQuery(query, includeDeleted);
            applyUriQuery(query, false);
        };
        if (!idSet.isEmpty()) {
            model = executeTx(t -> {
                configureQuery.run(t);
                List<MODEL> m = query.where().idIn(idCollection.toArray()).findList();
                return processFoundByIdsModelList(m, includeDeleted);
            });
        } else {
            model = executeTx(t -> {
                configureQuery.run(t);
                MODEL m = query.setId(firstId).findUnique();
                return processFoundByIdModel(m, includeDeleted);
            });
        }

        if (isEmptyEntity(model)) {
            throw new NotFoundException();
        }
        return Response.ok(model).build();
    }

    /**
     * <p>matchedFindByIds.</p>
     *
     * @param id             a MODEL_ID object.
     * @param ids            a {@link java.util.Set} object.
     * @param ids            a {@link java.util.Set} object.
     * @param includeDeleted a boolean.
     * @throws java.lang.Exception if any.
     */
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
     * @param query          a {@link io.ebean.Query} object.
     * @param includeDeleted a boolean.
     * @throws java.lang.Exception if any.
     */
    protected void configFindByIdsQuery(final Query<MODEL> query, boolean includeDeleted) throws Exception {

    }

    /**
     * <p>processFoundModel.</p>
     *
     * @param model          a MODEL object.
     * @param includeDeleted a boolean.
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
    protected Object processFoundByIdModel(final MODEL model, boolean includeDeleted) throws Exception {
        return model;
    }

    /**
     * <p>processFoundByIdsModelList.</p>
     *
     * @param models         a {@link java.util.List} object.
     * @param includeDeleted a boolean.
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
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
     * @param includeDeleted a boolean.
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     * @see javax.ws.rs.GET
     * @see AbstractModelResource#find
     */
    public Response find(@QueryParam("include_deleted") final boolean includeDeleted) throws Exception {
        matchedFind(includeDeleted);
        final Query<MODEL> query = server.find(modelType);

        if (includeDeleted) {
            query.setIncludeSoftDeletes();
        }

        defaultFindOrderBy(query);

        final Ref<FutureRowCount> rowCount = Refs.emptyRef();

        Object entity = executeTx(t -> {
            configDefaultQuery(query);
            configFindQuery(query, includeDeleted);
            rowCount.set(applyUriQuery(query));
            List<MODEL> list = query.findList();
            return processFoundModelList(list, includeDeleted);
        });

        if (isEmptyEntity(entity)) {
            return Response.noContent().build();
        }
        Response response = Response.ok(entity).build();
        applyRowCountHeader(response.getHeaders(), query, rowCount.get());
        return response;
    }

    /**
     * <p>matchedFind.</p>
     *
     * @param includeDeleted a boolean.
     * @throws java.lang.Exception if any.
     */
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
     * @param query          a {@link io.ebean.Query} object.
     * @param includeDeleted a boolean.
     * @throws java.lang.Exception if any.
     */
    protected void configFindQuery(final Query<MODEL> query, boolean includeDeleted) throws Exception {

    }

    /**
     * <p>processFoundModelList.</p>
     *
     * @param list           a {@link java.util.List} object.
     * @param includeDeleted a boolean.
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
    protected Object processFoundModelList(final List<MODEL> list, boolean includeDeleted) throws Exception {
        return list;
    }

    /**
     * <p>defaultFindOrderBy.</p>
     *
     * @param query a {@link io.ebean.Query} object.
     */
    protected void defaultFindOrderBy(Query<MODEL> query) {
        if (StringUtils.isNotBlank(defaultFindOrderBy)) {
            // see if we should use the default orderBy clause
            OrderBy<MODEL> orderBy = query.orderBy();
            if (orderBy.isEmpty()) {
                query.orderBy(defaultFindOrderBy);
            }
        }
    }

    /**
     * find model history between start to end timestamp versions
     * <p>
     * need model mark {@code @History}
     *
     * @param id    model id
     * @param start start timestamp
     * @param end   end timestamp
     * @return history versions
     * @throws java.lang.Exception any error
     */
    public Response fetchHistory(@PathParam("id") URI_ID id,
                                 @PathParam("start") final Timestamp start,
                                 @PathParam("end") final Timestamp end) throws Exception {
        final MODEL_ID mId = tryConvertId(id);
        matchedFetchHistory(mId, start, end);
        final Query<MODEL> query = server.find(modelType);

        defaultFindOrderBy(query);

        final Ref<FutureRowCount> rowCount = Refs.emptyRef();
        Object entity = executeTx(t -> {
            configDefaultQuery(query);
            configFetchHistoryQuery(query, mId, start, end);
            applyUriQuery(query, false);
            applyPageConfig(query);
            List<Version<MODEL>> list = query.findVersionsBetween(start, end);
            rowCount.set(fetchRowCount(query));
            return processFetchedHistoryModelList(list, mId, start, end);
        });

        if (isEmptyEntity(entity)) {
            return Response.noContent().build();
        }
        Response response = Response.ok(entity).build();
        applyRowCountHeader(response.getHeaders(), query, rowCount.get());
        return response;
    }

    /**
     * <p>matchedFetchHistory.</p>
     *
     * @param id    a MODEL_ID object.
     * @param start a {@link java.sql.Timestamp} object.
     * @param end   a {@link java.sql.Timestamp} object.
     * @throws java.lang.Exception if any.
     */
    protected void matchedFetchHistory(final MODEL_ID id,
                                       final Timestamp start,
                                       final Timestamp end) throws Exception {
        switchToSupportsScope();
    }

    /**
     * <p>configFetchHistoryQuery.</p>
     *
     * @param query a {@link io.ebean.Query} object.
     * @param id    a MODEL_ID object.
     * @param start a {@link java.sql.Timestamp} object.
     * @param end   a {@link java.sql.Timestamp} object.
     * @throws java.lang.Exception if any.
     */
    protected void configFetchHistoryQuery(final Query<MODEL> query,
                                           final MODEL_ID id,
                                           final Timestamp start,
                                           final Timestamp end) throws Exception {

    }

    /**
     * <p>processFetchedHistoryModelList.</p>
     *
     * @param list  a {@link java.util.List} object.
     * @param id    a MODEL_ID object.
     * @param start a {@link java.sql.Timestamp} object.
     * @param end   a {@link java.sql.Timestamp} object.
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
    protected Object processFetchedHistoryModelList(final List<Version<MODEL>> list,
                                                    final MODEL_ID id,
                                                    final Timestamp start,
                                                    final Timestamp end) throws Exception {
        return list;
    }

    /**
     * <p>fetchHistory.</p>
     *
     * @param id a URI_ID object.
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     */
    public Response fetchHistory(@PathParam("id") URI_ID id) throws Exception {
        final MODEL_ID mId = tryConvertId(id);
        matchedFetchHistory(mId);
        final Query<MODEL> query = server.find(modelType);

        defaultFindOrderBy(query);

        final Ref<FutureRowCount> rowCount = Refs.emptyRef();
        Object entity = executeTx(t -> {
            configDefaultQuery(query);
            configFetchHistoryQuery(query, mId);
            applyUriQuery(query, false);
            applyPageConfig(query);
            List<Version<MODEL>> list = query.findVersions();
            rowCount.set(fetchRowCount(query));
            return processFetchedHistoryModelList(list, mId);
        });

        if (isEmptyEntity(entity)) {
            return Response.noContent().build();
        }
        Response response = Response.ok(entity).build();
        applyRowCountHeader(response.getHeaders(), query, rowCount.get());
        return response;
    }

    /**
     * <p>matchedFetchHistory.</p>
     *
     * @param id a MODEL_ID object.
     * @throws java.lang.Exception if any.
     */
    protected void matchedFetchHistory(final MODEL_ID id) throws Exception {
        switchToSupportsScope();
    }

    /**
     * <p>configFetchHistoryQuery.</p>
     *
     * @param query a {@link io.ebean.Query} object.
     * @param id    a MODEL_ID object.
     * @throws java.lang.Exception if any.
     */
    protected void configFetchHistoryQuery(final Query<MODEL> query,
                                           final MODEL_ID id) throws Exception {

    }

    /**
     * <p>processFetchedHistoryModelList.</p>
     *
     * @param list a {@link java.util.List} object.
     * @param id   a MODEL_ID object.
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
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
     * @throws java.lang.Exception any error
     */
    public Response fetchHistoryAsOf(@PathParam("id") URI_ID id,
                                     @PathParam("asOf") final Timestamp asOf) throws Exception {
        final MODEL_ID mId = tryConvertId(id);
        matchedFetchHistoryAsOf(mId, asOf);
        final Query<MODEL> query = server.find(modelType);

        defaultFindOrderBy(query);

        Object entity = executeTx(t -> {
            configDefaultQuery(query);
            configFetchHistoryAsOfQuery(query, mId, asOf);
            applyUriQuery(query, false);
            MODEL model = query.asOf(asOf).setId(mId).findUnique();
            return processFetchedHistoryAsOfModel(mId, model, asOf);
        });

        if (isEmptyEntity(entity)) {
            return Response.noContent().build();
        }
        return Response.ok(entity).build();
    }

    /**
     * <p>matchedFetchHistoryAsOf.</p>
     *
     * @param id   a MODEL_ID object.
     * @param asOf a {@link java.sql.Timestamp} object.
     * @throws java.lang.Exception if any.
     */
    protected void matchedFetchHistoryAsOf(final MODEL_ID id, final Timestamp asOf) throws Exception {
        switchToSupportsScope();
    }

    /**
     * <p>configFetchHistoryAsOfQuery.</p>
     *
     * @param query a {@link io.ebean.Query} object.
     * @param id    a MODEL_ID object.
     * @param asOf  a {@link java.sql.Timestamp} object.
     * @throws java.lang.Exception if any.
     */
    protected void configFetchHistoryAsOfQuery(final Query<MODEL> query,
                                               final MODEL_ID id,
                                               final Timestamp asOf) throws Exception {

    }

    /**
     * <p>processFetchedHistoryAsOfModel.</p>
     *
     * @param id    a MODEL_ID object.
     * @param model a MODEL object.
     * @param asOf  a {@link java.sql.Timestamp} object.
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
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

    /**
     * <p>isEmptyEntity.</p>
     *
     * @param entity a {@link java.lang.Object} object.
     * @return a boolean.
     */
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
     * @param query a {@link io.ebean.Query} object.
     * @return a {@link io.ebean.FutureRowCount} object.
     */
    protected FutureRowCount applyUriQuery(final Query<MODEL> query) {
        return applyUriQuery(query, true);
    }

    /**
     * <p>applyPageConfig.</p>
     *
     * @param query a {@link io.ebean.Query} object.
     */
    protected void applyPageConfig(Query query) {
        ModelInterceptor.applyPageConfig(uriInfo.getQueryParameters(), query);
    }

    /**
     * <p>fetchRowCount.</p>
     *
     * @param query a {@link io.ebean.Query} object.
     * @return a {@link io.ebean.FutureRowCount} object.
     */
    protected FutureRowCount fetchRowCount(Query query) {
        return ModelInterceptor.fetchRowCount(uriInfo.getQueryParameters(), query);
    }

    /**
     * <p>applyRowCountHeader.</p>
     *
     * @param headerParams a {@link javax.ws.rs.core.MultivaluedMap} object.
     * @param query        a {@link io.ebean.Query} object.
     * @param rowCount     a {@link io.ebean.FutureRowCount} object.
     */
    protected void applyRowCountHeader(MultivaluedMap<String, Object> headerParams, Query query, FutureRowCount rowCount) {
        ModelInterceptor.applyRowCountHeader(headerParams, query, rowCount);
    }

    /**
     * <p>processTransactionError.</p>
     *
     * @param t        Transaction
     * @param callable a {@link ameba.db.ebean.support.ModelResourceStructure.TxCallable} object.
     * @param process  a {@link ameba.db.ebean.support.ModelResourceStructure.TxCallable} object.
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
     * @param r            a {@link ameba.db.ebean.support.ModelResourceStructure.TxRunnable} object.
     * @param errorHandler error handler
     * @throws java.lang.Exception if any.
     */
    protected void executeTx(final TxRunnable r, final TxRunnable errorHandler) throws Exception {
        Transaction transaction = beginTransaction();
        configureTransDefault(transaction);
        processTransactionError(transaction, t -> {
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
        }, errorHandler != null ? (TxCallable) t -> {
            errorHandler.run(t);
            return null;
        } : null);
    }

    /**
     * <p>beginTransaction.</p>
     *
     * @return a {@link io.ebean.Transaction} object.
     */
    protected Transaction beginTransaction() {
        return server.beginTransaction(txScope);
    }

    /**
     * <p>executeTx.</p>
     *
     * @param r a {@link ameba.db.ebean.support.ModelResourceStructure.TxRunnable} object.
     * @throws java.lang.Exception if any.
     */
    protected void executeTx(TxRunnable r) throws Exception {
        executeTx(r, null);
    }

    /**
     * <p>executeTx.</p>
     *
     * @param c   a {@link ameba.db.ebean.support.ModelResourceStructure.TxCallable} object.
     * @param <O> a O object.
     * @return a O object.
     * @throws java.lang.Exception if any.
     */
    protected <O> O executeTx(TxCallable<O> c) throws Exception {
        return executeTx(c, null);
    }


    /**
     * <p>executeTx.</p>
     *
     * @param c            a {@link ameba.db.ebean.support.ModelResourceStructure.TxCallable} object.
     * @param errorHandler a {@link ameba.db.ebean.support.ModelResourceStructure.TxCallable} object.
     * @param <O>          a O object.
     * @return a O object.
     * @throws java.lang.Exception if any.
     */
    @SuppressWarnings("unchecked")
    protected <O> O executeTx(final TxCallable<O> c, final TxCallable<O> errorHandler) throws Exception {
        Transaction transaction = beginTransaction();
        configureTransDefault(transaction);
        return processTransactionError(transaction, t -> {
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
        }, errorHandler);
    }

    /**
     * <p>configureTransDefault.</p>
     *
     * @param transaction a {@link io.ebean.Transaction} object.
     */
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

    /**
     * <p>buildLocationUri.</p>
     *
     * @param id a MODEL_ID object.
     * @return a {@link java.net.URI} object.
     */
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
