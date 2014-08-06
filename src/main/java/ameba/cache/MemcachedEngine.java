package ameba.cache;

import com.google.common.collect.Sets;
import org.glassfish.grizzly.memcached.GrizzlyMemcachedCache;
import org.glassfish.grizzly.memcached.GrizzlyMemcachedCacheManager;
import org.glassfish.grizzly.memcached.ValueWithCas;
import org.glassfish.grizzly.memcached.ValueWithKey;

import javax.ws.rs.core.FeatureContext;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author icode
 */
public class MemcachedEngine<K, V> implements CacheEngine<K, V> {
    final org.glassfish.grizzly.memcached.MemcachedCache<K, V> cache;

    @Override
    public void add(K key, V value, int expiration) {
        cache.add(key, value, expiration, true);
    }

    @Override
    public boolean safeAdd(K key, V value, int expiration) {
        return cache.add(key, value, expiration, false);
    }

    @Override
    public void set(K key, V value, int expiration) {
        cache.set(key, value, expiration, true);
    }

    @Override
    public boolean safeSet(K key, V value, int expiration) {
        return cache.set(key, value, expiration, false);
    }

    @Override
    public void replace(K key, V value, int expiration) {
        cache.replace(key, value, expiration, true);
    }

    @Override
    public boolean safeReplace(K key, V value, int expiration) {
        return cache.replace(key, value, expiration, false);
    }

    @Override
    public V get(K key) {
        return cache.get(key, false);
    }

    @Override
    public Map<K, V> get(K[] keys) {
        return cache.getMulti(Sets.newHashSet(keys));
    }

    @Override
    public V gat(K key, int expiration) {
        return cache.gat(key, expiration, true);
    }

    @Override
    public long incr(K key, int by, final long initial, final int expirationInSecs) {
        return cache.incr(key, by, initial, expirationInSecs, false);
    }

    @Override
    public long decr(K key, int by, final long initial, final int expirationInSecs) {
        return cache.decr(key, by, initial, expirationInSecs, false);
    }

    @Override
    public void clear() {
        for (SocketAddress address : cache.getCurrentServerList()) {
            cache.flushAll(address, -1, true);
        }
    }

    @Override
    public void delete(K key) {
        cache.delete(key, true);
    }

    @Override
    public boolean safeDelete(K key) {
        return cache.delete(key, false);
    }

    @Override
    public void stop() {
        this.cache.stop();
    }

    public boolean set(K key, V value, int expirationInSecs, boolean noReply, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.set(key, value, expirationInSecs, noReply, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public long decr(K key, long delta, long initial, int expirationInSecs, boolean noReply, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.decr(key, delta, initial, expirationInSecs, noReply, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public boolean noop(SocketAddress address, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.noop(address, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public String version(SocketAddress address) {
        return cache.version(address);
    }

    public Map<String, String> statsItems(SocketAddress address, String item, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.statsItems(address, item, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public boolean touch(K key, int expirationInSecs, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.touch(key, expirationInSecs, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public boolean quit(SocketAddress address, boolean noReply) {
        return cache.quit(address, noReply);
    }

    public boolean add(K key, V value, int expirationInSecs, boolean noReply, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.add(key, value, expirationInSecs, noReply, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public boolean verbosity(SocketAddress address, int verbosity, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.verbosity(address, verbosity, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public long incr(K key, long delta, long initial, int expirationInSecs, boolean noReply) {
        return cache.incr(key, delta, initial, expirationInSecs, noReply);
    }

    public boolean touch(K key, int expirationInSecs) {
        return cache.touch(key, expirationInSecs);
    }

    public boolean replace(K key, V value, int expirationInSecs, boolean noReply, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.replace(key, value, expirationInSecs, noReply, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public boolean append(K key, V value, boolean noReply, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.append(key, value, noReply, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public V gat(K key, int expirationInSecs, boolean noReplys) {
        return cache.gat(key, expirationInSecs, noReplys);
    }

    public boolean append(K key, V value, boolean noReply) {
        return cache.append(key, value, noReply);
    }

    public Map<K, Boolean> casMulti(Map<K, ValueWithCas<V>> map, int expirationInSecs) {
        return cache.casMulti(map, expirationInSecs);
    }

    public V get(K key, boolean noReply) {
        return cache.get(key, noReply);
    }

    public boolean delete(K key, boolean noReply, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.delete(key, noReply, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public boolean verbosity(SocketAddress address, int verbosity) {
        return cache.verbosity(address, verbosity);
    }

    public boolean quit(SocketAddress address, boolean noReply, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.quit(address, noReply, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public void removeServer(SocketAddress serverAddress) {
        cache.removeServer(serverAddress);
    }

    public boolean addServer(SocketAddress serverAddress) {
        return cache.addServer(serverAddress);
    }

    public boolean replace(K key, V value, int expirationInSecs, boolean noReply) {
        return cache.replace(key, value, expirationInSecs, noReply);
    }

    public Map<K, Boolean> setMulti(Map<K, V> map, int expirationInSecs) {
        return cache.setMulti(map, expirationInSecs);
    }

    public String saslAuth(SocketAddress address, String mechanism, byte[] data) {
        return cache.saslAuth(address, mechanism, data);
    }

    public String version(SocketAddress address, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.version(address, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public boolean isInServerList(SocketAddress serverAddress) {
        return cache.isInServerList(serverAddress);
    }

    public boolean add(K key, V value, int expirationInSecs, boolean noReply) {
        return cache.add(key, value, expirationInSecs, noReply);
    }

    public ValueWithKey<K, V> getKey(K key, boolean noReply) {
        return cache.getKey(key, noReply);
    }

    public Map<K, Boolean> deleteMulti(Set<K> keys, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.deleteMulti(keys, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public Map<K, Boolean> deleteMulti(Set<K> keys) {
        return cache.deleteMulti(keys);
    }

    public boolean prepend(K key, V value, boolean noReply, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.prepend(key, value, noReply, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public Map<String, String> statsItems(SocketAddress address, String item) {
        return cache.statsItems(address, item);
    }

    public ValueWithKey<K, V> getKey(K key, boolean noReply, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.getKey(key, noReply, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public Map<K, V> getMulti(Set<K> keys, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.getMulti(keys, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public boolean cas(K key, V value, int expirationInSecs, long cas, boolean noReply, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.cas(key, value, expirationInSecs, cas, noReply, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public V gat(K key, int expirationInSecs, boolean noReply, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.gat(key, expirationInSecs, noReply, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public Map<String, String> stats(SocketAddress address) {
        return cache.stats(address);
    }

    public Map<K, Boolean> casMulti(Map<K, ValueWithCas<V>> map, int expirationInSecs, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.casMulti(map, expirationInSecs, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public boolean prepend(K key, V value, boolean noReply) {
        return cache.prepend(key, value, noReply);
    }

    public boolean flushAll(SocketAddress address, int expirationInSecs, boolean noReply) {
        return cache.flushAll(address, expirationInSecs, noReply);
    }

    public String saslStep(SocketAddress address, String mechanism, byte[] data) {
        return cache.saslStep(address, mechanism, data);
    }

    public long decr(K key, long delta, long initial, int expirationInSecs, boolean noReply) {
        return cache.decr(key, delta, initial, expirationInSecs, noReply);
    }

    public boolean set(K key, V value, int expirationInSecs, boolean noReply) {
        return cache.set(key, value, expirationInSecs, noReply);
    }

    public ValueWithCas<V> gets(K key, boolean noReply) {
        return cache.gets(key, noReply);
    }

    public List<SocketAddress> getCurrentServerList() {
        return cache.getCurrentServerList();
    }

    public boolean cas(K key, V value, int expirationInSecs, long cas, boolean noReplys) {
        return cache.cas(key, value, expirationInSecs, cas, noReplys);
    }

    public ValueWithCas<V> gets(K key, boolean noReply, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.gets(key, noReply, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public String getName() {
        return cache.getName();
    }

    public String saslAuth(SocketAddress address, String mechanism, byte[] data, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.saslAuth(address, mechanism, data, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public Map<K, ValueWithCas<V>> getsMulti(Set<K> keys) {
        return cache.getsMulti(keys);
    }

    public boolean delete(K key, boolean noReply) {
        return cache.delete(key, noReply);
    }

    public boolean flushAll(SocketAddress address, int expirationInSecs, boolean noReply, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.flushAll(address, expirationInSecs, noReply, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public boolean noop(SocketAddress addresss) {
        return cache.noop(addresss);
    }

    public Map<K, ValueWithCas<V>> getsMulti(Set<K> keys, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.getsMulti(keys, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public Map<String, String> stats(SocketAddress address, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.stats(address, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public String saslStep(SocketAddress address, String mechanism, byte[] data, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.saslStep(address, mechanism, data, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public Map<K, Boolean> setMulti(Map<K, V> map, int expirationInSecs, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.setMulti(map, expirationInSecs, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public String saslList(SocketAddress address) {
        return cache.saslList(address);
    }

    public long incr(K key, long delta, long initial, int expirationInSecs, boolean noReply, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.incr(key, delta, initial, expirationInSecs, noReply, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public V get(K key, boolean noReply, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.get(key, noReply, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public String saslList(SocketAddress address, long writeTimeoutInMillis, long responseTimeoutInMillis) {
        return cache.saslList(address, writeTimeoutInMillis, responseTimeoutInMillis);
    }

    public static class MemcachedCache {
        public static <K, V> org.glassfish.grizzly.memcached.MemcachedCache<K, V> create(Set<SocketAddress> servers) {
            return create(new GrizzlyMemcachedCacheManager.Builder().build(), servers);
        }

        public static <K, V> org.glassfish.grizzly.memcached.MemcachedCache<K, V> create(GrizzlyMemcachedCacheManager manager, Set<SocketAddress> servers) {
            return create("AMEBA_CACHE", manager, servers);
        }

        public static <K, V> org.glassfish.grizzly.memcached.MemcachedCache<K, V> create(String cacheName, GrizzlyMemcachedCacheManager manager, Set<SocketAddress> servers) {
            // gets the cache builder
            final GrizzlyMemcachedCache.Builder<K, V> builder = manager.createCacheBuilder(cacheName);
            // initializes Memcached's list
            builder.servers(servers);
            // creates the cache
            return builder.build();
        }
    }

    public static <K, V> MemcachedEngine<K, V> create(Set<SocketAddress> servers) {
        return new MemcachedEngine<K, V>(MemcachedCache.<K, V>create(servers));
    }

    public static <K, V> MemcachedEngine<K, V> create(GrizzlyMemcachedCacheManager manager, Set<SocketAddress> servers) {
        return new MemcachedEngine<K, V>(MemcachedCache.<K, V>create(manager, servers));
    }

    public static <K, V> MemcachedEngine<K, V> create(String cacheName, GrizzlyMemcachedCacheManager manager, Set<SocketAddress> servers) {
        return new MemcachedEngine<K, V>(MemcachedCache.<K, V>create(cacheName, manager, servers));
    }

    private MemcachedEngine(org.glassfish.grizzly.memcached.MemcachedCache<K, V> cache) {
        this.cache = cache;
    }

    public MemcachedEngine(FeatureContext context) {
        this.cache = MemcachedCache.create((Set<SocketAddress>) null);
    }

}
