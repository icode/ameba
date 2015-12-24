package ameba.scanner;

import ameba.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * @author icode
 */
public class ClassFoundEvent implements Event {
    private static final Logger logger = LoggerFactory.getLogger(ClassFoundEvent.class);

    boolean accept;
    private boolean cacheMode = false;
    private ClassInfo classInfo;

    public ClassFoundEvent(ClassInfo classInfo, boolean cacheMode) {
        this.cacheMode = cacheMode;
        this.classInfo = classInfo;
    }

    public ClassFoundEvent(ClassInfo classInfo) {
        this.classInfo = classInfo;
    }

    public InputStream getFileStream() {
        return classInfo.getFileStream();
    }

    public void accept(Acceptable<ClassInfo> accept) {
        try {
            boolean re = accept.accept(classInfo);
            if (!this.accept && re)
                this.accept = true;
        } catch (Exception e) {
            logger.error("class accept error", e);
        }
    }

    public boolean isCacheMode() {
        return cacheMode;
    }
}