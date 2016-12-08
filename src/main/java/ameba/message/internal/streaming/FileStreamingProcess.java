package ameba.message.internal.streaming;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * <p>FileStreamingProcess class.</p>
 *
 * @author icode
 *
 */
@Singleton
public class FileStreamingProcess extends AbstractStreamingProcess<File> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupported(Object entity) {
        return entity instanceof File;
    }

    /** {@inheritDoc} */
    @Override
    public long length(File entity) {
        return entity.length();
    }

    /** {@inheritDoc} */
    @Override
    protected InputStream getInputStream(File entity) throws IOException {
        return Files.newInputStream(entity.toPath());
    }
}
