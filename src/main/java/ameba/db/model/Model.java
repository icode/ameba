package ameba.db.model;

import ameba.container.Container;
import ameba.db.TransactionFeature;
import ameba.event.Listener;
import ameba.event.SystemEventBus;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.NotImplementedException;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author icode
 */
@MappedSuperclass
public abstract class Model implements Serializable {
    private static final Map<Class, GSCache> GSMap = Maps.newConcurrentMap();

    static {
        SystemEventBus.subscribe(Container.BeginReloadEvent.class, new Listener<Container.BeginReloadEvent>() {
            @Override
            public void onReceive(Container.BeginReloadEvent event) {
                GSMap.clear();
            }
        });
    }

    public static String DB_DEFAULT_SERVER_NAME = "default";
    public static final String ID_SETTER_NAME = "__setId__";
    public static final String ID_GETTER_NAME = "__getId__";
    public static final String GET_FINDER_M_NAME = "withFinder";
    public static final String GET_UPDATE_M_NAME = "withUpdater";
    public final static String BASE_MODEL_PKG = Model.class.getPackage().getName();
    public static final String FINDER_C_NAME = BASE_MODEL_PKG + ".Finder";
    public static final String UPDATER_C_NAME = BASE_MODEL_PKG + ".Updater";

    @Transient
    private Method _idGetter = null;
    @Transient
    private Method _idSetter = null;

    protected static Constructor<? extends Finder> getFinderConstructor() {
        return TransactionFeature.getFinderConstructor();
    }

    @SuppressWarnings("unchecked")
    protected static <ID, T extends Model> Finder<ID, T> _getFinder(String server) {
        throw new NotImplementedException("model not enhanced!");
    }

    @SuppressWarnings("unchecked")
    public static <ID, T extends Model> Finder<ID, T> withFinder(String server) {
        Finder<ID, T> finder = _getFinder(server);
        if (finder == null) {
            throw new NotFinderFindException();
        }
        return finder;
    }

    public static <ID, T extends Model> Finder<ID, T> withFinder() {
        return withFinder(ModelManager.getDefaultDBName());
    }

    protected static Constructor<? extends Persister> getPersisterConstructor() {
        return TransactionFeature.getPersisterConstructor();
    }

    private GSCache getGSCache() {
        GSCache cache = GSMap.get(this.getClass());
        if (cache == null) {
            cache = new GSCache();
            GSMap.put(this.getClass(), cache);
        }
        return cache;
    }

    private Method _getIdGetter() throws NoSuchMethodException {
        if (_idGetter == null) {
            GSCache cache = getGSCache();
            if (cache.getter == null) {
                cache.getter = this.getClass().getDeclaredMethod(ID_GETTER_NAME);
            }
            _idGetter = cache.getter;
        }

        return _idGetter;
    }

    private Method _getIdSetter() throws NoSuchMethodException {
        if (_idSetter == null) {
            GSCache cache = getGSCache();
            if (cache.setter == null) {
                cache.setter = this.getClass().getDeclaredMethod(ID_SETTER_NAME, _getIdGetter().getReturnType());
            }
            _idSetter = cache.setter;
        }
        return _idSetter;
    }

    private static class GSCache {
        Method getter;
        Method setter;
    }

    @SuppressWarnings("unchecked")
    <R> R _getId() {
        try {
            return (R) _getIdGetter().invoke(this);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void _setId(Object id) {
        try {
            _getIdSetter().invoke(this, id);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @SuppressWarnings("unchecked")
    protected <M extends Model> Persister<M> _getPersister(String server) {
        try {
            return getPersisterConstructor().newInstance(server, this);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <M extends Model> Persister<M> withPersister(String server) {
        Persister<M> persister = _getPersister(server);
        if (persister == null) {
            throw new NotPersisterFindException();
        }
        return persister;
    }

    public <M extends Model> Persister<M> withPersister() {
        return withPersister(ModelManager.getDefaultDBName());
    }

    protected static Constructor<? extends Updater> getUpdaterConstructor() {
        return TransactionFeature.getUpdaterConstructor();
    }

    @SuppressWarnings("unchecked")
    protected static <M extends Model> Updater<M> _getUpdater(String server, String sql) {
        throw new NotImplementedException("model not enhanced!");
    }

    @SuppressWarnings("unchecked")
    public static <M extends Model> Updater<M> withUpdater(String server, String sql) {
        Updater<M> updater = _getUpdater(server, sql);
        if (updater == null) {
            throw new NotUpdaterFindException();
        }
        return updater;
    }

    public static <M extends Model> Updater<M> withUpdater(String sql) {
        return withUpdater(ModelManager.getDefaultDBName(), sql);
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

    public static class NotUpdaterFindException extends RuntimeException {
        public NotUpdaterFindException() {
            super("_getUpdater method not return Updater instance");
        }
    }
}
