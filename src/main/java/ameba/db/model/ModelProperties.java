package ameba.db.model;

import org.glassfish.jersey.internal.util.PropertiesClass;

/**
 * properties for classes byte code enhance
 *
 * @author icode
 * @since 0.1.6e
 */
@PropertiesClass
public class ModelProperties {
    /**
     * Constant <code>GET_FINDER_M_NAME="withFinder"</code>
     */
    public static final String GET_FINDER_M_NAME = "withFinder";
    /**
     * Constant <code>GET_UPDATE_M_NAME="withUpdater"</code>
     */
    public static final String GET_UPDATE_M_NAME = "withUpdater";
    /**
     * Constant <code>GET_PERSISTER_M_NAME="withPersister"</code>
     */
    public static final String GET_PERSISTER_M_NAME = "withPersister";
    /**
     * Constant <code>BASE_MODEL_PKG="ModelProperties.class.getPackage().getN"{trunked}</code>
     */
    public static final String BASE_MODEL_PKG = ModelProperties.class.getPackage().getName();
    /**
     * Constant <code>FINDER_C_NAME="BASE_MODEL_PKG + .Finder"</code>
     */
    public static final String FINDER_C_NAME = BASE_MODEL_PKG + ".Finder";
    /**
     * Constant <code>UPDATER_C_NAME="BASE_MODEL_PKG + .Updater"</code>
     */
    public static final String UPDATER_C_NAME = BASE_MODEL_PKG + ".Updater";
    /**
     * Constant <code>PERSISTER_C_NAME="BASE_MODEL_PKG + .Persister"</code>
     */
    public static final String PERSISTER_C_NAME = BASE_MODEL_PKG + ".Persister";
    /**
     * Constant <code>BASE_MODEL_NAME="BASE_MODEL_PKG + .Model"</code>
     */
    public static final String BASE_MODEL_NAME = BASE_MODEL_PKG + ".Model";
}
