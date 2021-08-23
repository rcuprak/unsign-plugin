package com.rcjava.unsign;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.Task;

/**
 * Gradle plugin for removing signatures.
 * @author Ryan Cuprak
 */
public class UnsignPlugin implements Plugin<Project> {

    public void apply(Project project) {
        project.getExtensions().create("unsign",UnsignExtension.class);
        Task t = project.getTasks().create("unsign",UnsignTask.class);
        t.setGroup("deploy");
    }
}
