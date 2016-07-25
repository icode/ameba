package ameba.db.ebean.filter;

import ameba.db.dsl.ExprArgTransformer;
import ameba.db.dsl.QuerySyntaxException;
import ameba.db.dsl.Transformed;
import ameba.i18n.Messages;

/**
 * @author icode
 */
public class CommonExprArgTransformer implements ExprArgTransformer<Object, Object, EbeanExprInvoker> {
    @Override
    public Transformed<Object> transform(String field, String operator,
                                         Object arg, int index, int count,
                                         EbeanExprInvoker invoker) {

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
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error1"));
                }
                break;
            case "filter":
            case "not":
            case "select":
                if (count < 1) {
                    throw new QuerySyntaxException(Messages.get("dsl.arguments.error2", operator));
                }
                break;
        }

        return Transformed.succ(this, arg);
    }
}
