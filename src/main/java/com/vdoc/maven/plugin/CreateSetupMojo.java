package com.vdoc.maven.plugin;

import com.vdoc.maven.plugin.create.setup.enums.PackagingType;
import com.vdoc.maven.plugin.packaging.Packaging;
import com.vdoc.maven.plugin.packaging.ZipEntryFilter;
import com.vdoc.maven.plugin.packaging.impl.filters.VersionPropertyFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProjectHelper;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * this task is used to create a project setup.
 */
@Mojo(name = "create-setup", threadSafe = false, defaultPhase = LifecyclePhase.PACKAGE)
public class CreateSetupMojo extends AbstractVDocMojo {
	
	public static final String BASE_ZIP_FOLDER = "";
	public static final String SETUP_SUFFIX = "setup";
	private static final Logger LOGGER = LoggerFactory.getLogger(CreateSetupMojo.class);
	/**
	 * this lock is used to avoid multiple includeOtherModules use.
	 */
	private static Boolean completedModulesLock = Boolean.FALSE;
	
	/**
	 * The entry point to Aether, i.e. the component doing all the work.
	 */
	@Component
	private RepositorySystem repoSystem;
	
	/**
	 * The current repository/network configuration of Maven.
	 */
	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
	private RepositorySystemSession repoSession;
	
	/**
	 * The project's remote repositories to use for the resolution of plugins and their dependencies.
	 */
	@Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
	private List<RemoteRepository> remoteRepos;
	
	/**
	 * Used for attaching the artifact in the project.
	 */
	@Component
	private MavenProjectHelper projectHelper;
	
	/**
	 * the VDoc home folder if set the apps is copied into apps folder.
	 */
	@Parameter(required = false)
	private File vdocHome;
	
	/**
	 * the project packaging type actually <b>APPS</b> is the only supported value.
	 */
	@Parameter(defaultValue = "APPS")
	private PackagingType packagingType;
	/**
	 * the apps file name without extension. The setup is suffixed with <b>-setup</b>.
	 */
	@Parameter(required = true)
	private String setupName;
	
	/**
	 * where found dependency jars
	 */
	@Parameter(defaultValue = "${project.build.directory}/lib")
	private File libFolder;
	/**
	 * if true test jar should be included in setup
	 */
	@Parameter(defaultValue = "true")
	private boolean includeTest;
	/**
	 * if true test javadoc should be included in setup
	 */
	@Parameter(defaultValue = "false")
	private boolean includeJavadoc;
	/**
	 * if true source jar should be included in setup
	 */
	@Parameter(defaultValue = "false")
	private boolean includeSource;
	/**
	 * other modules should be merged into this project setup.<br>
	 * <b>Warning : </b> if you turn on this option it's highly recommended to used <a href="https://cwiki.apache.org/confluence/display/MAVEN/Parallel+builds+in+Maven+3" >Parallel builds</a>.<br>
	 * Else the module with this option set to true should be the last to be build (to avoid thread locking).
	 */
	@Parameter(defaultValue = "false")
	private boolean includeOtherModules;
	/**
	 * how many time this thread should wait for other modules in second.
	 */
	@Parameter(defaultValue = "30")
	private long includeOtherModulesTimeout;
	/**
	 * dependencies setup should be merged into this project setup
	 */
	@Parameter(defaultValue = "true")
	private boolean includeDependenciesSetups;
	/**
	 * Create a tests-data setup as well as the APPS setup
	 */
	@Parameter(defaultValue = "false")
	private boolean includeTestDataCreation;
	
	private List<ZipEntryFilter> zipEntryFilters = new ArrayList<>();
	
	/**
	 * TODO :
	 */
	private Set<String> finalZipEntrys = new HashSet<>();
	
	/**
	 *
	 */
	private List<String> dependenciesSetupsGroupIds;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		if ("pom".equalsIgnoreCase(this.project.getPackaging())) {
			LOGGER.warn("This mojo can't work for pom packaging project!");
			return;
		}
		
		// TODO : very very poor code
		this.zipEntryFilters.add(new VersionPropertyFilter(StringUtils.substringBeforeLast(this.project.getVersion(), "."), "UTF-8"));
		
		File createdSetup;
		try {
			Packaging packaging = packagingType.getPackaging(this);
			createdSetup = packaging.execute();
			if (includeTestDataCreation) {
				packaging.createTestsDataSetup();
			}
			packaging.complete(createdSetup);
		}
		catch (IOException e) {
			throw new MojoFailureException("Zip File can't be build", e);
		}
	}
	
	public PackagingType getPackagingType() {
		return this.packagingType;
	}
	
	public void setPackagingType(PackagingType packagingType) {
		this.packagingType = packagingType;
	}
	
	public String getSetupName() {
		return this.setupName;
	}
	
	public void setSetupName(String setupName) {
		this.setupName = setupName;
	}
	
	public File getLibFolder() {
		return this.libFolder;
	}
	
	public void setLibFolder(File libFolder) {
		this.libFolder = libFolder;
	}
	
	public boolean isIncludeTest() {
		return this.includeTest;
	}
	
	public void setIncludeTest(boolean includeTest) {
		this.includeTest = includeTest;
	}
	
	public boolean isIncludeJavadoc() {
		return this.includeJavadoc;
	}
	
	public void setIncludeJavadoc(boolean includeJavadoc) {
		this.includeJavadoc = includeJavadoc;
	}
	
	public boolean isIncludeSource() {
		return this.includeSource;
	}
	
	public void setIncludeSource(boolean includeSource) {
		this.includeSource = includeSource;
	}
	
	public boolean isIncludeOtherModules() {
		return this.includeOtherModules;
	}
	
	public void setIncludeOtherModules(boolean includeOtherModules) {
		this.includeOtherModules = includeOtherModules;
	}
	
	public boolean isIncludeDependenciesSetups() {
		return this.includeDependenciesSetups;
	}
	
	public void setIncludeDependenciesSetups(boolean includeDependenciesSetups) {
		this.includeDependenciesSetups = includeDependenciesSetups;
	}
	
	public List<String> getDependenciesSetupsGroupIds() {
		return this.dependenciesSetupsGroupIds;
	}
	
	public void setDependenciesSetupsGroupIds(List<String> dependenciesSetupsGroupIds) {
		this.dependenciesSetupsGroupIds = dependenciesSetupsGroupIds;
	}
	
	public File getVdocHome() {
		return this.vdocHome;
	}
	
	public void setVdocHome(File vdocHome) {
		this.vdocHome = vdocHome;
	}
	
	public long getIncludeOtherModulesTimeout() {
		return this.includeOtherModulesTimeout;
	}
	
	public void setIncludeOtherModulesTimeout(long includeOtherModulesTimeout) {
		this.includeOtherModulesTimeout = includeOtherModulesTimeout;
	}
	
	public boolean isIncludeTestDataCreation() {
		return includeTestDataCreation;
	}
	
	public void setIncludeTestDataCreation(boolean includeTestDataCreation) {
		this.includeTestDataCreation = includeTestDataCreation;
	}
	
	/**
	 * get {@link CreateSetupMojo#projectHelper} property
	 *
	 * @return get the projectHelper property
	 **/
	public MavenProjectHelper getProjectHelper() {
		return projectHelper;
	}
	
	/**
	 * get {@link CreateSetupMojo#remoteRepos} property
	 *
	 * @return get the remoteRepos property
	 **/
	public List<RemoteRepository> getRemoteRepos() {
		return remoteRepos;
	}
	
	/**
	 * get {@link CreateSetupMojo#repoSystem} property
	 *
	 * @return get the repoSystem property
	 **/
	public RepositorySystem getRepoSystem() {
		return repoSystem;
	}
	
	/**
	 * get {@link CreateSetupMojo#repoSession} property
	 *
	 * @return get the repoSession property
	 **/
	public RepositorySystemSession getRepoSession() {
		return repoSession;
	}
	
	/**
	 * get {@link CreateSetupMojo#finalZipEntrys} property
	 *
	 * @return get the finalZipEntrys property
	 **/
	public Set<String> getFinalZipEntrys() {
		return finalZipEntrys;
	}
	
	public List<ZipEntryFilter> getZipEntryFilters()
	{
		return zipEntryFilters;
	}
}
