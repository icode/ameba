/**
 * jetbrick-template
 * http://subchen.github.io/jetbrick-template/
 *
 * Copyright 2010-2014 Guoqiang Chen. All rights reserved.
 * Email: subchen@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ameba.compiler;

import ameba.util.IOUtils;

import java.io.*;

public class JavaSource {
    public static final String CLASS_EXTENSION = ".class";
    public static final String JAVA_EXTENSION = ".java";

    public static final String JAVA_FILE_ENCODING = "utf-8";
    private final String qualifiedClassName;
    private final File outputDir;
    private final File javaFile;
    private final File classFile;
    private final File inputDir;
    private String sourceCode;
    private byte[] bytecode;

    public JavaSource(String qualifiedClassName, File inputDir, File outputDir) {
        this.qualifiedClassName = qualifiedClassName;
        this.outputDir = outputDir;
        this.inputDir = inputDir;
        String fileName = qualifiedClassName.replace(".", File.separator);
        this.javaFile = new File(inputDir, fileName + ".java");
        this.classFile = new File(outputDir, fileName + ".class");
    }

    public JavaSource(String qualifiedClassName, String sourceCode) {
        this(qualifiedClassName, null, new File(IOUtils.getResource("").getFile()));
    }

    public File getInputDir() {
        return inputDir;
    }

    public byte[] getBytecode() {
        return bytecode;
    }

    public void setBytecode(byte[] bytecode) {
        this.bytecode = bytecode;
    }

    public void clean() {
        if (javaFile.exists()) {
            javaFile.delete();
        }
        if (classFile.exists()) {
            classFile.delete();
        }
    }

    public void saveJavaFile() throws IOException {
        javaFile.getParentFile().mkdirs();

        OutputStream out = new FileOutputStream(javaFile);
        try {
            out.write(sourceCode.getBytes(JAVA_FILE_ENCODING));
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public void saveClassFile() throws IOException {
        classFile.getParentFile().mkdirs();

        OutputStream out = new FileOutputStream(classFile);
        try {
            out.write(bytecode);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public String getClassName() {
        return qualifiedClassName;
    }

    public String getSourceCode() {
        if (sourceCode == null) {
            synchronized (this) {
                InputStream in = null;
                try {
                    in = new FileInputStream(getJavaFile());
                    sourceCode = IOUtils.read(in);
                } catch (FileNotFoundException e) {
                    IOUtils.closeQuietly(in);
                }
            }
        }
        return sourceCode;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public File getJavaFile() {
        return javaFile;
    }

    public File getClassFile() {
        return classFile;
    }
}
