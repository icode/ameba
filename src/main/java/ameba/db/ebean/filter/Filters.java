package ameba.db.ebean.filter;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * @author icode
 */
public class Filters {
    private Filters() {
    }

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

    public static Query createQuery(Class beanClass, EbeanServer server) {
        return server.createQuery(beanClass);
    }
}
