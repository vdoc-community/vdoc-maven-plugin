package com.vdoc.maven.plugin.deploy.vdoc.beans;

import com.vdoc.maven.plugin.utils.OSUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Is the maven home
 * Created by famaridon on 21/02/15.
 */
public class Maven {
    private static final Logger LOGGER = LoggerFactory.getLogger(Maven.class);

    private final PluginDescriptor pluginDescriptor;
    private final MavenProject project;
    private final MavenSession session;
    private final File mavenHome;
    private final File mvn;

    public Maven(MavenSession session, MavenProject project, PluginDescriptor pluginDescriptor, File mavenHome) {
        super();
        this.pluginDescriptor = pluginDescriptor;
        this.project = project;
        this.session = session;
        // Check maven home directory.
        if (mavenHome == null) {
            String mavenEnv = System.getenv("M2_HOME");
            Validate.notEmpty(mavenEnv, "M2_HOME is not set you can used the -DmavenHome property!");
            this.mavenHome = new File(mavenEnv);
        }
        else
        {
            this.mavenHome = mavenHome;
        }
        if (!this.mavenHome.exists()) {
            throw new IllegalArgumentException("maven home (M2_HOME or mavenHome) is set to bad location : " + this.mavenHome.getAbsolutePath());
        }
        
        
        this.mvn = new File(this.mavenHome, "/bin/mvn" + (OSUtils.isWindows() ? ".bat" : ""));
    }

    public File getMvn() {
        return this.mvn;
    }

    public File getMavenHome() {
        return this.mavenHome;
    }

    public MavenProject getProject() {
        return this.project;
    }

    public MavenSession getSession() {
        return this.session;
    }

    public PluginDescriptor getPluginDescriptor() {
        return this.pluginDescriptor;
    }
}
