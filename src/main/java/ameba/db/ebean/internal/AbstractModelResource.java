package ameba.db.ebean.internal;

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
 * @author icode
 */
public abstract class AbstractModelResource<ID, M extends Model> extends ModelResourceStructure<ID, M> {
    public AbstractModelResource(Class<M> modelType) {
        this(modelType, (SpiEbeanServer) Ebean.getServer(null));
    }

    public AbstractModelResource(Class<M> modelType, SpiEbeanServer server) {
        super(modelType, server);
    }

    /**
     * Insert a model.
     * <p/>
     * success status 201
     *
     * @param model the model to insert
     */
    @POST
    @SuppressWarnings("unchecked")
    public final Response insert(@NotNull @Valid final M model) throws Exception {
        return super.insert(model);
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
    public final Response replace(@PathParam("id") final ID id, @NotNull @Valid final M model) throws Exception {
        return super.replace(id, model);
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
    public final Response patch(@PathParam("id") final ID id, @NotNull final M model) throws Exception {
        return super.patch(id, model);
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
        return super.deleteMultiple(ids);
    }

    /**
     * Find a model given its Id.
     *
     * @param id the id of the model.
     */
    @GET
    @Path("{id}")
    public final Response findById(@NotNull @PathParam("id") final ID id) throws Exception {
        return super.findById(id);
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
        return super.find();
    }
}
