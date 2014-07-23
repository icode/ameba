package ameba.util;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

/**
 * @author icode
 */
public class ClassLoaderUtils {
    public static ClassLoader getContextClassLoader() {
        ClassLoader loader = null;
        try {
            loader = Thread.currentThread().getContextClassLoader();
        } catch (Throwable e) {
        }
        if (loader == null) {
            loader = ClassLoaderUtils.class.getClassLoader();
        }
        return loader;
    }

    public static List<URL> getClasspathURLs(ClassLoader loader) {
        List<URL> urls = Lists.newArrayList();
        if (loader != null) {
            final Enumeration<URL> resources;
            try {
                resources = loader.getResources("");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while (resources.hasMoreElements()) {
                URL location = resources.nextElement();
                urls.add(location);
            }
        }
        return urls;
    }
}
