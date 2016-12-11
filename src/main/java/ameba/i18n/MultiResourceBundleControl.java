package ameba.i18n;

import ameba.util.IOUtils;
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
 * <p>MultiResourceBundleControl class.</p>
 *
 * @author icode
 *
 */
public class MultiResourceBundleControl extends ResourceBundle.Control {

    boolean noCache = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTimeToLive(String baseName, Locale locale) {
        return noCache ? ResourceBundle.Control.TTL_DONT_CACHE : super.getTimeToLive(baseName, locale);
    }

    /** {@inheritDoc} */
    @Override
    public ResourceBundle newBundle(
            String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
        String bundleName = toBundleName(baseName, locale);
        ResourceBundle bundle = null;
        switch (format) {
            case "java.class":
                try {
                    Class bundleClass
                            = loader.loadClass(bundleName);

                    // If the class isn't a ResourceBundle subclass, throw a
                    // ClassCastException.
                    if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
                        bundle = (ResourceBundle) bundleClass.newInstance();
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
                            (PrivilegedExceptionAction<Properties>) () -> {
                                Properties properties1 = null;
                                Enumeration<URL> urls = classLoader.getResources(resourceName);
                                if (urls != null && urls.hasMoreElements()) {
                                    properties1 = new Properties();
                                    while (urls.hasMoreElements()) {
                                        URL url = urls.nextElement();
                                        if (url.getPath().endsWith("/classes/" + resourceName)) continue;
                                        URLConnection connection = url.openConnection();
                                        if (connection != null) {
                                            // Disable caches to get fresh data for
                                            // reloading.
                                            if (reloadFlag) {
                                                connection.setUseCaches(false);
                                            }
                                            InputStreamReader reader = null;
                                            try {
                                                reader = new InputStreamReader(
                                                        connection.getInputStream(),
                                                        Charsets.UTF_8);
                                                properties1.load(reader);
                                            } finally {
                                                IOUtils.closeQuietly(reader);
                                            }
                                        }
                                    }
                                }
                                return properties1;
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
