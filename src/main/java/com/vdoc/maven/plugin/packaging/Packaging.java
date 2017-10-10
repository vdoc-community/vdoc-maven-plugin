package com.vdoc.maven.plugin.packaging;

import com.vdoc.maven.plugin.CreateSetupMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;

public interface Packaging {
	void init(CreateSetupMojo createSetupMojo);
	File execute() throws IOException, MojoExecutionException;
	File createTestsDataSetup() throws IOException, MojoExecutionException ;
}
