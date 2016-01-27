package ameba.db.migration;

import ameba.core.Application;
import ameba.db.DataSourceManager;
import ameba.db.migration.resources.MigrationResource;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ContainerRequest;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * @author icode
 */
@PreMatching
@Priority(Integer.MIN_VALUE)
@Singleton
public class MigrationFilter implements ContainerRequestFilter {
    private static final String FAVICON_ICO = "favicon.ico";
    private static final String MIGRATION_BASE_URI = "/" + MigrationResource.MIGRATION_BASE_URI + "/";
    private boolean ran = false;
    @Inject
    private Application.Mode mode;
    @Inject
    private Application application;
    @Inject
    private ServiceLocator locator;
    @Inject
    private MigrationResource resource;

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        if (!ran) {
            UriInfo uriInfo = req.getUriInfo();
            String path = uriInfo.getPath();
            if (path.equals(FAVICON_ICO) || path.endsWith("/" + FAVICON_ICO)) {
                return;
            }
            path = "/" + path;
            String migrationUri = MIGRATION_BASE_URI + MigrationFeature.getMigrationId();
            String repairUri = migrationUri + "/repair";
            if ((path.equals(migrationUri) || path.equals(repairUri)) && HttpMethod.GET.equals(req.getMethod())) {
                return;
            } else if (path.equals(migrationUri) && HttpMethod.POST.equals(req.getMethod())) {
                String desc = ((ContainerRequest) req).readEntity(Form.class).asMap().getFirst("description");
                req.abortWith(
                        Response.ok().entity(resource.migrate(desc, MigrationFeature.getMigrationId())).build()
                );
                return;
            } else if (path.equals(repairUri) && HttpMethod.POST.equals(req.getMethod())) {
                req.abortWith(
                        Response.ok().entity(resource.repair(MigrationFeature.getMigrationId())).build()
                );
                return;
            } else if (!mode.isDev() && path.equals(migrationUri) && HttpMethod.GET.equals(req.getMethod())) {
                req.abortWith(
                        Response.ok()
                                .entity(resource.migrateView(MigrationFeature.getMigrationId()))
                                .build()
                );
                return;
            } else if (!mode.isDev() && path.equals(repairUri) && HttpMethod.GET.equals(req.getMethod())) {
                req.abortWith(
                        Response.ok()
                                .entity(resource.repairView(MigrationFeature.getMigrationId()))
                                .type(MediaType.TEXT_HTML_TYPE)
                                .build()
                );
                return;
            }

            Map<String, Object> properties = application.getProperties();
            Map failMigrations = resource.getFailMigrations();
            if (failMigrations != null && !failMigrations.isEmpty()) {
                req.abortWith(Response.temporaryRedirect(URI.create(repairUri)).build());
                return;
            }
            if (mode.isDev()) {
                for (String dbName : DataSourceManager.getDataSourceNames()) {
                    if (!"false".equals(properties.get("db." + dbName + ".migration.enabled"))) {
                        Migration migration = locator.getService(Migration.class, dbName);
                        if (migration.hasChanged()) {
                            req.abortWith(Response.fromResponse(
                                    resource.migrateView(MigrationFeature.getMigrationId())
                            ).status(500).build());
                            return;
                        }
                    }
                }
            }

            if (!mode.isDev()) {
                ran = true;
            }
        }
    }
}
