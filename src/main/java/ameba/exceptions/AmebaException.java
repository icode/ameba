package ameba.exceptions;

import ameba.Ameba;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author icode
 */
public abstract class AmebaException extends RuntimeException {
    static AtomicLong atomicLong = new AtomicLong(System.currentTimeMillis());
    protected String id;

    public AmebaException() {
        setId();
    }

    public AmebaException(String message) {
        super(message);
        setId();
    }

    public AmebaException(String message, Throwable cause) {
        super(message, cause);
        setId();
    }

    public static StackTraceElement getInterestingStackTraceElement(Throwable cause) {
        for (StackTraceElement stackTraceElement : cause.getStackTrace()) {
            if (stackTraceElement.getLineNumber() > 0 &&
                    stackTraceElement.getClassName().startsWith(Ameba.getApp().getSourceRoot().getAbsolutePath())) {
                return stackTraceElement;
            }
        }
        return null;
    }

    void setId() {
        long nid = atomicLong.incrementAndGet();
        id = Long.toString(nid, 26);
    }

    public boolean isSourceAvailable() {
        return this instanceof SourceAttachment;
    }

    public Integer getLineNumber() {
        return -1;
    }

    public String getSourceFile() {
        return "";
    }

    public String getId() {
        return id;
    }
}
