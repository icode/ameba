package ameba.message.internal;

import org.glassfish.jersey.spi.Contract;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author icode
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface StreamingProcess<T> {

    boolean isSupported(Object entity);

    long length(T entity) throws IOException;

    void write(T entity, OutputStream output, Long pos, Long length) throws IOException;
}
