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
 * <p>PackageScanner class.</p>
 *
 * @author icode
 *
 */
public class PackageScanner {
    private Set<String> scanPkgs;
    private List<String> acceptClasses = Lists.newArrayList();
    private Set<String> foundClasses = Sets.newHashSet();

    /**
     * <p>Constructor for PackageScanner.</p>
     *
     * @param scanPkgs a {@link java.util.Set} object.
     */
    public PackageScanner(Set<String> scanPkgs) {
        this.scanPkgs = scanPkgs;
    }

    /**
     * <p>scan.</p>
     */
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

    /**
     * <p>clear.</p>
     */
    public void clear() {
        acceptClasses.clear();
        foundClasses.clear();
    }

    /**
     * <p>Getter for the field <code>scanPkgs</code>.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getScanPkgs() {
        return Collections.unmodifiableSet(scanPkgs);
    }

    /**
     * <p>Getter for the field <code>acceptClasses</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getAcceptClasses() {
        return Collections.unmodifiableList(acceptClasses);
    }

    /**
     * <p>Getter for the field <code>foundClasses</code>.</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<String> getFoundClasses() {
        return Collections.unmodifiableSet(foundClasses);
    }
}
