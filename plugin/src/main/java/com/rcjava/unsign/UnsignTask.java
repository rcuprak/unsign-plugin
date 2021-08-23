package com.rcjava.unsign;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.gradle.internal.impldep.bsh.commands.dir;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Performs the unsigning
 * @author Ryan Cuprak
 */
public abstract class UnsignTask extends DefaultTask {

    /**
     * Directory containing the jars we want to unsign
     */
    @InputDirectory
    @Optional
    DirectoryProperty directory = getProject().getObjects().directoryProperty().convention(getProject().getLayout().getProjectDirectory().dir("default-dir"));

    /**
     * Returns the directory
     * @return directory
     */
    public abstract DirectoryProperty getDirectory();

    /**
     * Performs the unsigning
     */
    @TaskAction
    public void unsign() {
        System.out.println("Unsigning the jars!");
        Object obj = getProject().getExtensions().findByName("unsign");
        int count = 0;
        if(obj != null) {
            UnsignExtension ue = (UnsignExtension)obj;
            Directory dir = ue.directory.getOrNull();
            if(dir != null) {
                count = performUnsigning(dir.getAsFile());
            }
        } else {
            count = performUnsigning(directory.get().getAsFile());
        }
        System.out.println("Total signatures removed: " + count);
    }

    /**
     * Performs the unsigning
     * @param directory - directory
     * @return count - number of jar files unsigned
     */
    private int performUnsigning(File directory) {
        int count = 0;
        if(directory != null && directory.isDirectory()) {
            File[] jars = directory.listFiles(pathname -> pathname.getName().toLowerCase().endsWith(".jar"));
            System.out.println("JARS: " + jars.length);
            if(jars != null) {
                for(File f : jars) {
                    try {
                        if(UnsignLogic.checkSigned(f)) {
                            UnsignLogic.unsignJar(f.toPath(),true);
                            count++;
                        }
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                    System.out.println("File: " + f.getName());
                }
            }
        } else {
            System.err.println(directory + " is not a directory!");
        }
        return count;
    }

}
