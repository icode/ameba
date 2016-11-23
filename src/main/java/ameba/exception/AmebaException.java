package ameba.exception;

import com.google.common.collect.Lists;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * <p>AmebaException class.</p>
 *
 * @author icode
 * @since 0.1.6e
 * @version $Id: $Id
 */
public class AmebaException extends RuntimeException {
    /**
     * <p>Constructor for AmebaException.</p>
     */
    public AmebaException() {

    }


    /**
     * <p>Constructor for AmebaException.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     */
    public AmebaException(Throwable cause) {
        super(cause);
    }

    /**
     * <p>Constructor for AmebaException.</p>
     *
     * @param message a {@link java.lang.String} object.
     */
    public AmebaException(String message) {
        super(message);
    }

    /**
     * <p>Constructor for AmebaException.</p>
     *
     * @param message a {@link java.lang.String} object.
     * @param cause   a {@link java.lang.Throwable} object.
     */
    public AmebaException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * <p>getInterestingSomething.</p>
     *
     * @param cause     a {@link java.lang.Throwable} object.
     * @param sourceDir source dir
     * @return a {@link ameba.exception.AmebaException.InterestingSomething} object.
     */
    public static InterestingSomething getInterestingSomething(Throwable cause, File sourceDir) {
        InterestingSomething something = null;
        for (StackTraceElement stackTraceElement : cause.getStackTrace()) {
            if (stackTraceElement.getLineNumber() > 0) {
                String path = stackTraceElement.getClassName().replace(".", "/");
                path = path.substring(0, path.lastIndexOf("/"));
                File source = new File(sourceDir, path);
                if (source.exists() && source.isDirectory()) {
                    String fN = stackTraceElement.getFileName();
                    int index = fN.indexOf("$");
                    if (index < 0) {
                        fN = fN.substring(0, fN.indexOf("."));
                    } else {
                        fN = fN.substring(0, index);
                    }
                    source = new File(source, fN + ".java");
                    if (something == null) {
                        something = new InterestingSomething(stackTraceElement, source);
                        something.setUsefulFiles(Lists.newArrayList());
                        something.setUsefulStackTraceElement(Lists.newArrayList());
                    } else {
                        if (source.exists()) {
                            something.getUsefulStackTraceElements().add(stackTraceElement);
                            something.getUsefulFiles().add(source);
                        }
                    }
                }
            }
        }
        return something;
    }

    /**
     * <p>getInterestingSomething.</p>
     *
     * @param cause a {@link java.lang.Throwable} object.
     * @return a {@link ameba.exception.AmebaException.InterestingSomething} object.
     */
    public static InterestingSomething getInterestingSomething(Throwable cause) {
        return getInterestingSomething(cause, new File("src/main/java"));
    }

    /**
     * <p>isSourceAvailable.</p>
     *
     * @return a boolean.
     */
    public boolean isSourceAvailable() {
        return this instanceof SourceAttachment;
    }

    /**
     * <p>getLineNumber.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getLineNumber() {
        return -1;
    }

    /**
     * <p>getSourceUrls.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public URL getSourceUrl() {
        return null;
    }

    public static class InterestingSomething {
        StackTraceElement stackTraceElement;
        File sourceFile;

        List<StackTraceElement> usefulStackTraceElement;
        List<File> usefulFiles;

        public InterestingSomething(StackTraceElement stackTraceElement, File sourceFile) {
            this.stackTraceElement = stackTraceElement;
            this.sourceFile = sourceFile;
        }

        public List<StackTraceElement> getUsefulStackTraceElements() {
            return usefulStackTraceElement;
        }

        void setUsefulStackTraceElement(List<StackTraceElement> usefulStackTraceElement) {
            this.usefulStackTraceElement = usefulStackTraceElement;
        }

        public List<File> getUsefulFiles() {
            return usefulFiles;
        }

        void setUsefulFiles(List<File> usefulFiles) {
            this.usefulFiles = usefulFiles;
        }

        public StackTraceElement getStackTraceElement() {
            return stackTraceElement;
        }

        public File getSourceFile() {
            return sourceFile;
        }
    }
}
