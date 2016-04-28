package ameba.message.internal.streaming;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * @author icode
 */
@Singleton
public class FileStreamingProcess extends AbstractStreamingProcess<File> {

    @Override
    public boolean isSupported(Object entity) {
        return entity instanceof File;
    }

    @Override
    public long length(File entity) {
        return entity.length();
    }

    @Override
    protected InputStream getInputStream(File entity) throws IOException {
        return Files.newInputStream(entity.toPath());
    }
}
