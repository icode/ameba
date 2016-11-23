package ameba.db.ebean.internal;

import com.avaje.ebean.plugin.BeanType;
import com.avaje.ebeaninternal.api.SpiExpressionValidation;

import java.util.Set;

/**
 * <p>ListExpressionValidation class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class ListExpressionValidation extends SpiExpressionValidation {
    private final Set<String> blacklist;
    private final Set<String> whitelist;
    private final BeanType<?> desc;
    private boolean lastValid = true;

    /**
     * <p>Constructor for ListExpressionValidation.</p>
     *
     * @param desc      a {@link com.avaje.ebean.plugin.BeanType} object.
     * @param whitelist a {@link java.util.Set} object.
     * @param blacklist a {@link java.util.Set} object.
     */
    public ListExpressionValidation(BeanType<?> desc, Set<String> whitelist, Set<String> blacklist) {
        super(desc);
        this.blacklist = blacklist;
        this.whitelist = whitelist;
        this.desc = desc;
    }

    /**
     * <p>lastValid.</p>
     *
     * @return a boolean.
     */
    public boolean lastValid() {
        return lastValid;
    }

    /**
     * {@inheritDoc}
     *
     * Validate that the property expression (path) is valid.
     */
    public void validate(String propertyName) {
        if (whitelist != null && whitelist.contains(propertyName)) {
            lastValid = true;
            return;
        }
        if (blacklist != null && blacklist.contains(propertyName)) {
            lastValid = false;
            getUnknownProperties().add(propertyName);
            return;
        }

        lastValid = desc.isValidExpression(propertyName);
        super.validate(propertyName);
    }
}
