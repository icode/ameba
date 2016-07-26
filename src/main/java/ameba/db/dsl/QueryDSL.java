package ameba.db.dsl;

import ameba.i18n.Messages;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.List;

/**
 * @author icode
 */
public class QueryDSL {

    private static final DiagnosticErrorListener ERROR_LISTENER = new DiagnosticErrorListener();

    private QueryDSL() {
    }

    public static <T> void invoke(String expression,
                                  QueryExprInvoker<T> invoker,
                                  ExprApplier<T> exprApplier) {
        QueryExprInvoker.invoke(parse(expression), invoker, exprApplier);
    }

    public static List<QueryExprMeta> parse(String expression) {
        QueryParser parser = parser(tokens(expression));
        try {
            return parse(parser);
        } catch (ParseCancellationException | RecognitionException e) {
            RecognitionException err;
            if (e instanceof ParseCancellationException) {
                err = (RecognitionException) e.getCause();
            } else {
                err = (RecognitionException) e;
            }
            throw new QuerySyntaxException(
                    Messages.get("dsl.parse.err", err.getOffendingToken().getCharPositionInLine()),
                    e
            );
        }
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
        QueryLexer lexer = new QueryLexer(input(expression));
        lexer.removeErrorListeners();
        lexer.addErrorListener(ERROR_LISTENER);
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
