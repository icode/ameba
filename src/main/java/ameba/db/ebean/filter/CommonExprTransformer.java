package ameba.db.ebean.filter;

import ameba.db.dsl.ExprTransformer;
import ameba.db.dsl.QueryExprMeta;
import ameba.db.dsl.QueryExprMeta.Val;
import ameba.db.dsl.QuerySyntaxException;
import ameba.db.dsl.Transformed;
import ameba.db.ebean.EbeanUtils;
import ameba.exception.UnprocessableEntityException;
import ameba.i18n.Messages;
import com.avaje.ebean.*;
import com.avaje.ebean.plugin.BeanType;
import com.avaje.ebean.search.*;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiExpressionFactory;
import com.avaje.ebeaninternal.api.SpiExpressionValidation;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author icode
 */
public class CommonExprTransformer implements ExprTransformer<Expression, EbeanExprInvoker> {

    private static ExpressionFactory factory(EbeanExprInvoker invoker) {
        EbeanServer server = invoker.getServer();
        return server.getExpressionFactory();
    }

    public static void fillArgs(String operator, Val<Expression>[] args, ExpressionList<?> et) {
        for (Val<Expression> val : args) {
            if (val.object() instanceof Expression) {
                et.add(val.expr());
            } else {
                throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
            }
        }
    }

    public static Junction<?> junction(String operator, Val<Expression>[] args, Junction.Type type,
                                       EbeanExprInvoker invoker, int checkCount) {
        if (args.length > checkCount) {
            Query query = invoker.getQuery();
            Junction junction = factory(invoker).junction(type, query, query.where());
            fillArgs(operator, args, junction);
            return junction;
        }
        throw new QuerySyntaxException(Messages.get("dsl.arguments.error2", operator, checkCount));
    }

    public static Expression filter(String field, String operator, Val<Expression>[] args, EbeanExprInvoker invoker) {
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
                Set<String> invalid = validation.getUnknownProperties();
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

    public static Expression select(String field, String operator, Val<Expression>[] args, EbeanExprInvoker invoker) {
        if (args.length > 0) {
            EbeanServer server = invoker.getServer();
            Class type = Filters.getBeanTypeByName(field, (SpiEbeanServer) server);
            if (type == null) {
                throw new QuerySyntaxException(Messages.get("dsl.bean.type.err", field));
            }
            Query q = Filters.createQuery(type, server);
            ExpressionList<?> et = q.select(args[0].string()).where();
            for (int i = 1; i < args.length; i++) {
                Object o = args[i].object();
                if (o instanceof HavingExpression) {
                    ExpressionList having = q.having();
                    for (Expression he : ((HavingExpression) o).expressionList) {
                        having.add(he);
                    }
                } else if (o instanceof AsOfExpression) {
                    et.asOf(((AsOfExpression) o).timestamp);
                } else if (o instanceof DistinctExpression) {
                    et.setDistinct(((DistinctExpression) o).distinct);
                } else if (o instanceof Expression) {
                    et.add(args[i].expr());
                } else {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
                }
            }
            EbeanUtils.checkQuery(q, invoker.getLocator());
            return QueryExpression.of(q);
        }
        throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
    }

    public static Expression having(String operator, Val<Expression>[] args) {
        if (args.length > 0) {
            return HavingExpression.of(transformArgsToList(operator, args));
        }
        throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
    }


    public static Expression in(String field, String operator, Val<Expression>[] args, EbeanExprInvoker invoker) {
        if (args.length == 1) {
            if (args[0].object() instanceof QueryExpression) {
                return factory(invoker).in(field, ((QueryExpression<?>) args[0].object()).query);
            }
            throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
        }
        return factory(invoker).in(field, args);
    }

    public static Expression notIn(String field, String operator, Val<Expression>[] args, EbeanExprInvoker invoker) {
        if (args.length == 1) {
            if (args[0].object() instanceof QueryExpression) {
                return factory(invoker).notIn(field, ((QueryExpression<?>) args[0].object()).query);
            }
            throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
        }
        return factory(invoker).notIn(field, args);
    }

    public static Expression exists(String operator, Val<Expression>[] args, EbeanExprInvoker invoker) {
        if (args.length == 1) {
            if (args[0].object() instanceof QueryExpression) {
                return factory(invoker).exists(((QueryExpression<?>) args[0].object()).query);
            }
            throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
        }
        throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
    }

    public static Expression notExists(String operator, Val<Expression>[] args, EbeanExprInvoker invoker) {
        if (args.length == 1) {
            if (args[0].object() instanceof QueryExpression) {
                return factory(invoker).notExists(((QueryExpression<?>) args[0].object()).query);
            }
            throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
        }
        throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
    }

    public static Expression asOf(Val<Expression>[] args) {
        if (args.length != 1) {
            throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", "asOf"));
        }
        return AsOfExpression.of(args[0].string());
    }

    public static Expression match(String field, String operator, Val<Expression>[] args, EbeanExprInvoker invoker) {
        if (field != null) {
            //field.match(text)
            //field.match(text,option(opAnd, phrase))
            if (args.length < 2) {
                throw new QuerySyntaxException(Messages.get("dsl.arguments.error2", operator, 1));
            }
            Match match = null;
            if (args[1].object() instanceof TextOptionsExpression) {
                match = new Match();
                Map<String, Val<Expression>> ops = ((TextOptionsExpression) args[1].object()).options;
                for (String o : ops.keySet()) {
                    switch (o) {
                        case "phrase":
                            match.phrase();
                            break;
                        case "phrasePre":
                            match.phrasePrefix();
                            break;
                        case "opAnd":
                            match.opAnd();
                            break;
                        case "opOr":
                            match.opOr();
                            break;
                        case "terms":
                            match.zeroTerms(ops.get(o).string());
                            break;
                        case "cutoff":
                            match.cutoffFrequency(ops.get(o).doubleV());
                            break;
                        case "maxExp":
                            match.maxExpansions(ops.get(o).integer());
                            break;
                        case "analyzer":
                            match.analyzer(ops.get(o).string());
                            break;
                        case "boost":
                            match.boost(ops.get(o).doubleV());
                            break;
                        case "minMatch":
                            match.minShouldMatch(ops.get(o).string());
                            break;
                        case "rewrite":
                            match.rewrite(ops.get(o).string());
                            break;
                    }
                }
            }
            return factory(invoker).textMatch(field, args[0].string(), match);
        } else if (args.length > 1) {
            MultiMatch match;
            if (args.length == 2) {
                //match(text, options(fields(f1,f2,f3),opOr))
                if (args[1].object() instanceof TextOptionsExpression) {
                    Map<String, Val<Expression>> ops = ((TextOptionsExpression) args[1].expr()).options;
                    Val<Expression> val = ops.get("fields");
                    if (val == null) {
                        throw new QuerySyntaxException(
                                Messages.get("dsl.arguments.error4", "option", 1, "fields")
                        );
                    }
                    match = MultiMatch.fields(((TextFieldsExpression) val.expr()).fields);
                    for (String o : ops.keySet()) {
                        switch (o) {
                            case "type": {
                                Val v = ops.get(o);
                                match.type((MultiMatch.Type) v.enumV(MultiMatch.Type.class));
                                break;
                            }
                            case "tie":
                                match.tieBreaker(ops.get(o).doubleV());
                                break;
                            case "opAnd":
                                match.opAnd();
                                break;
                            case "opOr":
                                match.opOr();
                                break;
                            case "terms":
                                match.zeroTerms(ops.get(o).string());
                                break;
                            case "cutoff":
                                match.cutoffFrequency(ops.get(o).doubleV());
                                break;
                            case "maxExp":
                                match.maxExpansions(ops.get(o).integer());
                                break;
                            case "analyzer":
                                match.analyzer(ops.get(o).string());
                                break;
                            case "boost":
                                match.boost(ops.get(o).doubleV());
                                break;
                            case "minMatch":
                                match.minShouldMatch(ops.get(o).string());
                                break;
                            case "rewrite":
                                match.rewrite(ops.get(o).string());
                                break;
                        }
                    }
                } else {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error4", operator, 1, "option"));
                }
            } else {
                //match(text, field0, field1, field2)
                String[] fields = new String[args.length - 1];
                for (int i = 1; i < args.length; i++) {
                    fields[i - 1] = args[i].string();
                }
                match = MultiMatch.fields(fields);
            }
            return factory(invoker).textMultiMatch(args[0].string(), match);
        } else {
            throw new QuerySyntaxException(Messages.get("dsl.arguments.error2", operator, 2));
        }
    }

    public static Expression textQueryString(String operator, Val<Expression>[] args, EbeanExprInvoker invoker) {
        checkTextOptions(operator, args);
        Map<String, Val<Expression>> ops = ((TextOptionsExpression) args[1].expr()).options;
        Val<Expression> val = ops.get("fields");
        if (val == null) {
            throw new QuerySyntaxException(
                    Messages.get("dsl.arguments.error4", "option", 1, "fields")
            );
        }
        TextQueryString queryString = TextQueryString.fields(((TextFieldsExpression) val.expr()).fields);
        for (String k : ops.keySet()) {
            switch (k) {
                case "opAnd":
                    queryString.opAnd();
                    break;
                case "opOr":
                    queryString.opOr();
                    break;
                case "locale":
                    queryString.locale(ops.get(k).string());
                    break;
                case "lenient":
                    queryString.lenient(ops.get(k).bool());
                    break;
                case "minMatch":
                    queryString.minShouldMatch(ops.get(k).string());
                    break;
                case "analyzer":
                    queryString.analyzer(ops.get(k).string());
                    break;
                case "disMax":
                    queryString.useDisMax(ops.get(k).bool());
                    break;
                case "tie":
                    queryString.tieBreaker(ops.get(k).doubleV());
                    break;
                case "defaultField":
                    queryString.defaultField(ops.get(k).string());
                    break;
                case "leadingWildcard":
                    queryString.allowLeadingWildcard(ops.get(k).bool());
                    break;
                case "lowerExp":
                    queryString.lowercaseExpandedTerms(ops.get(k).bool());
                    break;
                case "positionIncr":
                    queryString.enablePositionIncrements(ops.get(k).bool());
                    break;
                case "fuzzyMaxExp":
                    queryString.fuzzyMaxExpansions(ops.get(k).integer());
                    break;
                case "fuzziness":
                    queryString.fuzziness(ops.get(k).string());
                    break;
                case "fuzzyPreLen":
                    queryString.fuzzyPrefixLength(ops.get(k).integer());
                    break;
                case "phraseSlop":
                    queryString.phraseSlop(ops.get(k).doubleV());
                    break;
                case "boost":
                    queryString.boost(ops.get(k).doubleV());
                    break;
                case "analyzeWildcard":
                    queryString.analyzeWildcard(ops.get(k).bool());
                    break;
                case "autoPhrase":
                    queryString.autoGeneratePhraseQueries(ops.get(k).bool());
                    break;
                case "maxDeterminized":
                    queryString.maxDeterminizedStates(ops.get(k).integer());
                    break;
                case "timeZone":
                    queryString.timeZone(ops.get(k).string());
                    break;
                case "rewrite":
                    queryString.rewrite(ops.get(k).string());
                    break;
            }
        }
        return factory(invoker).textQueryString(args[0].string(), queryString);
    }

    public static Expression textCommonTerms(String operator, Val<Expression>[] args, EbeanExprInvoker invoker) {
        checkTextOptions(operator, args);
        Map<String, Val<Expression>> ops = ((TextOptionsExpression) args[1].expr()).options;
        TextCommonTerms common = new TextCommonTerms();
        for (String k : ops.keySet()) {
            switch (k) {
                case "cutoff":
                    common.cutoffFrequency(ops.get(k).doubleV());
                    break;
                case "lowFreqAnd":
                    common.lowFreqOperatorAnd(ops.get(k).bool());
                    break;
                case "highFreqAnd":
                    common.highFreqOperatorAnd(ops.get(k).bool());
                    break;
                case "minMatch":
                    common.minShouldMatch(ops.get(k).string());
                    break;
                case "minMatchLowFreq":
                    common.minShouldMatchLowFreq(ops.get(k).string());
                    break;
                case "minMatchHighFreq":
                    common.minShouldMatchHighFreq(ops.get(k).string());
                    break;
            }
        }
        return factory(invoker).textCommonTerms(args[0].string(), common);
    }

    private static void checkTextOptions(String operator, Val<Expression>[] args) {
        if (args.length == 2) {
            throw new QuerySyntaxException(Messages.get("dsl.arguments.error2", operator));
        }
        if (!(args[1].object() instanceof TextOptionsExpression)) {
            throw new QuerySyntaxException(Messages.get("dsl.arguments.error4", operator, 1, "option"));
        }
    }

    public static Expression textSimple(String operator, Val<Expression>[] args, EbeanExprInvoker invoker) {
        checkTextOptions(operator, args);
        Map<String, Val<Expression>> ops = ((TextOptionsExpression) args[1].expr()).options;
        TextSimple simple = new TextSimple();
        for (String k : ops.keySet()) {
            switch (k) {
                case "fields":
                    simple.fields(((TextFieldsExpression) ops.get(k).expr()).fields);
                    break;
                case "opAnd":
                    simple.opAnd();
                    break;
                case "opOr":
                    simple.opOr();
                    break;
                case "analyzer":
                    simple.analyzer(ops.get(k).string());
                    break;
                case "flags":
                    simple.flags(ops.get(k).string());
                    break;
                case "lowerExp":
                    simple.lowercaseExpandedTerms(ops.get(k).bool());
                    break;
                case "analyzeWildcard":
                    simple.analyzeWildcard(ops.get(k).bool());
                    break;
                case "locale":
                    simple.locale(ops.get(k).string());
                    break;
                case "lenient":
                    simple.lenient(ops.get(k).bool());
                    break;
                case "minMatch":
                    simple.minShouldMatch(ops.get(k).string());
                    break;
            }
        }
        return factory(invoker).textSimple(args[0].string(), simple);
    }

    public static Expression options(String operator, Val<Expression>[] args) {
        if (args.length < 1) {
            throw new QuerySyntaxException(Messages.get("dsl.arguments.error2", operator, 0));
        }
        TextOptionsExpression op = new TextOptionsExpression();
        for (Val v : args) {
            if (v.object() instanceof MapExpression) {
                MapExpression map = ((MapExpression) v.expr());
                op.options.put(map.key, map.value);
            } else {
                op.options.put(v.string(), null);
            }
        }
        return op;
    }

    public static Object transformArgs(Val<Expression>[] args) {
        Object[] objects = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            objects[i] = args[i].object();
        }
        return objects;
    }

    public static List<Expression> transformArgsToList(String operator, Val<Expression>[] args) {
        List<Expression> et = Lists.newArrayListWithCapacity(args.length);
        for (Val<Expression> e : args) {
            if (e.object() instanceof Expression) {
                et.add(e.expr());
            } else {
                throw new QuerySyntaxException(Messages.get("dsl.arguments.error3", operator));
            }
        }
        return et;
    }

    @Override
    public Transformed<Val<Expression>> transform(String field, String operator,
                                                  Val<Expression>[] args,
                                                  EbeanExprInvoker invoker,
                                                  QueryExprMeta parent) {
        EbeanServer server = invoker.getServer();
        ExpressionFactory factory = server.getExpressionFactory();
        Expression expr = null;
        switch (operator) {
            case "eq":
                expr = factory.eq(field, args[0].object());
                break;
            case "ne":
                expr = factory.ne(field, args[0].object());
                break;
            case "ieq":
                expr = factory.ieq(field, args[0].string());
                break;
            case "between":
                expr = factory.between(field, args[0].object(), args[1].object());
                break;
            case "gt":
                expr = factory.gt(field, args[0].object());
                break;
            case "ge":
                expr = factory.ge(field, args[0].object());
                break;
            case "lt":
                expr = factory.lt(field, args[0].object());
                break;
            case "le":
                expr = factory.le(field, args[0].object());
                break;
            case "isNull":
                expr = factory.isNull(field);
                break;
            case "notNull":
                expr = factory.isNotNull(field);
                break;
            case "startsWith":
                expr = factory.startsWith(field, args[0].string());
                break;
            case "istartsWith":
                expr = factory.istartsWith(field, args[0].string());
                break;
            case "endsWith":
                expr = factory.endsWith(field, args[0].string());
                break;
            case "iendsWith":
                expr = factory.iendsWith(field, args[0].string());
                break;
            case "contains":
                expr = factory.contains(field, args[0].string());
                break;
            case "icontains":
                expr = factory.icontains(field, args[0].string());
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
                expr = factory.idIn(transformArgs(args));
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
                expr = select(field, operator, args, invoker);
                break;
            case "distinct":
                boolean dis = true;
                if (args.length > 1) {
                    Object o = args[0].object();
                    if (o instanceof Boolean || o instanceof BigDecimal) {
                        dis = args[0].bool();
                    }
                }
                expr = new DistinctExpression(dis);
                break;
            case "asOf":
                expr = asOf(args);
                break;
            case "text":
                if (args.length < 1) {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error2", operator, 0));
                }
                expr = TextExpression.of(transformArgsToList(operator, args));
                break;
            case "match":
                expr = match(field, operator, args, invoker);
                break;
            case "simple":
                expr = textSimple(operator, args, invoker);
                break;
            case "query":
                expr = textQueryString(operator, args, invoker);
                break;
            case "common":
                expr = textCommonTerms(operator, args, invoker);
                break;
            case "option":
                expr = options(operator, args);
                break;
            case "fields":
                if (args.length < 1) {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error2", operator, 0));
                }
                expr = TextFieldsExpression.of(args);
                break;
            case "phrase":
            case "phrasePre":
            case "opAnd":
            case "opOr":
                expr = MapExpression.of(operator, null);
                break;
            case "type":
            case "tie":
            case "terms":
            case "cutoff":
            case "maxExp":
            case "analyzer":
            case "boost":
            case "minMatch":
            case "rewrite":
            case "disMax":
            case "defaultField":
            case "leadingWildcard":
            case "lowerExp":
            case "positionIncr":
            case "fuzzyMaxExp":
            case "fuzziness":
            case "fuzzyPreLen":
            case "phraseSlop":
            case "analyzeWildcard":
            case "autoPhrase":
            case "maxDeterminized":
            case "timeZone":
            case "lowFreqAnd":
            case "highFreqAnd":
            case "minMatchLowFreq":
            case "minMatchHighFreq":
                if (args.length != 1) {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
                }
                expr = MapExpression.of(operator, args[0]);
                break;
        }
        if (expr != null)
            return Transformed.succ(this, Val.of(expr));
        else
            return Transformed.fail(this);
    }

    private static class TextOptionsExpression implements Expression {
        private Map<String, Val<Expression>> options = Maps.newLinkedHashMap();
    }

    static class MapExpression implements Expression {
        private String key;
        private Val<Expression> value;

        MapExpression(String key, Val<Expression> value) {
            this.key = key;
            this.value = value;
        }

        public static MapExpression of(String key, Val<Expression> v) {
            return new MapExpression(key, v);
        }
    }

    private static class TextFieldsExpression implements Expression {
        private String[] fields;

        public static TextFieldsExpression of(Val<Expression>[] args) {
            TextFieldsExpression e = new TextFieldsExpression();
            e.fields = new String[args.length];
            for (int i = 0; i < args.length; i++) {
                e.fields[i] = args[i].string();
            }
            return e;
        }
    }

    private static class QueryExpression<T> implements Expression {
        private Query<T> query;

        QueryExpression(Query<T> query) {
            this.query = query;
        }

        public static <T> QueryExpression<T> of(Query<T> query) {
            return new QueryExpression<>(query);
        }
    }

    static class TextExpression implements Expression {
        private List<Expression> expressionList;

        TextExpression(List<Expression> expressionList) {
            this.expressionList = expressionList;
        }

        public static TextExpression of(List<Expression> expressionList) {
            return new TextExpression(expressionList);
        }

        public List<Expression> getExpressionList() {
            return expressionList;
        }
    }

    static class DistinctExpression implements Expression {
        boolean distinct;

        public DistinctExpression(boolean distinct) {
            this.distinct = distinct;
        }
    }

    static class AsOfExpression implements Expression {
        private Timestamp timestamp;

        AsOfExpression(Timestamp timestamp) {
            this.timestamp = timestamp;
        }

        public static AsOfExpression of(String time) {
            if (time.length() == 8 || time.length() == 10) {
                time += "0:0:0";
            }
            return new AsOfExpression(Timestamp.valueOf(time));
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }
    }

    static class HavingExpression implements Expression {
        private List<Expression> expressionList;

        HavingExpression(List<Expression> expressionList) {
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
