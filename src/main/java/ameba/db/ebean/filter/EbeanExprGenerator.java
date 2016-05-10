package ameba.db.ebean.filter;

import ameba.db.dsl.QueryExprGenerator;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Expression;
import com.avaje.ebean.Query;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author icode
 */
public class EbeanExprGenerator<T> extends QueryExprGenerator<Expression> {
    private static final Map<String, Object> DEFAULT_MAPPING = Maps.newHashMap();
    private Query<T> query;
    private EbeanServer server;

    public EbeanExprGenerator(Query<T> query, EbeanServer server, Map<String, Object> mapping) {
        this.query = query;
        this.server = server;
    }

    public EbeanExprGenerator(Query<T> query, EbeanServer server) {
        this(query, server, DEFAULT_MAPPING);
    }

    @Override
    protected Object arg(String field, String op, Object arg) {
        if (arg != null) {

        }
        return null;
    }

    protected Query<T> createQuery() {
        return server.createQuery(query.getBeanType());
    }

    @Override
    protected Expression expr(String field, String op, Object[] args) {
        //query.getExpressionFactory()
        return null;
    }
}
