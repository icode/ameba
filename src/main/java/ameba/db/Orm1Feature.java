package ameba.db;

import ameba.db.model.Finder;
import ameba.db.model.Model;
import ameba.db.model.Persister;
import ameba.db.model.Updater;

import javax.ws.rs.core.Feature;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * <p>Abstract ORMFeature class.</p>
 *
 * @author icode
 */
public abstract class Orm1Feature implements Feature {
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
    public static synchronized void setFinderClass(Class finderClass) {
        if (Orm1Feature.finderClass != null) return;
        if (finderClass == null || Modifier.isAbstract(finderClass.getModifiers())) {
            throw new IllegalArgumentException("finder must instance of ameba.db.model.Finder");
        }
        Orm1Feature.finderClass = finderClass;
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
    public static synchronized void setPersisterClass(Class persisterClass) {
        if (Orm1Feature.persisterClass != null) return;
        if (persisterClass == null || Modifier.isAbstract(persisterClass.getModifiers())) {
            throw new IllegalArgumentException("persister must instance of ameba.db.model.Persister");
        }
        Orm1Feature.persisterClass = persisterClass;
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
    public static synchronized void setUpdaterClass(Class updaterClass) {
        if (Orm1Feature.updaterClass != null) return;
        if (updaterClass == null || Modifier.isAbstract(updaterClass.getModifiers())) {
            throw new IllegalArgumentException("updater must instance of ameba.db.model.Updater");
        }
        Orm1Feature.updaterClass = updaterClass;
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
}
