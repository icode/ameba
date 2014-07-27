package ameba.dev;

import ameba.Application;
import ameba.compiler.JavaSource;
import ameba.util.UrlExternalFormComparator;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author icode
 */
public class ReloadingClassLoader extends URLClassLoader {

    private static final Set<URL> urls = new TreeSet<URL>(new UrlExternalFormComparator());
    File packageRoot;
    Application app;

    /**
     * Add all the url locations we can find for the provided class loader
     *
     * @param loader class loader
     */
    private static void addClassLoaderUrls(ClassLoader loader) {
        if (loader != null) {
            final Enumeration<URL> resources;
            try {
                resources = loader.getResources("");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while (resources.hasMoreElements()) {
                URL location = resources.nextElement();
                ReloadingClassLoader.addLocation(location);
            }
        }
    }

    public ReloadingClassLoader(Application app) {
        this(ReloadingClassLoader.class.getClassLoader(), app);
    }

    public ReloadingClassLoader(ClassLoader parent, Application app) {
        super(new URL[]{}, parent);

        addClassLoaderUrls(parent);

        for (URL url : urls) {
            addURL(url);
        }
        this.app = app;
        packageRoot = app.getPackageRoot();
    }


    @Override
    public final URL getResource(final String name) {
        URL resource = findResource(name);
        ClassLoader parent = getParent();
        if (resource == null && parent != null) {
            resource = parent.getResource(name);
        }

        return resource;
    }

    /**
     * Add the location of a directory containing class files
     *
     * @param url the URL for the directory
     */
    public static void addLocation(URL url) {
        urls.add(url);
    }

    /**
     * Returns the list of all configured locations of directories containing class files
     *
     * @return list of locations as URL
     */
    public static Set<URL> getLocations() {
        return urls;
    }

    public synchronized Class<?> defineClass(String name, byte[] code) {
        if (name == null) {
            throw new IllegalArgumentException("");
        }
        return defineClass(name, code, 0, code.length);
    }

    public boolean hasClass(String clazz) {
        return findLoadedClass(clazz) != null;
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        Class<?> c = findLoadedClass(name);
        if (c != null) {
            return c;
        }

        // First check if it's an application Class
        Class<?> appClass = loadApplicationClass(name);
        if (appClass != null) {
            if (resolve) {
                resolveClass(appClass);
            }
            return appClass;
        }


        // Delegate to the classic classLoader
        return super.loadClass(name, resolve);
    }

    protected boolean tryClassHere(String name) {
        // don't include classes in the java or javax.servlet package
        if (name != null && (name.startsWith("java.") || name.startsWith("javax.servlet"))) {
            return false;
        }
        // Scan includes, then excludes
        boolean tryHere = false;
        File f = JavaSource.getJava(name, this.app);
        if (f != null && f.exists()) {
            tryHere = true;
        }

        return tryHere;
    }

    private Class<?> loadApplicationClass(String name) {
        Class<?> clazz = findLoadedClass(name);

        if (clazz == null) {
            final ClassLoader parent = getParent();

            if (tryClassHere(name)) {
                try {
                    clazz = findClass(name);
                } catch (ClassNotFoundException cnfe) {
                    if (parent == null) {
                        // Propagate exception
                        return null;
                    }
                }
            }

            /*if (clazz == null) {
                if (parent == null) {
                    return null;
                } else {
                    // Will throw a CFNE if not found in parent
                    // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6500212
                    // clazz = parent.loadClass(name);
                    try {
                        clazz = Class.forName(name, false, parent);
                    } catch (ClassNotFoundException e) {
                        //noop
                    }
                }
            }*/
        }


        return clazz;
    }

    public void detectChanges(List<ClassDefinition> classes) throws UnmodifiableClassException, ClassNotFoundException {
        JvmAgent.reload(classes.toArray(new ClassDefinition[classes.size()]));
    }
}