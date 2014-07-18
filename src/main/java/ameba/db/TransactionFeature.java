package ameba.db;

import ameba.db.model.Finder;
import ameba.db.model.Persister;

import javax.ws.rs.core.Feature;
import java.lang.reflect.Modifier;

/**
 * @author: ICode
 * @since: 13-8-17 下午6:17
 */
public abstract class TransactionFeature implements Feature {
    private static Class<? extends Finder> finderClass = null;
    private static Class<? extends Persister> persisterClass = null;

    public TransactionFeature(Class<? extends Finder> finder, Class<? extends Persister> persister) {
        if (finder == null || finder.isInterface() || Modifier.isAbstract(finder.getModifiers())) {
            throw new IllegalArgumentException("finder must instance of ameba.db.model.Finder");
        }
        if (persister == null || persister.isInterface() || Modifier.isAbstract(persister.getModifiers())) {
            throw new IllegalArgumentException("persister must instance of ameba.db.model.Persister");
        }
        finderClass = finder;
        persisterClass = persister;
    }

    public static Class<? extends Finder> getFinderClass() {
        return finderClass;
    }

    public static Class<? extends Persister> getPersisterClass() {
        return persisterClass;
    }
}
