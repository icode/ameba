package ameba.db.model;

import ameba.db.DataSourceManager;
import org.apache.commons.lang3.NotImplementedException;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * <p>Abstract Model class.</p>
 *
 * @author sulijuan
 * @version $Id: $Id
 */
@MappedSuperclass
public abstract class Model implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * <p>_getFinder.</p>
     *
     * @param server a {@link java.lang.String} object.
     * @param <ID>   a ID object.
     * @return a {@link ameba.db.model.Finder} object.
     * @param <T> a T object.
     */
    protected static <ID, T extends Model> Finder<ID, T> _getFinder(String server) {
        throw new NotImplementedException("Model not enhanced!");
    }

    /**
     * <p>withFinder.</p>
     *
     * @param server a {@link java.lang.String} object.
     * @param <ID>   a ID object.
     * @return a {@link ameba.db.model.Finder} object.
     * @param <T> a T object.
     */
    public static <ID, T extends Model> Finder<ID, T> withFinder(String server) {
        Finder<ID, T> finder = _getFinder(server);
        if (finder == null) {
            throw new NotFinderFindException();
        }
        return finder;
    }

    /**
     * <p>withFinder.</p>
     *
     * @param <ID> a ID object.
     * @return a {@link ameba.db.model.Finder} object.
     * @param <T> a T object.
     */
    public static <ID, T extends Model> Finder<ID, T> withFinder() {
        return withFinder(DataSourceManager.getDefaultDataSourceName());
    }

    /**
     * <p>_getUpdater.</p>
     *
     * @param server a {@link java.lang.String} object.
     * @param sql    a {@link java.lang.String} object.
     * @return a {@link ameba.db.model.Updater} object.
     * @since 0.1.6e
     * @param <M> a M object.
     */
    protected static <M extends Model> Updater<M> _getUpdater(String server, String sql) {
        throw new NotImplementedException("Model not enhanced!");
    }

    /**
     * <p>withUpdater.</p>
     *
     * @param server a {@link java.lang.String} object.
     * @param sql    a {@link java.lang.String} object.
     * @return a {@link ameba.db.model.Updater} object.
     * @since 0.1.6e
     * @param <M> a M object.
     */
    public static <M extends Model> Updater<M> withUpdater(String server, String sql) {
        Updater<M> updater = _getUpdater(server, sql);
        if (updater == null) {
            throw new NotUpdaterFindException();
        }
        return updater;
    }

    /**
     * <p>withUpdater.</p>
     *
     * @param sql a {@link java.lang.String} object.
     * @return a {@link ameba.db.model.Updater} object.
     * @since 0.1.6e
     * @param <M> a M object.
     */
    public static <M extends Model> Updater<M> withUpdater(String sql) {
        return withUpdater(DataSourceManager.getDefaultDataSourceName(), sql);
    }

    /**
     * <p>_getPersister.</p>
     *
     * @param server a {@link java.lang.String} object.
     * @return a {@link ameba.db.model.Persister} object.
     * @param <M> a M object.
     */
    protected <M extends Model> Persister<M> _getPersister(String server) {
        throw new NotImplementedException("Model not enhanced!");
    }

    /**
     * <p>withPersister.</p>
     *
     * @param server a {@link java.lang.String} object.
     * @return a {@link ameba.db.model.Persister} object.
     * @param <M> a M object.
     */
    public <M extends Model> Persister<M> withPersister(String server) {
        Persister<M> persister = _getPersister(server);
        if (persister == null) {
            throw new NotPersisterFindException();
        }
        return persister;
    }

    /**
     * <p>withPersister.</p>
     *
     * @return a {@link ameba.db.model.Persister} object.
     * @param <M> a M object.
     */
    public <M extends Model> Persister<M> withPersister() {
        return withPersister(DataSourceManager.getDefaultDataSourceName());
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
