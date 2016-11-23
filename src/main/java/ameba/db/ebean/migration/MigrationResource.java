package ameba.db.ebean.migration;

import ameba.db.migration.models.ScriptInfo;
import com.avaje.ebean.dbmigration.migration.Migration;
import com.avaje.ebean.dbmigration.model.MigrationVersion;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

/**
 * <p>MigrationResource class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class MigrationResource implements Comparable<MigrationResource> {

    private ScriptInfo info;
    private MigrationVersion version;

    /**
     * Construct with a migration xml file.
     *
     * @param info a {@link ameba.db.migration.models.ScriptInfo} object.
     */
    public MigrationResource(ScriptInfo info) {
        this.info = info;
        this.version = MigrationVersion.parse(info.getRevision());
    }

    /**
     * <p>toString.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return version.asString() + "->" + this.info.toString();
    }

    /**
     * Return the version associated with this resource.
     *
     * @return a {@link com.avaje.ebean.dbmigration.model.MigrationVersion} object.
     */
    public MigrationVersion getVersion() {
        return version;
    }

    /**
     * Read and return the migration from the resource.
     *
     * @return a {@link com.avaje.ebean.dbmigration.migration.Migration} object.
     */
    public Migration read() {
        try (StringReader reader = new StringReader(info.getModelDiff())) {
            JAXBContext jaxbContext = JAXBContext.newInstance(Migration.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (Migration) unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>Getter for the field <code>info</code>.</p>
     *
     * @return a {@link ameba.db.migration.models.ScriptInfo} object.
     */
    public ScriptInfo getInfo() {
        return info;
    }

    /**
     * {@inheritDoc}
     *
     * Compare by underlying version.
     */
    @Override
    public int compareTo(MigrationResource other) {
        return version.compareTo(other.version);
    }
}
