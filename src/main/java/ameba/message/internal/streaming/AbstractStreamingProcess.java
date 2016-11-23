package ameba.message.internal.streaming;

import ameba.message.internal.StreamingProcess;
import com.google.common.io.ByteStreams;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.message.internal.ReaderWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>Abstract AbstractStreamingProcess class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public abstract class AbstractStreamingProcess<T> implements StreamingProcess<T> {
    /**
     * <p>getInputStream.</p>
     *
     * @param entity a T object.
     * @return a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     */
    protected abstract InputStream getInputStream(T entity) throws IOException;

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(T entity, OutputStream output, Long pos, Long length) throws IOException {
        InputStream in = getInputStream(entity);
        if (pos != null && pos > 0) {
            in.skip(pos);
        }
        if (length != null && length > 0) {
            in = ByteStreams.limit(in, length);
        }
        try {
            ReaderWriter.writeTo(in, output);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
