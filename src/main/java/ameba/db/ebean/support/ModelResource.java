package ameba.db.ebean.support;

import ameba.core.ws.rs.PATCH;
import io.ebean.Ebean;
import io.ebeaninternal.api.SpiEbeanServer;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;

/**
 * <p>Abstract ModelResource class.</p>
 *
 * @author icode
 *
 */
public abstract class ModelResource<URI_ID, MODEL_ID, MODEL>
        extends ModelResourceStructure<URI_ID, MODEL_ID, MODEL> {
    /**
     * Constant <code>DATE_REGEX="^(\\d{4})(0[1-9]|1[012])(0[1-9]|[12][0-"{trunked}</code>
     */
    protected static final String DATE_REGEX = "^(\\d{4})(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])" + //date yyyyMMDD
            "(T(2[0-3]|[01][0-9])([0-5][0-9]|0[0-9])([0-5][0-9]|0[0-9])(\\.\\d{1,3}))?" + //time 'T'HH:mm:ss.EEE
            "(Z|(-1[012]00|-0[0-9][0-3]0|[+ ]1[0-4]{2}[0-5]|[+ ]0[0-9][0-3][0-5]))?$";//time zone +0800

    /**
     * <p>Constructor for AbstractModelResource.</p>
     *
     * @param modelType a {@link java.lang.Class} object.
     */
    public ModelResource(Class<MODEL> modelType) {
        this(modelType, (SpiEbeanServer) Ebean.getServer(null));
    }

    /**
     * <p>Constructor for AbstractModelResource.</p>
     *
     * @param modelType a {@link java.lang.Class} object.
     * @param server    a {@link io.ebeaninternal.api.SpiEbeanServer} object.
     */
    public ModelResource(Class<MODEL> modelType, SpiEbeanServer server) {
        super(modelType, server);
    }

    /**
     * Insert a model.
     * <p>
     * success status 201
     *
     * @param model the model to insert
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     */
    @POST
    public final Response insert(@NotNull @Valid final MODEL model) throws Exception {
        return super.insert(model);
    }

    /**
     * replace or insert a model.
     * <p>
     * success replace status 204
     * <br>
     * fail replace but inserted status 201
     *
     * @param id    the unique id of the model
     * @param model the model to update
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     */
    @PUT
    @Path("{id}")
    public final Response replace(@PathParam("id") final URI_ID id, @NotNull @Valid final MODEL model) throws Exception {
        return super.replace(id, model);
    }

    /**
     * Update a model items.
     * <p>
     * success status 204
     * <br>
     * fail status 422
     *
     * @param id    the unique id of the model
     * @param model the model to update
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     */
    @PATCH
    @Path("{id}")
    public final Response patch(@PathParam("id") final URI_ID id, @NotNull final MODEL model) throws Exception {
        return super.patch(id, model);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delete multiple model using Id's from the Matrix.
     * <p>
     * success status 200
     * <br>
     * fail status 404
     * <br>
     * logical delete status 202
     */
    @DELETE
    @Path("{ids}")
    public final Response deleteMultiple(@NotNull @PathParam("ids") URI_ID id,
                                         @NotNull @PathParam("ids") final PathSegment ids,
                                         @QueryParam("permanent") final boolean permanent) throws Exception {
        return super.deleteMultiple(id, ids, permanent);
    }

    /**
     * {@inheritDoc}
     *
     * Find a model or model list given its Ids.
     */
    @GET
    @Path("{ids}")
    public final Response findByIds(@NotNull @PathParam("ids") URI_ID id,
                                    @NotNull @PathParam("ids") final PathSegment ids,
                                    @QueryParam("include_deleted") boolean includeDeleted) throws Exception {
        return super.findByIds(id, ids, includeDeleted);
    }

    /**
     * {@inheritDoc}
     *
     * Find the beans for this beanType.
     * <p>
     * This can use URL query parameters such as order and maxrows to configure
     * the query.
     * </p>
     */
    @GET
    public final Response find(@QueryParam("include_deleted") boolean includeDeleted) throws Exception {
        return super.find(includeDeleted);
    }

    /**
     * {@inheritDoc}
     *
     *
     *  find model history between start to end timestamp versions
     *
     *  need model mark {@code @History}
     */
    @GET
    @Path("{id}/history/{start: " + DATE_REGEX + "}-{end: " + DATE_REGEX + "}")
    public Response fetchHistory(@PathParam("id") URI_ID id,
                                 @PathParam("start") Timestamp start,
                                 @PathParam("end") Timestamp end) throws Exception {
        return super.fetchHistory(id, start, end);
    }

    /**
     * find history
     *
     * @param id model id
     * @return history models
     * @throws java.lang.Exception any error
     */
    @GET
    @Path("{id}/history")
    public Response fetchHistory(@PathParam("id") URI_ID id) throws Exception {
        return super.fetchHistory(id);
    }

    /**
     * {@inheritDoc}
     *
     * find history as of timestamp
     */
    @GET
    @Path("{id}/history/{asOf: " + DATE_REGEX + "}")//todo 正则匹配时间格式
    public Response fetchHistoryAsOf(@PathParam("id") URI_ID id, @PathParam("asOf") Timestamp asOf) throws Exception {
        return super.fetchHistoryAsOf(id, asOf);
    }
}
