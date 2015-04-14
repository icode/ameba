package ameba.mvc.assets;

import ameba.util.MimeType;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.net.URI;

/**
 * <p>AssetsResource class.</p>
 *
 * @author ICode
 * @since 13-8-17 下午2:55
 */
@Path("assets")
@Singleton
public class AssetsResource {

    /**
     * <p>getResource.</p>
     *
     * @param file    a {@link java.lang.String} object.
     * @param uriInfo a {@link javax.ws.rs.core.UriInfo} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @GET
    @Path("{file:.*}")
    public Response getResource(@PathParam("file") String file, @Context UriInfo uriInfo) {

        InputStream in = AssetsFeature.findAsset(uriInfo.getPath().replace(file, ""), file);

        if (in == null)
            return Response.status(404).build();

        Response.ResponseBuilder builder = Response.ok(in);

        String path = URI.create(file).getPath();

        int dot = path.lastIndexOf('.');

        if (dot > 0) {
            String ext = path.substring(dot + 1);
            String ct = MimeType.get(ext);
            if (ct != null) {
                builder.type(ct);
            }
        } else {
            builder.type(MimeType.get("html"));
        }

        return builder.build();
    }
}
