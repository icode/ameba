package ameba.db.ebean;

import ameba.core.Application;
import ameba.core.Requests;
import ameba.db.ebean.filter.Filter;
import ameba.db.ebean.internal.ListExpressionValidation;
import ameba.db.ebean.jackson.CommonBeanSerializer;
import ameba.exception.UnprocessableEntityException;
import ameba.i18n.Messages;
import ameba.message.filtering.EntityFieldsUtils;
import ameba.message.internal.PathProperties;
import com.avaje.ebean.FetchPath;
import com.avaje.ebean.OrderBy;
import com.avaje.ebean.Query;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiExpression;
import com.avaje.ebeaninternal.api.SpiExpressionList;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;

import javax.ws.rs.container.ResourceInfo;
import java.util.*;

import static com.avaje.ebean.OrderBy.Property;

/**
 * <p>EbeanUtils class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public class EbeanUtils {
    public static final String PATH_PROPS_PARSED = EbeanUtils.class + ".PathProperties";


    private EbeanUtils() {
    }

    /**
     * <p>forceUpdateAllProperties.</p>
     *
     * @param server a {@link com.avaje.ebeaninternal.api.SpiEbeanServer} object.
     * @param model  a T object.
     * @param <T>    a T object.
     */
    @SuppressWarnings("unchecked")
    public static <T> void forceUpdateAllProperties(SpiEbeanServer server, T model) {
        forceUpdateAllProperties(server.getBeanDescriptor((Class<T>) model.getClass()), model);
    }

    /**
     * <p>forceUpdateAllProperties.</p>
     *
     * @param beanDescriptor a {@link com.avaje.ebeaninternal.server.deploy.BeanDescriptor} object.
     * @param model          a T object.
     * @param <T>            a T object.
     */
    public static <T> void forceUpdateAllProperties(BeanDescriptor<T> beanDescriptor, T model) {
        EntityBeanIntercept intercept = ((EntityBean) model)._ebean_getIntercept();
        intercept.setLoaded();
        int idIndex = beanDescriptor.getIdProperty().getPropertyIndex();
        for (int i = 0; i < intercept.getPropertyLength(); i++) {
            if (i != idIndex) {
                intercept.markPropertyAsChanged(i);
                intercept.setLoadedProperty(i);
            }
        }
    }

    /**
     * parse uri query param to PathProperties for Ebean.json().toJson()
     *
     * @return PathProperties
     * @see CommonBeanSerializer#serialize(Object, JsonGenerator, SerializerProvider)
     */
    public static FetchPath getCurrentRequestPathProperties() {
        FetchPath properties = Requests.getProperty(PATH_PROPS_PARSED);
        if (properties == null) {
            PathProperties pathProperties = EntityFieldsUtils.parsePathProperties();
            Requests.setProperty(PATH_PROPS_PARSED, EbeanPathProps.of(pathProperties));
        }
        return properties;
    }

    public static <T> void appendOrder(OrderBy<T> orderBy, String orderByClause) {

        if (orderByClause == null) {
            return;
        }

        String[] chunks = orderByClause.split(",");
        for (String chunk : chunks) {
            String[] pairs = chunk.split(" ");
            Property p = parseOrderProperty(pairs);
            if (p != null) {
                orderBy.add(p);
            }
        }
    }

    public static void checkQuery(Query<?> query, ServiceLocator locator) {
        checkQuery(query, null, null, locator);
    }

    public static void checkQuery(Query<?> query, Set<String> whitelist,
                                  Set<String> blacklist, ServiceLocator locator) {
        ResourceInfo resource = locator.getService(ResourceInfo.class);
        Class<?> rc = resource.getResourceClass();
        Set<String> wl = null, bl = null;
        if (rc != null) {
            Filter filter = rc.getAnnotation(Filter.class);

            if (filter != null) {
                if (filter.whitelist().length > 0) {
                    wl = Sets.newLinkedHashSet();
                    Collections.addAll(wl, filter.whitelist());
                }
                if (filter.blacklist().length > 0) {
                    bl = Sets.newLinkedHashSet();
                    Collections.addAll(bl, filter.blacklist());
                }
            }
        }

        if (whitelist != null) {
            if (wl == null) {
                wl = Sets.newLinkedHashSet();
            }
            wl.addAll(whitelist);
        }

        if (blacklist != null) {
            if (bl == null) {
                bl = Sets.newLinkedHashSet();
            }
            bl.addAll(blacklist);
        }
        checkQuery((SpiQuery) query, wl, bl, locator.getService(Application.Mode.class).isProd());
    }

    public static void checkQuery(SpiQuery<?> query, Set<String> whitelist,
                                  Set<String> blacklist, boolean ignoreUnknown) {
        checkQuery(
                query,
                new ListExpressionValidation(
                        query.getBeanDescriptor(), whitelist, blacklist
                ),
                ignoreUnknown
        );
    }

    public static void checkQuery(SpiQuery<?> query, ListExpressionValidation validation, boolean ignoreUnknown) {
        if (query != null) {
            validate(query.getWhereExpressions(), validation, ignoreUnknown);
            validate(query.getHavingExpressions(), validation, ignoreUnknown);
            validate(query.getOrderBy(), validation, ignoreUnknown);

            Set<String> invalid = validation.getUnknownProperties();

            if (!ignoreUnknown && !invalid.isEmpty()) {
                UnprocessableEntityException.throwQuery(invalid);
            }
        }
    }

    public static void validate(SpiExpressionList<?> expressions,
                                ListExpressionValidation validation,
                                boolean ignoreUnknown) {
        if (expressions == null) return;
        List<SpiExpression> list = expressions.getUnderlyingList();
        Iterator<SpiExpression> it = list.iterator();
        while (it.hasNext()) {
            it.next().validate(validation);
            if (ignoreUnknown && !validation.lastValid()) {
                it.remove();
            }
        }
    }

    public static void validate(OrderBy<?> orderBy,
                                ListExpressionValidation validation,
                                boolean ignoreUnknown) {
        if (orderBy == null) return;
        Iterator<Property> it = orderBy.getProperties().iterator();
        while (it.hasNext()) {
            validation.validate(it.next().getProperty());
            if (ignoreUnknown && !validation.lastValid()) {
                it.remove();
            }
        }
    }

    private static Property parseOrderProperty(String[] pairs) {
        if (pairs.length == 0) {
            return null;
        }

        ArrayList<String> wordList = Lists.newArrayListWithCapacity(pairs.length);
        for (String pair : pairs) {
            if (StringUtils.isNotBlank(pair)) {
                wordList.add(pair);
            }
        }
        if (wordList.isEmpty()) {
            return null;
        }
        String field = wordList.get(0);
        if (wordList.size() == 1) {
            if (field.startsWith("-")) {
                return new Property(field.substring(1), false);
            } else {
                return new Property(field, true);
            }
        }
        if (wordList.size() == 2) {
            boolean asc = isOrderAscending(wordList.get(1));
            return new Property(field, asc);
        }

        throw new UnprocessableEntityException(
                Messages.get("info.query.orderby1.unprocessable.entity",
                        Arrays.toString(pairs), wordList.size())
        );
    }

    private static boolean isOrderAscending(String s) {
        s = s.toLowerCase();
        if (s.startsWith("asc")) {
            return true;
        }
        if (s.startsWith("desc")) {
            return false;
        }
        throw new UnprocessableEntityException(Messages.get("info.query.orderby0.unprocessable.entity", s));
    }
}
