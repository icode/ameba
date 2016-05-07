package ameba.db.dsl;

import com.google.common.collect.Lists;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.Collections;
import java.util.List;

/**
 * @author icode
 */
public class QueryInfoListener extends QueryBaseListener {

    private ParseTreeProperty<QueryInfo> infoMap = new ParseTreeProperty<>();
    private List<QueryInfo> queryInfoList = Lists.newArrayList();
    private QueryInfo info;

    public List<QueryInfo> getQueryInfoList() {
        return queryInfoList;
    }

    @Override
    public void exitSourceElements(QueryParser.SourceElementsContext ctx) {
        infoMap = null;
        info = null;
        queryInfoList = Collections.unmodifiableList(queryInfoList);
    }

    @Override
    public void exitSourceElement(QueryParser.SourceElementContext ctx) {
        if (info != null) {
            String op = info.operator();
            if (op == null) {
                String co = info.column();
                if (co != null && info.arguments() == null) {
                    int offset = co.lastIndexOf('.');
                    if (offset == -1) {
                        if (info.parent() == null) {
                            info.operator(co);
                            info.column(null);
                        } else {
                            List<Object> args = info.parent().arguments();
                            int argOffset = args.indexOf(info);
                            if (argOffset != -1) {
                                args.set(argOffset, co);
                                info = null;
                            }
                        }
                    } else {
                        info.column(co.substring(0, offset));
                        info.operator(co.substring(offset + 1));
                    }
                }
            }
        }
    }

    @Override
    public void enterSourceElement(QueryParser.SourceElementContext ctx) {
        info = QueryInfo.create();
        infoMap.put(ctx, info);
        // root source element
        if (ctx.getParent() instanceof QueryParser.SourceElementsContext) {
            queryInfoList.add(info);
        } else {
            QueryInfo parentInfo = infoMap.get(getParentSourceElement(ctx)).arguments(info);
            info.parent(parentInfo);
        }
    }

    @Override
    public void exitIdentifierName(QueryParser.IdentifierNameContext ctx) {
        info.operator(ctx.getText());
    }

    @Override
    public void exitFieldExpression(QueryParser.FieldExpressionContext ctx) {
        info.column(ctx.getText());
    }

    @Override
    public void exitLiteral(QueryParser.LiteralContext ctx) {
        if (ctx.NullLiteral() != null) {
            info.arguments((Object) null);
        } else {
            info.arguments(ctx.getText());
        }
    }

    @Override
    public void exitIdentifierVal(QueryParser.IdentifierValContext ctx) {
        info.arguments(ctx.getText());
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