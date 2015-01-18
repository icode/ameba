package ameba.db;

import ameba.db.model.Finder;
import ameba.db.model.Model;
import ameba.db.model.Persister;
import ameba.db.model.Updater;
import ameba.feature.AmebaFeature;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * @author ICode
 * @since 13-8-17 下午6:17
 */
public abstract class TransactionFeature extends AmebaFeature {
    private static Class<? extends Finder> finderClass = null;
    private static Class<? extends Persister> persisterClass = null;
    private static Class<? extends Updater> updaterClass = null;

    private static Constructor<? extends Finder> finderConstructor = null;
    private static Constructor<? extends Persister> persisterConstructor = null;
    private static Constructor<? extends Updater> updaterConstructor = null;

    public TransactionFeature(Class<? extends Finder> finder, Class<? extends Persister> persister, Class<? extends Updater> updater) {
        if (finder == null || Modifier.isAbstract(finder.getModifiers())) {
            throw new IllegalArgumentException("finder must instance of ameba.db.model.Finder");
        }
        if (persister == null || Modifier.isAbstract(persister.getModifiers())) {
            throw new IllegalArgumentException("persister must instance of ameba.db.model.Persister");
        }
        if (updater == null || Modifier.isAbstract(updater.getModifiers())) {
            throw new IllegalArgumentException("updater must instance of ameba.db.model.Updater");
        }

        finderClass = finder;
        try {
            finderConstructor = finder.getConstructor(String.class, Class.class, Class.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        persisterClass = persister;
        try {
            persisterConstructor = persister.getConstructor(String.class, Model.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        updaterClass = updater;
        try {
            updaterConstructor = updater.getConstructor(String.class, Class.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<? extends Finder> getFinderClass() {
        return finderClass;
    }

    public static Class<? extends Persister> getPersisterClass() {
        return persisterClass;
    }

    public static Class<? extends Updater> getUpdaterClass() {
        return updaterClass;
    }

    public static Constructor<? extends Finder> getFinderConstructor() {
        return finderConstructor;
    }

    public static Constructor<? extends Persister> getPersisterConstructor() {
        return persisterConstructor;
    }

    public static Constructor<? extends Updater> getUpdaterConstructor() {
        return updaterConstructor;
    }
}
