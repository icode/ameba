package ameba.db.dsl;

import com.google.common.collect.Lists;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.Collections;
import java.util.List;

/**
 * @author icode
 */
public class QueryExprListener extends QueryBaseListener {

    private ParseTreeProperty<QueryExpr> infoMap = new ParseTreeProperty<>();
    private List<QueryExpr> queryExprList = Lists.newArrayList();
    private QueryExpr queryExpr;

    public List<QueryExpr> getQueryExprList() {
        return queryExprList;
    }

    @Override
    public void exitSourceElements(QueryParser.SourceElementsContext ctx) {
        infoMap = null;
        queryExpr = null;
        queryExprList = Collections.unmodifiableList(queryExprList);
    }

    @Override
    public void exitSourceElement(QueryParser.SourceElementContext ctx) {
        if (queryExpr != null) {
            String op = queryExpr.operator();
            if (op == null) {
                String co = queryExpr.field();
                if (co != null && queryExpr.arguments() == null) {
                    int offset = co.lastIndexOf('.');
                    if (offset == -1) {
                        if (queryExpr.parent() == null) {
                            queryExpr.operator(co);
                            queryExpr.field(null);
                        } else {
                            List<Object> args = queryExpr.parent().arguments();
                            int argOffset = args.indexOf(queryExpr);
                            if (argOffset != -1) {
                                args.set(argOffset, co);
                                queryExpr = null;
                            }
                        }
                    } else {
                        queryExpr.field(co.substring(0, offset));
                        queryExpr.operator(co.substring(offset + 1));
                    }
                }
            }
        }
    }

    @Override
    public void enterSourceElement(QueryParser.SourceElementContext ctx) {
        queryExpr = QueryExpr.create();
        infoMap.put(ctx, queryExpr);
        // root source element
        if (ctx.getParent() instanceof QueryParser.SourceElementsContext) {
            queryExprList.add(queryExpr);
        } else {
            QueryExpr parentInfo = infoMap.get(getParentSourceElement(ctx)).arguments(queryExpr);
            queryExpr.parent(parentInfo);
        }
    }

    @Override
    public void exitIdentifierName(QueryParser.IdentifierNameContext ctx) {
        queryExpr.operator(ctx.getText());
    }

    @Override
    public void exitFieldExpression(QueryParser.FieldExpressionContext ctx) {
        queryExpr.field(ctx.getText());
    }

    @Override
    public void exitLiteral(QueryParser.LiteralContext ctx) {
        if (ctx.NullLiteral() != null) {
            queryExpr.arguments((Object) null);
        } else {
            queryExpr.arguments(ctx.getText());
        }
    }

    @Override
    public void exitIdentifierVal(QueryParser.IdentifierValContext ctx) {
        queryExpr.arguments(ctx.getText());
    }

    protected ParserRuleContext getParentSourceElement(ParserRuleContext node) {
        if (node == null) return null;
        ParserRuleContext parent = node.getParent();
        if (parent instanceof QueryParser.SourceElementContext) {
            return parent;
        }
        return getParentSourceElement(parent);
    }
}