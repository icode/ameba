package ameba.db.migration;

import ameba.db.DataSourceManager;
import ameba.exception.AmebaException;
import ameba.util.ClassUtils;
import liquibase.CatalogAndSchema;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.change.CheckSum;
import liquibase.changelog.*;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changelog.visitor.ChangeLogSyncListener;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.structure.DatabaseObject;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * database migrant
 *
 * @author icode
 * @since 0.1.6e
 */
public class DatabaseMigrant {

    private final String databaseName;
    private final String changeLogFile;
    private final String defaultSchemaName;
    private final String contexts;
    private final String labels;
    private final Map<String, Object> changeLogParams;
    private CompositeResourceAccessor accessor;
    private Database database;
    private Connection dataSourceConnection;
    private Liquibase liquibase;

    /**
     * <p>Constructor for DatabaseMigrant.</p>
     *
     * @param databaseName      a {@link java.lang.String} object.
     * @param changeLogFile     a {@link java.lang.String} object.
     * @param defaultSchemaName a {@link java.lang.String} object.
     * @param contexts          a {@link java.lang.String} object.
     * @param labels            a {@link java.lang.String} object.
     * @param changeLogParams   a {@link java.util.Map} object.
     */
    public DatabaseMigrant(String databaseName,
                           String changeLogFile,
                           String defaultSchemaName,
                           String contexts,
                           String labels,
                           Map<String, Object> changeLogParams) {
        this.databaseName = databaseName;
        this.changeLogFile = changeLogFile;
        this.defaultSchemaName = defaultSchemaName;
        this.contexts = contexts;
        this.labels = labels;
        this.changeLogParams = changeLogParams;
    }

    /**
     * <p>getDataSource.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link javax.sql.DataSource} object.
     */
    protected DataSource getDataSource(String name) {
        return DataSourceManager.getDataSource(name);
    }

    /**
     * <p>Getter for the field <code>changeLogParams</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Object> getChangeLogParams() {
        return changeLogParams;
    }

    /**
     * <p>Getter for the field <code>defaultSchemaName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultSchemaName() {
        return defaultSchemaName;
    }

    /**
     * <p>Getter for the field <code>changeLogFile</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getChangeLogFile() {
        return changeLogFile;
    }

    /**
     * <p>Getter for the field <code>databaseName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * <p>Getter for the field <code>contexts</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getContexts() {
        return contexts;
    }

    /**
     * <p>Getter for the field <code>labels</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabels() {
        return labels;
    }

    /**
     * <p>Getter for the field <code>liquibase</code>.</p>
     *
     * @return a {@link liquibase.Liquibase} object.
     */
    public Liquibase getLiquibase() {
        if (liquibase == null) {
            try {
                liquibase = new Liquibase(getChangeLogFile(), getResourceAccessor(), getDatabase());
            } catch (LiquibaseException e) {
                throw new AmebaException(e);
            }
            for (Map.Entry<String, Object> entry : changeLogParams.entrySet()) {
                liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
            }
        }
        return liquibase;
    }

    /**
     * <p>clear.</p>
     */
    protected void clear() {
        if (database != null) {
            try {
                database.close();
            } catch (DatabaseException e) {
                // no op
            }
        }
        if (dataSourceConnection != null) {
            try {
                dataSourceConnection.close();
            } catch (SQLException e) {
                // no op
            }
        }
        accessor = null;
        database = null;
        liquibase = null;
        dataSourceConnection = null;
    }

    /**
     * <p>Getter for the field <code>dataSourceConnection</code>.</p>
     *
     * @return a {@link java.sql.Connection} object.
     * @throws java.sql.SQLException if any.
     */
    protected Connection getDataSourceConnection() throws SQLException {
        if (dataSourceConnection == null) {
            dataSourceConnection = getDataSource(getDatabaseName()).getConnection();
        }
        return dataSourceConnection;
    }

    /**
     * <p>Getter for the field <code>database</code>.</p>
     *
     * @return a {@link liquibase.database.Database} object.
     */
    protected Database getDatabase() {
        if (database == null) {
            try {
                JdbcConnection connection = new JdbcConnection(getDataSourceConnection());
                database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
                database.setDefaultSchemaName(getDefaultSchemaName());
            } catch (Exception e) {
                throw new AmebaException(e);
            }
        }
        return database;
    }

    /**
     * <p>getResourceAccessor.</p>
     *
     * @return a {@link liquibase.resource.CompositeResourceAccessor} object.
     */
    protected CompositeResourceAccessor getResourceAccessor() {
        if (accessor == null) {
            ResourceAccessor threadClFO = new ClassLoaderResourceAccessor(ClassUtils.getContextClassLoader());

            ResourceAccessor clFO = new ClassLoaderResourceAccessor();
            ResourceAccessor fsFO = new FileSystemResourceAccessor();

            accessor = new CompositeResourceAccessor(clFO, fsFO, threadClFO);
        }
        return accessor;
    }

    /**
     * <p>update.</p>
     *
     * @throws java.sql.SQLException                  if any.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void update() throws SQLException, LiquibaseException {
        try {
            Liquibase liquibase = getLiquibase();
            liquibase.update(new Contexts(getContexts()), new LabelExpression(getLabels()));
        } finally {
            clear();
        }
    }

    /**
     * <p>update.</p>
     *
     * @param output a {@link java.io.Writer} object.
     * @throws java.sql.SQLException                  if any.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void update(Writer output) throws SQLException, LiquibaseException {
        Liquibase liquibase = getLiquibase();
        liquibase.update(new Contexts(getContexts()), new LabelExpression(getLabels()), output);
    }

    /**
     * <p>generateDocumentation.</p>
     *
     * @param outputDirectory a {@link java.lang.String} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void generateDocumentation(String outputDirectory) throws LiquibaseException {
        getLiquibase().generateDocumentation(outputDirectory);
    }

    /**
     * <p>generateChangeLog.</p>
     *
     * @param catalogAndSchema a {@link liquibase.CatalogAndSchema} object.
     * @param changeLogWriter  a {@link liquibase.diff.output.changelog.DiffToChangeLog} object.
     * @param outputStream     a {@link java.io.PrintStream} object.
     * @param snapshotTypes    a {@link java.lang.Class} object.
     * @throws liquibase.exception.LiquibaseException         if any.
     * @throws java.io.IOException                            if any.
     * @throws javax.xml.parsers.ParserConfigurationException if any.
     */
    public void generateChangeLog(CatalogAndSchema catalogAndSchema, DiffToChangeLog changeLogWriter, PrintStream outputStream, Class<? extends DatabaseObject>... snapshotTypes) throws LiquibaseException, IOException, ParserConfigurationException {
        getLiquibase().generateChangeLog(catalogAndSchema, changeLogWriter, outputStream, snapshotTypes);
    }

    /**
     * <p>listUnexpectedChangeSets.</p>
     *
     * @return a {@link java.util.Collection} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public Collection<RanChangeSet> listUnexpectedChangeSets() throws LiquibaseException {
        return getLiquibase().listUnexpectedChangeSets(new Contexts(getContexts()), new LabelExpression(getLabels()));
    }

    /**
     * <p>rollback.</p>
     *
     * @param changesToRollback a int.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void rollback(int changesToRollback) throws LiquibaseException {
        getLiquibase().rollback(changesToRollback, getContexts());
    }

    /**
     * <p>updateTestingRollback.</p>
     *
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void updateTestingRollback() throws LiquibaseException {
        getLiquibase().updateTestingRollback(new Contexts(getContexts()), new LabelExpression(getLabels()));
    }

    /**
     * <p>setChangeExecListener.</p>
     *
     * @param listener a {@link liquibase.changelog.visitor.ChangeExecListener} object.
     * @throws java.sql.SQLException                  if any.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void setChangeExecListener(ChangeExecListener listener) throws SQLException, LiquibaseException {
        getLiquibase().setChangeExecListener(listener);
    }

    /**
     * <p>listUnrunChangeSets.</p>
     *
     * @return a {@link java.util.List} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public List<ChangeSet> listUnrunChangeSets() throws LiquibaseException {
        return getLiquibase().listUnrunChangeSets(new Contexts(getContexts()), new LabelExpression(getLabels()));
    }

    /**
     * <p>diff.</p>
     *
     * @param referenceDatabase a {@link liquibase.database.Database} object.
     * @param targetDatabase    a {@link liquibase.database.Database} object.
     * @param compareControl    a {@link liquibase.diff.compare.CompareControl} object.
     * @return a {@link liquibase.diff.DiffResult} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public DiffResult diff(Database referenceDatabase, Database targetDatabase, CompareControl compareControl) throws LiquibaseException {
        return getLiquibase().diff(referenceDatabase, targetDatabase, compareControl);
    }

    /**
     * <p>dropAll.</p>
     *
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void dropAll() throws LiquibaseException {
        getLiquibase().dropAll();
    }

    /**
     * <p>markNextChangeSetRan.</p>
     *
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void markNextChangeSetRan() throws LiquibaseException {
        getLiquibase().markNextChangeSetRan(getContexts());
    }

    /**
     * <p>getChangeLogParameters.</p>
     *
     * @return a {@link liquibase.changelog.ChangeLogParameters} object.
     * @throws java.sql.SQLException                  if any.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public ChangeLogParameters getChangeLogParameters() throws SQLException, LiquibaseException {
        return getLiquibase().getChangeLogParameters();
    }

    /**
     * <p>isSafeToRunUpdate.</p>
     *
     * @return a boolean.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public boolean isSafeToRunUpdate() throws LiquibaseException {
        return getLiquibase().isSafeToRunUpdate();
    }

    /**
     * <p>rollback.</p>
     *
     * @param dateToRollBackTo a {@link java.util.Date} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void rollback(Date dateToRollBackTo) throws LiquibaseException {
        getLiquibase().rollback(dateToRollBackTo, getContexts());
    }

    /**
     * <p>rollback.</p>
     *
     * @param tagToRollBackTo a {@link java.lang.String} object.
     * @param contexts        a {@link liquibase.Contexts} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void rollback(String tagToRollBackTo, Contexts contexts) throws LiquibaseException {
        getLiquibase().rollback(tagToRollBackTo, new Contexts(getContexts()));
    }

    /**
     * <p>clearCheckSums.</p>
     *
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void clearCheckSums() throws LiquibaseException {
        getLiquibase().clearCheckSums();
    }

    /**
     * <p>tag.</p>
     *
     * @param tagString a {@link java.lang.String} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void tag(String tagString) throws LiquibaseException {
        getLiquibase().tag(tagString);
    }

    /**
     * <p>reportLocks.</p>
     *
     * @param out a {@link java.io.PrintStream} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void reportLocks(PrintStream out) throws LiquibaseException {
        getLiquibase().reportLocks(out);
    }

    /**
     * <p>getDatabaseChangeLog.</p>
     *
     * @return a {@link liquibase.changelog.DatabaseChangeLog} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public DatabaseChangeLog getDatabaseChangeLog() throws LiquibaseException {
        return getLiquibase().getDatabaseChangeLog();
    }

    /**
     * <p>rollback.</p>
     *
     * @param changesToRollback a int.
     * @param output            a {@link java.io.Writer} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void rollback(int changesToRollback, Writer output) throws LiquibaseException {
        getLiquibase().rollback(changesToRollback, new Contexts(getContexts()), new LabelExpression(getLabels()), output);
    }

    /**
     * <p>futureRollbackSQL.</p>
     *
     * @param output a {@link java.io.Writer} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void futureRollbackSQL(Writer output) throws LiquibaseException {
        getLiquibase().futureRollbackSQL(getContexts(), output);
    }

    /**
     * <p>generateChangeLog.</p>
     *
     * @param catalogAndSchema    a {@link liquibase.CatalogAndSchema} object.
     * @param changeLogWriter     a {@link liquibase.diff.output.changelog.DiffToChangeLog} object.
     * @param outputStream        a {@link java.io.PrintStream} object.
     * @param changeLogSerializer a {@link liquibase.serializer.ChangeLogSerializer} object.
     * @param snapshotTypes       a {@link java.lang.Class} object.
     * @throws liquibase.exception.LiquibaseException         if any.
     * @throws java.io.IOException                            if any.
     * @throws javax.xml.parsers.ParserConfigurationException if any.
     */
    public void generateChangeLog(CatalogAndSchema catalogAndSchema, DiffToChangeLog changeLogWriter, PrintStream outputStream, ChangeLogSerializer changeLogSerializer, Class<? extends DatabaseObject>... snapshotTypes) throws LiquibaseException, IOException, ParserConfigurationException {
        getLiquibase().generateChangeLog(catalogAndSchema, changeLogWriter, outputStream, changeLogSerializer, snapshotTypes);
    }

    /**
     * <p>reportUnexpectedChangeSets.</p>
     *
     * @param verbose a boolean.
     * @param out     a {@link java.io.Writer} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void reportUnexpectedChangeSets(boolean verbose, Writer out) throws LiquibaseException {
        getLiquibase().reportUnexpectedChangeSets(verbose, new Contexts(getContexts()), new LabelExpression(getLabels()), out);
    }

    /**
     * <p>futureRollbackSQL.</p>
     *
     * @param count  a {@link java.lang.Integer} object.
     * @param output a {@link java.io.Writer} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void futureRollbackSQL(Integer count, Writer output) throws LiquibaseException {
        getLiquibase().futureRollbackSQL(count, getContexts(), output);
    }

    /**
     * <p>getChangeSetStatuses.</p>
     *
     * @return a {@link java.util.List} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public List<ChangeSetStatus> getChangeSetStatuses() throws LiquibaseException {
        return getLiquibase().getChangeSetStatuses(new Contexts(getContexts()), new LabelExpression(getLabels()));
    }

    /**
     * <p>reportStatus.</p>
     *
     * @param verbose a boolean.
     * @param out     a {@link java.io.Writer} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void reportStatus(boolean verbose, Writer out) throws LiquibaseException {
        getLiquibase().reportStatus(verbose, new Contexts(getContexts()), out);
    }

    /**
     * <p>forceReleaseLocks.</p>
     *
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void forceReleaseLocks() throws LiquibaseException {
        getLiquibase().forceReleaseLocks();
    }

    /**
     * <p>changeLogSync.</p>
     *
     * @param output a {@link java.io.Writer} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void changeLogSync(Writer output) throws LiquibaseException {
        getLiquibase().changeLogSync(getContexts(), output);
    }

    /**
     * <p>isIgnoreClasspathPrefix.</p>
     *
     * @return a boolean.
     */
    public boolean isIgnoreClasspathPrefix() {
        return getLiquibase().isIgnoreClasspathPrefix();
    }

    /**
     * <p>setIgnoreClasspathPrefix.</p>
     *
     * @param ignoreClasspathPrefix a boolean.
     * @throws java.sql.SQLException                  if any.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void setIgnoreClasspathPrefix(boolean ignoreClasspathPrefix) throws SQLException, LiquibaseException {
        getLiquibase().setIgnoreClasspathPrefix(ignoreClasspathPrefix);
    }

    /**
     * <p>calculateCheckSum.</p>
     *
     * @param changeSetIdentifier a {@link java.lang.String} object.
     * @return a {@link liquibase.change.CheckSum} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public CheckSum calculateCheckSum(String changeSetIdentifier) throws LiquibaseException {
        return getLiquibase().calculateCheckSum(changeSetIdentifier);
    }

    /**
     * <p>changeLogSync.</p>
     *
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void changeLogSync() throws LiquibaseException {
        getLiquibase().changeLogSync(new Contexts(getContexts()), new LabelExpression(getLabels()));
    }

    /**
     * <p>update.</p>
     *
     * @param changesToApply a int.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void update(int changesToApply) throws LiquibaseException {
        getLiquibase().update(changesToApply, getContexts());
    }

    /**
     * <p>update.</p>
     *
     * @param changesToApply a int.
     * @param output         a {@link java.io.Writer} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void update(int changesToApply, Writer output) throws LiquibaseException {
        getLiquibase().update(changesToApply, new Contexts(getContexts()), new LabelExpression(getLabels()), output);
    }

    /**
     * <p>calculateCheckSum.</p>
     *
     * @param filename a {@link java.lang.String} object.
     * @param id       a {@link java.lang.String} object.
     * @param author   a {@link java.lang.String} object.
     * @return a {@link liquibase.change.CheckSum} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public CheckSum calculateCheckSum(String filename, String id, String author) throws LiquibaseException {
        return getLiquibase().calculateCheckSum(filename, id, author);
    }

    /**
     * <p>setChangeLogParameter.</p>
     *
     * @param key   a {@link java.lang.String} object.
     * @param value a {@link java.lang.Object} object.
     */
    public void setChangeLogParameter(String key, Object value) {
        getLiquibase().setChangeLogParameter(key, value);
    }

    /**
     * <p>setChangeLogSyncListener.</p>
     *
     * @param changeLogSyncListener a {@link liquibase.changelog.visitor.ChangeLogSyncListener} object.
     */
    public void setChangeLogSyncListener(ChangeLogSyncListener changeLogSyncListener) {
        getLiquibase().setChangeLogSyncListener(changeLogSyncListener);
    }

    /**
     * <p>markNextChangeSetRan.</p>
     *
     * @param output a {@link java.io.Writer} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void markNextChangeSetRan(Writer output) throws LiquibaseException {
        getLiquibase().markNextChangeSetRan(getContexts(), output);
    }

    /**
     * <p>validate.</p>
     *
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void validate() throws LiquibaseException {
        getLiquibase().validate();
    }

    /**
     * <p>checkLiquibaseTables.</p>
     *
     * @param updateExistingNullChecksums a boolean.
     * @param databaseChangeLog           a {@link liquibase.changelog.DatabaseChangeLog} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void checkLiquibaseTables(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog) throws LiquibaseException {
        getLiquibase().checkLiquibaseTables(updateExistingNullChecksums, databaseChangeLog, new Contexts(getContexts()), new LabelExpression(getLabels()));
    }

    /**
     * <p>rollback.</p>
     *
     * @param dateToRollBackTo a {@link java.util.Date} object.
     * @param output           a {@link java.io.Writer} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void rollback(Date dateToRollBackTo, Writer output) throws LiquibaseException {
        getLiquibase().rollback(dateToRollBackTo, new Contexts(getContexts()), new LabelExpression(getLabels()), output);
    }

    /**
     * <p>rollback.</p>
     *
     * @param tagToRollBackTo a {@link java.lang.String} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void rollback(String tagToRollBackTo) throws LiquibaseException {
        getLiquibase().rollback(tagToRollBackTo, new Contexts(getContexts()), new LabelExpression(getLabels()));
    }

    /**
     * <p>listLocks.</p>
     *
     * @return an array of {@link liquibase.lockservice.DatabaseChangeLogLock} objects.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public DatabaseChangeLogLock[] listLocks() throws LiquibaseException {
        return getLiquibase().listLocks();
    }

    /**
     * <p>rollback.</p>
     *
     * @param tagToRollBackTo a {@link java.lang.String} object.
     * @param output          a {@link java.io.Writer} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void rollback(String tagToRollBackTo, Writer output) throws LiquibaseException {
        getLiquibase().rollback(tagToRollBackTo, getContexts(), output);
    }

    /**
     * <p>dropAll.</p>
     *
     * @param schemas a {@link liquibase.CatalogAndSchema} object.
     * @throws liquibase.exception.LiquibaseException if any.
     */
    public void dropAll(CatalogAndSchema... schemas) throws LiquibaseException {
        getLiquibase().dropAll(schemas);
    }
}
