package com.vdoc.maven.plugin.packaging.impl;

import com.vdoc.maven.plugin.CreateSetupMojo;
import com.vdoc.maven.plugin.create.setup.beans.CompletedModule;
import com.vdoc.maven.plugin.create.setup.enums.PackagingType;
import com.vdoc.maven.plugin.packaging.Packaging;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractPackaging implements Packaging {
	
	protected static final String BASE_ZIP_FOLDER = "";
	protected static final String SETUP_SUFFIX = "setup";
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPackaging.class);
	
	/**
	 * this is used to synchronize multiple modules build on multiple thread
	 */
	public static final BlockingQueue<CompletedModule> COMPLETED_MODULES = new LinkedBlockingQueue<>();
	/**
	 * this lock is used to avoid multiple includeOtherModules use.
	 */
	protected static Boolean completedModulesLock = Boolean.FALSE;
	private CreateSetupMojo createSetupMojo;
	
	public Set<String> getFinalZipEntrys() {
		return getCreateSetupMojo().getFinalZipEntrys();
	}
	
	public RepositorySystem getRepoSystem() {
		return getCreateSetupMojo().getRepoSystem();
	}
	
	public RepositorySystemSession getRepoSession() {
		return getCreateSetupMojo().getRepoSession();
	}
	
	public PackagingType getPackagingType() {
		return getCreateSetupMojo().getPackagingType();
	}
	
	public String getSetupName() {
		return getCreateSetupMojo().getSetupName();
	}
	
	public File getLibFolder() {
		return getCreateSetupMojo().getLibFolder();
	}
	
	public boolean isIncludeTest() {
		return getCreateSetupMojo().isIncludeTest();
	}
	
	public boolean isIncludeJavadoc() {
		return getCreateSetupMojo().isIncludeJavadoc();
	}
	
	public boolean isIncludeSource() {
		return getCreateSetupMojo().isIncludeSource();
	}
	
	public boolean isIncludeOtherModules() {
		return getCreateSetupMojo().isIncludeOtherModules();
	}
	
	public boolean isIncludeDependenciesSetups() {
		return getCreateSetupMojo().isIncludeDependenciesSetups();
	}
	
	public List<String> getDependenciesSetupsGroupIds() {
		return getCreateSetupMojo().getDependenciesSetupsGroupIds();
	}
	
	public File getVdocHome() {
		return getCreateSetupMojo().getVdocHome();
	}
	
	public long getIncludeOtherModulesTimeout() {
		return getCreateSetupMojo().getIncludeOtherModulesTimeout();
	}
	
	public boolean isIncludeTestDataCreation() {
		return getCreateSetupMojo().isIncludeTestDataCreation();
	}
	
	public MavenProject getProject() {
		return getCreateSetupMojo().getProject();
	}
	
	public MavenSession getSession() {
		return getCreateSetupMojo().getSession();
	}
	
	public File getBuildDirectory() {
		return getCreateSetupMojo().getBuildDirectory();
	}
	
	public String getJarName() {
		return getCreateSetupMojo().getJarName();
	}
	
	public Map getPluginContext() {
		return getCreateSetupMojo().getPluginContext();
	}
	
	public MavenProjectHelper getProjectHelper() {
		return getCreateSetupMojo().getProjectHelper();
	}
	
	public List<RemoteRepository> getRemoteRepos() {
		return getCreateSetupMojo().getRemoteRepos();
	}
	
	/**
	 * get {@link AbstractPackaging#createSetupMojo} property
	 *
	 * @return get the createSetupMojo property
	 **/
	public CreateSetupMojo getCreateSetupMojo() {
		return createSetupMojo;
	}
	
	@Override
	public void init(CreateSetupMojo createSetupMojo) {
		this.createSetupMojo = createSetupMojo;
	}
	
	@Override
	public File execute() throws IOException, MojoExecutionException {
		return createSetup();
	}
	
	protected abstract File createSetup() throws IOException, MojoExecutionException;
	
	
	/**
	 * compress a directory to an archive
	 *
	 * @param outputStream the archive stream
	 * @param directory    the source directory
	 * @param base         the zip base directory
	 * @throws IOException
	 */
	protected void compressDirectory(ArchiveOutputStream outputStream, File directory, String base) throws IOException {
		
		Validate.notNull(outputStream);
		Validate.notNull(directory);
		Validate.notNull(base);
		
		if (!directory.exists()) {
			throw new IllegalArgumentException("directory '" + directory.getPath() + "' to compress not found!");
		}
		
		// we must remove first / for base archive entry else we get a blank directory.
		if (base.startsWith("/")) {
			base = StringUtils.substring(base, 1);
		}
		// ignore all files whose start with ~
		if (directory.isFile() && directory.getName().startsWith("~")) {
			LOGGER.debug("File ignored : " + directory);
			return;
		}
		
		String entryName = base + directory.getName();
		LOGGER.debug("Add Zip entry : " + entryName);
		ArchiveEntry tarEntry = outputStream.createArchiveEntry(directory, entryName);
		outputStream.putArchiveEntry(tarEntry);
		
		if (directory.isFile()) {
			try (FileInputStream fis = new FileInputStream(directory)) {
				IOUtils.copy(fis, outputStream);
			}
			outputStream.closeArchiveEntry();
		}
		else {
			outputStream.closeArchiveEntry();
			File[] children = directory.listFiles();
			if (children != null) {
				for (File child : children) {
					this.compressDirectory(outputStream, child, entryName + '/');
				}
			}
		}
	}
	
	@Override
	public File createTestsDataSetup() throws IOException, MojoExecutionException {
		
		LOGGER.info("Create the VDoc tests data packaging Zip.");
		File vdocTestDataOutput = createTestDataZip();
		
		if ((getVdocHome() != null) && getVdocHome().exists()) {
			FileUtils.copyFileToDirectory(vdocTestDataOutput, new File(getVdocHome(), "apps"));
		}
		
		LOGGER.info("create the tests data meta setup zip with apps, documentation, fix, ...");
		File metaAppOutput = createMetaSetupTestData(vdocTestDataOutput);
		
		return metaAppOutput;
	}
	
	public void complete(File setup) throws MojoExecutionException {
		// default nothing to do
	}
	
	protected File createTestDataZip() throws IOException {
		File vdocTestDataOutput = new File(getBuildDirectory(),  "tests-data-"+ getSetupName() + ".zip");
		try (ZipArchiveOutputStream output = new ZipArchiveOutputStream(vdocTestDataOutput)) {
			LOGGER.debug("try to add tests data custom resources ");
			for (Resource r : getProject().getTestResources()) {
				File resourcesDirectory = new File(r.getDirectory());
				File customFolder = new File(resourcesDirectory.getParentFile(), "custom");
				LOGGER.debug("add tests data custom folder " + customFolder.getAbsolutePath());
				if (customFolder.isDirectory()) {
					File[] customFolders = customFolder.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
					for (File f : customFolders) {
						this.compressDirectory(output, f, BASE_ZIP_FOLDER);
					}
				}
			}
		}
		return vdocTestDataOutput;
	}
	
	protected File createMetaSetupTestData(File vdocAppOutput) throws IOException, MojoExecutionException {
		File metaAppOutput = new File(getBuildDirectory(), "tests-data-"+getSetupName() + '-' + SETUP_SUFFIX + ".zip");
		try (ZipArchiveOutputStream output = new ZipArchiveOutputStream(metaAppOutput)) {
			for (Resource r : getProject().getTestResources()) {
				Path userAppsCustomFolder = Paths.get(r.getDirectory()).getParent().resolve("user_apps_custom");
				LOGGER.info("looking for the tests data user_apps_custom in {}", userAppsCustomFolder);
				if (Files.isDirectory(userAppsCustomFolder)) {
					LOGGER.info("tests data user_apps_custom found {} add it's files into custom zip folder", userAppsCustomFolder);
					File[] customFolders = userAppsCustomFolder.toFile().listFiles();
					for (File f : customFolders) {
						this.compressDirectory(output, f, "custom/");
						LOGGER.debug("add folder {} to tests data custom zip folder ", f);
					}
				}
			}
			// add the packaged tests data apps
			LOGGER.debug("add the tests data packaged apps");
			this.compressDirectory(output, vdocAppOutput, BASE_ZIP_FOLDER + "apps/");
		}
		return metaAppOutput;
	}
}
