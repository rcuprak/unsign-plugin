package com.rcjava.unsign;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;

import java.io.File;

/**
 * Configuration for the unsign
 * @author Ryan Cuprak
 */
public class UnsignExtension {

    /**
     * Directory we want to loop over
     */
    DirectoryProperty directory;

    /**
     * Initializes the extension
     * @param objects - object factory
     */
    public UnsignExtension(ObjectFactory objects) {
        directory = objects.directoryProperty();
    }

    /**
     * Sets the directory that will be scanned for JARs to remove the signature
     * @param directory - directory
     */
    public void setDirectory(String directory) {
        this.directory.set(new File(directory));
    }

    /**
     * Returns the directory
     * @return - directory
     */
    public String getDirectory() {
        return directory.get().toString();
    }
}
