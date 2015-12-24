package ameba.scanner;

import ameba.event.SystemEventBus;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * @author icode
 */
public class PackageScanner {
    private Set<String> scanPkgs;
    private List<String> acceptClasses = Lists.newArrayList();
    private Set<String> foundClasses = Sets.newHashSet();

    public PackageScanner(Set<String> scanPkgs) {
        this.scanPkgs = scanPkgs;
    }

    public void scan() {
        final PackageNamesScanner scanner = new PackageNamesScanner(
                scanPkgs.toArray(new String[scanPkgs.size()]), true);
        while (scanner.hasNext()) {
            String fileName = scanner.next();
            if (!fileName.endsWith(".class")) continue;
            ClassInfo info = new ClassInfo(fileName) {

                InputStream in;

                @Override
                public InputStream getFileStream() {
                    if (in == null) {
                        in = scanner.open();
                    }
                    return in;
                }

                @Override
                public void closeFileStream() {
                    closeQuietly(in);
                }
            };
            String className = info.getCtClass().getName();
            if (!foundClasses.contains(className)) {
                ClassFoundEvent event = new ClassFoundEvent(info);
                SystemEventBus.publish(event);
                info.closeFileStream();

                if (event.accept) {
                    acceptClasses.add(className);
                }
            }
            foundClasses.add(className);
        }
    }

    public void clear() {
        acceptClasses.clear();
        foundClasses.clear();
    }

    public Set<String> getScanPkgs() {
        return Collections.unmodifiableSet(scanPkgs);
    }

    public List<String> getAcceptClasses() {
        return Collections.unmodifiableList(acceptClasses);
    }

    public Set<String> getFoundClasses() {
        return Collections.unmodifiableSet(foundClasses);
    }
}
