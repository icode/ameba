package ameba.mvc.assets;

import ameba.container.server.Request;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * Assets Resource
 *
 * @author icode
 */
@Path("assets")
@Singleton
public class AssetsResource extends AbstractAssetsResource {
    @GET
    @Path("{file:.*}")
    public Response getResource(@PathParam("file") String fileName,
                                @Context ContainerRequest request,
                                @Context ExtendedUriInfo uriInfo) throws URISyntaxException, IOException {

        if (!fileName.startsWith("/")) {
            fileName = "/" + fileName;
        }
        URI rawUri = ((Request) request).getRawReqeustUri();
        String reqPath = rawUri.getPath();
        int pathFileIndex = reqPath.lastIndexOf("/");
        String reqFileName = reqPath;
        if (pathFileIndex != -1) {
            reqFileName = reqPath.substring(pathFileIndex);
        }
        if (!fileName.endsWith(reqFileName)) {
            if (pathFileIndex != -1) {
                fileName = fileName.substring(0, fileName.lastIndexOf("/")) + reqFileName;
            } else {
                fileName = reqFileName;
            }
        }

        List<String> uris = uriInfo.getMatchedURIs(true);
        String mapName = uris.get(uris.size() - 1);

        URL url = AssetsFeature.lookupAsset(mapName, fileName);

        return assets(url, request);
    }
}
