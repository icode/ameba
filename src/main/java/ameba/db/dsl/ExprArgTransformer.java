package ameba.db.dsl;

import org.glassfish.jersey.spi.Contract;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;

/**
 * @author icode
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface ExprArgTransformer<I, O> {
    O transform(String field, String operator, I arg);
}
