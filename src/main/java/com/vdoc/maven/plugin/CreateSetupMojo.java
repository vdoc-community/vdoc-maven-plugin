package com.vdoc.maven.plugin;

import com.vdoc.maven.plugin.create.setup.beans.CompletedModule;
import com.vdoc.maven.plugin.create.setup.enums.PackagingType;
import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProjectHelper;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * this task is used to create a project setup.
 */
@Mojo(name = "create-setup", threadSafe = false, defaultPhase = LifecyclePhase.PACKAGE)
public class CreateSetupMojo extends AbstractVDocMojo {

    public static final String BASE_ZIP_FOLDER = "";
    public static final String SETUP_SUFFIX = "setup";
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateSetupMojo.class);
    /**
     * this is used to synchronize multiple modules build on multiple thread
     */
    private static final BlockingQueue<CompletedModule> completedModules = new LinkedBlockingQueue<>();
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

        String s = "";

        File createdSetup;
        try {
            switch (this.packagingType) {
                case APPS:
                    createdSetup = this.createAppsSetup();
                    break;
                case CUSTOM:
                    createdSetup = this.createCustomSetup();
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported packaging type !");
            }

        } catch (IOException e) {
            throw new MojoFailureException("Zip File can't be build", e);
        }
        this.complete(createdSetup);
    }

    public File createAppsSetup() throws IOException, MojoExecutionException {

        LOGGER.info("Create the VDoc apps packaging Zip.");
        File vdocAppOutput = createAppsZip();

        // #5 copy apps to vdoc
        if ((this.vdocHome != null) && this.vdocHome.exists()) {
            FileUtils.copyFileToDirectory(vdocAppOutput, new File(this.vdocHome, "apps"));
        }

        LOGGER.info("create the meta setup zip with apps, documentation, fix, ...");
        File metaAppOutput = createMetaSetup(vdocAppOutput);

        LOGGER.debug("adding setup to project artifacts");
        projectHelper.attachArtifact(this.project, "zip", SETUP_SUFFIX, metaAppOutput);

        return metaAppOutput;
    }

    protected File createAppsZip() throws IOException {
        File vdocAppOutput = new File(this.buildDirectory, this.setupName + ".zip");
        try (ZipArchiveOutputStream output = new ZipArchiveOutputStream(vdocAppOutput)) {

            LOGGER.debug("try to add custom resources ");
            for (Resource r : this.project.getResources()) {
                File resourcesDirectory = new File(r.getDirectory());
                File customFolder = new File(resourcesDirectory.getParentFile(), "custom");
                LOGGER.debug("add custom folder " + customFolder.getAbsolutePath());
                if (customFolder.isDirectory()) {
                    File[] customFolders = customFolder.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
                    for (File f : customFolders) {
                        this.compressDirectory(output, f, BASE_ZIP_FOLDER);
                    }
                }
            }
            this.compressDirectory(output, AbstractVDocMojo.getJarFile(this.buildDirectory, this.jarName, null), "lib/");
            if (this.libFolder.exists()) {
                this.compressDirectory(output, this.libFolder, BASE_ZIP_FOLDER);
            }

            if (this.includeTest) {
                File testJar = AbstractVDocMojo.getJarFile(this.buildDirectory, this.jarName, "tests");
                if (testJar.exists()) {
                    this.compressDirectory(output, testJar, "lib/");
                } else {
                    LOGGER.warn("Test jar not found!");
                }
            }

            if (this.includeSource) {
                this.compressDirectory(output, AbstractVDocMojo.getJarFile(this.buildDirectory, this.jarName, "source"), "lib/");
            }

            if (this.includeJavadoc) {
                this.compressDirectory(output, AbstractVDocMojo.getJarFile(this.buildDirectory, this.jarName, "javadoc"), "lib/");
            }
        }
        return vdocAppOutput;
    }

    protected File createMetaSetup(File vdocAppOutput) throws IOException, MojoExecutionException {
        File metaAppOutput = new File(this.buildDirectory, this.setupName + '-' + SETUP_SUFFIX + ".zip");
        try (ZipArchiveOutputStream output = new ZipArchiveOutputStream(metaAppOutput)) {
            for (Resource r : this.project.getResources()) {
                Path userAppsCustomFolder = Paths.get(r.getDirectory()).getParent().resolve("user_apps_custom");
                LOGGER.info("looking for user_apps_custom in {}", userAppsCustomFolder);
                if (Files.isDirectory(userAppsCustomFolder)) {
                    LOGGER.info("user_apps_custom found {} add it's files into custom zip folder", userAppsCustomFolder);
                    File[] customFolders = userAppsCustomFolder.toFile().listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
                    for (File f : customFolders) {
                        this.compressDirectory(output, f, "custom/");
                        LOGGER.debug("add folder {} to custom zip folder ", f);
                    }
                }
            }

            // add the packaged apps
            LOGGER.debug("add the packaged apps");
            this.compressDirectory(output, vdocAppOutput, BASE_ZIP_FOLDER + "apps/");


            // include linked apps
            if (this.includeDependenciesSetups) {

                includeDependenciesSetups(output);
            }


            // include vdoc fix
            File fix = new File(this.project.getBasedir(), "fix");
            if (fix.exists()) {
                LOGGER.debug("add fix folder");
                this.compressDirectory(output, fix, BASE_ZIP_FOLDER);
            }

            // include documentation
            File documentation = new File(this.project.getBasedir(), "documentation");
            if (documentation.exists()) {
                LOGGER.debug("add documentation folder");
                this.compressDirectory(output, documentation, BASE_ZIP_FOLDER);
            }

            this.includeOtherModules(output);

        }
        return metaAppOutput;
    }

    protected void includeDependenciesSetups(ZipArchiveOutputStream output) throws IOException, MojoExecutionException {
        File file = new File(this.buildDirectory, "../apps");
        File[] depApps = file.listFiles((FilenameFilter) new WildcardFileFilter("*-setup.zip"));
        LOGGER.info("merge local apps");
        if (depApps != null) {
            for (File depApp : depApps) {
                LOGGER.warn("merge {} apps.", depApp.getName());
                this.mergeArchive(output, depApp);
            }
        }

        LOGGER.info("remote apps");
        Set<org.eclipse.aether.artifact.Artifact> setupArtifactSet = new HashSet<>();
        for (Artifact artifact : this.project.getDependencyArtifacts()) {
            if ("provided".equals(artifact.getScope()) && (artifact.getGroupId().startsWith("com.vdoc") || artifact.getGroupId().startsWith("com.moovapps"))) {
                LOGGER.info("Try to get {}:{}:{}:{}:{}", artifact.getGroupId(), artifact.getArtifactId(), "setup", "zip", artifact.getVersion());
                // check for remote setup
                ArtifactRequest request = new ArtifactRequest();
                request.setArtifact(new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), "setup", "zip", artifact.getVersion()));
                request.setRepositories(remoteRepos);
                try {
                    ArtifactResult result = repoSystem.resolveArtifact(repoSession, request);
                    setupArtifactSet.add(result.getArtifact());
                } catch (ArtifactResolutionException e) {
                    LOGGER.warn("No setup found for {}:{}:{}:{}:{}", artifact.getGroupId(), artifact.getArtifactId(), "setup", "zip", artifact.getVersion());
                }
            }
        }
        for (org.eclipse.aether.artifact.Artifact artifact : setupArtifactSet) {
            LOGGER.warn("merge {} apps.", artifact.getFile().getName());
            this.mergeArchive(output, artifact.getFile());
        }
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
        if (this.includeOtherModules) {

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
                    CompletedModule completedModule = completedModules.poll(this.includeOtherModulesTimeout, TimeUnit.SECONDS);
                    LOGGER.info("Join module " + completedModule.getArtifactId() + " merge setup file " + completedModule.getSetup().getName());

                    if (completedModule.getSetup() == null) {
                        LOGGER.warn(completedModule.getArtifactId() + " have fail!");
                        throw new MojoExecutionException(completedModule.getArtifactId() + " have fail!");
                    }

                    try (FileInputStream fileInputStream = new FileInputStream(completedModule.getSetup());
                         BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                         ArchiveInputStream input = new ArchiveStreamFactory().createArchiveInputStream(bufferedInputStream)) {

                        this.mergeArchive(output, input);

                    } catch (ArchiveException e) {
                        throw new MojoExecutionException("Can't read module '" + completedModule.getArtifactId() + "' setup file '" + completedModule.getArtifactId() + "'!", e);
                    }

                } catch (InterruptedException e) {
                    throw new MojoExecutionException("Waiting for other module fail!", e);
                }

                modulesCount--;
            } while (modulesCount > 0);
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
        } catch (ArchiveException e) {
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
            if (this.finalZipEntrys.add(entry.getName())) {
                to.putArchiveEntry(entry);
                if (entry.getSize() != 0) {
                    IOUtils.copyLarge(from, to, 0, entry.getSize());
                    offset += entry.getSize();
                }
                to.closeArchiveEntry();
            }
        }
    }

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
        } else {
            outputStream.closeArchiveEntry();
            File[] children = directory.listFiles();
            if (children != null) {
                for (File child : children) {
                    this.compressDirectory(outputStream, child, entryName + '/');
                }
            }
        }
    }


    public File createCustomSetup() {
        throw new NotImplementedException("Currently not implemented!");
    }

    /**
     * flag this module as completed
     *
     * @param setup
     * @throws MojoExecutionException
     */
    private void complete(File setup) throws MojoExecutionException {
        synchronized (completedModules) {
            try {
                completedModules.put(new CompletedModule(this.getProject().getArtifactId(), setup));
            } catch (InterruptedException e) {
                throw new MojoExecutionException("Can't complete this module!", e);
            }
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
}
