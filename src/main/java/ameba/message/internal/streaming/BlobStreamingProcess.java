package ameba.message.internal.streaming;

import ameba.message.internal.StreamingProcess;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.message.internal.ReaderWriter;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * <p>BlobStreamingProcess class.</p>
 *
 * @author icode
 *
 */
@Singleton
public class BlobStreamingProcess implements StreamingProcess<Blob> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupported(Object entity) {
        return entity instanceof Blob;
    }

    /** {@inheritDoc} */
    @Override
    public long length(Blob entity) throws IOException {
        try {
            return entity.length();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void write(Blob entity, OutputStream output, Long pos, Long length) throws IOException {
        InputStream in;
        try {
            in = entity.getBinaryStream(pos, length);
        } catch (SQLException e) {
            throw new IOException(e);
        }
        try {
            ReaderWriter.writeTo(in, output);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
