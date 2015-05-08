package ameba.mvc.template.httl.internal;

import httl.Engine;
import httl.spi.loaders.resources.ClasspathResource;
import httl.spi.loaders.resources.InputStreamResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.jar.JarFile;

/**
 * @author icode
 */
public class HttlClasspathResource extends InputStreamResource {

    public HttlClasspathResource(Engine engine, String name, Locale locale, String encoding) {
        super(engine, name, locale, encoding);
    }

    @Override
    public InputStream openStream() throws IOException {
        return null;
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
//        try {
//            JarFile jarFile = new JarFile(file);
//            try {
//                return jarFile.getEntry(getName()).getSize();
//            } finally {
//                jarFile.close();
//            }
//        } catch (IOException e) {
//            return super.getLength();
//        }
        return super.getLength();
    }
}
