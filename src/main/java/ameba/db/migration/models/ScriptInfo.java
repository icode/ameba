package ameba.db.migration.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;

/**
 * <p>ScriptInfo class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
@Entity
public class ScriptInfo {
    @Id
    private String revision;
    @Transient
    private String description;
    @Lob
    private String modelDiff;
    @Lob
    private String applyDdl;

    /**
     * <p>Getter for the field <code>revision</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRevision() {
        return revision;
    }

    /**
     * <p>Setter for the field <code>revision</code>.</p>
     *
     * @param revision a {@link java.lang.String} object.
     */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    /**
     * <p>Getter for the field <code>modelDiff</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getModelDiff() {
        return modelDiff;
    }

    /**
     * <p>Setter for the field <code>modelDiff</code>.</p>
     *
     * @param modelDiff a {@link java.lang.String} object.
     */
    public void setModelDiff(String modelDiff) {
        this.modelDiff = modelDiff;
    }

    /**
     * <p>Getter for the field <code>description</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return description;
    }

    /**
     * <p>Setter for the field <code>description</code>.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setDescription(String name) {
        this.description = name;
    }

    /**
     * <p>Getter for the field <code>applyDdl</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getApplyDdl() {
        return applyDdl;
    }

    /**
     * <p>Setter for the field <code>applyDdl</code>.</p>
     *
     * @param applyDdl a {@link java.lang.String} object.
     */
    public void setApplyDdl(String applyDdl) {
        this.applyDdl = applyDdl;
    }
}
