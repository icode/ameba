package ameba.db.dsl;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.List;

/**
 * @author icode
 */
public class QueryDSL {

    private static final ANTLRErrorListener DEFAULT_ERROR_LISTENER = new ThrowErrorListener();

    private QueryDSL() {
    }

    public static List<QueryExpr> parse(String expression) {
        return parse(parser(tokens(expression)));
    }

    public static List<QueryExpr> parse(QueryParser parser) {
        QueryExprListener listener = listener(parser);
        parser.query();
        return listener.getQueryExprList();
    }

    public static CommonTokenStream tokens(String expression) {
        return tokens(lexer(expression));
    }

    public static CommonTokenStream tokens(QueryLexer lexer) {
        return new CommonTokenStream(lexer);
    }

    public static QueryLexer lexer(String expression) {
        QueryLexer lexer = new QueryLexer(input(expression));
        lexer.removeErrorListeners();
        lexer.addErrorListener(DEFAULT_ERROR_LISTENER);
        return lexer;
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
        parser.removeErrorListeners();
        parser.addErrorListener(DEFAULT_ERROR_LISTENER);
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
