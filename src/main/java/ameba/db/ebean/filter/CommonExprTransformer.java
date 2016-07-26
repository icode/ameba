package ameba.db.ebean.filter;

import ameba.db.dsl.ExprTransformer;
import ameba.db.dsl.QuerySyntaxException;
import ameba.db.dsl.Transformed;
import ameba.exception.UnprocessableEntityException;
import ameba.i18n.Messages;
import com.avaje.ebean.*;
import com.avaje.ebean.plugin.BeanType;
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
                if (args.length > 0) {
                    List<Expression> expressionList = Lists.newArrayListWithCapacity(args.length);
                    for (Object e : args) {
                        if (e instanceof Expression) {
                            expressionList.add((Expression) e);
                        } else {
                            throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
                        }
                    }
                    expr = HavingExpression.of(expressionList);
                } else {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
                }
                break;
            case "in":
                if (args.length == 1) {
                    if (args[0] instanceof QueryExpression) {
                        expr = factory.in(field, ((QueryExpression<?>) args[0]).query);
                    } else {
                        throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
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
                    } else {
                        throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
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
                    } else {
                        throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
                    }
                } else {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
                }
                break;
            case "notExists":
                if (args.length == 1) {
                    if (args[0] instanceof QueryExpression) {
                        expr = factory.notExists(((QueryExpression<?>) args[0]).query);
                    } else {
                        throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
                    }
                } else {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
                }
                break;
            case "not":
                if (args.length > 0) {
                    Query query = invoker.getQuery();
                    Junction junction = factory.junction(Junction.Type.NOT, query, query.where());
                    for (Object e : args) {
                        if (e instanceof Expression) {
                            junction.add((Expression) e);
                        } else {
                            throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
                        }
                    }
                    expr = junction;
                } else {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
                }
                break;
            case "or":
                if (args.length > 1) {
                    Query query = invoker.getQuery();
                    Junction junction = factory.disjunction(query);
                    for (Object e : args) {
                        if (e instanceof Expression) {
                            junction.add((Expression) e);
                        } else {
                            throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
                        }
                    }
                    expr = junction;
                } else {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error2", operator));
                }
                break;
            case "filter": {
                if (args.length > 0) {
                    SpiExpressionFactory queryEf = (SpiExpressionFactory) factory;
                    ExpressionFactory filterEf = queryEf.createExpressionFactory();
                    SpiQuery<?> query = invoker.getQuery();
                    FilterExpression filter = new FilterExpression<>(field, filterEf, query);
                    for (Object e : args) {
                        if (e instanceof Expression) {
                            filter.add((Expression) e);
                        } else {
                            throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
                        }
                    }
                    try {
                        BeanType type = query.getBeanDescriptor().getBeanTypeAtPath(field);
                        SpiExpressionValidation validation = new SpiExpressionValidation(type);
                        filter.validate(validation);
                        Set invalid = validation.getUnknownProperties();
                        if (invalid != null && !invalid.isEmpty()) {
                            UnprocessableEntityException.throwQuery(invalid);
                        }

                        expr = filter;
                    } catch (Exception e) {
                        UnprocessableEntityException.throwQuery("[" + field + "]");
                    }
                } else {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
                }
                break;
            }
            case "select":
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
                    expr = QueryExpression.of(q);
                } else {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
                }
                break;
            case "text": {

                break;
            }
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
