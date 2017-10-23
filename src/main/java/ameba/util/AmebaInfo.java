package ameba.util;

import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

/**
 * @author icode
 */
public class AmebaInfo {
    public static final String INFO_SEPARATOR = "---------------------------------------------------";
    private static final String banner = LINE_SEPARATOR + LINE_SEPARATOR +
            "    _                   _           " + LINE_SEPARATOR +
            "   / \\   _ __ ___   ___| |__   __ _ " + LINE_SEPARATOR +
            "  / _ \\ | '_ ` _ \\ / _ \\ '_ \\ / _` |" + LINE_SEPARATOR +
            " / ___ \\| | | | | |  __/ |_) | (_| |" + LINE_SEPARATOR +
            "/_/   \\_\\_| |_| |_|\\___|_.__/ \\__,_|   {}" + LINE_SEPARATOR + LINE_SEPARATOR;
    private static final String version;
    private static final String built;

    static {
        version = IOUtils.getJarManifestValue(AmebaInfo.class, "Ameba-Version");
        built = IOUtils.getJarManifestValue(AmebaInfo.class, "Ameba-Built");
    }

    public static String getBanner() {
        return banner;
    }

    public static String getVersion() {
        return version;
    }

    public static String getBuilt() {
        return built;
    }

    public static String getOsName() {
        return System.getProperty("os.name");
    }

    public static String getOsVersion() {
        return System.getProperty("os.version");
    }

    public static String getOsArchitecture() {
        return System.getProperty("os.arch");
    }

    public static String getJvmVersion() {
        return System.getProperty("java.runtime.version");
    }

    public static String getJvmVendor() {
        return System.getProperty("java.vm.vendor");
    }
}
