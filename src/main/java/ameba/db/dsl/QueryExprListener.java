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

    private ParseTreeProperty<QueryExprMeta> infoMap = new ParseTreeProperty<>();
    private List<QueryExprMeta> queryExprMetaList = Lists.newArrayList();
    private QueryExprMeta queryExprMeta;

    public List<QueryExprMeta> getQueryExprMetaList() {
        return queryExprMetaList;
    }

    @Override
    public void exitSourceElements(QueryParser.SourceElementsContext ctx) {
        infoMap = null;
        queryExprMeta = null;
        queryExprMetaList = Collections.unmodifiableList(queryExprMetaList);
    }

    @Override
    public void exitSourceElement(QueryParser.SourceElementContext ctx) {
        if (queryExprMeta != null) {
            String op = queryExprMeta.operator();
            if (op == null) {
                String co = queryExprMeta.field();
                if (co != null && queryExprMeta.arguments() == null) {
                    int offset = co.lastIndexOf('.');
                    if (offset == -1) {
                        if (queryExprMeta.parent() == null) {
                            queryExprMeta.operator(co);
                            queryExprMeta.field(null);
                        } else {
                            List<Object> args = queryExprMeta.parent().arguments();
                            int argOffset = args.indexOf(queryExprMeta);
                            if (argOffset != -1) {
                                args.set(argOffset, co);
                                queryExprMeta = null;
                            }
                        }
                    } else {
                        queryExprMeta.field(co.substring(0, offset));
                        queryExprMeta.operator(co.substring(offset + 1));
                    }
                }
            }
        }
    }

    @Override
    public void enterSourceElement(QueryParser.SourceElementContext ctx) {
        queryExprMeta = QueryExprMeta.create();
        infoMap.put(ctx, queryExprMeta);
        // root source element
        if (ctx.getParent() instanceof QueryParser.SourceElementsContext) {
            queryExprMetaList.add(queryExprMeta);
        } else {
            QueryExprMeta parentInfo = infoMap.get(getParentSourceElement(ctx)).arguments(queryExprMeta);
            queryExprMeta.parent(parentInfo);
        }
    }

    @Override
    public void exitIdentifierName(QueryParser.IdentifierNameContext ctx) {
        queryExprMeta.operator(ctx.getText());
    }

    @Override
    public void exitFieldExpression(QueryParser.FieldExpressionContext ctx) {
        queryExprMeta.field(ctx.getText());
    }

    @Override
    public void exitLiteral(QueryParser.LiteralContext ctx) {
        if (ctx.NullLiteral() != null) {
            queryExprMeta.arguments((Object) null);
        } else {
            queryExprMeta.arguments(ctx.getText());
        }
    }

    @Override
    public void exitIdentifierVal(QueryParser.IdentifierValContext ctx) {
        queryExprMeta.arguments(ctx.getText());
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