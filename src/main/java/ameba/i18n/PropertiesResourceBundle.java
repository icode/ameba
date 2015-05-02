package ameba.i18n;

import com.google.common.collect.Maps;
import sun.util.ResourceBundleEnumeration;

import java.io.IOException;
import java.util.*;

/**
 * @author icode
 */
public class PropertiesResourceBundle extends ResourceBundle {
    private Map<String, Object> lookup;

    public PropertiesResourceBundle(Properties properties) throws IOException {
        lookup = Maps.newHashMap((Map) properties);
    }

    // Implements java.util.ResourceBundle.handleGetObject; inherits javadoc specification.
    public Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return lookup.get(key);
    }

    /**
     * Returns an <code>Enumeration</code> of the keys contained in
     * this <code>ResourceBundle</code> and its parent bundles.
     *
     * @return an <code>Enumeration</code> of the keys contained in
     * this <code>ResourceBundle</code> and its parent bundles.
     * @see #keySet()
     */
    public Enumeration<String> getKeys() {
        ResourceBundle parent = this.parent;
        return new ResourceBundleEnumeration(lookup.keySet(),
                (parent != null) ? parent.getKeys() : null);
    }

    /**
     * Returns a <code>Set</code> of the keys contained
     * <em>only</em> in this <code>ResourceBundle</code>.
     *
     * @return a <code>Set</code> of the keys contained only in this
     * <code>ResourceBundle</code>
     * @see #keySet()
     * @since 1.6
     */
    protected Set<String> handleKeySet() {
        return lookup.keySet();
    }
}
