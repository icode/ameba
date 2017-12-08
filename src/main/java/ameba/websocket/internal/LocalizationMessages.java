package ameba.websocket.internal;

import org.glassfish.jersey.internal.l10n.Localizable;
import org.glassfish.jersey.internal.l10n.LocalizableMessageFactory;
import org.glassfish.jersey.internal.l10n.Localizer;

/**
 * @author icode
 */
public final class LocalizationMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("ameba.websocket.internal.localization");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableENDPOINT_WRONG_PATH_PARAM(Object arg0, Object arg1) {
        return messageFactory.getMessage("endpoint.wrong.path.param", arg0, arg1);
    }

    /**
     * Method: {0}: {1} is not allowed type for @PathParameter.
     */
    public static String ENDPOINT_WRONG_PATH_PARAM(Object arg0, Object arg1) {
        return localizer.localize(localizableENDPOINT_WRONG_PATH_PARAM(arg0, arg1));
    }

    public static Localizable localizableENDPOINT_MAX_MESSAGE_SIZE_TOO_LONG(Object arg0, Object arg1, Object arg2, Object arg3) {
        return messageFactory.getMessage("endpoint.max.message.size.too.long", arg0, arg1, arg2, arg3);
    }

    /**
     * MaxMessageSize {0} on method {1} in endpoint {2} is larger than the container incoming buffer size {3}.
     */
    public static String ENDPOINT_MAX_MESSAGE_SIZE_TOO_LONG(Object arg0, Object arg1, Object arg2, Object arg3) {
        return localizer.localize(localizableENDPOINT_MAX_MESSAGE_SIZE_TOO_LONG(arg0, arg1, arg2, arg3));
    }

    public static Localizable localizableENDPOINT_MULTIPLE_SESSION_PARAM(Object arg0) {
        return messageFactory.getMessage("endpoint.multiple.session.param", arg0);
    }

    /**
     * Method {0} has got two or more Session parameters.
     */
    public static String ENDPOINT_MULTIPLE_SESSION_PARAM(Object arg0) {
        return localizer.localize(localizableENDPOINT_MULTIPLE_SESSION_PARAM(arg0));
    }

    public static Localizable localizableENDPOINT_MULTIPLE_METHODS(Object arg0, Object arg1, Object arg2, Object arg3) {
        return messageFactory.getMessage("endpoint.multiple.methods", arg0, arg1, arg2, arg3);
    }

    /**
     * Multiple methods using {0} annotation in class {1}: {2} and {3}. The latter will be ignored.
     */
    public static String ENDPOINT_MULTIPLE_METHODS(Object arg0, Object arg1, Object arg2, Object arg3) {
        return localizer.localize(localizableENDPOINT_MULTIPLE_METHODS(arg0, arg1, arg2, arg3));
    }

    public static Localizable localizableENDPOINT_UNKNOWN_PARAMS(Object arg0, Object arg1, Object arg2) {
        return messageFactory.getMessage("endpoint.unknown.params", arg0, arg1, arg2);
    }

    /**
     * Unknown parameter(s) for {0}.{1} method annotated with @OnError annotation: {2}. This method will be ignored.
     */
    public static String ENDPOINT_UNKNOWN_PARAMS(Object arg0, Object arg1, Object arg2) {
        return localizer.localize(localizableENDPOINT_UNKNOWN_PARAMS(arg0, arg1, arg2));
    }

    public static Localizable localizableENDPOINT_WRONG_PARAMS(Object arg0, Object arg1) {
        return messageFactory.getMessage("endpoint.wrong.params", arg0, arg1);
    }

    /**
     * Method: {0}.{1}: has got wrong number of params.
     */
    public static String ENDPOINT_WRONG_PARAMS(Object arg0, Object arg1) {
        return localizer.localize(localizableENDPOINT_WRONG_PARAMS(arg0, arg1));
    }
}
