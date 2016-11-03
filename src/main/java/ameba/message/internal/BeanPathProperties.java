package ameba.message.internal;


import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;

/**
 * This is a Tree like structure of paths and properties that can be used for
 * defining which parts of an object graph to render in JSON or XML, and can
 * also be used to define which parts to select and fetch for an ORM query.
 * <p>
 * It provides a way of parsing a string representation of nested path
 * properties and applying that to both what to fetch (ORM query) and what to
 * render (JAX-RS JSON / XML).
 * </p>
 *
 * @author icode
 */
public class BeanPathProperties {

    private final Map<String, Props> pathMap;

    /**
     * Construct an empty BeanPathProperties.
     */
    public BeanPathProperties() {
        this.pathMap = Maps.newLinkedHashMap();
        this.pathMap.put(null, new Props(this, null, null));
    }

    /**
     * Construct for creating copy.
     */
    private BeanPathProperties(BeanPathProperties orig) {
        this.pathMap = new LinkedHashMap<>(orig.pathMap.size());
        Set<Map.Entry<String, Props>> entrySet = orig.pathMap.entrySet();
        for (Map.Entry<String, Props> e : entrySet) {
            pathMap.put(e.getKey(), e.getValue().copy(this));
        }
    }

    /**
     * Parse and return a BeanPathProperties from nested string format like
     * (a,b,c(d,e),f(g)) where "c" is a path containing "d" and "e" and "f" is a
     * path containing "g" and the root path contains "a","b","c" and "f".
     *
     * @param source source path
     * @return path properties
     */
    public static BeanPathProperties parse(String source) {
        return PathPropertiesParser.parse(source);
    }

    /**
     * Create a copy of this instance so that it can be modified.
     * <p>
     * For example, you may want to create a copy to add extra properties to a
     * path so that they are fetching in a ORM query but perhaps not rendered by
     * default. That is, use a BeanPathProperties for JSON or XML rendering, but
     * create a copy, add some extra properties and then use that copy to define
     * an ORM query.
     * </p>
     *
     * @return path properties
     */
    public BeanPathProperties copy() {
        return new BeanPathProperties(this);
    }

    /**
     * Return true if there are no paths defined.
     *
     * @return true is empty
     */
    public boolean isEmpty() {
        return pathMap.isEmpty();
    }

    public String toString() {
        return pathMap.toString();
    }

    /**
     * Return true if the path is defined and has properties.
     *
     * @param path path
     * @return true is has path
     */
    public boolean hasPath(String path) {
        Props props = pathMap.get(path);
        return props != null && !props.isEmpty();
    }

    /**
     * Get the properties for a given path.
     *
     * @param path path
     * @return properties
     */
    public Set<String> getProperties(String path) {
        Props props = pathMap.get(path);
        return props == null ? null : props.getProperties();
    }

    public void addToPath(String path, String property) {
        Props props = pathMap.get(path);
        if (props == null) {
            props = new Props(this, null, path);
            pathMap.put(path, props);
        }
        props.getProperties().add(property);
    }

    /**
     * Set the properties for a given path.
     *
     * @param path       path
     * @param properties properties set
     */
    public void put(String path, Set<String> properties) {
        pathMap.put(path, new Props(this, null, path, properties));
    }

    /**
     * Remove a path returning the properties set for that path.
     *
     * @param path path
     * @return properties set
     */
    public Set<String> remove(String path) {
        Props props = pathMap.remove(path);
        return props == null ? null : props.getProperties();
    }

    /**
     * Return a shallow copy of the paths.
     *
     * @return path set
     */
    public Set<String> getPaths() {
        return Sets.newLinkedHashSet(pathMap.keySet());
    }

    public Collection<Props> getPathProps() {
        return pathMap.values();
    }

    /**
     * Each these path properties as fetch paths to the query.
     *
     * @param each each process
     */
    public void each(Each<Props> each) {

        for (Map.Entry<String, Props> entry : pathMap.entrySet()) {
            Props props = entry.getValue();

            each.execute(props);
        }
    }

    public Props getRootProperties() {
        return pathMap.get(null);
    }

    public interface Each<PROPS> {
        void execute(PROPS props);
    }

    public static class Props {

        private final BeanPathProperties owner;

        private final String parentPath;
        private final String path;

        private final Set<String> propSet;

        private Props(BeanPathProperties owner, String parentPath, String path, Set<String> propSet) {
            this.owner = owner;
            this.path = path;
            this.parentPath = parentPath;
            this.propSet = propSet;
        }

        private Props(BeanPathProperties owner, String parentPath, String path) {
            this(owner, parentPath, path, new LinkedHashSet<String>());
        }

        /**
         * Create a shallow copy of this Props instance.
         *
         * @param newOwner new owner
         * @return properties
         */
        public Props copy(BeanPathProperties newOwner) {
            return new Props(newOwner, parentPath, path, Sets.newLinkedHashSet(propSet));
        }

        public String getPath() {
            return path;
        }

        public String toString() {
            return propSet.toString();
        }

        public boolean isEmpty() {
            return propSet.isEmpty();
        }

        /**
         * Return the properties for this property set.
         *
         * @return property set
         */
        public Set<String> getProperties() {
            return propSet;
        }

        /**
         * Return the properties as a comma delimited string.
         *
         * @return properties string
         */
        public String getPropertiesAsString() {

            StringBuilder sb = new StringBuilder();

            Iterator<String> it = propSet.iterator();
            boolean hasNext = it.hasNext();
            while (hasNext) {
                sb.append(it.next());
                hasNext = it.hasNext();
                if (hasNext) {
                    sb.append(",");
                }
            }
            return sb.toString();
        }

        /**
         * Return the parent path
         *
         * @return parent props
         */
        protected Props getParent() {
            return owner.pathMap.get(parentPath);
        }

        /**
         * Add a child Property set.
         *
         * @param subpath subpath
         * @return prop
         */
        protected Props addChild(String subpath) {

            subpath = subpath.trim();
            addProperty(subpath);

            // build the subPath
            String p = path == null ? subpath : path + "." + subpath;
            Props nested = new Props(owner, path, p);
            owner.pathMap.put(p, nested);
            return nested;
        }

        /**
         * Add a properties to include for this path.
         *
         * @param property property
         */
        protected void addProperty(String property) {
            propSet.add(property.trim());
        }
    }

}
