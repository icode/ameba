package ameba.classloading;

import ameba.Application;
import ameba.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * @author icode
 */
public class AmebaClass {

    private static final Logger logger = LoggerFactory.getLogger(AmebaClass.class);
    /**
     * The fully qualified class name
     */
    public String name;
    /**
     * A reference to the java source file
     */
    public File javaFile;
    /**
     * The Java source
     */
    public String javaSource;
    /**
     * The compiled byteCode
     */
    public byte[] javaByteCode;
    /**
     * The enhanced byteCode
     */
    public byte[] enhancedByteCode;
    /**
     * The in JVM loaded class
     */
    public Class<?> javaClass;
    /**
     * The in JVM loaded package
     */
    public Package javaPackage;
    /**
     * Last time than this class was compiled
     */
    public Long timestamp = 0L;
    /**
     * Signatures checksum
     */
    public int sigChecksum;
    /**
     * Is this class compiled
     */
    boolean compiled;

    Application app;

    public AmebaClass(Application app) {
        this.app = app;
    }

    public AmebaClass(String name, Application app) {
        this.name = name;
        this.app = app;
        this.javaFile = getJava(name, app);
        this.refresh();
    }

    public static File getJava(String name, Application app) {
        String fileName = name;
        if (fileName.contains("$")) {
            fileName = fileName.substring(0, fileName.indexOf("$"));
        }
        fileName = fileName.replace(".", "/") + ".java";
        File pkgRoot = app.getPackageRoot();
        if (pkgRoot != null) {
            File javaFile = new File(pkgRoot, fileName);
            if (javaFile.exists()) {
                return javaFile;
            }
        }
        return null;
    }

    /**
     * Need to refresh this class !
     */
    public void refresh() {
        if (this.javaFile != null) {
            try {
                this.javaSource = IOUtils.read(new FileReader(this.javaFile));
            } catch (FileNotFoundException e) {
                this.javaSource = null;
            }
        }
        this.javaByteCode = null;
        this.enhancedByteCode = null;
        this.compiled = false;
        this.timestamp = 0L;
    }

    /**
     * Enhance this class
     *
     * @return the enhanced byteCode
     */
    public byte[] enhance() {
        this.enhancedByteCode = this.javaByteCode;

        return this.enhancedByteCode;

    }

    /**
     * Is this class already compiled but not defined ?
     *
     * @return if the class is compiled but not defined
     */
    public boolean isDefinable() {
        return compiled && javaClass != null;
    }

    public String getPackage() {
        int dot = name.lastIndexOf('.');
        return dot > -1 ? name.substring(0, dot) : "";
    }

    /**
     * Compile the class from Java source
     *
     * @return the bytes that comprise the class file
     */
    public byte[] compile() {
        long start = System.currentTimeMillis();

        logger.trace("%sms to compile class %s", System.currentTimeMillis() - start, name);
        return this.javaByteCode;
    }

    /**
     * Unload the class
     */
    public void uncompile() {
        this.javaClass = null;
    }

    /**
     * Call back when a class is compiled.
     *
     * @param code The bytecode.
     */
    public void compiled(byte[] code) {
        javaByteCode = code;
        enhancedByteCode = code;
        compiled = true;
        this.timestamp = this.javaFile.lastModified();
    }

    @Override
    public String toString() {
        return name + " (compiled:" + compiled + ")";
    }
}