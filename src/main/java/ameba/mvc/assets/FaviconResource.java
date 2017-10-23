package ameba.mvc.assets;

import ameba.util.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.ContainerRequest;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author icode
 */
@Path("favicon.ico")
@Singleton
public class FaviconResource extends AbstractAssetsResource {

    private static final String DEFAULT_ICO = "ameba/favicon.ico";

    @Named("resource.favicon.ico")
    private String faviconIco;


    @GET
    public Response get(@Context ContainerRequest request) throws IOException, URISyntaxException {
        URL url = null;
        if (StringUtils.isNotBlank(faviconIco)) {
            url = IOUtils.getResource(faviconIco);
        }

        if (url == null) {
            url = IOUtils.getResource(DEFAULT_ICO);
        }

        return assets(url, request);
    }
}
