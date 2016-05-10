package ameba.db.dsl;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.List;

/**
 * @author icode
 */
public class QueryDSL {

    private QueryDSL() {
    }

    public static <T> void invoke(String expression,
                                  QueryExprGenerator<T> invoker,
                                  ApplyExpr<T> applyExpr) {
        QueryExprGenerator.invoke(parse(expression), invoker, applyExpr);
    }

    public static List<QueryExprMeta> parse(String expression) {
        return parse(parser(tokens(expression)));
    }

    public static List<QueryExprMeta> parse(QueryParser parser) {
        QueryExprListener listener = listener(parser);
        parser.query();
        return listener.getQueryExprMetaList();
    }

    public static CommonTokenStream tokens(String expression) {
        return tokens(lexer(expression));
    }

    public static CommonTokenStream tokens(QueryLexer lexer) {
        return new CommonTokenStream(lexer);
    }

    public static QueryLexer lexer(String expression) {
        return new QueryLexer(input(expression));
    }

    public static CharStream input(String expression) {
        ANTLRInputStream input;
        if (expression == null) {
            input = new ANTLRInputStream();
        } else {
            input = new ANTLRInputStream(expression);
        }
        return input;
    }

    public static QueryParser parser(CommonTokenStream tokens) {
        QueryParser parser = new QueryParser(tokens);
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }

    public static QueryExprListener listener() {
        return new QueryExprListener();
    }

    public static QueryExprListener listener(QueryParser parser) {
        QueryExprListener listener = listener();
        parser.addParseListener(listener);
        return listener;
    }
}
