package ameba.message.internal.streaming;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author icode
 */
@Singleton
public class BytesStreamingProcess extends AbstractStreamingProcess<byte[]> {

    @Override
    public boolean isSupported(Object entity) {
        return entity != null && entity.getClass() == byte[].class;
    }

    @Override
    public long length(byte[] entity) throws IOException {
        return entity.length;
    }

    @Override
    protected InputStream getInputStream(byte[] entity) throws IOException {
        return new ByteArrayInputStream(entity);
    }
}
