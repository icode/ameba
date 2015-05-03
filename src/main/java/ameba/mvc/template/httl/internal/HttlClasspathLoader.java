package ameba.mvc.template.httl.internal;

import ameba.util.IOUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import httl.Resource;
import httl.spi.loaders.AbstractLoader;
import httl.spi.loaders.resources.ClasspathResource;
import httl.util.UrlUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author icode
 */
public class HttlClasspathLoader extends AbstractLoader {
    @Override
    public List<String> doList(String directory, String suffix) throws IOException {
        Set<String> set = Sets.newHashSet();
        Enumeration<URL> urls = IOUtils.getResources(cleanPath(directory));
        while (urls.hasMoreElements()) {
            List<String> res = UrlUtils.listUrl(urls.nextElement(), suffix);
            for (String s : res) {
                String tpl = cleanPath(directory) + UrlUtils.cleanName(s);
                set.add(tpl);
            }
        }
        return Lists.newArrayList(set);
    }

    protected Resource doLoad(String name, Locale locale, String encoding, String path) throws IOException {
        name = cleanPath(name);
        return new ClasspathResource(getEngine(), name, encoding, name, locale);
    }

    public boolean doExists(String name, Locale locale, String path) throws IOException {
        return IOUtils.getResource(cleanPath(name)) != null;
    }

    private String cleanPath(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    protected String relocate(String name, Locale locale, String[] directories) {
        return name;
    }
}
