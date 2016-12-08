package ameba.message.internal.streaming;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>InputStreamingProcess class.</p>
 *
 * @author icode
 *
 */
@Singleton
public class InputStreamingProcess extends AbstractStreamingProcess<InputStream> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupported(Object entity) {
        return entity instanceof InputStream;
    }

    /** {@inheritDoc} */
    @Override
    public long length(InputStream entity) throws IOException {
        return -1;
    }

    /** {@inheritDoc} */
    @Override
    protected InputStream getInputStream(InputStream entity) throws IOException {
        return entity;
    }
}
