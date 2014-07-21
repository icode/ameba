package ameba.feature;

import com.google.common.collect.Lists;

import javax.ws.rs.core.Feature;
import java.util.List;

/**
 * @author icode
 */
public abstract class AmebaFeature implements Feature {
    public List<Class> onClassesChange(List<Class> modified) {
        return Lists.newArrayList();
    }

    public void detectChange() {
    }

    public boolean detectClassesChange() {
        return false;
    }
}
