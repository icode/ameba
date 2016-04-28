package ameba.message.internal.streaming;

import ameba.message.internal.StreamingProcess;
import com.google.common.io.ByteStreams;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.message.internal.ReaderWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author icode
 */
public abstract class AbstractStreamingProcess<T> implements StreamingProcess<T> {
    protected abstract InputStream getInputStream(T entity) throws IOException;

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
