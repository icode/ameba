package ameba.cache;

import ameba.util.Times;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.Map;

/**
 * 缓存
 *
 * @author icode
 */
public abstract class Cache {

    /**
     * 缓存引擎
     */
    static CacheEngine cacheEngine;

    /**
     * Add an element only if it doesn't exist.
     *
     * @param key        Element key
     * @param value      Element value
     * @param expiration Ex: 10s, 3mn, 8h
     */
    public static void add(String key, Object value, String expiration) {
        checkSerializable(value);
        cacheEngine.add(key, value, Times.parseDurationToSeconds(expiration));
    }

    /**
     * Add an element only if it doesn't exist, and return only when
     * the element is effectively cached.
     *
     * @param key        Element key
     * @param value      Element value
     * @param expiration Ex: 10s, 3mn, 8h
     * @return If the element an eventually been cached
     */
    public static boolean safeAdd(String key, Object value, String expiration) {
        checkSerializable(value);
        return cacheEngine.safeAdd(key, value, Times.parseDurationToSeconds(expiration));
    }

    /**
     * Add an element only if it doesn't exist and store it indefinitely.
     *
     * @param key   Element key
     * @param value Element value
     */
    public static void add(String key, Object value) {
        checkSerializable(value);
        cacheEngine.add(key, value, Times.parseDurationToSeconds(null));
    }

    /**
     * Set an element.
     *
     * @param key        Element key
     * @param value      Element value
     * @param expiration Ex: 10s, 3mn, 8h, 2d 5h 30min
     */
    public static void set(String key, Object value, String expiration) {
        checkSerializable(value);
        cacheEngine.set(key, value, Times.parseDurationToSeconds(expiration));
    }

    /**
     * Set an element and return only when the element is effectively cached.
     *
     * @param key        Element key
     * @param value      Element value
     * @param expiration Ex: 10s, 3mn, 8h, 2d 5h 30min
     * @return If the element an eventually been cached
     */
    public static boolean safeSet(String key, Object value, String expiration) {
        checkSerializable(value);
        return cacheEngine.safeSet(key, value, Times.parseDurationToSeconds(expiration));
    }

    /**
     * Set an element and store it indefinitely.
     *
     * @param key   Element key
     * @param value Element value
     */
    public static void set(String key, Object value) {
        checkSerializable(value);
        cacheEngine.set(key, value, Times.parseDurationToSeconds(null));
    }

    /**
     * Replace an element only if it already exists.
     *
     * @param key        Element key
     * @param value      Element value
     * @param expiration Ex: 10s, 3mn, 8h, 2d 5h 30min
     */
    public static void replace(String key, Object value, String expiration) {
        checkSerializable(value);
        cacheEngine.replace(key, value, Times.parseDurationToSeconds(expiration));
    }

    /**
     * Replace an element only if it already exists and return only when the
     * element is effectively cached.
     *
     * @param key        Element key
     * @param value      Element value
     * @param expiration Ex: 10s, 3mn, 8h, 2d 5h 30min
     * @return If the element an eventually been cached
     */
    public static boolean safeReplace(String key, Object value, String expiration) {
        checkSerializable(value);
        return cacheEngine.safeReplace(key, value, Times.parseDurationToSeconds(expiration));
    }

    /**
     * Replace an element only if it already exists and store it indefinitely.
     *
     * @param key   Element key
     * @param value Element value
     */
    public static void replace(String key, Object value) {
        checkSerializable(value);
        cacheEngine.replace(key, value, Times.parseDurationToSeconds(null));
    }

    /**
     * Increment the element value (must be a Number).
     *
     * @param key Element key
     * @param by  The incr value
     * @return The new value
     */
    public static long incr(String key, int by) {
        return cacheEngine.incr(key, by);
    }

    /**
     * Increment the element value (must be a Number) by 1.
     *
     * @param key Element key
     * @return The new value
     */
    public static long incr(String key) {
        return cacheEngine.incr(key, 1);
    }

    /**
     * Decrement the element value (must be a Number).
     *
     * @param key Element key
     * @param by  The decr value
     * @return The new value
     */
    public static long decr(String key, int by) {
        return cacheEngine.decr(key, by);
    }

    /**
     * Decrement the element value (must be a Number) by 1.
     *
     * @param key Element key
     * @return The new value
     */
    public static long decr(String key) {
        return cacheEngine.decr(key, 1);
    }

    /**
     * Retrieve an object.
     *
     * @param key The element key
     * @return The element value or null
     */
    public static Object get(String key) {
        return cacheEngine.get(key);
    }

    /**
     * Bulk retrieve.
     *
     * @param key List of keys
     * @return Map of keys & values
     */
    public static Map<String, Object> get(String... key) {
        return cacheEngine.get(key);
    }

    /**
     * Delete an element from the cache.
     *
     * @param key The element key
     */
    public static void delete(String key) {
        cacheEngine.delete(key);
    }

    /**
     * Delete an element from the cache and return only when the
     * element is effectively removed.
     *
     * @param key The element key
     * @return If the element an eventually been deleted
     */
    public static boolean safeDelete(String key) {
        return cacheEngine.safeDelete(key);
    }

    /**
     * Clear all data from cache.
     */
    public static void clear() {
        cacheEngine.clear();
    }

    /**
     * Convenient clazz to get a value a class type;
     *
     * @param <T>   The needed type
     * @param key   The element key
     * @param clazz The type class
     * @return The element value or null
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Class<T> clazz) {
        return (T) cacheEngine.get(key);
    }

    /**
     * Stop the cache system.
     */
    public static void stop() {
        cacheEngine.stop();
    }

    /**
     * Utility that check that an object is serializable.
     */
    static void checkSerializable(Object value) {
        if (value != null && !(value instanceof Serializable)) {
            throw new CacheException("Cannot cache a non-serializable value of type " + value.getClass().getName(), new NotSerializableException(value.getClass().getName()));
        }
    }
}

