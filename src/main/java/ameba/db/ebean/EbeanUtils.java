package ameba.db.ebean;

import ameba.core.Requests;
import ameba.db.ebean.jackson.CommonBeanSerializer;
import ameba.message.filtering.EntityFieldsUtils;
import ameba.message.internal.PathProperties.Apply;
import ameba.message.internal.PathProperties.Props;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.ebean.common.BeanMap;
import com.avaje.ebean.text.PathProperties;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.util.Collection;

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
     * <p>forcePropertiesLoaded.</p>
     *
     * @param model a T object.
     * @param <T>   a T object.
     */
    public static <T> void forcePropertiesLoaded(T model) {
        if (model == null) return;
        if (model instanceof EntityBean) {
            EntityBeanIntercept intercept = ((EntityBean) model)._ebean_getIntercept();
            intercept.setLoaded();
            for (int i = 0; i < intercept.getPropertyLength(); i++) {
                intercept.setLoadedProperty(i);
            }
        } else if (model instanceof Collection) {
            for (Object m : (Collection) model) {
                forcePropertiesLoaded(m);
            }
        } else if (model instanceof BeanMap) {
            forcePropertiesLoaded(((BeanMap) model).values());
        } else if (model.getClass().isArray()) {
            for (Object m : (Object[]) model) {
                forcePropertiesLoaded(m);
            }
        }
    }

    /**
     * parse uri query param to PathProperties for Ebean.json().toJson()
     *
     * @return PathProperties
     * @see {@link JsonContext#toJson(Object, JsonGenerator, PathProperties)}
     * @see {@link CommonBeanSerializer#serialize(Object, JsonGenerator, SerializerProvider)}
     */
    public static PathProperties getCurrentRequestPathProperties() {
        PathProperties properties = (PathProperties) Requests.getProperty(PATH_PROPS_PARSED);
        if (properties == null) {
            final ameba.message.internal.PathProperties pathProperties = EntityFieldsUtils.parsePathProperties();
            final PathProperties finalProperties = properties = new PathProperties();
            pathProperties.apply(new Apply<String, Props>() {
                @Override
                public void execute(Props props) {
                    finalProperties.put(null, props.getProperties());
                }

                @Override
                public void execute(String s, Props props) {
                    finalProperties.put(s, props.getProperties());
                }
            });
            Requests.setProperty(PATH_PROPS_PARSED, properties);
        }
        return properties;
    }
}
