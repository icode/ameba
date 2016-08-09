package ameba.db.ebean.filter;

import ameba.db.dsl.ExprArgTransformer;
import ameba.db.dsl.QueryExprMeta;
import ameba.db.dsl.QueryExprMeta.Val;
import ameba.db.dsl.QuerySyntaxException;
import ameba.db.dsl.Transformed;
import ameba.db.ebean.filter.CommonExprTransformer.MapExpression;
import ameba.i18n.Messages;
import com.avaje.ebean.Expression;
import org.apache.commons.lang3.ArrayUtils;

/**
 * @author icode
 */
public class CommonExprArgTransformer implements ExprArgTransformer<Expression, EbeanExprInvoker> {
    private static final String[] OPT_STR_ARG = ArrayUtils.toArray(
            "phrase",
            "phrasePre",
            "opAnd",
            "opOr");

    @Override
    public Transformed<Val<Expression>> transform(String field, String operator,
                                                  Val<Expression> arg, int index, int count,
                                                  EbeanExprInvoker invoker,
                                                  QueryExprMeta parent) {
        switch (operator) {
            case "eq":
            case "ne":
            case "ieq":
            case "gt":
            case "ge":
            case "lt":
            case "le":
            case "startsWith":
            case "istartsWith":
            case "endsWith":
            case "iendsWith":
            case "contains":
            case "icontains":
            case "id":
            case "in":
            case "notIn":
            case "exists":
            case "notExists":
                if (count != 1) {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error0", operator));
                }
                break;
            case "between":
                if (count != 2) {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error1", operator));
                }
                break;
            case "filter":
            case "not":
            case "select":
                if (count < 1) {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error2", operator, ">", "1"));
                }
                break;
            case "option":
                String argStr = arg.string();
                if (ArrayUtils.contains(OPT_STR_ARG, argStr)) {
                    arg = Val.<Expression>of(MapExpression.of(argStr, null));
                }
                break;
        }

        return Transformed.succ(this, arg);
    }
}
