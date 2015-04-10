package ameba.db.ebean.internal;

import ameba.core.ws.rs.PATCH;
import ameba.db.model.Model;
import ameba.lib.LoggerOwner;
import com.avaje.ebean.*;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.api.ScopeTrans;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.internal.util.collection.Refs;

import javax.persistence.OptimisticLockException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * @author icode
 */
public abstract class AbstractModelResource<T extends Model> extends LoggerOwner {

    protected final SpiEbeanServer server;
    protected Class<T> modelType;
    protected String defaultFindOrderBy;
    @Context
    protected UriInfo uriInfo;

    public AbstractModelResource(Class<T> modelType) {
        this(modelType, (SpiEbeanServer) Ebean.getServer(null));
    }

    public AbstractModelResource(Class<T> modelType, SpiEbeanServer server) {
        this.modelType = modelType;
        this.server = server;
    }

    /**
     * Insert a model.
     * <p/>
     * success status 201
     *
     * @param model the model to insert
     */
    @POST
    public final Response insert(@NotNull @Valid final T model) throws Exception {
        BeanDescriptor descriptor = server.getBeanDescriptor(model.getClass());
        Object idProp = descriptor.getId((EntityBean) model);
        if (idProp instanceof CharSequence) {
            if (StringUtils.isNotBlank((CharSequence) idProp)) {
                descriptor.getIdProperty().setValue((EntityBean) model, null);
            }
        } else if (idProp != null) {
            descriptor.getIdProperty().setValue((EntityBean) model, null);
        }

        executeTx(new TxRunnable() {
            @Override
            public void run() throws Exception {
                preInsertModel(model);
                insertModel(model);
                postInsertModel(model);
            }
        });
        Object id = server.getBeanId(model);

        return Response.created(buildLocationUri(id != null ? id.toString() : "")).build();
    }

    protected void preInsertModel(final T model) throws Exception {

    }

    protected void insertModel(final T model) throws Exception {
        server.insert(model);
    }

    protected void postInsertModel(final T model) throws Exception {

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
     */
    @PUT
    @Path("{id}")
    public final Response replace(@PathParam("id") final String id, @NotNull @Valid final T model) throws Exception {

        BeanDescriptor descriptor = server.getBeanDescriptor(model.getClass());
        descriptor.convertSetId(id, (EntityBean) model);
        EntityBeanIntercept intercept = ((EntityBean) model)._ebean_getIntercept();
        intercept.setLoaded();
        int idIndex = descriptor.getIdProperty().getPropertyIndex();
        for (int i = 0; i < intercept.getPropertyLength(); i++) {
            if (i != idIndex) {
                intercept.markPropertyAsChanged(i);
                intercept.setLoadedProperty(i);
            }
        }
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

    protected void preReplaceModel(final T model) throws Exception {

    }

    protected void replaceModel(final T model) throws Exception {
        server.update(model);
    }

    protected void postReplaceModel(final T model) throws Exception {

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
     */
    @PATCH
    @Path("{id}")
    public final Response patch(@PathParam("id") final String id, @NotNull final T model) throws Exception {
        BeanDescriptor descriptor = server.getBeanDescriptor(model.getClass());
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

    protected void prePatchModel(final T model) throws Exception {

    }

    protected void patchModel(final T model) throws Exception {
        server.update(model);
    }

    protected void postPatchModel(final T model) throws Exception {

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
     */
    @DELETE
    @Path("{ids}")
    public final Response deleteMultiple(@NotNull @PathParam("ids") final PathSegment ids) throws Exception {
        final String firstId = ids.getPath();
        Set<String> idSet = ids.getMatrixParameters().keySet();
        final Response.ResponseBuilder builder = Response.noContent();
        final TxRunnable failProcess = new TxRunnable() {
            @Override
            public void run() {
                builder.status(Response.Status.NOT_FOUND);
            }
        };
        if (!idSet.isEmpty()) {
            final Set<String> idCollection = Sets.newLinkedHashSet();
            idCollection.add(firstId);
            idCollection.addAll(idSet);
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

    protected void preDeleteMultipleModel(Set<String> idCollection) throws Exception {

    }

    /**
     * delete multiple Model
     *
     * @param idCollection model id collection
     * @return delete from physical device, if logical delete return false, response status 202
     */
    protected boolean deleteMultipleModel(Set<String> idCollection) throws Exception {
        server.delete(modelType, idCollection);
        return true;
    }

    protected void postDeleteMultipleModel(Set<String> idCollection) throws Exception {

    }

    protected void preDeleteModel(String id) throws Exception {

    }

    /**
     * delete a model
     *
     * @param id model id
     * @return delete from physical device, if logical delete return false, response status 202
     */
    protected boolean deleteModel(String id) throws Exception {
        server.delete(modelType, id);
        return true;
    }

    protected void postDeleteModel(String id) throws Exception {

    }

    /**
     * Find a model given its Id.
     *
     * @param id the id of the model.
     */
    @GET
    @Path("{id}")
    public final Response findById(@NotNull @PathParam("id") final String id) throws Exception {
        final Query<T> query = server.find(modelType);
        Response.ResponseBuilder builder = Response.ok();

        return builder.entity(executeTx(new TxCallable<Object>() {
            @Override
            public Object call() throws Exception {
                configDefaultQuery(query);
                configFindByIdQuery(query);
                applyUriQuery(query, false);
                T m = query.setId(id).findUnique();

                return processFoundModel(m);
            }
        })).build();
    }

    /**
     * Configure the "Find By Id" query.
     * <p>
     * This is only used when no PathProperties where set via UriOptions.
     * </p>
     * <p>
     * This effectively controls the "default" query used to render this model.
     * </p>
     */
    protected void configFindByIdQuery(final Query<T> query) throws Exception {

    }

    protected Object processFoundModel(final T model) throws Exception {
        return model;
    }

    /**
     * Find the beans for this beanType.
     * <p>
     * This can use URL query parameters such as order and maxrows to configure
     * the query.
     * </p>
     */
    @GET
    public final Response find() throws Exception {

        final Query<T> query = server.find(modelType);

        if (StringUtils.isNotBlank(defaultFindOrderBy)) {
            // see if we should use the default orderBy clause
            OrderBy<T> orderBy = query.orderBy();
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
                        List<T> list = query.findList();
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
     */
    protected void configFindQuery(final Query<T> query) throws Exception {

    }

    protected Object processFoundModelList(final List<T> list) throws Exception {
        return list;
    }


    /**
     * all query config default query
     *
     * @param query query
     * @throws Exception
     */
    protected void configDefaultQuery(final Query<T> query) throws Exception {

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
     * @see {@link EbeanModelProcessor#applyUriQuery(MultivaluedMap, Query, boolean)}
     */
    protected FutureRowCount applyUriQuery(final Query<T> query, boolean needPageList) {
        return EbeanModelProcessor.applyUriQuery(uriInfo.getQueryParameters(), query, needPageList);
    }

    protected FutureRowCount applyUriQuery(final Query<T> query) {
        return applyUriQuery(query, true);
    }

    protected void applyRowCountHeader(MultivaluedMap<String, Object> headerParams, Query query, FutureRowCount rowCount) {
        EbeanModelProcessor.applyRowCountHeader(headerParams, query, rowCount);
    }

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

    protected void executeTx(TxRunnable r) throws Exception {
        executeTx(null, r);
    }

    protected <O> O executeTx(TxCallable<O> c) throws Exception {
        return executeTx(null, c);
    }

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

    protected void processCheckRowCountError(TxRunnable runnable) throws Exception {
        processCheckRowCountError(runnable, null);
    }

    protected URI buildLocationUri(String id) {
        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        return ub.path(id).build();
    }

    protected interface TxRunnable {
        void run() throws Exception;
    }

    protected interface TxCallable<O> {
        O call() throws Exception;
    }
}
