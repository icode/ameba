package ameba.mvc.template.httl.internal;

import ameba.i18n.Messages;

import java.util.Locale;

/**
 * @author icode
 */
public class MessageMethod {

    public String message(String key) {
        return message(key, null, new String[0]);
    }

    public String message(String key, Object arg0) {
        return message(key, null, new Object[]{arg0});
    }

    public String message(String key, Object arg0, Object arg1) {
        return message(key, null, new Object[]{arg0, arg1});
    }

    public String message(String key, Object arg0, Object arg1, Object arg2) {
        return message(key, null, new Object[]{arg0, arg1, arg2});
    }

    public String message(String key, Object arg0, Object arg1, Object arg2, Object arg3) {
        return message(key, null, new Object[]{arg0, arg1, arg2, arg3});
    }

    public String message(String key, Object[] args) {
        return message(key, null, args);
    }

    public String message(String key, Locale locale) {
        return message(key, locale, new String[0]);
    }

    public String message(String key, Locale locale, Object arg0) {
        return message(key, locale, new Object[]{arg0});
    }

    public String message(String key, Locale locale, Object arg0, Object arg1) {
        return message(key, locale, new Object[]{arg0, arg1});
    }

    public String message(String key, Locale locale, Object arg0, Object arg1, Object arg2) {
        return message(key, locale, new Object[]{arg0, arg1, arg2});
    }

    public String message(String key, Locale locale, Object arg0, Object arg1, Object arg2, Object arg3) {
        return message(key, locale, new Object[]{arg0, arg1, arg2, arg3});
    }

    public String message(String key, Locale locale, Object[] args) {
        return Messages.get(locale, key, args);
    }
}
