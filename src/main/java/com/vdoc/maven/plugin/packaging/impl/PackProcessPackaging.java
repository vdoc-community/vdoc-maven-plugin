package com.vdoc.maven.plugin.packaging.impl;

import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;

public class PackProcessPackaging extends AppsPackaging {
	
	@Override
	protected void includeDependenciesSetups(ZipArchiveOutputStream output) throws IOException, MojoExecutionException {
	}
}
