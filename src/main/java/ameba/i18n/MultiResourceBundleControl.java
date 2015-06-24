package ameba.i18n;

import com.google.common.base.Charsets;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @author icode
 */
public class MultiResourceBundleControl extends ResourceBundle.Control {

    boolean noCache = false;

    @Override
    public long getTimeToLive(String baseName, Locale locale) {
        return noCache ? ResourceBundle.Control.TTL_DONT_CACHE : super.getTimeToLive(baseName, locale);
    }

    @Override
    public ResourceBundle newBundle(
            String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
        String bundleName = toBundleName(baseName, locale);
        ResourceBundle bundle = null;
        switch (format) {
            case "java.class":
                try {
                    Class<? extends ResourceBundle> bundleClass
                            = (Class<? extends ResourceBundle>) loader.loadClass(bundleName);

                    // If the class isn't a ResourceBundle subclass, throw a
                    // ClassCastException.
                    if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
                        bundle = bundleClass.newInstance();
                    } else {
                        throw new ClassCastException(bundleClass.getName()
                                + " cannot be cast to ResourceBundle");
                    }
                } catch (ClassNotFoundException e) {
                    // no op
                }
                break;
            case "java.properties":
                final String resourceName = toResourceName(bundleName, "properties");
                final ClassLoader classLoader = loader;
                final boolean reloadFlag = reload;
                Properties properties;
                try {
                    properties = AccessController.doPrivileged(
                            new PrivilegedExceptionAction<Properties>() {
                                public Properties run() throws IOException {
                                    Properties properties = null;
                                    Enumeration<URL> urls = classLoader.getResources(resourceName);
                                    if (urls != null && urls.hasMoreElements()) {
                                        properties = new Properties();
                                        while (urls.hasMoreElements()) {
                                            URLConnection connection = urls.nextElement().openConnection();
                                            if (connection != null) {
                                                // Disable caches to get fresh data for
                                                // reloading.
                                                if (reloadFlag) {
                                                    connection.setUseCaches(false);
                                                }
                                                properties.load(
                                                        new InputStreamReader(
                                                                connection.getInputStream(),
                                                                Charsets.UTF_8)
                                                );
                                            }
                                        }
                                    }
                                    return properties;
                                }
                            });
                } catch (PrivilegedActionException e) {
                    throw (IOException) e.getException();
                }
                if (properties != null) {
                    bundle = new PropertiesResourceBundle(properties);
                }
                break;
            default:
                throw new IllegalArgumentException("unknown format: " + format);
        }
        return bundle;
    }
}