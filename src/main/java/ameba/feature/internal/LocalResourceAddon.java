package ameba.feature.internal;

import ameba.core.Addon;
import ameba.core.Application;
import ameba.event.Listener;
import ameba.scanner.Acceptable;
import ameba.scanner.ClassFoundEvent;
import ameba.scanner.ClassInfo;
import com.google.common.collect.Sets;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.Set;

/**
 * <p>LocalResourceAddon class.</p>
 *
 * @author icode
 * @since 0.1.6e
 */
public class LocalResourceAddon extends Addon {
    /**
     * {@inheritDoc}
     */
    @Override
    public void setup(final Application application) {

        final Set<ClassInfo> classInfoSet = Sets.newLinkedHashSet();
        subscribeSystemEvent(ClassFoundEvent.class, new Listener<ClassFoundEvent>() {
            @Override
            public void onReceive(ClassFoundEvent event) {
                event.accept(new Acceptable<ClassInfo>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public final boolean accept(ClassInfo info) {
                        if (info.containsAnnotations(Service.class)) {
                            classInfoSet.add(info);
                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        final Feature localResource = new Feature() {

            @Inject
            private ServiceLocator locator;

            @Override
            public boolean configure(FeatureContext context) {
                for (ClassInfo classInfo : classInfoSet) {
                    ServiceLocatorUtilities.addClasses(locator, classInfo.toClass());
                }
                classInfoSet.clear();
                return true;
            }
        };

        application.register(localResource);
    }

}
