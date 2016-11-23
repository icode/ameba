package ameba.message.filtering;

import ameba.core.Requests;
import ameba.message.internal.BeanPathProperties;
import ameba.util.bean.BeanMap;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.List;

/**
 * <p>EntityFieldsUtils class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class EntityFieldsUtils {
    /**
     * Constant <code>PATH_PROPS_PARSED="EntityFieldsUtils.class + .BeanPathProp"{trunked}</code>
     */
    public static final String PATH_PROPS_PARSED = EntityFieldsUtils.class + ".BeanPathProperties";

    private EntityFieldsUtils() {
    }

    /**
     * Parse and return a BeanPathProperties format from UriInfo
     *
     * @param uriInfo uri info
     * @return query fields
     */
    public static String parseQueryFields(UriInfo uriInfo) {
        List<String> selectables = uriInfo.getQueryParameters()
                .get(EntityFieldsScopeResolver.FIELDS_PARAM_NAME);
        StringBuilder builder = new StringBuilder();
        if (selectables != null) {
            for (int i = 0; i < selectables.size(); i++) {
                String s = selectables.get(i);
                if (StringUtils.isNotBlank(s)) {
                    if (!s.startsWith("(")) {
                        builder.append("(");
                    }
                    builder.append(s);
                    if (!s.endsWith(")")) {
                        builder.append(")");
                    }
                    if (i < selectables.size() - 1) {
                        builder.append(":");
                    }
                }
            }
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    /**
     * Parse and return a BeanPathProperties format from UriInfo
     *
     * @return query fields
     */
    public static String parseQueryFields() {
        return parseQueryFields(Requests.getUriInfo());
    }

    /**
     * Parse and return a BeanPathProperties from nested string format like
     * (a,b,c(d,e),f(g)) where "c" is a path containing "d" and "e" and "f" is a
     * path containing "g" and the root path contains "a","b","c" and "f".
     *
     * @param uriInfo uri info
     * @return path properties
     */
    public static BeanPathProperties parsePathProperties(UriInfo uriInfo) {
        return BeanPathProperties.parse(parseQueryFields(uriInfo));
    }

    /**
     * Parse and return a BeanPathProperties from nested string format like
     * (a,b,c(d,e),f(g)) where "c" is a path containing "d" and "e" and "f" is a
     * path containing "g" and the root path contains "a","b","c" and "f".
     *
     * @return pah properties
     */
    public static BeanPathProperties parsePathProperties() {
        Object pathProperties = Requests.getProperty(PATH_PROPS_PARSED);
        if (pathProperties == null) {
            String fields = parseQueryFields();
            if (fields != null) {
                pathProperties = BeanPathProperties.parse(fields);
                Requests.setProperty(PATH_PROPS_PARSED, pathProperties);
            } else {
                Requests.setProperty(PATH_PROPS_PARSED, false);
            }
        } else if (pathProperties.equals(false)) {
            return null;
        }
        return (BeanPathProperties) pathProperties;
    }

    /**
     * <p>filterBeanFields.</p>
     *
     * @param src a T object.
     * @param pathProperties a {@link ameba.message.internal.BeanPathProperties} object.
     * @param <T> a T object.
     * @return a {@link ameba.util.bean.BeanMap} object.
     */
    @SuppressWarnings("unchecked")
    public static <T> BeanMap<T> filterBeanFields(T src, BeanPathProperties pathProperties) {
        return (BeanMap<T>) FilteringBeanMap.from(src, pathProperties);
    }

    /**
     * <p>filterBeanFields.</p>
     *
     * @param src an array of T objects.
     * @param pathProperties a {@link ameba.message.internal.BeanPathProperties} object.
     * @param <T> a T object.
     * @return an array of {@link ameba.util.bean.BeanMap} objects.
     */
    @SuppressWarnings("unchecked")
    public static <T> BeanMap[] filterBeanFields(T[] src, BeanPathProperties pathProperties) {
        return (BeanMap[]) FilteringBeanMap.from(src, pathProperties);
    }

    /**
     * <p>filterBeanFields.</p>
     *
     * @param src a {@link java.util.Collection} object.
     * @param pathProperties a {@link ameba.message.internal.BeanPathProperties} object.
     * @param <T> a T object.
     * @return a {@link java.util.Collection} object.
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<BeanMap<T>> filterBeanFields(Collection<T> src, BeanPathProperties pathProperties) {
        return (Collection<BeanMap<T>>) FilteringBeanMap.from(src, pathProperties);
    }

    /**
     * <p>filterRequestFields.</p>
     *
     * @param src a T object.
     * @param <T> a T object.
     * @return a {@link ameba.util.bean.BeanMap} object.
     */
    @SuppressWarnings("unchecked")
    public static <T> BeanMap<T> filterRequestFields(T src) {
        return (BeanMap<T>) FilteringBeanMap.from(src, parsePathProperties());
    }

    /**
     * <p>filterRequestFields.</p>
     *
     * @param src an array of T objects.
     * @param <T> a T object.
     * @return an array of {@link ameba.util.bean.BeanMap} objects.
     */
    @SuppressWarnings("unchecked")
    public static <T> BeanMap[] filterRequestFields(T[] src) {
        return (BeanMap[]) FilteringBeanMap.from(src, parsePathProperties());
    }

    /**
     * <p>filterRequestFields.</p>
     *
     * @param src a {@link java.util.Collection} object.
     * @param <T> a T object.
     * @return a {@link java.util.Collection} object.
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<BeanMap<T>> filterRequestFields(Collection<T> src) {
        return (Collection<BeanMap<T>>) FilteringBeanMap.from(src, parsePathProperties());
    }

}
