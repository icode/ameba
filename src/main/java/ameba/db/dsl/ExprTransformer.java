package ameba.db.dsl;

import org.glassfish.jersey.spi.Contract;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;

/**
 * @author icode
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface ExprTransformer<T, V extends QueryExprInvoker> extends Transformer<Object[], Transformed<T>> {
    Transformed<T> transform(String field, String operator, Object[] arg, V invoker);
}