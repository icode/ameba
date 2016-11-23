package ameba.message.internal;

import org.glassfish.jersey.spi.Contract;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>StreamingProcess interface.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
@Contract
@ConstrainedTo(RuntimeType.SERVER)
public interface StreamingProcess<T> {

    /**
     * <p>isSupported.</p>
     *
     * @param entity a {@link java.lang.Object} object.
     * @return a boolean.
     */
    boolean isSupported(Object entity);

    /**
     * <p>length.</p>
     *
     * @param entity a T object.
     * @return a long.
     * @throws java.io.IOException if any.
     */
    long length(T entity) throws IOException;

    /**
     * <p>write.</p>
     *
     * @param entity a T object.
     * @param output a {@link java.io.OutputStream} object.
     * @param pos    a {@link java.lang.Long} object.
     * @param length a {@link java.lang.Long} object.
     * @throws java.io.IOException if any.
     */
    void write(T entity, OutputStream output, Long pos, Long length) throws IOException;
}
