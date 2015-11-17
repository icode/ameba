package ameba.db.ebean.support;

import ameba.db.ebean.EbeanUtils;
import ameba.db.ebean.internal.EbeanModelInterceptor;
import ameba.db.model.Model;
import ameba.lib.LoggerOwner;
import com.avaje.ebean.*;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.deploy.BeanProperty;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.internal.util.collection.Refs;

import javax.annotation.Nullable;
import javax.persistence.OptimisticLockException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * <p>Abstract ModelResourceStructure class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public abstract class ModelResourceStructure<ID, M extends Model> extends LoggerOwner {

    protected final SpiEbeanServer server;
    protected Class<M> modelType;
    protected String defaultFindOrderBy;
    @Context
    protected UriInfo uriInfo;
    private BeanDescriptor descriptor;

    /**
     * <p>Constructor for ModelResourceStructure.</p>
     *
     * @param modelType a {@link java.lang.Class} object.
     */
    public ModelResourceStructure(Class<M> modelType) {
        this(modelType, (SpiEbeanServer) Ebean.getServer(null));
    }

    /**
     * <p>Constructor for ModelResourceStructure.</p>
     *
     * @param modelType a {@link java.lang.Class} object.
     * @param server    a {@link com.avaje.ebeaninternal.api.SpiEbeanServer} object.
     */
    public ModelResourceStructure(Class<M> modelType, SpiEbeanServer server) {
        this.modelType = modelType;
        this.server = server;
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
     * convert string to id
     *
     * @param id id string
     * @return ID object
     * @see ModelResourceStructure#deleteMultiple(Object, PathSegment)
     */
    @SuppressWarnings("unchecked")
    protected ID stringToId(String id) {
        return (ID) getModelBeanDescriptor().convertId(id);
    }

    /**
     * convert string to id
     *
     * @param id string id
     * @return ID
     * @throws BadRequestException response status 400
     */
    protected final ID tryConvertId(Object id) {
        try {
            return stringToId(id.toString());
        } catch (Exception e) {
            throw new BadRequestException("Id syntax error", e);
        }
    }

    /**
     * convert id to string for insert
     *
     * @param id id object
     * @return id string
     * @see ModelResourceStructure#insert(Model)
     * @see ModelResourceStructure#patch(Object, Model)
     */
    protected String idToString(@NotNull ID id) {
        return id.toString();
    }

    /**
     * <p>setForInsertId.</p>
     *
     * @param model a M object.
     */
    protected void setForInsertId(final M model) {
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
     * @see AbstractModelResource#insert(Model)
     */
    @SuppressWarnings("unchecked")
    public Response insert(@NotNull @Valid final M model) throws Exception {
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
        ID id = (ID) server.getBeanId(model);

        return Response.created(buildLocationUri(id)).build();
    }

    protected void matchedInsert(final M model) throws Exception {

    }

    /**
     * <p>preInsertModel.</p>
     *
     * @param model a M object.
     * @throws java.lang.Exception if any.
     */
    protected void preInsertModel(final M model) throws Exception {

    }

    /**
     * <p>insertModel.</p>
     *
     * @param model a M object.
     * @throws java.lang.Exception if any.
     */
    protected void insertModel(final M model) throws Exception {
        server.insert(model);
        flushBatch();
    }

    /**
     * <p>postInsertModel.</p>
     *
     * @param model a M object.
     * @throws java.lang.Exception if any.
     */
    protected void postInsertModel(final M model) throws Exception {

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
     * @see AbstractModelResource#replace(Object, Model)
     */
    public Response replace(@PathParam("id") final ID id, @NotNull @Valid final M model) throws Exception {
        matchedReplace(id, model);
        final ID mId = tryConvertId(id);
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

    protected void matchedReplace(ID id, final M model) throws Exception {

    }

    /**
     * <p>preReplaceModel.</p>
     *
     * @param model a M object.
     * @throws java.lang.Exception if any.
     */
    protected void preReplaceModel(final M model) throws Exception {

    }

    /**
     * <p>replaceModel.</p>
     *
     * @param model a M object.
     * @throws java.lang.Exception if any.
     */
    protected void replaceModel(final M model) throws Exception {
        server.update(model, null, true);
    }

    /**
     * <p>postReplaceModel.</p>
     *
     * @param model a M object.
     * @throws java.lang.Exception if any.
     */
    protected void postReplaceModel(final M model) throws Exception {

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
     * @see AbstractModelResource#patch(Object, Model)
     */
    public Response patch(@PathParam("id") final ID id, @NotNull final M model) throws Exception {
        matchedPatch(id, model);
        BeanDescriptor descriptor = getModelBeanDescriptor();
        descriptor.convertSetId(tryConvertId(id), (EntityBean) model);
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

    protected void matchedPatch(final ID id, final M model) throws Exception {

    }

    /**
     * <p>prePatchModel.</p>
     *
     * @param model a M object.
     * @throws java.lang.Exception if any.
     */
    protected void prePatchModel(final M model) throws Exception {

    }

    /**
     * <p>patchModel.</p>
     *
     * @param model a M object.
     * @throws java.lang.Exception if any.
     */
    protected void patchModel(final M model) throws Exception {
        server.update(model, null, false);
    }

    /**
     * <p>postPatchModel.</p>
     *
     * @param model a M object.
     * @throws java.lang.Exception if any.
     */
    protected void postPatchModel(final M model) throws Exception {

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
     * @see AbstractModelResource#deleteMultiple(Object, PathSegment)
     */
    public Response deleteMultiple(@NotNull @PathParam("ids") ID id,
                                   @NotNull @PathParam("ids") final PathSegment ids) throws Exception {
        matchedDelete(id, ids);
        final ID firstId = tryConvertId(ids.getPath());
        Set<String> idSet = ids.getMatrixParameters().keySet();
        final Response.ResponseBuilder builder = Response.noContent();
        final TxRunnable failProcess = new TxRunnable() {
            @Override
            public void run(Transaction t) {
                builder.status(Response.Status.NOT_FOUND);
            }
        };
        if (!idSet.isEmpty()) {
            final Set<ID> idCollection = Sets.newLinkedHashSet();
            idCollection.add(firstId);
            idCollection.addAll(Collections2.transform(idSet, new Function<String, ID>() {
                @Nullable
                @Override
                public ID apply(String input) {
                    return tryConvertId(input);
                }
            }));
            executeTx(new TxRunnable() {
                @Override
                public void run(Transaction t) throws Exception {
                    preDeleteMultipleModel(idCollection);
                    if (!deleteMultipleModel(idCollection)) {
                        builder.status(Response.Status.ACCEPTED);
                    }
                    postDeleteMultipleModel(idCollection);
                }
            }, failProcess);
        } else {
            executeTx(new TxRunnable() {
                @Override
                public void run(Transaction t) throws Exception {
                    preDeleteModel(firstId);
                    if (!deleteModel(firstId)) {
                        builder.status(Response.Status.ACCEPTED);
                    }
                    postDeleteModel(firstId);
                }
            }, failProcess);
        }
        return builder.build();
    }

    protected void matchedDelete(ID id, PathSegment ids) throws Exception {

    }

    /**
     * <p>preDeleteMultipleModel.</p>
     *
     * @param idCollection a {@link java.util.Set} object.
     * @throws java.lang.Exception if any.
     */
    protected void preDeleteMultipleModel(Set<ID> idCollection) throws Exception {

    }

    /**
     * delete multiple Model
     *
     * @param idCollection model id collection
     * @return delete from physical device, if logical delete return false, response status 202
     * @throws java.lang.Exception if any.
     */
    protected boolean deleteMultipleModel(Set<ID> idCollection) throws Exception {
        server.delete(modelType, idCollection);
        return true;
    }

    /**
     * <p>postDeleteMultipleModel.</p>
     *
     * @param idCollection a {@link java.util.Set} object.
     * @throws java.lang.Exception if any.
     */
    protected void postDeleteMultipleModel(Set<ID> idCollection) throws Exception {

    }

    /**
     * <p>preDeleteModel.</p>
     *
     * @param id a ID object.
     * @throws java.lang.Exception if any.
     */
    protected void preDeleteModel(ID id) throws Exception {

    }

    /**
     * delete a model
     *
     * @param id model id
     * @return delete from physical device, if logical delete return false, response status 202
     * @throws java.lang.Exception if any.
     */
    protected boolean deleteModel(ID id) throws Exception {
        server.delete(modelType, id);
        return true;
    }

    /**
     * <p>postDeleteModel.</p>
     *
     * @param id a ID object.
     * @throws java.lang.Exception if any.
     */
    protected void postDeleteModel(ID id) throws Exception {

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
    public Response findByIds(@NotNull @PathParam("ids") ID id,
                              @NotNull @PathParam("ids") final PathSegment ids) throws Exception {
        matchedFindByIds(id, ids);
        final Query<M> query = server.find(modelType);
        final ID firstId = tryConvertId(ids.getPath());
        Set<String> idSet = ids.getMatrixParameters().keySet();
        Object model;
        final TxRunnable configureQuery = new TxRunnable() {
            @Override
            public void run(Transaction t) throws Exception {
                configDefaultQuery(query);
                configFindByIdsQuery(query);
                applyUriQuery(query, false);
            }
        };
        if (!idSet.isEmpty()) {
            final List<ID> idCollection = Lists.newArrayList();
            idCollection.add(firstId);
            idCollection.addAll(Collections2.transform(idSet, new Function<String, ID>() {
                @Nullable
                @Override
                public ID apply(String input) {
                    return tryConvertId(input);
                }
            }));
            model = executeTx(new TxCallable() {
                @Override
                public Object call(Transaction t) throws Exception {
                    configureQuery.run(t);
                    List<M> m = query.where().idIn(idCollection).findList();
                    return processFoundByIdsModelList(m);
                }
            });
        } else {
            model = executeTx(new TxCallable<Object>() {
                @Override
                public Object call(Transaction t) throws Exception {
                    configureQuery.run(t);
                    M m = query.setId(firstId).findUnique();
                    return processFoundByIdModel(m);
                }
            });
        }

        if (model != null)
            return Response.ok(model).build();
        else
            throw new NotFoundException();
    }

    protected void matchedFindByIds(ID id, PathSegment ids) throws Exception {

    }

    /**
     * Configure the "Find By Ids" query.
     * <p>
     * This is only used when no PathProperties where set via UriOptions.
     * </p>
     * <p>
     * This effectively controls the "default" query used to render this model.
     * </p>
     *
     * @param query a {@link com.avaje.ebean.Query} object.
     * @throws java.lang.Exception if any.
     */
    protected void configFindByIdsQuery(final Query<M> query) throws Exception {

    }

    /**
     * <p>processFoundModel.</p>
     *
     * @param model a M object.
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
    protected Object processFoundByIdModel(final M model) throws Exception {
        return model;
    }

    protected Object processFoundByIdsModelList(final List<M> models) throws Exception {
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
    public Response find() throws Exception {
        matchedFind();
        final Query<M> query = server.find(modelType);

        if (StringUtils.isNotBlank(defaultFindOrderBy)) {
            // see if we should use the default orderBy clause
            OrderBy<M> orderBy = query.orderBy();
            if (orderBy.isEmpty()) {
                query.orderBy(defaultFindOrderBy);
            }
        }

        final Ref<FutureRowCount> rowCount = Refs.emptyRef();

        Response.ResponseBuilder builder = Response.ok();
        Response response = builder.entity(executeTx(new TxCallable<Object>() {
                    @Override
                    public Object call(Transaction t) throws Exception {
                        configDefaultQuery(query);
                        configFindQuery(query);
                        rowCount.set(applyUriQuery(query));
                        List<M> list = query.findList();
                        return processFoundModelList(list);
                    }
                })
        ).build();

        applyRowCountHeader(response.getHeaders(), query, rowCount.get());

        return response;
    }

    protected void matchedFind() throws Exception {

    }

    /**
     * Configure the "Find" query.
     * <p>
     * This is only used when no PathProperties where set via UriOptions.
     * </p>
     * <p>
     * This effectively controls the "default" query used with the find all
     * query.
     * </p>
     *
     * @param query a {@link com.avaje.ebean.Query} object.
     * @throws java.lang.Exception if any.
     */
    protected void configFindQuery(final Query<M> query) throws Exception {

    }

    /**
     * <p>processFoundModelList.</p>
     *
     * @param list a {@link java.util.List} object.
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
    protected Object processFoundModelList(final List<M> list) throws Exception {
        return list;
    }


    /**
     * all query config default query
     *
     * @param query query
     * @throws java.lang.Exception if any.
     */
    protected void configDefaultQuery(final Query<M> query) throws Exception {

    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////                                   ///////////////////////////////////
    ///////////////////////////////////        useful method block        ///////////////////////////////////
    ///////////////////////////////////                                   ///////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * apply uri query parameter on query
     *
     * @param query        Query
     * @param needPageList need page list
     * @return page list count or null
     * @see EbeanModelInterceptor#applyUriQuery(MultivaluedMap, Query, boolean)
     */
    protected FutureRowCount applyUriQuery(final Query<M> query, boolean needPageList) {
        return EbeanModelInterceptor.applyUriQuery(uriInfo.getQueryParameters(), query, needPageList);
    }

    /**
     * <p>applyUriQuery.</p>
     *
     * @param query a {@link com.avaje.ebean.Query} object.
     * @return a {@link com.avaje.ebean.FutureRowCount} object.
     */
    protected FutureRowCount applyUriQuery(final Query<M> query) {
        return applyUriQuery(query, true);
    }

    /**
     * <p>applyRowCountHeader.</p>
     *
     * @param headerParams a {@link javax.ws.rs.core.MultivaluedMap} object.
     * @param query        a {@link com.avaje.ebean.Query} object.
     * @param rowCount     a {@link com.avaje.ebean.FutureRowCount} object.
     */
    protected void applyRowCountHeader(MultivaluedMap<String, Object> headerParams, Query query, FutureRowCount rowCount) {
        EbeanModelInterceptor.applyRowCountHeader(headerParams, query, rowCount);
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
        final Transaction transaction = server.beginTransaction();
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
        final Transaction transaction = server.beginTransaction();
        configureTransDefault(transaction);
        return processTransactionError(transaction, new TxCallable<O>() {
            @Override
            public O call(Transaction t) throws Exception {
                Object o = null;
                try {
                    o = c.call(t);
                    t.commit();
                } catch (Throwable e) {
                    transaction.rollback(e);
                    throw e;
                } finally {
                    transaction.end();
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
     * @param id          a ID object.
     * @param useTemplate use current template
     * @return a {@link java.net.URI} object.
     */
    protected URI buildLocationUri(ID id, boolean useTemplate) {
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

    protected URI buildLocationUri(ID id) {
        return buildLocationUri(id, false);
    }

    protected interface TxRunnable {
        void run(Transaction transaction) throws Exception;
    }

    protected interface TxCallable<O> {
        O call(Transaction transaction) throws Exception;
    }
}
