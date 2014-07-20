package ameba;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author icode
 */
public class Ameba {
    public static final Logger logger = LoggerFactory.getLogger(Ameba.class);

    private static Application app;

    public static Application getApp() {
        return app;
    }

    public static void main(String[] args) {
        app = Application.bootstrap();
    }
}
