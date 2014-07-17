package ameba.db.model;

import ameba.db.TransactionFeature;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.NotImplementedException;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by ICode on 14-3-6.
 */
@MappedSuperclass
public abstract class Model implements Serializable {
    @Transient
    private static final long serialVersionUID = 1L;
    @Transient
    private static Constructor<? extends Finder> finderConstructor = null;
    @Transient
    private static Constructor<? extends Persister> persisterConstructor = null;
    @Transient
    private final byte[] lock = new byte[0];
    @Transient
    private Method _idGetter = null;
    @Transient
    private Method _idSetter = null;
    @Transient
    private static final HashMap<Class, Finder> FinderMap = Maps.newHashMap();
    @Transient
    private static final HashMap<Class, Persister> PersisterMap = Maps.newHashMap();


    @Transient
    protected static Constructor<? extends Finder> getFinderConstructor() {
        if (finderConstructor == null)
            synchronized (Model.class) {
                if (finderConstructor == null)
                    try {
                        finderConstructor = TransactionFeature.getFinderClass()
                                .getConstructor(String.class, Class.class, Class.class);
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
            }
        return finderConstructor;
    }

    @Transient
    protected static void putFinderCache(Class clzz, Finder finder) {
        FinderMap.put(clzz, finder);
    }

    @Transient
    protected static Finder getFinderCache(Class clzz) {
        return FinderMap.get(clzz);
    }

    @Transient
    protected static Persister getPersisterCache(Class clzz) {
        return PersisterMap.get(clzz);
    }

    @Transient
    protected static void putPersisterCache(Class clzz, Persister persister) {
        PersisterMap.put(clzz, persister);
    }

    @Transient
    @SuppressWarnings("unchecked")
    protected static <ID, T> Finder<ID, T> _getFinder(String server) {
        throw new NotImplementedException("model not enhanced!");
    }

    @Transient
    @SuppressWarnings("unchecked")
    public static <ID, T> Finder<ID, T> getFinder(String server) {
        Finder<ID, T> finder = _getFinder(server);
        if (finder == null) {
            throw new NotFinderFindException();
        }
        return finder;
    }

    @Transient
    public static <ID, T> Finder<ID, T> getFinder() {
        return getFinder(DefaultProperties.DB_DEFAULT_SERVER_NAME);
    }

    @Transient
    protected static Constructor<? extends Persister> getPersisterConstructor() {
        if (persisterConstructor == null)
            synchronized (Model.class) {
                if (persisterConstructor == null)
                    try {
                        persisterConstructor = TransactionFeature.getPersisterClass()
                                .getConstructor(String.class, Model.class);
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
            }
        return persisterConstructor;
    }

    @Transient
    private Method _getIdGetter() throws NoSuchMethodException {
        if (_idGetter == null)
            synchronized (lock) {
                if (_idGetter == null)
                    _idGetter = this.getClass().getDeclaredMethod(ModelManager.ID_GETTER_NAME);
            }
        return _idGetter;
    }

    @Transient
    private Method _getIdSetter() throws NoSuchMethodException {
        if (_idSetter == null)
            synchronized (lock) {
                if (_idSetter == null)
                    _idSetter = this.getClass().getDeclaredMethod(ModelManager.ID_SETTER_NAME, _getIdGetter().getReturnType());
            }
        return _idSetter;
    }

    @SuppressWarnings("unchecked")
    @Transient
    <R> R _getId() {
        try {
            return (R) _getIdGetter().invoke(this);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transient
    void _setId(Object id) {
        try {
            _getIdSetter().invoke(this, id);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Transient
    @SuppressWarnings("unchecked")
    protected <M extends Model> Persister<M> _getPersister(String server) {
        Persister persister = getPersisterCache(this.getClass());
        if (persister == null)
            try {
                persister = getPersisterConstructor().newInstance(server, this);
                putPersisterCache(this.getClass(), persister);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        return persister;
    }

    @Transient
    @SuppressWarnings("unchecked")
    public <M extends Model> Persister<M> getPersister(String server) {
        Persister<M> persister = _getPersister(server);
        if (persister == null) {
            throw new NotPersisterFindException();
        }
        return persister;
    }

    @Transient
    public <M extends Model> Persister<M> getPersister() {
        return getPersister(DefaultProperties.DB_DEFAULT_SERVER_NAME);
    }

    public static class NotPersisterFindException extends RuntimeException {
        public NotPersisterFindException() {
            super("_getPersister method not return Persister instance");
        }
    }

    public static class NotFinderFindException extends RuntimeException {
        public NotFinderFindException() {
            super("_getFinder method not return Persister instance");
        }
    }
}
