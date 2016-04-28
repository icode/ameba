package ameba.message.internal.streaming;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author icode
 */
@Singleton
public class InputStreamingProcess extends AbstractStreamingProcess<InputStream> {

    @Override
    public boolean isSupported(Object entity) {
        return entity instanceof InputStream;
    }

    @Override
    public long length(InputStream entity) throws IOException {
        return -1;
    }

    @Override
    protected InputStream getInputStream(InputStream entity) throws IOException {
        return entity;
    }
}
