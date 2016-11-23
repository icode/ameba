package ameba.inject.generator;

import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.Populator;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.external.generator.ServiceLocatorGeneratorImpl;

import java.io.IOException;

/**
 * <p>ServiceLocatorGenerator class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class ServiceLocatorGenerator extends ServiceLocatorGeneratorImpl {
    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceLocator create(String name, ServiceLocator parent) {
        ServiceLocator retVal = super.create(name, parent);
        DynamicConfigurationService dcs = retVal.getService(DynamicConfigurationService.class);
        Populator populator = dcs.getPopulator();

        try {
            populator.populate();
        } catch (IOException e) {
            throw new MultiException(e);
        }
        return retVal;
    }
}
