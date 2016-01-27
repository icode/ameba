package ameba.db.migration.resources;

import ameba.core.Application;
import ameba.db.DataSourceManager;
import ameba.db.migration.Migration;
import ameba.db.migration.MigrationFeature;
import ameba.db.migration.models.ScriptInfo;
import ameba.exception.AmebaException;
import ameba.i18n.Messages;
import ameba.util.IOUtils;
import ameba.util.Result;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.glassfish.hk2.api.ServiceLocator;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author icode
 */
@Path(MigrationResource.MIGRATION_BASE_URI)
@Singleton
public class MigrationResource {
    public static final String MIGRATION_BASE_URI = "/@db/migration/";
    private static final String MIGRATION_HTML;

    static {
        try {
            MIGRATION_HTML = IOUtils.readFromResource("db/migration/migration.html");
        } catch (IOException e) {
            throw new AmebaException(e);
        }
    }

    @Inject
    private ServiceLocator locator;
    @Inject
    private Application.Mode mode;
    @Inject
    private Application application;
    private Map<String, MigrationFail> failMigrations;

    @GET
    public Map<String, MigrationInfo[]> listInfo() {
        Map<String, MigrationInfo[]> infoMap = Maps.newLinkedHashMap();
        for (String dbName : DataSourceManager.getDataSourceNames()) {
            Flyway flyway = locator.getService(Flyway.class, dbName);
            infoMap.put(dbName, flyway.info().all());
        }
        return infoMap;
    }

    @GET
    @Path("{dbName}/{revision}")
    public Response info(@PathParam("dbName") String dbName, @PathParam("revision") String revision) {
        Flyway flyway = locator.getService(Flyway.class, dbName);
        if (flyway == null)
            throw new NotFoundException();
        Object entity = null;
        switch (revision) {
            case "current":
                entity = flyway.info().current();
                break;
            case "pending":
                entity = flyway.info().pending();
                break;
            case "applied":
                entity = flyway.info().applied();
                break;
            case "first": {
                MigrationInfo[] migrationInfos = flyway.info().all();
                if (migrationInfos.length == 0)
                    throw new NotFoundException();
                entity = migrationInfos[0];
                break;
            }
            case "resolved":
                entity = resolved(flyway.info().all());
                break;
            case "failed":
                entity = failed(flyway.info().all());
                break;
            case "future":
                entity = future(flyway.info().all());
                break;
            case "latest": {
                MigrationInfo[] migrationInfos = flyway.info().all();
                if (migrationInfos.length == 0)
                    throw new NotFoundException();
                entity = migrationInfos[migrationInfos.length - 1];
                break;
            }
            default:
                for (MigrationInfo info : flyway.info().all()) {
                    if (revision.equalsIgnoreCase(info.getVersion().getVersion())) {
                        entity = info;
                        break;
                    }
                }
        }
        if (entity == null)
            throw new NotFoundException();
        return Response.ok(entity).build();
    }

    @GET
    @Path("scripts")
    public Map<String, List<ScriptInfo>> scripts() {
        Map<String, List<ScriptInfo>> infoMap = Maps.newLinkedHashMap();
        for (String dbName : DataSourceManager.getDataSourceNames()) {
            infoMap.put(dbName, scripts(dbName));
        }
        return infoMap;
    }

    @GET
    @Path("{dbName}/scripts")
    public List<ScriptInfo> scripts(@PathParam("dbName") String dbName) {
        Migration migration = locator.getService(Migration.class, dbName);
        if (migration == null)
            throw new NotFoundException();
        return migration.allScript();
    }

    @GET
    @Path("{dbName}/scripts/{revision}")
    public ScriptInfo script(@PathParam("dbName") String dbName, @PathParam("revision") String revision) {
        Migration migration = locator.getService(Migration.class, dbName);
        if (migration == null)
            throw new NotFoundException();
        return migration.getScript(revision);
    }


    /////////////////////////////////////////
    ////////   Database Migration   /////////
    /////////////////////////////////////////

    @GET
    @Path("{uuid}")// uuid or dbName
    public Response migrateView(@PathParam("uuid") String uuid) {
        try {
            MigrationFeature.checkMigrationId(uuid);
        } catch (NotFoundException e) {
            Flyway flyway = locator.getService(Flyway.class, uuid);
            if (flyway == null)
                throw new NotFoundException();
            return Response.ok(flyway.info().all()).build();
        }

        Map<String, Migration> migrations = getMigrations();
        Map<String, String> model = buildPageModel(MigrationType.MIGRATE, uuid);

        String desc = model.get("description")
                .concat("&nbsp; - &nbsp;<input name=\"description\" placeholder=\"" +
                        Messages.get("view.app.database.migrate.description.placeholder")
                        + "\">");
        model.put("description", desc);

        StringBuilder tabs = new StringBuilder();
        StringBuilder diffs = new StringBuilder();

        StrSubstitutor sub = new StrSubstitutor(model);
        int i = 0;
        for (String dbName : migrations.keySet()) {
            Migration migration = migrations.get(dbName);
            ScriptInfo info = migration.generate();
            Flyway flyway = locator.getService(Flyway.class, dbName);
            boolean hasTable;
            try (Connection connection = flyway.getDataSource().getConnection()) {
                hasTable = connection.getMetaData().getTables(null, null, flyway.getTable(), null).next();
            } catch (SQLException e) {
                throw new AmebaException(e);
            }

            tabs.append("<li i=\"").append(i).append("\" class=\"db-name\">").append(dbName).append("</li>");
            diffs.append("<div class=\"diff\"><h2>");
            if (hasTable) {
                diffs.append(Messages.get("view.app.database.migrate.subTitle"));
            } else {
                diffs.append(Messages.get("view.app.database.migrate.baseline.subTitle"));
            }
            diffs.append("</h2><pre>")
                    .append(info.getDiffDdl())
                    .append("</pre></div>");
            i++;
        }
        model.put("dbNames", tabs.toString());
        model.put("diffs", diffs.toString());

        return Response.ok(sub.replace(MIGRATION_HTML))
                .type(MediaType.TEXT_HTML_TYPE)
                .build();
    }

    @POST
    @Path("{uuid}")
    public Result migrate(@FormParam("description") String desc,
                          @PathParam("uuid") String uuid) {
        MigrationFeature.checkMigrationId(uuid);
        String generatedDesc = (mode.isDev() ? "dev " : "") + "migrate";
        if (StringUtils.isNotBlank(desc)) {
            generatedDesc = desc;
        }

        Map<String, Migration> migrations = getMigrations();
        for (String dbName : migrations.keySet()) {
            Migration migration = migrations.get(dbName);
            ScriptInfo info = migration.generate();
            info.setDescription(generatedDesc);
            Flyway flyway = locator.getService(Flyway.class, dbName);
            flyway.setBaselineDescription(info.getDescription());
            flyway.setBaselineVersionAsString(info.getRevision());

            try {
                flyway.migrate();
                migration.persist();
                migration.reset();
            } catch (Throwable err) {
                if (failMigrations == null) {
                    synchronized (this) {
                        if (failMigrations == null) {
                            failMigrations = Maps.newHashMap();
                        }
                    }
                }
                failMigrations.put(dbName, MigrationFail.create(flyway, err, migration));
            }
        }
        if (failMigrations.isEmpty()) {
            return Result.success();
        } else {
            return Result.failure();
        }
    }

    @GET
    @Path("{uuid}/repair")
    @Produces("text/html")
    public String repairView(@PathParam("uuid") String uuid) {
        MigrationFeature.checkMigrationId(uuid);
        if (failMigrations.isEmpty()) {
            throw new NotFoundException();
        }

        //REPAIR
        Map<String, String> model = buildPageModel(MigrationType.REPAIR, uuid);
        StrSubstitutor sub = new StrSubstitutor(model);
        model.put("migrationUri", model.get("migrationUri") + "/repair");

        StringBuilder tabs = new StringBuilder();
        StringBuilder diffs = new StringBuilder();

        int i = 0;
        for (Map.Entry<String, MigrationFail> failEntry : failMigrations.entrySet()) {
            String dbName = failEntry.getKey();
            MigrationFail fail = failEntry.getValue();
            Migration migration = fail.migration;
            ScriptInfo info = migration.generate();

            tabs.append("<li i=\"").append(i).append("\" class=\"db-name\">").append(dbName).append("</li>");
            diffs.append("<div class=\"diff\"><h2>");
            diffs.append(Messages.get("view.app.database.repair.subTitle", fail.throwable.getLocalizedMessage()));
            diffs.append("</h2><pre>")
                    .append(info.getDiffDdl())
                    .append("</pre></div>");
            i++;
        }
        model.put("dbNames", tabs.toString());
        model.put("diffs", diffs.toString());

        return sub.replace(MIGRATION_HTML);
    }

    @POST
    @Path("{uuid}/repair")
    public Result repair(@PathParam("uuid") String uuid) {
        MigrationFeature.checkMigrationId(uuid);
        if (failMigrations.isEmpty()) {
            throw new NotFoundException();
        }
        for (String dbName : failMigrations.keySet()) {
            MigrationFail fail = failMigrations.get(dbName);
            fail.flyway.repair();
            fail.migration.persist();
            fail.migration.reset();
        }
        failMigrations.clear();
        return Result.success();
    }

    /////////////////////////////////////////
    ////////       inner utils      /////////
    /////////////////////////////////////////

    private Map<String, String> buildPageModel(MigrationType type, String uuid) {
        Map<String, String> valuesMap = Maps.newHashMap();
        String key = type.key();
        valuesMap.put("pageTitle", Messages.get("view.app.database." + key + ".page.title"));
        valuesMap.put("title", Messages.get("view.app.database." + key + ".title"));
        valuesMap.put("migrationUri", MIGRATION_BASE_URI + uuid);
        valuesMap.put("description", Messages.get("view.app.database." + key + ".description"));
        valuesMap.put("applyButtonText", Messages.get("view.app.database." + key + ".apply.button"));
        return valuesMap;
    }

    private Map<String, Migration> getMigrations() {
        Map<String, Migration> migrations = Maps.newHashMap();
        Map<String, Object> properties = application.getProperties();
        for (String dbName : DataSourceManager.getDataSourceNames()) {
            if (!"false".equals(properties.get("db." + dbName + ".migration.enabled"))) {
                Migration migration = locator.getService(Migration.class, dbName);
                if (migration.hasChanged()) {
                    migrations.put(dbName, migration);
                }
            }
        }
        if (migrations.isEmpty()) {
            throw new NotFoundException();
        }
        return migrations;
    }

    /**
     * Retrieves the full set of infos about the migrations resolved on the classpath.
     *
     * @return The resolved migrations. An empty array if none.
     */
    private List<MigrationInfo> resolved(MigrationInfo[] migrationInfos) {
        if (migrationInfos.length == 0)
            throw new NotFoundException();
        List<MigrationInfo> resolvedMigrations = Lists.newArrayList();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isResolved()) {
                resolvedMigrations.add(migrationInfo);
            }
        }

        return resolvedMigrations;
    }

    /**
     * Retrieves the full set of infos about the migrations that failed.
     *
     * @return The failed migrations. An empty array if none.
     */
    private List<MigrationInfo> failed(MigrationInfo[] migrationInfos) {
        if (migrationInfos.length == 0)
            throw new NotFoundException();
        List<MigrationInfo> failedMigrations = Lists.newArrayList();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if (migrationInfo.getState().isFailed()) {
                failedMigrations.add(migrationInfo);
            }
        }

        return failedMigrations;
    }

    /**
     * Retrieves the full set of infos about future migrations applied to the DB.
     *
     * @return The future migrations. An empty array if none.
     */
    private List<MigrationInfo> future(MigrationInfo[] migrationInfos) {
        if (migrationInfos.length == 0)
            throw new NotFoundException();
        List<MigrationInfo> futureMigrations = Lists.newArrayList();
        for (MigrationInfo migrationInfo : migrationInfos) {
            if ((migrationInfo.getState() == MigrationState.FUTURE_SUCCESS)
                    || (migrationInfo.getState() == MigrationState.FUTURE_FAILED)) {
                futureMigrations.add(migrationInfo);
            }
        }

        return futureMigrations;
    }

    public Map<String, MigrationFail> getFailMigrations() {
        return failMigrations;
    }

    private enum MigrationType {
        MIGRATE, REPAIR;

        public String key() {
            return name().toLowerCase();
        }
    }

    private static class MigrationFail {
        private Flyway flyway;
        private Throwable throwable;
        private Migration migration;

        static MigrationFail create(Flyway flyway, Throwable throwable, Migration migration) {
            MigrationFail fail = new MigrationFail();
            fail.flyway = flyway;
            fail.throwable = throwable;
            fail.migration = migration;
            return fail;
        }
    }
}
