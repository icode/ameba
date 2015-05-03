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
 * @author icode
 */
public class Messages {

    public static final String BUNDLE_DIR = "conf/messages/";
    public static final String BUNDLE_NAME = BUNDLE_DIR + "message";
    private static final Table<String, Locale, ResourceBundle> RESOURCE_BUNDLES = HashBasedTable.create();


    private Messages() {
    }

    public static String get(String key, Object... args) {
        return get(BUNDLE_NAME, key, args);
    }


    public static String get(Locale locale, String key, Object... args) {
        return get(BUNDLE_NAME, getLocale(locale), key, args);
    }

    public static String get(String bundleName, String key, Object... args) {
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

    public static ResourceBundle getResourceBundle(String bundleName, Locale locale) {
        ResourceBundle bundle = null;

        if (!Ameba.getApp().getMode().isDev()) {
            bundle = RESOURCE_BUNDLES.get(bundleName, locale);
        }
        try {
            bundle = ResourceBundle.getBundle(
                    bundleName,
                    locale,
                    ClassUtils.getContextClassLoader(),
                    new MultiResourceBundleControl()
            );
        } catch (MissingResourceException e) {
            // no op
        }

        if (bundle != null && !Ameba.getApp().getMode().isDev()) {
            RESOURCE_BUNDLES.put(bundleName, locale, bundle);
        }

        return bundle;
    }

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
