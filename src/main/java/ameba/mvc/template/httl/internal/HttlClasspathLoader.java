package ameba.mvc.template.httl.internal;

import ameba.util.IOUtils;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import httl.Resource;
import httl.spi.loaders.AbstractLoader;
import httl.util.CollectionUtils;
import httl.util.StringUtils;
import httl.util.UrlUtils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author icode
 */
public class HttlClasspathLoader extends AbstractLoader {
    private String[] templateDirectory;

    private String[] templateSuffix;

    @Override
    public List<String> doList(String directory, String suffix) throws IOException {
        Set<String> set = Sets.newHashSet();
        Enumeration<URL> urls = IOUtils.getResources(cleanPath(directory));
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            String path = URLDecoder.decode(cleanPath(url.toExternalForm()), Charsets.UTF_8.name());
            List<String> res = UrlUtils.listUrl(url, suffix);
            for (String s : res) {
                String tpl = path + UrlUtils.cleanName(s);
                set.add(tpl);
            }
        }
        return Lists.newArrayList(set);
    }

    /**
     * httl.properties: template.directory=/META-INF/templates
     */
    public void setTemplateDirectory(String[] directory) {
        this.templateDirectory = UrlUtils.cleanDirectory(directory);
        super.setTemplateDirectory(templateDirectory);
    }

    /**
     * httl.properties: template.suffix=.httl
     */
    public void setTemplateSuffix(String[] suffix) {
        super.setTemplateSuffix(suffix);
        this.templateSuffix = suffix;
    }

    public List<String> list(String suffix) throws IOException {
        String[] directories;
        if (StringUtils.endsWith(suffix, templateSuffix)) {
            directories = templateDirectory;
        } else {
            directories = null;
        }
        if (CollectionUtils.isEmpty(directories)) {
            directories = new String[]{"/"};
        }
        List<String> result = Lists.newArrayList();
        for (String directory : directories) {
            List<String> list = doList(directory, suffix);
            if (CollectionUtils.isNotEmpty(list)) {
                for (String name : list) {
                    if (StringUtils.isNotEmpty(name)) {
                        result.add(name);
                    }
                }
            }
        }
        return result;
    }

    protected Resource doLoad(String name, Locale locale, String encoding, String path) throws IOException {
        return new HttlClasspathResource(getEngine(), name, path, locale, encoding);
    }

    public boolean doExists(String name, Locale locale, String path) throws IOException {
        try {
            URL url = new URL(name);
            String pr = url.getProtocol();
            if (pr.equals("jar")) {
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                return connection.getJarEntry() != null;
            } else if (pr.equals("file")) {
                return new File(url.toURI()).exists();
            }
        } catch (Exception e) {
            // no op
        }
        return false;
    }

    private String cleanPath(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    protected String relocate(String name, Locale locale, String[] directories) {
        return cleanPath(name);
    }

    @Override
    protected String toPath(String name, Locale locale) {
        return cleanPath(super.toPath(name, locale));
    }
}
