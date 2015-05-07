package ameba.mvc.template.httl.internal;

import httl.Engine;
import httl.spi.loaders.resources.ClasspathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

/**
 * @author icode
 */
public class HttlClasspathResource extends ClasspathResource {

    public HttlClasspathResource(Engine engine, String name, String encoding, String path, Locale locale) {
        super(engine, name, encoding, path, locale);
    }

    public HttlClasspathResource(Engine engine, String name, String encoding, Locale locale) {
        super(engine, name, encoding, name, locale);
    }

    @Override
    public InputStream openStream() throws IOException {
        return super.openStream();
    }

    @Override
    protected URL getUrl() {
        return super.getUrl();
    }

    @Override
    public long getLastModified() {
        return super.getLastModified();
    }

    @Override
    public long getLength() {
        return super.getLength();
    }

    @Override
    public File getFile() {
        return super.getFile();
    }
}
