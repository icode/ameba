package ameba.i18n;

import ameba.Ameba;
import ameba.core.Requests;
import ameba.util.ClassUtils;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * <p>Messages class.</p>
 *
 * @author icode
 */
public class Messages {

    /**
     * Constant <code>BUNDLE_DIR="conf/messages/"</code>
     */
    public static final String BUNDLE_DIR = "conf/messages/";
    /**
     * Constant <code>BUNDLE_NAME="BUNDLE_DIR + message"</code>
     */
    public static final String BUNDLE_NAME = BUNDLE_DIR + "message";
    private static final Table<String, Locale, ResourceBundle> RESOURCE_BUNDLES = HashBasedTable.create();
    private static final MultiResourceBundleControl BUNDLE_CONTROL = new MultiResourceBundleControl();

    private Messages() {
    }

    /**
     * <p>get.</p>
     *
     * @param key  a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     * @return a {@link java.lang.String} object.
     */
    public static String get(String key, Object... args) {
        return get(BUNDLE_NAME, key, args);
    }


    /**
     * <p>get.</p>
     *
     * @param locale a {@link java.util.Locale} object.
     * @param key    a {@link java.lang.String} object.
     * @param args   a {@link java.lang.Object} object.
     * @return a {@link java.lang.String} object.
     */
    public static String get(Locale locale, String key, Object... args) {
        return get(BUNDLE_NAME, getLocale(locale), key, args);
    }

    /**
     * <p>get.</p>
     *
     * @param bundleName a {@link java.lang.String} object.
     * @param key        a {@link java.lang.String} object.
     * @param args       an array of {@link java.lang.Object} objects.
     * @return a {@link java.lang.String} object.
     */
    public static String get(String bundleName, String key, Object[] args) {
        return get(bundleName, getLocale(), key, args);
    }

    private static Locale getLocale(Locale locale) {
        if (locale == null) {
            return getLocale();
        }
        return locale;
    }

    private static Locale getLocale() {
        Locale locale = null;
        try {
            List<Locale> acceptableLanguages = Requests.getAcceptableLanguages();
            if (acceptableLanguages != null && acceptableLanguages.size() > 0) {
                locale = acceptableLanguages.get(0);
            }
        } catch (Exception e) {
            // no op
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return locale;
    }

    /**
     * <p>getResourceBundle.</p>
     *
     * @param bundleName a {@link java.lang.String} object.
     * @param locale     a {@link java.util.Locale} object.
     * @return a {@link java.util.ResourceBundle} object.
     */
    public static ResourceBundle getResourceBundle(String bundleName, Locale locale) {
        ResourceBundle bundle = null;

        boolean isDev = false;
        if (Ameba.getApp() != null) {
            isDev = Ameba.getApp().getMode().isDev();
            BUNDLE_CONTROL.noCache = isDev;
        }
        if (!isDev) {
            bundle = RESOURCE_BUNDLES.get(bundleName, locale);
        }
        if (bundle == null) {
            try {
                bundle = ResourceBundle.getBundle(
                        bundleName,
                        locale,
                        ClassUtils.getContextClassLoader(),
                        BUNDLE_CONTROL
                );
            } catch (MissingResourceException e) {
                // no op
            }
        }

        if (bundle != null && !isDev) {
            RESOURCE_BUNDLES.put(bundleName, locale, bundle);
        }

        return bundle;
    }

    /**
     * <p>get.</p>
     *
     * @param bundleName a {@link java.lang.String} object.
     * @param locale     a {@link java.util.Locale} object.
     * @param key        a {@link java.lang.String} object.
     * @param args       a {@link java.lang.Object} object.
     * @return a {@link java.lang.String} object.
     */
    public static String get(String bundleName, Locale locale, String key, Object... args) {
        try {
            ResourceBundle bundle = getResourceBundle(bundleName, locale);

            if (bundle == null) {
                return getDefaultMessage(key, args);
            }

            if (key == null) {
                key = "undefined";
            }

            String msg;
            try {
                msg = bundle.getString(key);
            } catch (MissingResourceException e) {
                // notice that this may throw a MissingResourceException of its own (caught below)
                try {
                    msg = bundle.getString("undefined");
                } catch (MissingResourceException ex) {
                    return getDefaultMessage(key, args);
                }
            }

            return MessageFormat.format(msg, args);

        } catch (MissingResourceException e) {
            return getDefaultMessage(key, args);
        }

    }

    private static String getDefaultMessage(String key, Object... args) {
        StringBuilder sb = new StringBuilder();
        sb.append("[failed to localize] ");
        sb.append(key);
        if (args != null && args.length > 0) {
            sb.append('(');
            for (int i = 0; i < args.length; ++i) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(String.valueOf(args[i]));
            }
            sb.append(')');
        }
        return sb.toString();
    }
}
