package ameba.db.ebean.filter;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * <p>Filters class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class Filters {
    private Filters() {
    }

    /**
     * <p>getBeanTypeByName.</p>
     *
     * @param className a {@link java.lang.String} object.
     * @param server    a {@link com.avaje.ebeaninternal.api.SpiEbeanServer} object.
     * @return a {@link java.lang.Class} object.
     */
    public static Class getBeanTypeByName(String className, SpiEbeanServer server) {
        if (className == null) return null;
        for (BeanDescriptor descriptor : server.getBeanDescriptors()) {
            Class beanClass = descriptor.getBeanType();
            if (beanClass.getName().equalsIgnoreCase(className)
                    || beanClass.getSimpleName().equalsIgnoreCase(className)) {
                return beanClass;
            }
        }
        return null;
    }

    /**
     * <p>createQuery.</p>
     *
     * @param beanClass a {@link java.lang.Class} object.
     * @param server a {@link com.avaje.ebean.EbeanServer} object.
     * @return a {@link com.avaje.ebean.Query} object.
     */
    public static Query createQuery(Class beanClass, EbeanServer server) {
        return server.createQuery(beanClass);
    }
}
