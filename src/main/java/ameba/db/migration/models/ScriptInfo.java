package ameba.db.migration.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;

/**
 * @author icode
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

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getModelDiff() {
        return modelDiff;
    }

    public void setModelDiff(String modelDiff) {
        this.modelDiff = modelDiff;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String name) {
        this.description = name;
    }

    public String getApplyDdl() {
        return applyDdl;
    }

    public void setApplyDdl(String applyDdl) {
        this.applyDdl = applyDdl;
    }
}
