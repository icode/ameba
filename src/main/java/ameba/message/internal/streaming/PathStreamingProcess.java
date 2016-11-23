package ameba.message.internal.streaming;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * <p>PathStreamingProcess class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
@Singleton
public class PathStreamingProcess extends AbstractStreamingProcess<Path> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupported(Object entity) {
        return entity instanceof Path;
    }

    /** {@inheritDoc} */
    @Override
    public long length(Path entity) throws IOException {
        return Files.size(entity);
    }

    /** {@inheritDoc} */
    @Override
    protected InputStream getInputStream(Path entity) throws IOException {
        return Files.newInputStream(entity);
    }
}
