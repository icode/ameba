package ameba.db.dsl;

import ameba.db.dsl.QueryExprMeta.Val;
import org.glassfish.jersey.spi.Contract;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;

/**
 * @author icode
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface ExprTransformer<T, V extends QueryExprInvoker> extends Transformer<Transformed<Val<T>>> {
    Transformed<Val<T>> transform(String field, String operator, Val<T>[] arg, V invoker, QueryExprMeta parent);
}