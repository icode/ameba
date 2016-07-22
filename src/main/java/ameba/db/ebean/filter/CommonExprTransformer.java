package ameba.db.ebean.filter;

import ameba.db.dsl.ExprTransformer;
import ameba.db.dsl.QuerySyntaxException;
import ameba.db.dsl.Transformed;
import ameba.exception.UnprocessableEntityException;
import com.avaje.ebean.*;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiExpressionFactory;

import java.util.Set;

/**
 * @author icode
 */
public class CommonExprTransformer implements ExprTransformer<Expression, EbeanExprInvoker> {
    @Override
    public Transformed<Expression> transform(String field, String operator,
                                             Object[] args, EbeanExprInvoker invoker) {
        EbeanServer server = invoker.getServer();
        ExpressionFactory factory = server.getExpressionFactory();
        Expression expr = null;
        switch (operator) {
            case "eq":
                expr = factory.eq(field, args[0]);
                break;
            case "ne":
                expr = factory.ne(field, args[0]);
                break;
            case "ieq":
                expr = factory.ieq(field, (String) args[0]);
                break;
            case "between":
                expr = factory.between(field, args[0], args[1]);
                break;
            case "gt":
                expr = factory.gt(field, args[0]);
                break;
            case "ge":
                expr = factory.ge(field, args[0]);
                break;
            case "lt":
                expr = factory.lt(field, args[0]);
                break;
            case "le":
                expr = factory.le(field, args[0]);
                break;
            case "isNull":
                expr = factory.isNull(field);
                break;
            case "notNull":
                expr = factory.isNotNull(field);
                break;
            case "startsWith":
                expr = factory.startsWith(field, (String) args[0]);
                break;
            case "istartsWith":
                expr = factory.istartsWith(field, (String) args[0]);
                break;
            case "endsWith":
                expr = factory.endsWith(field, (String) args[0]);
                break;
            case "iendsWith":
                expr = factory.iendsWith(field, (String) args[0]);
                break;
            case "contains":
                expr = factory.contains(field, (String) args[0]);
                break;
            case "icontains":
                expr = factory.icontains(field, (String) args[0]);
                break;
            case "empty":
                expr = factory.isEmpty(field);
                break;
            case "notEmpty":
                expr = factory.isNotEmpty(field);
                break;
            case "id":
                expr = factory.idEq(args[0]);
                break;
            case "idIn":
                expr = factory.idIn(args);
                break;
            case "in":
                if (args.length == 1) {
                    if (args[0] instanceof QueryExpression) {
                        expr = factory.in(field, ((QueryExpression<?>) args[0]).query);
                    }
                }
                if (expr == null) {
                    expr = factory.in(field, args);
                }
                break;
            case "notIn":
                if (args.length == 1) {
                    if (args[0] instanceof QueryExpression) {
                        expr = factory.notIn(field, ((QueryExpression<?>) args[0]).query);
                    }
                }
                if (expr == null) {
                    expr = factory.notIn(field, args);
                }
                break;
            case "exists":
                if (args.length == 1) {
                    if (args[0] instanceof QueryExpression) {
                        expr = factory.exists(((QueryExpression<?>) args[0]).query);
                    }
                }
                break;
            case "notExists":
                if (args.length == 1) {
                    if (args[0] instanceof QueryExpression) {
                        expr = factory.notExists(((QueryExpression<?>) args[0]).query);
                    }
                }
                break;
            case "not":
                if (args.length > 1) {
                    Query query = invoker.getQuery();
                    Junction junction = factory.junction(Junction.Type.NOT, query, query.where());
                    for (Object e : args) {
                        junction.add((Expression) e);
                    }
                    expr = junction;
                }
                break;
            case "or":
                if (args.length > 1) {
                    Query query = invoker.getQuery();
                    Junction junction = factory.disjunction(query);
                    for (Object e : args) {
                        junction.add((Expression) e);
                    }
                    expr = junction;
                }
                break;
            case "filter":
                SpiExpressionFactory queryEf = (SpiExpressionFactory) factory;
                ExpressionFactory filterEf = queryEf.createExpressionFactory();
                FilterExpression filter = new FilterExpression<>(field, filterEf, invoker.getQuery());
                for (Object e : args) {
                    filter.add((Expression) e);
                }
                expr = filter;
                break;
            case "select":
                Class type = Filters.getBeanTypeByName(field, (SpiEbeanServer) server);
                if (type == null) {
                    throw new QuerySyntaxException(
                            "Can not found bean type: '" + field + "'."
                    );
                }
                Query q = Filters.createQuery(type, server);
                ExpressionList<?> et = q.select((String) args[0]).where();
                for (int i = 1; i < args.length; i++) {
                    et.add((Expression) args[i]);
                }
                Set invalid = q.validate();
                if (invalid != null && !invalid.isEmpty()) {
                    throw new UnprocessableEntityException("Validate query error, can not found " + invalid + " field.");
                }
                expr = QueryExpression.of(q);
        }
        if (expr != null)
            return Transformed.succ(this, expr);
        else
            return Transformed.fail(this);
    }

    private static class QueryExpression<T> implements Expression {
        private Query<T> query;

        public QueryExpression(Query<T> query) {
            this.query = query;
        }

        public static <T> QueryExpression<T> of(Query<T> query) {
            return new QueryExpression<>(query);
        }
    }
}
