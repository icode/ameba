package ameba;

import ameba.container.Container;
import ameba.core.Application;
import ameba.exception.AmebaException;
import ameba.util.IOUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <p>Ameba class.</p>
 *
 * @author icode
 */
public class Ameba {
    /**
     * Constant <code>logger</code>
     */
    public static final Logger logger = LoggerFactory.getLogger(Ameba.class);
    /**
     * Constant <code>LOGO="\n\n    _                   _          "{trunked}</code>
     */
    public static final String LOGO = "\n\n" +
            "    _                   _           \n" +
            "   / \\   _ __ ___   ___| |__   __ _ \n" +
            "  / _ \\ | '_ ` _ \\ / _ \\ '_ \\ / _` |\n" +
            " / ___ \\| | | | | |  __/ |_) | (_| |\n" +
            "/_/   \\_\\_| |_| |_|\\___|_.__/ \\__,_|   {}\n\n";
    private static Application app;
    private static Container container;
    private static String version;

    private Ameba() {
    }

    /**
     * <p>getServiceLocator.</p>
     *
     * @return a {@link org.glassfish.hk2.api.ServiceLocator} object.
     */
    public static ServiceLocator getServiceLocator() {
        return container.getServiceLocator();
    }

    /**
     * <p>Getter for the field <code>container</code>.</p>
     *
     * @return a {@link ameba.container.Container} object.
     * @since 0.1.6e
     */
    public static Container getContainer() {
        return container;
    }

    /**
     * <p>Getter for the field <code>app</code>.</p>
     *
     * @return a {@link ameba.core.Application} object.
     */
    public static Application getApp() {
        return app;
    }

    /**
     * <p>Getter for the field <code>version</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     * @since 0.1.6e
     */
    public static String getVersion() {

        if (version == null) {
            version = IOUtils.getJarImplVersion(Ameba.class);
        }

        return version;
    }

    /**
     * <p>printInfo.</p>
     *
     * @since 0.1.6e
     */
    public static void printInfo() {
        logger.info(LOGO, getVersion());
    }

    /**
     * <p>main.</p>
     *
     * @param args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] args) {

        List<String> list = Lists.newArrayList();

        String idCommand = "--#";

        int idArgLen = idCommand.length();

        for (String arg : args) {
            if (arg.startsWith(idCommand)) {
                String idConf = arg.substring(idArgLen);
                if (StringUtils.isNotBlank(idConf)) {
                    list.add(idConf);
                }
            }
        }

        try {
            bootstrap(list.toArray(new String[list.size()]));
        } catch (Throwable e) {
            logger.error("发生错误,10s后退出", e);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e1) {
                //no op
            }
            shutdown();
            System.exit(500);
        }

        // register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                shutdown();
            }
        }, "shutdownHook"));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            //no op
        }
    }

    /**
     * <p>bootstrap.</p>
     *
     * @param ids a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public static void bootstrap(String... ids) throws Exception {
        bootstrap(new Application(ids));
    }

    /**
     * <p>bootstrap.</p>
     *
     * @param application a {@link ameba.core.Application} object.
     * @throws java.lang.Exception if any.
     */
    public static synchronized void bootstrap(Application application) throws Exception {
        if (Ameba.container != null) {
            throw new AmebaException("无法启动多个实例");
        }

        app = application;
        container = Container.create(app);

        // run
        logger.info("启动服务...");
        container.start();
    }

    /**
     * <p>shutdown.</p>
     */
    public static synchronized void shutdown() {
        logger.info("关闭服务器...");
        if (container != null)
            try {
                container.shutdown();
            } catch (Exception e) {
                logger.error("服务器关闭出错", e);
            }
        logger.info("服务器已关闭");
    }
}
