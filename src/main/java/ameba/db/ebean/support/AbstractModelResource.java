package ameba.db.ebean.support;

import ameba.core.ws.rs.PATCH;
import ameba.db.model.Model;
import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.api.SpiEbeanServer;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

/**
 * <p>Abstract AbstractModelResource class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public abstract class AbstractModelResource<ID, M extends Model> extends ModelResourceStructure<ID, M> {
    /**
     * <p>Constructor for AbstractModelResource.</p>
     *
     * @param modelType a {@link java.lang.Class} object.
     */
    public AbstractModelResource(Class<M> modelType) {
        this(modelType, (SpiEbeanServer) Ebean.getServer(null));
    }

    /**
     * <p>Constructor for AbstractModelResource.</p>
     *
     * @param modelType a {@link java.lang.Class} object.
     * @param server    a {@link com.avaje.ebeaninternal.api.SpiEbeanServer} object.
     */
    public AbstractModelResource(Class<M> modelType, SpiEbeanServer server) {
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
    public final Response insert(@NotNull @Valid final M model) throws Exception {
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
    public final Response replace(@PathParam("id") final ID id, @NotNull @Valid final M model) throws Exception {
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
    public final Response patch(@PathParam("id") final ID id, @NotNull final M model) throws Exception {
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
    public final Response deleteMultiple(@NotNull @PathParam("ids") ID id,
                                         @NotNull @PathParam("ids") final PathSegment ids) throws Exception {
        return super.deleteMultiple(id, ids);
    }

    /**
     * Find a model or model list given its Ids.
     *
     * @param ids the id of the model.
     * @return a {@link javax.ws.rs.core.Response} object.
     * @throws java.lang.Exception if any.
     */
    @GET
    @Path("{ids}")
    public final Response findByIds(@NotNull @PathParam("ids") ID id,
                                    @NotNull @PathParam("ids") final PathSegment ids) throws Exception {
        return super.findByIds(id, ids);
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
     */
    @GET
    public final Response find() throws Exception {
        return super.find();
    }
}
