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
public interface ExprArgTransformer<O, V extends QueryExprInvoker> extends Transformer<Transformed<Val<O>>> {
    Transformed<Val<O>> transform(String field, String operator, Val<O> arg,
                                  int index, int count, V invoker, QueryExprMeta parent);
}