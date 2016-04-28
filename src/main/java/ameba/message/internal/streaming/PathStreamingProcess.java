package ameba.message.internal.streaming;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author icode
 */
@Singleton
public class PathStreamingProcess extends AbstractStreamingProcess<Path> {

    @Override
    public boolean isSupported(Object entity) {
        return entity instanceof Path;
    }

    @Override
    public long length(Path entity) throws IOException {
        return Files.size(entity);
    }

    @Override
    protected InputStream getInputStream(Path entity) throws IOException {
        return Files.newInputStream(entity);
    }
}
