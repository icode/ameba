package ameba.db;

import ameba.db.model.Finder;
import ameba.db.model.Model;
import ameba.db.model.Persister;
import ameba.db.model.Updater;

import javax.ws.rs.core.Feature;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * @author ICode
 */
public abstract class TransactionFeature implements Feature {
    private static Class<? extends Finder> finderClass = null;
    private static Class<? extends Persister> persisterClass = null;
    private static Class<? extends Updater> updaterClass = null;

    private static Constructor<? extends Finder> finderConstructor = null;
    private static Constructor<? extends Persister> persisterConstructor = null;
    private static Constructor<? extends Updater> updaterConstructor = null;

    public static Class<? extends Finder> getFinderClass() {
        return finderClass;
    }

    public static synchronized void setFinderClass(Class finderClass) {
        if (TransactionFeature.finderClass != null) return;
        if (finderClass == null || Modifier.isAbstract(finderClass.getModifiers())) {
            throw new IllegalArgumentException("finder must instance of ameba.db.model.Finder");
        }
        TransactionFeature.finderClass = finderClass;
    }

    public static Class<? extends Persister> getPersisterClass() {
        return persisterClass;
    }

    public static synchronized void setPersisterClass(Class persisterClass) {
        if (TransactionFeature.persisterClass != null) return;
        if (persisterClass == null || Modifier.isAbstract(persisterClass.getModifiers())) {
            throw new IllegalArgumentException("persister must instance of ameba.db.model.Persister");
        }
        TransactionFeature.persisterClass = persisterClass;
    }

    public static Class<? extends Updater> getUpdaterClass() {
        return updaterClass;
    }

    public static synchronized void setUpdaterClass(Class updaterClass) {
        if (TransactionFeature.updaterClass != null) return;
        if (updaterClass == null || Modifier.isAbstract(updaterClass.getModifiers())) {
            throw new IllegalArgumentException("updater must instance of ameba.db.model.Updater");
        }
        TransactionFeature.updaterClass = updaterClass;
    }

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
