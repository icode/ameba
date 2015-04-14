package ameba.db.ebean.internal;

import ameba.db.ebean.EbeanUtils;
import ameba.db.model.Model;
import ameba.lib.LoggerOwner;
import com.avaje.ebean.*;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebeaninternal.api.ScopeTrans;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.internal.util.collection.Refs;

import javax.annotation.Nullable;
import javax.persistence.OptimisticLockException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
     * convert string to id for delete deleteMultiple
     *
     * @param id id string
     * @return ID object
     * @see #deleteMultiple(PathSegment)
     */
    @SuppressWarnings("unchecked")
    protected ID stringToId(String id) {
        return (ID) getModelBeanDescriptor().convertId(id);
    }

    /**
     * convert id to string for insert
     *
     * @param id id object
     * @return id string
     * @see #insert(Model)
     * @see #patch(Object, Model)
     * @see #insert(Model)
     * @see #patch(Object, Model)
     */
    protected String idToString(@NotNull ID id) {
        return id.toString();
    }

    /**
     * <p>seForInsertId.</p>
     *
     * @param model a M object.
     */
    protected void seForInsertId(final M model) {
        BeanDescriptor descriptor = getModelBeanDescriptor();
        descriptor.getIdProperty().setValue((EntityBean) model, null);
    }

    /**
     * Insert a model.
     * <p/>
     * success status 201
     *
     * @param model the model to insert
     * @see {@link javax.ws.rs.POST}
     * @see {@link ameba.db.ebean.internal.AbstractModelResource#insert(Model)}
     * @see {@link javax.ws.rs.POST}
     * @see {@link ameba.db.ebean.internal.AbstractModelResource#insert(Model)}
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     */
    @SuppressWarnings("unchecked")
    public Response insert(@NotNull @Valid final M model) throws Exception {
        seForInsertId(model);

        executeTx(new TxRunnable() {
            @Override
            public void run() throws Exception {
                preInsertModel(model);
                insertModel(model);
                postInsertModel(model);
            }
        });
        ID id = (ID) server.getBeanId(model);

        return Response.created(buildLocationUri(id)).build();
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
     * <p/>
     * success replace status 204
     * <br/>
     * fail replace but inserted status 201
     *
     * @param id    the unique id of the model
     * @param model the model to update
     * @see {@link javax.ws.rs.PUT}
     * @see {@link ameba.db.ebean.internal.AbstractModelResource#replace(Object, Model)}
     * @see {@link javax.ws.rs.PUT}
     * @see {@link ameba.db.ebean.internal.AbstractModelResource#replace(Object, Model)}
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     */
    public Response replace(@PathParam("id") final ID id, @NotNull @Valid final M model) throws Exception {

        BeanDescriptor descriptor = getModelBeanDescriptor();
        descriptor.convertSetId(id, (EntityBean) model);
        EbeanUtils.forceUpdateAllProperties(server, model);

        final Response.ResponseBuilder builder = Response.noContent();
        executeTx(new TxRunnable() {
            @Override
            public void run() throws Exception {
                preReplaceModel(model);
                processCheckRowCountError(new TxRunnable() {
                    @Override
                    public void run() throws Exception {
                        replaceModel(model);
                    }
                }, new TxRunnable() {
                    @Override
                    public void run() throws Exception {
                        logger().debug("not found model record, insert a model record.");
                        preInsertModel(model);
                        insertModel(model);
                        postInsertModel(model);
                        builder.status(Response.Status.CREATED).location(buildLocationUri(id));
                    }
                });
                postReplaceModel(model);
            }
        });
        return builder.build();
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
        server.update(model);
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
     * <p/>
     * success status 204
     * <br/>
     * fail status 422
     *
     * @param id    the unique id of the model
     * @param model the model to update
     * @see {@link ameba.core.ws.rs.PATCH}
     * @see {@link ameba.db.ebean.internal.AbstractModelResource#patch(Object, Model)}
     * @see {@link ameba.core.ws.rs.PATCH}
     * @see {@link ameba.db.ebean.internal.AbstractModelResource#patch(Object, Model)}
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     */
    public Response patch(@PathParam("id") final ID id, @NotNull final M model) throws Exception {
        BeanDescriptor descriptor = getModelBeanDescriptor();
        descriptor.convertSetId(id, (EntityBean) model);
        return executeTx(new TxCallable<Response>() {
            @Override
            public Response call() throws Exception {
                prePatchModel(model);
                final Response.ResponseBuilder builder = Response.noContent()
                        .contentLocation(buildLocationUri(id));
                processCheckRowCountError(new TxRunnable() {
                    @Override
                    public void run() throws Exception {
                        patchModel(model);
                    }
                }, new TxRunnable() {
                    @Override
                    public void run() {
                        // id 无法对应数据。实体对象和补丁都正确，但无法处理请求，所以返回422
                        builder.status(422);
                    }
                });
                postPatchModel(model);
                return builder.build();
            }
        });
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
        server.update(model);
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
     * <p/>
     * success status 200
     * <br/>
     * fail status 404
     * <br/>
     * logical delete status 202
     *
     * @param ids The ids in the form "/resource/id1" or "/resource/id1;id2;id3"
     * @see {@link javax.ws.rs.DELETE}
     * @see {@link ameba.db.ebean.internal.AbstractModelResource#deleteMultiple(PathSegment)}
     * @see {@link javax.ws.rs.DELETE}
     * @see {@link ameba.db.ebean.internal.AbstractModelResource#deleteMultiple(PathSegment)}
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     */
    public Response deleteMultiple(@NotNull @PathParam("ids") final PathSegment ids) throws Exception {
        final ID firstId = stringToId(ids.getPath());
        Set<String> idSet = ids.getMatrixParameters().keySet();
        final Response.ResponseBuilder builder = Response.noContent();
        final TxRunnable failProcess = new TxRunnable() {
            @Override
            public void run() {
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
                    return stringToId(input);
                }
            }));
            executeTx(new TxRunnable() {
                @Override
                public void run() throws Exception {
                    preDeleteMultipleModel(idCollection);
                    processCheckRowCountError(new TxRunnable() {
                        @Override
                        public void run() throws Exception {
                            if (!deleteMultipleModel(idCollection)) {
                                builder.status(Response.Status.ACCEPTED);
                            }
                        }
                    }, failProcess);
                    postDeleteMultipleModel(idCollection);
                }
            });
        } else {
            executeTx(new TxRunnable() {
                @Override
                public void run() throws Exception {
                    preDeleteModel(firstId);
                    processCheckRowCountError(new TxRunnable() {
                        @Override
                        public void run() throws Exception {
                            if (!deleteModel(firstId)) {
                                builder.status(Response.Status.ACCEPTED);
                            }
                        }
                    }, failProcess);
                    postDeleteModel(firstId);
                }
            });
        }
        return builder.build();
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
     * Find a model given its Id.
     *
     * @param id the id of the model.
     * @see {@link javax.ws.rs.GET}
     * @see {@link ameba.db.ebean.internal.AbstractModelResource#findById}
     * @see {@link javax.ws.rs.GET}
     * @see {@link ameba.db.ebean.internal.AbstractModelResource#findById}
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     */
    public Response findById(@NotNull @PathParam("id") final ID id) throws Exception {
        final Query<M> query = server.find(modelType);

        Object model = executeTx(new TxCallable<Object>() {
            @Override
            public Object call() throws Exception {
                configDefaultQuery(query);
                configFindByIdQuery(query);
                applyUriQuery(query, false);
                M m = query.setId(id).findUnique();

                return processFoundModel(m);
            }
        });

        if (model != null)
            return Response.ok(model).build();
        else
            throw new NotFoundException();
    }

    /**
     * Configure the "Find By Id" query.
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
    protected void configFindByIdQuery(final Query<M> query) throws Exception {

    }

    /**
     * <p>processFoundModel.</p>
     *
     * @param model a M object.
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
    protected Object processFoundModel(final M model) throws Exception {
        return model;
    }

    /**
     * Find the beans for this beanType.
     * <p>
     * This can use URL query parameters such as order and maxrows to configure
     * the query.
     * </p>
     *
     * @see {@link javax.ws.rs.GET}
     * @see {@link ameba.db.ebean.internal.AbstractModelResource#find()}
     * @see {@link javax.ws.rs.GET}
     * @see {@link ameba.db.ebean.internal.AbstractModelResource#find()}
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     */
    public Response find() throws Exception {

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
                    public Object call() throws Exception {
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
     * @see {@link ameba.db.ebean.internal.EbeanModelProcessor#applyUriQuery(MultivaluedMap, Query, boolean)}
     */
    protected FutureRowCount applyUriQuery(final Query<M> query, boolean needPageList) {
        return EbeanModelProcessor.applyUriQuery(uriInfo.getQueryParameters(), query, needPageList);
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
     * @param query a {@link com.avaje.ebean.Query} object.
     * @param rowCount a {@link com.avaje.ebean.FutureRowCount} object.
     */
    protected void applyRowCountHeader(MultivaluedMap<String, Object> headerParams, Query query, FutureRowCount rowCount) {
        EbeanModelProcessor.applyRowCountHeader(headerParams, query, rowCount);
    }

    /**
     * <p>processCheckRowCountError.</p>
     *
     * @param runnable a {@link ameba.db.ebean.internal.ModelResourceStructure.TxRunnable} object.
     * @param process a {@link ameba.db.ebean.internal.ModelResourceStructure.TxRunnable} object.
     * @throws java.lang.Exception if any.
     */
    protected void processCheckRowCountError(TxRunnable runnable, TxRunnable process) throws Exception {
        try {
            runnable.run();
        } catch (OptimisticLockException e) {
            if ("checkRowCount".equals(e.getStackTrace()[0].getMethodName())) {
                if (process != null)
                    process.run();
            }
        }
    }

    /**
     * <p>executeTx.</p>
     *
     * @param scope a {@link com.avaje.ebean.TxScope} object.
     * @param r a {@link ameba.db.ebean.internal.ModelResourceStructure.TxRunnable} object.
     * @throws java.lang.Exception if any.
     */
    protected void executeTx(TxScope scope, TxRunnable r) throws Exception {
        ScopeTrans scopeTrans = server.createScopeTrans(scope);
        try {
            r.run();
        } catch (Error e) {
            throw scopeTrans.caughtError(e);
        } catch (Exception e) {
            throw scopeTrans.caughtThrowable(e);
        } finally {
            scopeTrans.onFinally();
        }
    }

    /**
     * <p>executeTx.</p>
     *
     * @param r a {@link ameba.db.ebean.internal.ModelResourceStructure.TxRunnable} object.
     * @throws java.lang.Exception if any.
     */
    protected void executeTx(TxRunnable r) throws Exception {
        executeTx(null, r);
    }

    /**
     * <p>executeTx.</p>
     *
     * @param c a {@link ameba.db.ebean.internal.ModelResourceStructure.TxCallable} object.
     * @param <O> a O object.
     * @return a O object.
     * @throws java.lang.Exception if any.
     */
    protected <O> O executeTx(TxCallable<O> c) throws Exception {
        return executeTx(null, c);
    }

    /**
     * <p>executeTx.</p>
     *
     * @param scope a {@link com.avaje.ebean.TxScope} object.
     * @param c a {@link ameba.db.ebean.internal.ModelResourceStructure.TxCallable} object.
     * @param <O> a O object.
     * @return a O object.
     * @throws java.lang.Exception if any.
     */
    protected <O> O executeTx(TxScope scope, TxCallable<O> c) throws Exception {
        ScopeTrans scopeTrans = server.createScopeTrans(scope);
        try {
            return c.call();
        } catch (Error e) {
            throw scopeTrans.caughtError(e);
        } catch (Exception e) {
            throw scopeTrans.caughtThrowable(e);
        } finally {
            scopeTrans.onFinally();
        }
    }

    /**
     * <p>processCheckRowCountError.</p>
     *
     * @param runnable a {@link ameba.db.ebean.internal.ModelResourceStructure.TxRunnable} object.
     * @throws java.lang.Exception if any.
     */
    protected void processCheckRowCountError(TxRunnable runnable) throws Exception {
        processCheckRowCountError(runnable, null);
    }

    /**
     * <p>buildLocationUri.</p>
     *
     * @param id a ID object.
     * @return a {@link java.net.URI} object.
     */
    protected URI buildLocationUri(ID id) {
        if (id == null) {
            throw new NotFoundException();
        }
        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        return ub.path(idToString(id)).build();
    }

    protected interface TxRunnable {
        void run() throws Exception;
    }

    protected interface TxCallable<O> {
        O call() throws Exception;
    }
}
