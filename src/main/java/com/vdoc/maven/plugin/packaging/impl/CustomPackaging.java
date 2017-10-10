package com.vdoc.maven.plugin.packaging.impl;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.IOException;

public class CustomPackaging extends AbstractPackaging {
	
	@Override
	protected File createSetup() throws IOException, MojoExecutionException {
		throw new NotImplementedException("");
	}
}
