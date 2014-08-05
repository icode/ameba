package ameba.cache;

import ameba.exceptions.AmebaException;

/**
 * @author icode
 */
public class CacheException extends AmebaException {
    public CacheException() {
    }

    public CacheException(Throwable cause) {
        super(cause);
    }

    public CacheException(String message) {
        super(message);
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
