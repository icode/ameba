package ameba.mvc.template.httl.internal;

import httl.Engine;
import httl.spi.loaders.resources.InputStreamResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

/**
 * @author icode
 */
public class HttlClasspathResource extends InputStreamResource {

    private URL url;

    public HttlClasspathResource(Engine engine, String name, String path, Locale locale, String encoding) {
        super(engine, name, locale, encoding);
        try {
            url = new URL(path);
        } catch (MalformedURLException e) {
            //no op
        }
    }

    @Override
    public InputStream openStream() throws IOException {
        return url.openStream();
    }

    @Override
    protected URL getUrl() {
        return url;
    }

    @Override
    public long getLength() {
        try {
            URLConnection urlConnection = url.openConnection();
            if (urlConnection != null) {
                return urlConnection.getContentLength();
            }
        } catch (IOException e) {
            // no op
        }
        return super.getLength();
    }

}
