package ameba.i18n;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.*;

/**
 * <p>PropertiesResourceBundle class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class PropertiesResourceBundle extends ResourceBundle {
    private Map<String, Object> lookup;

    /**
     * <p>Constructor for PropertiesResourceBundle.</p>
     *
     * @param properties a {@link java.util.Properties} object.
     * @throws java.io.IOException if any.
     */
    @SuppressWarnings("all")
    public PropertiesResourceBundle(Properties properties) throws IOException {
        lookup = Maps.newHashMap((Map) properties);
    }

    // Implements java.util.ResourceBundle.handleGetObject; inherits javadoc specification.

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("all")
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
    @SuppressWarnings("all")
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
    @SuppressWarnings("all")
    protected Set<String> handleKeySet() {
        return lookup.keySet();
    }

    private class ResourceBundleEnumeration implements Enumeration<String> {
        Set<String> set;
        Iterator<String> iterator;
        Enumeration<String> enumeration;
        String next = null;

        public ResourceBundleEnumeration(Set<String> set, Enumeration<String> enumeration) {
            this.set = set;
            this.iterator = set.iterator();
            this.enumeration = enumeration;
        }

        public boolean hasMoreElements() {
            if (this.next == null) {
                if (this.iterator.hasNext()) {
                    this.next = this.iterator.next();
                } else if (this.enumeration != null) {
                    while (this.next == null && this.enumeration.hasMoreElements()) {
                        this.next = this.enumeration.nextElement();
                        if (this.set.contains(this.next)) {
                            this.next = null;
                        }
                    }
                }
            }

            return this.next != null;
        }

        public String nextElement() {
            if (this.hasMoreElements()) {
                String next = this.next;
                this.next = null;
                return next;
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
