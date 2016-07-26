package ameba.db.ebean.filter;

import ameba.db.dsl.ExprTransformer;
import ameba.db.dsl.QuerySyntaxException;
import ameba.db.dsl.Transformed;
import ameba.exception.UnprocessableEntityException;
import ameba.i18n.Messages;
import com.avaje.ebean.*;
import com.avaje.ebean.plugin.BeanType;
import com.avaje.ebean.search.MultiMatch;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiExpressionFactory;
import com.avaje.ebeaninternal.api.SpiExpressionValidation;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;

/**
 * @author icode
 */
public class CommonExprTransformer implements ExprTransformer<Expression, EbeanExprInvoker> {

    private static ExpressionFactory factory(EbeanExprInvoker invoker) {
        EbeanServer server = invoker.getServer();
        return server.getExpressionFactory();
    }

    public static void fillArgs(String operator, Object[] args, ExpressionList<?> et) {
        for (Object e : args) {
            if (e instanceof Expression) {
                et.add((Expression) e);
            } else {
                throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
            }
        }
    }

    public static Junction<?> junction(String operator, Object[] args, Junction.Type type,
                                       EbeanExprInvoker invoker, int checkCount) {
        if (args.length > checkCount) {
            Query query = invoker.getQuery();
            Junction junction = factory(invoker).junction(type, query, query.where());
            fillArgs(operator, args, junction);
            return junction;
        }
        throw new QuerySyntaxException(Messages.get("dsl.arguments.error2", operator, checkCount));
    }

    public static Expression filter(String field, String operator, Object[] args, EbeanExprInvoker invoker) {
        if (args.length > 0) {
            SpiExpressionFactory queryEf = (SpiExpressionFactory) invoker.getServer().getExpressionFactory();
            ExpressionFactory filterEf = queryEf.createExpressionFactory();
            SpiQuery<?> query = invoker.getQuery();
            FilterExpression filter = new FilterExpression<>(field, filterEf, query);
            fillArgs(operator, args, filter);
            try {
                BeanType type = query.getBeanDescriptor().getBeanTypeAtPath(field);
                SpiExpressionValidation validation = new SpiExpressionValidation(type);
                filter.validate(validation);
                Set invalid = validation.getUnknownProperties();
                if (invalid != null && !invalid.isEmpty()) {
                    UnprocessableEntityException.throwQuery(invalid);
                }

                return filter;
            } catch (Exception e) {
                UnprocessableEntityException.throwQuery("[" + field + "]");
            }
        }
        throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
    }

    public static Expression select(String field, String operator, Object[] args, EbeanServer server) {
        if (args.length > 0) {
            Class type = Filters.getBeanTypeByName(field, (SpiEbeanServer) server);
            if (type == null) {
                throw new QuerySyntaxException(Messages.get("dsl.bean.type.err", field));
            }
            Query q = Filters.createQuery(type, server);
            ExpressionList<?> et = q.select((String) args[0]).where();
            for (int i = 1; i < args.length; i++) {
                if (args[i] instanceof HavingExpression) {
                    ExpressionList having = q.having();
                    for (Expression he : ((HavingExpression) args[i]).expressionList) {
                        having.add(he);
                    }
                } else if (args[i] instanceof Expression) {
                    et.add((Expression) args[i]);
                } else {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
                }
            }
            Set invalid = q.validate();
            if (invalid != null && !invalid.isEmpty()) {
                UnprocessableEntityException.throwQuery(invalid);
            }
            return QueryExpression.of(q);
        }
        throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
    }

    public static Expression having(String operator, Object[] args) {
        if (args.length > 0) {
            List<Expression> et = Lists.newArrayListWithCapacity(args.length);
            for (Object e : args) {
                if (e instanceof Expression) {
                    et.add((Expression) e);
                } else {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
                }
            }
            return HavingExpression.of(et);
        }
        throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
    }

    public static Expression in(String field, String operator, Object[] args, EbeanExprInvoker invoker) {
        if (args.length == 1) {
            if (args[0] instanceof QueryExpression) {
                return factory(invoker).in(field, ((QueryExpression<?>) args[0]).query);
            } else {
                throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
            }
        }
        return factory(invoker).in(field, args);
    }

    public static Expression notIn(String field, String operator, Object[] args, EbeanExprInvoker invoker) {
        if (args.length == 1) {
            if (args[0] instanceof QueryExpression) {
                return factory(invoker).notIn(field, ((QueryExpression<?>) args[0]).query);
            } else {
                throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
            }
        }
        return factory(invoker).notIn(field, args);
    }

    public static Expression exists(String operator, Object[] args, EbeanExprInvoker invoker) {
        if (args.length == 1) {
            if (args[0] instanceof QueryExpression) {
                return factory(invoker).exists(((QueryExpression<?>) args[0]).query);
            } else {
                throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
            }
        }
        throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
    }

    public static Expression notExists(String operator, Object[] args, EbeanExprInvoker invoker) {
        if (args.length == 1) {
            if (args[0] instanceof QueryExpression) {
                return factory(invoker).notExists(((QueryExpression<?>) args[0]).query);
            } else {
                throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
            }
        }
        throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
    }

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
            case "having":
                expr = having(operator, args);
                break;
            case "in":
                expr = in(field, operator, args, invoker);
                break;
            case "notIn":
                expr = notIn(field, operator, args, invoker);
                break;
            case "exists":
                expr = exists(operator, args, invoker);
                break;
            case "notExists":
                expr = notExists(operator, args, invoker);
                break;
            case "not":
                expr = junction(operator, args, Junction.Type.NOT, invoker, 0);
                break;
            case "or":
                expr = junction(operator, args, Junction.Type.OR, invoker, 1);
                break;
            case "must":
                expr = junction(operator, args, Junction.Type.MUST, invoker, 0);
                break;
            case "should":
                expr = junction(operator, args, Junction.Type.SHOULD, invoker, 0);
                break;
            case "mustNot":
                expr = junction(operator, args, Junction.Type.MUST_NOT, invoker, 0);
                break;
            case "filter":
                expr = filter(field, operator, args, invoker);
                break;
            case "select":
                expr = select(field, operator, args, server);
                break;
            case "text":
                if (args.length > 0) {

                } else {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error2", operator, 0));
                }
                break;
            case "match":
                if (field != null) {
                    //field.match(text)
                    if (args.length != 1) {
                        throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", field + "." + operator));
                    } else {
                        expr = factory.textMatch(field, (String) args[0], null);
                    }
                } else if (args.length > 2) {
                    //match(text, field0, field1, field2)
                    String[] fields = new String[args.length - 1];
                    for (int i = 1; i < args.length; i++) {
                        fields[i - 1] = (String) args[i];
                    }
                    expr = factory.textMultiMatch((String) args[0], MultiMatch.fields(fields));
                } else {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error2", operator, 2));
                }
                break;
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

    static class HavingExpression implements Expression {
        private List<Expression> expressionList;

        public HavingExpression(List<Expression> expressionList) {
            this.expressionList = expressionList;
        }

        public static HavingExpression of(List<Expression> expressionList) {
            return new HavingExpression(expressionList);
        }

        public List<Expression> getExpressionList() {
            return expressionList;
        }
    }
}
