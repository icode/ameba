package ameba.db;

import ameba.core.Addon;
import ameba.core.Application;
import ameba.db.model.Finder;
import ameba.db.model.Model;
import ameba.db.model.Persister;
import ameba.db.model.Updater;
import ameba.exception.AmebaException;
import ameba.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * <p>Abstract OrmAddon class.</p>
 *
 * @author icode
 */
public class OrmAddon extends Addon {
    private static Class<? extends Finder> finderClass = null;
    private static Class<? extends Persister> persisterClass = null;
    private static Class<? extends Updater> updaterClass = null;

    private static Constructor<? extends Finder> finderConstructor = null;
    private static Constructor<? extends Persister> persisterConstructor = null;
    private static Constructor<? extends Updater> updaterConstructor = null;

    /**
     * <p>Getter for the field <code>finderClass</code>.</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    public static Class<? extends Finder> getFinderClass() {
        return finderClass;
    }

    /**
     * <p>Setter for the field <code>finderClass</code>.</p>
     *
     * @param finderClass a {@link java.lang.Class} object.
     * @since 0.1.6e
     */
    private static void setFinderClass(Class<? extends Finder> finderClass) {
        if (OrmAddon.finderClass != null) return;
        if (finderClass == null || Modifier.isAbstract(finderClass.getModifiers())) {
            throw new IllegalArgumentException("finder must instance of ameba.db.model.Finder");
        }
        OrmAddon.finderClass = finderClass;
    }

    /**
     * <p>Getter for the field <code>persisterClass</code>.</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    public static Class<? extends Persister> getPersisterClass() {
        return persisterClass;
    }

    /**
     * <p>Setter for the field <code>persisterClass</code>.</p>
     *
     * @param persisterClass a {@link java.lang.Class} object.
     * @since 0.1.6e
     */
    private static void setPersisterClass(Class<? extends Persister> persisterClass) {
        if (OrmAddon.persisterClass != null) return;
        if (persisterClass == null || Modifier.isAbstract(persisterClass.getModifiers())) {
            throw new IllegalArgumentException("persister must instance of ameba.db.model.Persister");
        }
        OrmAddon.persisterClass = persisterClass;
    }

    /**
     * <p>Getter for the field <code>updaterClass</code>.</p>
     *
     * @return a {@link java.lang.Class} object.
     * @since 0.1.6e
     */
    public static Class<? extends Updater> getUpdaterClass() {
        return updaterClass;
    }

    /**
     * <p>Setter for the field <code>updaterClass</code>.</p>
     *
     * @param updaterClass a {@link java.lang.Class} object.
     * @since 0.1.6e
     */
    private static void setUpdaterClass(Class<? extends Updater> updaterClass) {
        if (OrmAddon.updaterClass != null) return;
        if (updaterClass == null || Modifier.isAbstract(updaterClass.getModifiers())) {
            throw new IllegalArgumentException("updater must instance of ameba.db.model.Updater");
        }
        OrmAddon.updaterClass = updaterClass;
    }

    /**
     * <p>Getter for the field <code>finderConstructor</code>.</p>
     *
     * @return a {@link java.lang.reflect.Constructor} object.
     * @since 0.1.6e
     */
    public static Constructor<? extends Finder> getFinderConstructor() {
        if (finderConstructor == null) {
            try {
                finderConstructor = finderClass.getConstructor(String.class, Class.class, Class.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return finderConstructor;
    }

    /**
     * <p>Getter for the field <code>persisterConstructor</code>.</p>
     *
     * @return a {@link java.lang.reflect.Constructor} object.
     * @since 0.1.6e
     */
    public static Constructor<? extends Persister> getPersisterConstructor() {
        if (persisterConstructor == null) {
            try {
                persisterConstructor = persisterClass.getConstructor(String.class, Model.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return persisterConstructor;
    }

    /**
     * <p>Getter for the field <code>updaterConstructor</code>.</p>
     *
     * @return a {@link java.lang.reflect.Constructor} object.
     * @since 0.1.6e
     */
    public static Constructor<? extends Updater> getUpdaterConstructor() {
        if (updaterConstructor == null) {
            try {
                updaterConstructor = updaterClass.getConstructor(String.class, Class.class, String.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return updaterConstructor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setup(Application application) {

        Map<String, Object> map = application.getSrcProperties();
        String finder = (String) map.get("orm.finder");
        String persister = (String) map.get("orm.persister");
        String updater = (String) map.get("orm.updater");

        try {
            setFinderClass((Class<? extends Finder>) ClassUtils.getClass(finder));
            setPersisterClass((Class<? extends Persister>) ClassUtils.getClass(persister));
            setUpdaterClass((Class<? extends Updater>) ClassUtils.getClass(updater));
        } catch (ClassNotFoundException e) {
            throw new AmebaException(e);
        }
    }
}
