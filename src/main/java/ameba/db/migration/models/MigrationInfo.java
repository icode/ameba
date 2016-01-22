package ameba.db.migration.models;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;

/**
 * @author icode
 */
@Entity
public class MigrationInfo {
    @Id
    private String revision;
    @Transient
    private String description;
    @Lob
    private String modelDiff;
    @Lob
    private String applyDdl;
    @Lob
    private String rollbackDdl;
    @Lob
    private String dropDdl;

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

    public String getRollbackDdl() {
        return rollbackDdl;
    }

    public void setRollbackDdl(String rollbackDdl) {
        this.rollbackDdl = rollbackDdl;
    }

    public String getApplyDdl() {
        return applyDdl;
    }

    public void setApplyDdl(String applyDdl) {
        this.applyDdl = applyDdl;
    }

    public String getDropDdl() {
        return dropDdl;
    }

    public void setDropDdl(String dropDdl) {
        this.dropDdl = dropDdl;
    }

    public String getDiffDdl() {
        StringBuilder diff = new StringBuilder();
        if (StringUtils.isNotBlank(getApplyDdl())) {
            diff.append(getApplyDdl());
        }
        if (diff.length() == 0) {
            diff.append("\r\n");
        }
        if (StringUtils.isNotBlank(getDropDdl())) {
            diff.append(getDropDdl());
        }
        return diff.toString();
    }
}
