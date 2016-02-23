package ameba.db.ebean.migration;

import ameba.db.migration.models.ScriptInfo;
import com.avaje.ebean.dbmigration.migration.Migration;
import com.avaje.ebean.dbmigration.model.MigrationVersion;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

/**
 * @author icode
 */
public class MigrationResource implements Comparable<MigrationResource> {

    private ScriptInfo info;
    private MigrationVersion version;

    /**
     * Construct with a migration xml file.
     */
    public MigrationResource(ScriptInfo info) {
        this.info = info;
        this.version = MigrationVersion.parse(info.getRevision());
    }

    public String toString() {
        return version.asString() + "->" + this.info.toString();
    }

    /**
     * Return the version associated with this resource.
     */
    public MigrationVersion getVersion() {
        return version;
    }

    /**
     * Read and return the migration from the resource.
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

    public ScriptInfo getInfo() {
        return info;
    }

    /**
     * Compare by underlying version.
     */
    @Override
    public int compareTo(MigrationResource other) {
        return version.compareTo(other.version);
    }
}
