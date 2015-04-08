package ameba.db.ebean.internal;

import ameba.core.ws.rs.PATCH;
import ameba.db.model.Model;
import ameba.lib.LoggerOwner;
import com.avaje.ebean.*;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

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

    protected Class<T> modelType;
    protected final SpiEbeanServer server;
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
    public final Response insert(@NotNull @Valid final T model) {
        BeanDescriptor descriptor = server.getBeanDescriptor(model.getClass());
        Object idProp = descriptor.getId((EntityBean) model);
        if (idProp instanceof CharSequence) {
            if (StringUtils.isNotBlank((CharSequence) idProp)) {
                descriptor.getIdProperty().setValue((EntityBean) model, null);
            }
        } else if (idProp != null) {
            descriptor.getIdProperty().setValue((EntityBean) model, null);
        }

        server.execute(new TxRunnable() {
            @Override
            public void run() {
                preInsertModel(model);
                insertModel(model);
                postInsertModel(model);
            }
        });
        Object id = server.getBeanId(model);

        return Response.created(buildLocationUri(id.toString())).build();
    }

    protected void preInsertModel(final T model) {

    }

    protected void insertModel(final T model) {
        server.insert(model);
    }

    protected void postInsertModel(final T model) {

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
    public final Response replace(@PathParam("id") final String id, @NotNull @Valid final T model) {

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
        server.execute(new TxRunnable() {
            @Override
            public void run() {
                preReplaceModel(model);
                processCheckRowCountError(new Runnable() {
                    @Override
                    public void run() {
                        replaceModel(model);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
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

    protected void preReplaceModel(final T model) {

    }

    protected void replaceModel(final T model) {
        server.update(model);
    }

    protected void postReplaceModel(final T model) {

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
    public final Response patch(@PathParam("id") final String id, @NotNull final T model) {
        BeanDescriptor descriptor = server.getBeanDescriptor(model.getClass());
        descriptor.convertSetId(id, (EntityBean) model);
        return server.execute(new TxCallable<Response>() {
            @Override
            public Response call() {
                prePatchModel(model);
                final Response.ResponseBuilder builder = Response.noContent()
                        .contentLocation(buildLocationUri(id));
                processCheckRowCountError(new Runnable() {
                    @Override
                    public void run() {
                        patchModel(model);
                    }
                }, new Runnable() {
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

    protected void prePatchModel(final T model) {

    }

    protected void patchModel(final T model) {
        server.update(model);
    }

    protected void postPatchModel(final T model) {

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
    public final Response deleteMultiple(@NotNull @PathParam("ids") final PathSegment ids) {
        final String firstId = ids.getPath();
        Set<String> idSet = ids.getMatrixParameters().keySet();
        final Response.ResponseBuilder builder = Response.noContent();
        final Runnable failProcess = new Runnable() {
            @Override
            public void run() {
                builder.status(Response.Status.NOT_FOUND);
            }
        };
        if (!idSet.isEmpty()) {
            final Set<String> idCollection = Sets.newLinkedHashSet();
            idCollection.add(firstId);
            idCollection.addAll(idSet);
            server.execute(new TxRunnable() {
                @Override
                public void run() {
                    preDeleteMultipleModel(idCollection);
                    processCheckRowCountError(new Runnable() {
                        @Override
                        public void run() {
                            if (!deleteMultipleModel(idCollection)) {
                                builder.status(Response.Status.ACCEPTED);
                            }
                        }
                    }, failProcess);
                    postDeleteMultipleModel(idCollection);
                }
            });
        } else {
            server.execute(new TxRunnable() {
                @Override
                public void run() {
                    preDeleteModel(firstId);
                    processCheckRowCountError(new Runnable() {
                        @Override
                        public void run() {
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

    protected void preDeleteMultipleModel(Set<String> idCollection) {

    }

    /**
     * delete multiple Model
     *
     * @param idCollection model id collection
     * @return delete from physical device, if logical delete return false, response status 202
     */
    protected boolean deleteMultipleModel(Set<String> idCollection) {
        server.delete(modelType, idCollection);
        return true;
    }

    protected void postDeleteMultipleModel(Set<String> idCollection) {

    }

    protected void preDeleteModel(String id) {

    }

    /**
     * delete a model
     *
     * @param id model id
     * @return delete from physical device, if logical delete return false, response status 202
     */
    protected boolean deleteModel(String id) {
        server.delete(modelType, id);
        return true;
    }

    protected void postDeleteModel(String id) {

    }

    /**
     * Find a model given its Id.
     *
     * @param id the id of the model.
     */
    @GET
    @Path("{id}")
    public final Response find(@NotNull @PathParam("id") final String id) {
        final Query<T> query = server.find(modelType);
        applyUriQuery(query, false);
        Response.ResponseBuilder builder = Response.ok();

        return builder.entity(server.execute(new TxCallable<Object>() {
            @Override
            public Object call() {
                configFindByIdQuery(query);
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
    protected void configFindByIdQuery(final Query<T> query) {

    }

    protected Object processFoundModel(final T model) {
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
    public final Response find() {

        final Query<T> query = server.find(modelType);

        if (StringUtils.isNotBlank(defaultFindOrderBy)) {
            // see if we should use the default orderBy clause
            OrderBy<T> orderBy = query.orderBy();
            if (orderBy.isEmpty()) {
                query.orderBy(defaultFindOrderBy);
            }
        }

        final FutureRowCount rowCount = applyUriQuery(query);
        Response.ResponseBuilder builder = Response.ok();
        Response response = builder.entity(server.execute(new TxCallable<Object>() {
                    @Override
                    public Object call() {
                        configFindQuery(query);
                        List<T> list = query.findList();
                        return processFoundModelList(list);
                    }
                })
        ).build();

        applyRowCountHeader(response.getHeaders(), query, rowCount);

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
    protected void configFindQuery(final Query<T> query) {

    }

    protected Object processFoundModelList(final List<T> list) {
        return list;
    }

    protected FutureRowCount applyUriQuery(final Query<T> query, boolean needPageList) {
        return EbeanModelProcessor.applyUriQuery(uriInfo.getQueryParameters(), query, needPageList);
    }

    protected FutureRowCount applyUriQuery(final Query<T> query) {
        return applyUriQuery(query, true);
    }

    protected void applyRowCountHeader(MultivaluedMap<String, Object> headerParams, Query query, FutureRowCount rowCount) {
        EbeanModelProcessor.applyRowCountHeader(headerParams, query, rowCount);
    }

    protected void processCheckRowCountError(Runnable runnable, Runnable process) {
        try {
            runnable.run();
        } catch (OptimisticLockException e) {
            if ("checkRowCount".equals(e.getStackTrace()[0].getMethodName())) {
                if (process != null)
                    process.run();
            }
        }
    }

    protected void processCheckRowCountError(Runnable runnable) {
        processCheckRowCountError(runnable, null);
    }

    protected URI buildLocationUri(String id) {
        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        return ub.path(id).build();
    }
}
