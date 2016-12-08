package ameba.message.internal.streaming;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>BytesStreamingProcess class.</p>
 *
 * @author icode
 *
 */
@Singleton
public class BytesStreamingProcess extends AbstractStreamingProcess<byte[]> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupported(Object entity) {
        return entity != null && entity.getClass() == byte[].class;
    }

    /** {@inheritDoc} */
    @Override
    public long length(byte[] entity) throws IOException {
        return entity.length;
    }

    /** {@inheritDoc} */
    @Override
    protected InputStream getInputStream(byte[] entity) throws IOException {
        return new ByteArrayInputStream(entity);
    }
}
