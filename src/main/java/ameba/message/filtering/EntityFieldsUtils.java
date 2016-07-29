package ameba.message.filtering;

import ameba.core.Requests;
import ameba.message.internal.PathProperties;
import ameba.util.bean.BeanMap;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.List;

/**
 * @author icode
 */
public class EntityFieldsUtils {
    public static final String PATH_PROPS_PARSED = EntityFieldsUtils.class + ".PathProperties";

    private EntityFieldsUtils() {
    }

    /**
     * Parse and return a PathProperties format from UriInfo
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
        return builder.length() == 0 ? "(*)" : builder.toString();
    }

    /**
     * Parse and return a PathProperties format from UriInfo
     *
     * @return query fields
     */
    public static String parseQueryFields() {
        return parseQueryFields(Requests.getUriInfo());
    }

    /**
     * Parse and return a PathProperties from nested string format like
     * (a,b,c(d,e),f(g)) where "c" is a path containing "d" and "e" and "f" is a
     * path containing "g" and the root path contains "a","b","c" and "f".
     *
     * @param uriInfo uri info
     * @return path properties
     */
    public static PathProperties parsePathProperties(UriInfo uriInfo) {
        return PathProperties.parse(parseQueryFields(uriInfo));
    }

    /**
     * Parse and return a PathProperties from nested string format like
     * (a,b,c(d,e),f(g)) where "c" is a path containing "d" and "e" and "f" is a
     * path containing "g" and the root path contains "a","b","c" and "f".
     *
     * @return pah properties
     */
    public static PathProperties parsePathProperties() {
        PathProperties pathProperties = Requests.getProperty(PATH_PROPS_PARSED);
        if (pathProperties == null) {
            pathProperties = PathProperties.parse(parseQueryFields());
            Requests.setProperty(PATH_PROPS_PARSED, pathProperties);
        }
        return pathProperties;
    }

    @SuppressWarnings("unchecked")
    public static <T> BeanMap<T> filterBeanFields(T src, PathProperties pathProperties) {
        return (BeanMap<T>) FilteringBeanMap.from(src, pathProperties);
    }

    @SuppressWarnings("unchecked")
    public static <T> BeanMap[] filterBeanFields(T[] src, PathProperties pathProperties) {
        return (BeanMap[]) FilteringBeanMap.from(src, pathProperties);
    }

    @SuppressWarnings("unchecked")
    public static <T> Collection<BeanMap<T>> filterBeanFields(Collection<T> src, PathProperties pathProperties) {
        return (Collection<BeanMap<T>>) FilteringBeanMap.from(src, pathProperties);
    }

    @SuppressWarnings("unchecked")
    public static <T> BeanMap<T> filterRequestFields(T src) {
        return (BeanMap<T>) FilteringBeanMap.from(src, parsePathProperties());
    }

    @SuppressWarnings("unchecked")
    public static <T> BeanMap[] filterRequestFields(T[] src) {
        return (BeanMap[]) FilteringBeanMap.from(src, parsePathProperties());
    }

    @SuppressWarnings("unchecked")
    public static <T> Collection<BeanMap<T>> filterRequestFields(Collection<T> src) {
        return (Collection<BeanMap<T>>) FilteringBeanMap.from(src, parsePathProperties());
    }

}
