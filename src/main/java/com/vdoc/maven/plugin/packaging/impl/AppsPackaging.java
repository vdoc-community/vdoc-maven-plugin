package com.vdoc.maven.plugin.packaging.impl;

import com.vdoc.maven.plugin.AbstractVDocMojo;
import com.vdoc.maven.plugin.create.setup.beans.CompletedModule;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AppsPackaging extends AbstractPackaging {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AppsPackaging.class);
	
	@Override
	protected File createSetup() throws IOException, MojoExecutionException {
		LOGGER.info("Create the VDoc apps packaging Zip.");
		File vdocAppOutput = createAppsZip();
		
		// #5 copy apps to vdoc
		if ((getVdocHome() != null) && getVdocHome().exists()) {
			FileUtils.copyFileToDirectory(vdocAppOutput, new File(getVdocHome(), "apps"));
		}
		
		LOGGER.info("create the meta setup zip with apps, documentation, fix, ...");
		File metaAppOutput = createMetaSetup(vdocAppOutput);
		
		LOGGER.debug("adding setup to project artifacts");
		getProjectHelper().attachArtifact(getProject(), "zip", SETUP_SUFFIX, metaAppOutput);
		
		return metaAppOutput;
	}
	
	protected File createAppsZip() throws IOException {
		File vdocAppOutput = new File(getBuildDirectory(), getSetupName() + ".zip");
		try (ZipArchiveOutputStream output = new ZipArchiveOutputStream(vdocAppOutput)) {
			
			LOGGER.debug("try to add custom resources ");
			for (Resource r : getProject().getResources()) {
				File resourcesDirectory = new File(r.getDirectory());
				File customFolder = new File(resourcesDirectory.getParentFile(), "custom");
				LOGGER.debug("add custom folder " + customFolder.getAbsolutePath());
				if (customFolder.isDirectory()) {
					File[] customFolders = customFolder.listFiles((FileFilter)DirectoryFileFilter.INSTANCE);
					for (File f : customFolders) {
						this.compressDirectory(output, f, BASE_ZIP_FOLDER);
					}
				}
			}
			this.compressDirectory(output, AbstractVDocMojo.getJarFile(getBuildDirectory(), getJarName(), null), "lib/");
			if (getLibFolder().exists()) {
				this.compressDirectory(output, getLibFolder(), BASE_ZIP_FOLDER);
			}
			
			if (isIncludeTest()) {
				File testJar = AbstractVDocMojo.getJarFile(getBuildDirectory(), getJarName(), "tests");
				if (testJar.exists()) {
					this.compressDirectory(output, testJar, "lib/");
				}
				else {
					LOGGER.warn("Test jar not found!");
				}
			}
			
			if (isIncludeSource()) {
				this.compressDirectory(output, AbstractVDocMojo.getJarFile(getBuildDirectory(), getJarName(), "source"), "lib/");
			}
			
			if (isIncludeJavadoc()) {
				this.compressDirectory(output, AbstractVDocMojo.getJarFile(getBuildDirectory(), getJarName(), "javadoc"), "lib/");
			}
		}
		return vdocAppOutput;
	}
	
	
	protected File createMetaSetup(File vdocAppOutput) throws IOException, MojoExecutionException {
		File metaAppOutput = new File(getBuildDirectory(), getSetupName() + '-' + SETUP_SUFFIX + ".zip");
		try (ZipArchiveOutputStream output = new ZipArchiveOutputStream(metaAppOutput)) {
			for (Resource r : getProject().getResources()) {
				Path userAppsCustomFolder = Paths.get(r.getDirectory()).getParent().resolve("user_apps_custom");
				LOGGER.info("looking for user_apps_custom in {}", userAppsCustomFolder);
				if (Files.isDirectory(userAppsCustomFolder)) {
					LOGGER.info("user_apps_custom found {} add it's files into custom zip folder", userAppsCustomFolder);
					File[] customFolders = userAppsCustomFolder.toFile().listFiles();
					for (File f : customFolders) {
						this.compressDirectory(output, f, "custom/");
						LOGGER.debug("add folder {} to custom zip folder ", f);
					}
				}
			}
			
			// add the packaged apps
			LOGGER.debug("add the packaged apps");
			this.compressDirectory(output, vdocAppOutput, BASE_ZIP_FOLDER + "apps/");
			includeDependenciesSetups(output);
			
			// include vdoc fix
			File fix = new File(getProject().getBasedir(), "fix");
			if (fix.exists()) {
				LOGGER.debug("add fix folder");
				this.compressDirectory(output, fix, BASE_ZIP_FOLDER);
			}
			
			// include documentation
			File documentation = new File(getProject().getBasedir(), "documentation");
			if (documentation.exists()) {
				LOGGER.debug("add documentation folder");
				this.compressDirectory(output, documentation, BASE_ZIP_FOLDER);
			}
			
			this.includeOtherModules(output);
			
		}
		return metaAppOutput;
	}
	
	protected void includeDependenciesSetups(ZipArchiveOutputStream output) throws IOException, MojoExecutionException {
		File file = new File(getBuildDirectory(), "../apps");
		File[] depApps = file.listFiles((FilenameFilter)new WildcardFileFilter("*-setup.zip"));
		LOGGER.info("merge local apps");
		if (depApps != null) {
			for (File depApp : depApps) {
				LOGGER.warn("merge {} apps.", depApp.getName());
				this.mergeArchive(output, depApp);
			}
		}
		
		
		
		LOGGER.info("remote apps");
		Set<Artifact> setupArtifactSet = new HashSet<>();
		Set<org.apache.maven.artifact.Artifact> artifacts = getArtifacts();
		for (org.apache.maven.artifact.Artifact artifact : artifacts) {
			LOGGER.info("Try to get {}:{}:{}:{}:{}", artifact.getGroupId(), artifact.getArtifactId(), "setup", "zip", artifact.getVersion());
			// check for remote setup
			ArtifactRequest request = new ArtifactRequest();
			request.setArtifact(new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), "setup", "zip", artifact.getVersion()));
			request.setRepositories(getRemoteRepos());
			try {
				ArtifactResult result = getRepoSystem().resolveArtifact(getRepoSession(), request);
				setupArtifactSet.add(result.getArtifact());
			}
			catch (ArtifactResolutionException e) {
				LOGGER.warn("No setup found for {}:{}:{}:{}:{}", artifact.getGroupId(), artifact.getArtifactId(), "setup", "zip", artifact.getVersion());
			}
		}
		for (org.eclipse.aether.artifact.Artifact artifact : setupArtifactSet) {
			LOGGER.warn("merge {} apps.", artifact.getFile().getName());
			this.mergeArchive(output, artifact.getFile());
		}
	}
	
	protected Set<org.apache.maven.artifact.Artifact> getArtifacts() {
		Set<org.apache.maven.artifact.Artifact> artifacts = new HashSet<>();
		for (org.apache.maven.artifact.Artifact artifact : getProject().getDependencyArtifacts()) {
			if ("provided".equals(artifact.getScope()) && (artifact.getGroupId().startsWith("com.vdoc") || artifact.getGroupId().startsWith("com.moovapps"))) {
				artifacts.add(artifact);
			}
		}
		return artifacts;
	}
	
	/**
	 * wait for others modules and merge it into the current module setup
	 *
	 * @param output
	 * @throws MojoExecutionException
	 * @throws IOException
	 */
	protected void includeOtherModules(ZipArchiveOutputStream output) throws MojoExecutionException, IOException {
		// include other modules
		if (isIncludeOtherModules()) {
			
			LOGGER.info("Join for other modules");
			// only 1 module can join others tack lock
			synchronized (completedModulesLock) {
				if (completedModulesLock == Boolean.TRUE) {
					throw new MojoExecutionException("Too many project use includeOtherModules = true");
				}
				completedModulesLock = Boolean.TRUE;
			}
			
			// join other modules if multi-thread else it should be the last compiled module.
			int modulesCount = this.getProject().getParent().getModules().size() - 1;
			do {
				try {
					CompletedModule completedModule = completedModules.poll(getIncludeOtherModulesTimeout(), TimeUnit.SECONDS);
					LOGGER.info("Join module " + completedModule.getArtifactId() + " merge setup file " + completedModule.getSetup().getName());
					
					if (completedModule.getSetup() == null) {
						LOGGER.warn(completedModule.getArtifactId() + " have fail!");
						throw new MojoExecutionException(completedModule.getArtifactId() + " have fail!");
					}
					
					try (FileInputStream fileInputStream = new FileInputStream(completedModule.getSetup());
					     BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
					     ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream(bufferedInputStream)) {
						
						this.mergeArchive(output, input);
						
					}
					catch (ArchiveException e) {
						throw new MojoExecutionException("Can't read module '" + completedModule.getArtifactId() + "' setup file '" + completedModule.getArtifactId() + "'!", e);
					}
					
				}
				catch (InterruptedException e) {
					throw new MojoExecutionException("Waiting for other module fail!", e);
				}
				
				modulesCount--;
			}
			while (modulesCount > 0);
		}
	}
	
	/**
	 * copy any entry of the <b>from</b> archive into the <b>to</b>.
	 *
	 * @param to   the output archive
	 * @param from the source archive
	 * @throws IOException
	 * @throws MojoExecutionException
	 */
	protected void mergeArchive(ArchiveOutputStream to, File from) throws IOException, MojoExecutionException {
		try (FileInputStream fileInputStream = new FileInputStream(from);
		     BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
		     ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream(bufferedInputStream)) {
			
			this.mergeArchive(to, input);
		}
		catch (ArchiveException e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * copy any entry of the <b>from</b> archive into the <b>to</b>.
	 *
	 * @param to   the output archive
	 * @param from the source archive
	 * @throws IOException
	 * @throws MojoExecutionException
	 */
	protected void mergeArchive(ArchiveOutputStream to, ArchiveInputStream from) throws IOException, MojoExecutionException {
		
		long offset = 0L;
		ArchiveEntry entry;
		while ((entry = from.getNextEntry()) != null) {
			if (!to.canWriteEntryData(entry)) {
				throw new MojoExecutionException("Can't merge setup files!");
			}
			LOGGER.debug("merge entry : " + entry.getName());
			if (getFinalZipEntrys().add(entry.getName())) {
				to.putArchiveEntry(entry);
				if (entry.getSize() != 0) {
					IOUtils.copyLarge(from, to, 0, entry.getSize());
					offset += entry.getSize();
				}
				to.closeArchiveEntry();
			}
		}
	}
}
