package com.rcjava.unsign;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import static org.apache.commons.io.output.NullOutputStream.NULL_OUTPUT_STREAM;

/**
 * Core logic that removes digital signatures from JAR files.
 * @author Ryan Cuprak
 */
public class UnsignLogic {

    /**
     * File extensions for signed jars
     */
    private static final Set<String> ENDINGS = new HashSet<>();

    /**
     * Buffer used in generating the file
     */
    private static final byte[] buffer = new byte[4096];

    static {
        ENDINGS.add(".SF");
        ENDINGS.add(".DSA");
        ENDINGS.add(".EC");
        ENDINGS.add(".RSA");
    }

    /**
     * Checks if a JAR file is signed. Assumes a jar file is signed if there are files in the META-INF directory
     * with .SF, .DSA, .EC or .RSA. Also loops through the classes for completeness.
     * @param jarFile - jar file to check
     * @return boolean - true if the jar is signed
     * @throws IOException - thrown if there is a problem processing the JAR file
     */
    public static boolean checkSigned(File jarFile) throws IOException {
        boolean verify = true;
        JarFile jar = new JarFile(jarFile, verify);
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            IOUtils.copy(jar.getInputStream(entry), NULL_OUTPUT_STREAM);
        }
        entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String fileName = entry.getName().toUpperCase(Locale.ENGLISH);
            if(fileName.endsWith(".SF") || fileName.endsWith(".DSA") || fileName.endsWith(".EC") || fileName.endsWith(".RSA")) {
                return true;
            } else if (!entry.isDirectory()) {
                if(entry.getCodeSigners() != null && entry.getCodeSigners().length > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Duplicates a JAR file without
     * @param jarFile - JAR File
     * @param overwrite - true if we need to overwrite the jar file
     * @throws IOException - thrown if there is an error (file permissions etc.)
     * @return path to unsigned jar
     */
    public static Path unsignJar(Path jarFile, boolean overwrite) throws IOException {
        Path unsignedPath = Files.createTempFile("tmp","jar");
        Path target = null;
        try(JarOutputStream outputStream = new JarOutputStream(new FileOutputStream(unsignedPath.toFile()));
            JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarFile.toFile()))) {
            JarEntry entry;
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if (entry.getName().startsWith("META-INF")) {
                    boolean copy = true;
                    for (String ending : ENDINGS) {
                        if (entry.getName().endsWith(ending)) {
                            copy = false;
                            break;
                        }
                    }
                    if(copy) {
                        copyFile(outputStream,jarInputStream,entry);
                    }
                } else {
                    copyFile(outputStream,jarInputStream,entry);
                }
            }
        } finally {
            if(overwrite) {
                target = jarFile;
                Files.delete(jarFile);
                Files.copy(unsignedPath,jarFile);
                Files.delete(unsignedPath);
            } else {
                String newName = jarFile.getName(jarFile.getNameCount()-1).toString();
                newName = newName.substring(0, newName.lastIndexOf('.')) + "_unsigned.jar";
                target = jarFile.getParent().resolve(newName);
                Files.move(unsignedPath,target);
            }
        }
        return target;
    }

    /**
     * Writes a stream
     */
    private static void copyFile(JarOutputStream outputStream,JarInputStream jarInputStream, JarEntry entry) throws IOException {
        outputStream.putNextEntry(new JarEntry(entry.getName()));
        int read;
        while((read = jarInputStream.read(buffer)) != -1) {
            outputStream.write(buffer,0,read);
        }
        outputStream.closeEntry();
    }
}
