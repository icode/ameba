package ameba.db.migration;

import ameba.core.Application;
import ameba.db.migration.models.MigrationInfo;
import ameba.exception.AmebaException;
import ameba.i18n.Messages;
import ameba.util.IOUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.flywaydb.core.Flyway;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static ameba.db.migration.MigrationFeature.migrationMap;
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
    private Application application;
    @Inject
    private Application.Mode mode;
    private boolean ran = false;
    private boolean rewriteMethod = false;
    private List<Migration> migrations;

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
                migrate(req);
            } else {
                migrations = Lists.newArrayList();
                for (Migration migration : migrationMap.values()) {
                    if (migration.hasChanged()) {
                        migrations.add(migration);
                    }
                }
                if (migrations.size() > 0) {
                    if (mode.isDev() || path.equals(uri) && req.getMethod().equals(HttpMethod.GET)) {
                        // todo 需要多个选项卡展示内容
                        Migration migration = migrations.get(0);
                        MigrationInfo info = migration.generate().get(0);
                        String dbName = info.getDatabaseName();
                        Flyway flyway = MigrationFeature.getMigration(dbName);
                        boolean hasTable;
                        try (Connection connection = flyway.getDataSource().getConnection()) {
                            hasTable = connection.getMetaData().getTables(null, null, flyway.getTable(), null).next();
                        } catch (SQLException e) {
                            throw new AmebaException(e);
                        }
                        Map<String, String> valuesMap = Maps.newHashMap();

                        valuesMap.put("migrationUri", "/" + uri);
                        valuesMap.put("pageTitle", Messages.get("view.app.database.migration.page.title"));
                        valuesMap.put("title", Messages.get("view.app.database.migration.title", dbName));
                        if (hasTable) {
                            valuesMap.put("subTitle", Messages.get("view.app.database.migration.subTitle"));
                            valuesMap.put("description", Messages.get("view.app.database.migration.description"));
                            valuesMap.put("applyButtonText", Messages.get("view.app.database.migration.apply.button"));
                        } else {
                            valuesMap.put("applyButtonText", Messages.get("view.app.database.migration.baseline.button"));
                            valuesMap.put("subTitle", Messages.get("view.app.database.migration.baseline.subTitle"));
                            valuesMap.put("description", Messages.get("view.app.database.migration.baseline.description"));
                        }
                        valuesMap.put("applyDdl", info.getDiffDdl());

                        StrSubstitutor sub = new StrSubstitutor(valuesMap);
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

    private void migrate(ContainerRequestContext req) {
        Map<String, Object> properties = application.getProperties();
        //todo 这里应该是特性名称或着简单的描述,在dev模式直接设置
        //todo 发布和测试模式应该让用户输入
        //todo {dbName} {generatedName} from web ui
        //todo 判断flyway.getTable()是否存在，不存在则不更新数据库表结构，创建migrationInfo表，增加migrationInfo信息，调用flyway.baseline()

        for (String dbName : migrationMap.keySet()) {
            if (!"false".equals(properties.get("db." + dbName + ".migration.enabled"))) {
                Migration migration = migrationMap.get(dbName);
                String generatedName = (mode.isDev() ? "dev " : "") + "migrate";
                MigrationInfo info = migration.generate().get(0);
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
        }
        String referer = req.getHeaders().getFirst("Referer");
        if (StringUtils.isBlank(referer)) {
            referer = "/";
        }
        req.abortWith(Response.temporaryRedirect(URI.create(referer)).build());
    }
}
