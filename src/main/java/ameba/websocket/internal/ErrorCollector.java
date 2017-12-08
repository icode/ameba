package ameba.websocket.internal;

import com.google.common.collect.Lists;

import javax.websocket.DeploymentException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author icode
 */
public class ErrorCollector {

    private static final Logger LOGGER = Logger.getLogger(ErrorCollector.class.getName());

    private final List<Exception> exceptionsToPublish = Lists.newArrayList();

    /**
     * Add {@link Exception} to the collector.
     *
     * @param exception to be collected.
     */
    public void addException(Exception exception) {
        LOGGER.log(Level.FINE, "Adding exception", exception);
        exceptionsToPublish.add(exception);
    }

    /**
     * Create {@link DeploymentException} with message concatenated from collected exceptions.
     *
     * @return comprehensive exception.
     */
    public DeploymentException composeComprehensiveException() {
        StringBuilder sb = new StringBuilder();

        for (Exception exception : exceptionsToPublish) {
            sb.append(exception.getMessage());
            sb.append("\n");
        }

        return new DeploymentException(sb.toString());
    }

    /**
     * Checks whether any exception has been logged.
     *
     * @return {@code true} iff no exception was logged, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return exceptionsToPublish.isEmpty();
    }
}
