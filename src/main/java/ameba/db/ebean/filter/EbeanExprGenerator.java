package ameba.db.ebean.filter;

import ameba.db.dsl.QueryExprGenerator;
import com.avaje.ebean.Expression;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author icode
 */
public class EbeanExprGenerator<T> extends QueryExprGenerator<Expression> {
    private static final Map<String, Object> DEFAULT_MAPPING = Maps.newHashMap();
//    private static final Object o = CacheBuilder.newBuilder().build();

    private Query<T> query;
    private SpiEbeanServer server;

    public EbeanExprGenerator(Query<T> query, SpiEbeanServer server, Map<String, Object> mapping) {
        this.query = query;
        this.server = server;
    }

    public EbeanExprGenerator(Query<T> query, SpiEbeanServer server) {
        this(query, server, DEFAULT_MAPPING);
    }

    @Override
    protected Object arg(String field, String op, Object arg) {
        if (arg != null) {
        }
        return null;
    }

    // todo 使用cache
    protected Class getBeanTypeByName(String className) {
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

    protected Query createQuery(Class beanClass) {
        return server.createQuery(beanClass);
    }

    @Override
    protected Expression expr(String field, String op, Object[] args) {
        //query.getExpressionFactory()
        return null;
    }
}
