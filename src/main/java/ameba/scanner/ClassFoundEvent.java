package ameba.scanner;

import ameba.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * <p>ClassFoundEvent class.</p>
 *
 * @author icode
 * @version $Id: $Id
 */
public class ClassFoundEvent implements Event {
    private static final Logger logger = LoggerFactory.getLogger(ClassFoundEvent.class);

    boolean accept;
    private boolean cacheMode = false;
    private ClassInfo classInfo;

    /**
     * <p>Constructor for ClassFoundEvent.</p>
     *
     * @param classInfo a {@link ameba.scanner.ClassInfo} object.
     * @param cacheMode a boolean.
     */
    public ClassFoundEvent(ClassInfo classInfo, boolean cacheMode) {
        this.cacheMode = cacheMode;
        this.classInfo = classInfo;
    }

    /**
     * <p>Constructor for ClassFoundEvent.</p>
     *
     * @param classInfo a {@link ameba.scanner.ClassInfo} object.
     */
    public ClassFoundEvent(ClassInfo classInfo) {
        this.classInfo = classInfo;
    }

    /**
     * <p>getFileStream.</p>
     *
     * @return a {@link java.io.InputStream} object.
     */
    public InputStream getFileStream() {
        return classInfo.getFileStream();
    }

    /**
     * <p>accept.</p>
     *
     * @param accept a {@link ameba.scanner.Acceptable} object.
     */
    public void accept(Acceptable<ClassInfo> accept) {
        try {
            boolean re = accept.accept(classInfo);
            if (!this.accept && re)
                this.accept = true;
        } catch (Exception e) {
            logger.error("class accept error", e);
        }
    }

    /**
     * <p>isCacheMode.</p>
     *
     * @return a boolean.
     */
    public boolean isCacheMode() {
        return cacheMode;
    }
}
