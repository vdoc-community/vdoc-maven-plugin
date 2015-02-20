package com.vdoc.maven.plugin.create.setup.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Is used by Create setup mojo to join multi modules project
 * Created by famaridon on 06/01/2015.
 */
public class CompletedModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompletedModule.class);

	private final String artifactId;
	private final File setup;

	public CompletedModule(String artifactId, File setup) {
        super();
        this.artifactId = artifactId;
        this.setup = setup;
    }

	public String getArtifactId() {
        return this.artifactId;
    }

	public File getSetup() {
        return this.setup;
    }
}
