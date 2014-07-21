package ameba.exceptions;

import ameba.Ameba;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

/**
 * @author icode
 */
public abstract class AmebaException extends RuntimeException {
    public AmebaException() {

    }

    public AmebaException(String message) {
        super(message);
    }

    public AmebaException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InterestingSomething getInterestingSomething(Throwable cause) {
        InterestingSomething something = null;
        for (StackTraceElement stackTraceElement : cause.getStackTrace()) {
            if (stackTraceElement.getLineNumber() > 0) {
                String path = stackTraceElement.getClassName().replaceAll("\\.", File.separator);
                path = path.substring(0, path.lastIndexOf(File.separator));
                File source = new File(Ameba.getApp().getPackageRoot(), path);
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
                        something.setUsefulFiles(Lists.<File>newArrayList());
                        something.setUsefulStackTraceElement(Lists.<StackTraceElement>newArrayList());
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

    public boolean isSourceAvailable() {
        return this instanceof SourceAttachment;
    }

    public Integer getLineNumber() {
        return -1;
    }

    public File getSourceFile() {
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
