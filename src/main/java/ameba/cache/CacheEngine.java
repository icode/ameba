package ameba.cache;

import java.util.Map;

/**
 * 缓存引擎接口
 *
 * @author icode
 */
public interface CacheEngine<K, V> {
    public void add(K key, V value, int expiration);

    public boolean safeAdd(K key, V value, int expiration);

    public void set(K key, V value, int expiration);

    public boolean safeSet(K key, V value, int expiration);

    public void replace(K key, V value, int expiration);

    public boolean safeReplace(K key, V value, int expiration);

    public V get(K key);

    public V gat(K key, int expiration);

    public boolean touch(K key, int expiration);

    public Map<K, V> get(K[] keys);

    public long incr(K key, int by, final long initial, final int expirationInSecs);

    public long decr(K key, int by, final long initial, final int expirationInSecs);

    public void clear();

    public void delete(K key);

    public boolean safeDelete(K key);

    public void stop();
}
