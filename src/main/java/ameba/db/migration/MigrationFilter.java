package ameba.db.migration;

import ameba.core.Application;
import ameba.db.migration.models.MigrationInfo;
import ameba.exception.AmebaException;
import ameba.i18n.Messages;
import ameba.util.IOUtils;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.flywaydb.core.Flyway;
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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static ameba.db.migration.MigrationFeature.uri;

/**
 * @author icode
 */
@PreMatching
@Priority(Integer.MIN_VALUE)
@Singleton
public class MigrationFilter implements ContainerRequestFilter {
    private static final String MIGRATION_HTML;
    private static final String FAVICON_ICO = "favicon.ico";

    static {
        try {
            MIGRATION_HTML = IOUtils.readFromResource("db/migration/migration.html");
        } catch (IOException e) {
            throw new AmebaException(e);
        }
    }

    @Inject
    private Application.Mode mode;
    private boolean ran = false;
    private boolean rewriteMethod = false;
    private Map<String, Migration> migrations;

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        if (!ran) {
            String path = req.getUriInfo().getPath();
            if (path.equals(FAVICON_ICO) || path.endsWith("/" + FAVICON_ICO)) {
                return;
            }
            if (path.equals(uri)
                    && req.getMethod().equalsIgnoreCase(HttpMethod.POST)
                    && migrations.size() > 0) {
                migrate((ContainerRequest) req);
            } else {
                migrations = Maps.newHashMap();
                for (String dbName : MigrationFeature.migrationMap.keySet()) {
                    Migration migration = MigrationFeature.migrationMap.get(dbName);
                    if (migration.hasChanged()) {
                        migrations.put(dbName, migration);
                    }
                }
                if (migrations.size() > 0) {
                    if (mode.isDev() || path.equals(uri) && req.getMethod().equals(HttpMethod.GET)) {
                        Map<String, String> valuesMap = Maps.newHashMap();
                        valuesMap.put("pageTitle", Messages.get("view.app.database.migration.page.title"));
                        valuesMap.put("title", Messages.get("view.app.database.migration.title"));
                        valuesMap.put("migrationUri", "/" + uri);
                        valuesMap.put("description", Messages.get("view.app.database.migration.description"));
                        valuesMap.put("applyButtonText", Messages.get("view.app.database.migration.apply.button"));
                        valuesMap.put("namePlaceholder", Messages.get("view.app.database.migration.name.placeholder"));
                        StringBuilder tabs = new StringBuilder();
                        StringBuilder diffs = new StringBuilder();

                        StrSubstitutor sub = new StrSubstitutor(valuesMap);
                        int i = 0;
                        for (String dbName : migrations.keySet()) {
                            Migration migration = migrations.get(dbName);
                            MigrationInfo info = migration.generate();
                            Flyway flyway = MigrationFeature.getMigration(dbName);
                            boolean hasTable;
                            try (Connection connection = flyway.getDataSource().getConnection()) {
                                hasTable = connection.getMetaData().getTables(null, null, flyway.getTable(), null).next();
                            } catch (SQLException e) {
                                throw new AmebaException(e);
                            }

                            tabs.append("<li i=\"").append(i).append("\" class=\"db-name\">").append(dbName).append("</li>");
                            diffs.append("<div class=\"diff\"><h2>");
                            if (hasTable) {
                                diffs.append(Messages.get("view.app.database.migration.subTitle"));
                            } else {
                                diffs.append(Messages.get("view.app.database.migration.baseline.subTitle"));
                            }
                            diffs.append("</h2><pre>")
                                    .append(info.getDiffDdl())
                                    .append("</pre></div>");
                            i++;
                        }
                        valuesMap.put("dbNames", tabs.toString());
                        valuesMap.put("diffs", diffs.toString());
                        req.abortWith(
                                Response.serverError()
                                        .entity(sub.replace(MIGRATION_HTML))
                                        .type(MediaType.TEXT_HTML_TYPE)
                                        .build()
                        );
                    }
                } else if (!mode.isDev()) {
                    ran = true;
                }
            }
        } else if (rewriteMethod) {
            if (HttpMethod.POST.equals(req.getMethod())) {
                req.setMethod(HttpMethod.GET);
                rewriteMethod = false;
            }
        }
    }

    private void migrate(ContainerRequest req) {
        MultivaluedMap<String, String> params = req.readEntity(Form.class).asMap();

        String generatedName = (mode.isDev() ? "dev " : "") + "migrate";
        String name = params.getFirst("name");
        if (StringUtils.isNotBlank(name)) {
            generatedName = name;
        }
        for (String dbName : migrations.keySet()) {
            Migration migration = migrations.get(dbName);
            MigrationInfo info = migration.generate();
            info.setName(generatedName);
            Flyway flyway = MigrationFeature.getMigration(dbName);
            flyway.setBaselineDescription(info.getName());
            flyway.setBaselineVersionAsString(info.getRevision());

            flyway.migrate();
            migration.persist();
            migration.reset();
            ran = true;
            rewriteMethod = true;
        }
        String referer = req.getHeaders().getFirst("Referer");
        if (StringUtils.isBlank(referer)) {
            referer = "/";
        }
        req.abortWith(Response.temporaryRedirect(URI.create(referer)).build());
    }
}
