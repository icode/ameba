package ameba.db.ebean.filter;

import ameba.db.dsl.ExprArgTransformer;
import ameba.db.dsl.QuerySyntaxException;
import ameba.db.dsl.Transformed;

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
                if (count != 1) {
                    throw new QuerySyntaxException("Query '" + operator + "' command must be 1 argument.");
                }
                break;
            case "between":
                if (count != 2) {
                    throw new QuerySyntaxException("Query 'between' command must be 2 arguments.");
                }
                break;
            case "in":
            case "notIn":
            case "exists":
            case "notExists":
                if (count != 1) {
                    throw new QuerySyntaxException("Query '" + operator + "' command must be 1 argument.");
                }
                break;
            case "filter":
            case "not":
            case "select":
                if (count < 1) {
                    throw new QuerySyntaxException("Query '" + operator + "' command must be arguments > 1.");
                }
                break;
            case "or":
                if (count < 2) {
                    throw new QuerySyntaxException("Query '" + operator + "' command must be arguments > 2.");
                }
                break;
        }

        return Transformed.succ(this, arg);
    }
}
