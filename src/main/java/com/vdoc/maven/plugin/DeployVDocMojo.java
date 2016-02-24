package com.vdoc.maven.plugin;

import com.vdoc.maven.plugin.deploy.vdoc.DeployFileConfiguration;
import com.vdoc.maven.plugin.deploy.vdoc.beans.Artifact;
import com.vdoc.maven.plugin.deploy.vdoc.beans.Maven;
import com.vdoc.maven.plugin.deploy.vdoc.beans.Repository;
import com.vdoc.maven.plugin.deploy.vdoc.beans.RepositoryBuilder;
import com.vdoc.maven.plugin.deploy.vdoc.pom.ParentPOM;
import com.vdoc.maven.plugin.deploy.vdoc.pom.ParentPOMGenerator;
import com.vdoc.maven.plugin.deploy.vdoc.pom.ParentPOMGeneratorImpl;
import com.vdoc.maven.plugin.deploy.vdoc.pom.exception.PomGenerationException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * this task can deploy all VDoc jars into a repository
 */
@Mojo(name = "deploy-vdoc", threadSafe = false, requiresProject = false, requiresDirectInvocation = true)
public class DeployVDocMojo extends AbstractVDocMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployVDocMojo.class);

    /**
     * the VDoc home folder
     */
    @Parameter(property = "vdocHome", required = true)
    protected File vdocHome;

    /**
     * the VDoc upload version
     */
    @Parameter(property = "targetVersion", required = true)
    protected String targetVersion;

    /**
     * the VDoc group id
     */
    @Parameter(property = "targetGroupId", required = true, defaultValue = "com.vdoc.engineering")
    protected String targetGroupId;

    /**
     * the repository id used if password is needed for upload
     */
    @Parameter(property = "repositoryId", required = true)
    protected String repositoryId;

    /**
     * the repository url
     */
    @Parameter(property = "repositoryUrl", required = true)
    protected String repositoryUrl;

    /**
     * the maven home folder
     *
     */
    @Parameter(property = "mavenHome", required = false, defaultValue = "${env.M2_HOME}")
    protected File mavenHome;

    /**
     * set to true for only deploy parents pom
     */
    @Parameter(property = "onlyParentPom", required = false, defaultValue = "false")
    protected boolean onlyParentPom;

    /**
     * turn it to false to save splitted jars.
     */
    @Parameter(property = "deleteSplittedJar", required = false, defaultValue = "true")
    protected boolean deleteSplittedJar;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        // Check maven home directory.
        if (this.mavenHome == null) {
            String mavenEnv = System.getenv("M2_HOME");
            Validate.notEmpty(mavenEnv, "M2_HOME is not set you can used the -DmavenHome property!");
            this.mavenHome = new File(mavenEnv);
        }
        if (!this.mavenHome.exists()) {
            throw new IllegalArgumentException("maven home (M2_HOME or mavenHome) is set to bad location : " + this.mavenHome.getAbsolutePath());
        }
        Maven maven = new Maven(this.session, this.project, (PluginDescriptor) this.getPluginContext().get("pluginDescriptor"), this.mavenHome);

        Repository repository = new RepositoryBuilder().setRepositoryId(this.repositoryId).setUrl(this.repositoryUrl).createRepository();

        // create the file filter to include all jar whose start with VDoc or VDP.
        OrFileFilter prefixFileFilter = new OrFileFilter();
        prefixFileFilter.addFileFilter(new PrefixFileFilter("VDoc"));
        prefixFileFilter.addFileFilter(new PrefixFileFilter("VDP"));

        AndFileFilter fileFilter = new AndFileFilter();
        fileFilter.addFileFilter(prefixFileFilter);
        fileFilter.addFileFilter(new SuffixFileFilter(".jar"));
        fileFilter.addFileFilter(new NotFileFilter(new NameFileFilter("VDocSDKClient-suite.jar")));

        LOGGER.info("Start scanning the vdoc from :" + this.vdocHome.getAbsolutePath());
        Set<File> fileSet = new HashSet<>();
        LOGGER.debug("Start reading vdoc.ear folder");
        File vdocEar = new File(this.vdocHome, "/configurator/vdoc.ear/");
        fileSet.addAll(Arrays.asList(vdocEar.listFiles((FileFilter) fileFilter)));

        LOGGER.debug("Start reading vdoc.ear/lib folder");
        File vdocEarLib = new File(vdocEar, "lib");
        fileSet.addAll(Arrays.asList(vdocEarLib.listFiles((FileFilter) fileFilter)));

        LOGGER.debug("Start reading JBoss/bin/run.jar folder");
        File jbossBin = new File(this.vdocHome, "JBoss/bin");
        fileSet.addAll(Arrays.asList(jbossBin.listFiles((FileFilter) new NameFileFilter("run.jar", IOCase.INSENSITIVE))));

        Set<Artifact> artifacts = this.deployFiles(maven, repository, fileSet);

        LOGGER.debug("Start building engineering parent pom");
        this.buildParentPom(maven, repository, artifacts, ParentPOM.SDK);
        this.buildParentPom(maven, repository, artifacts, ParentPOM.SDK_ADVANCED);
        this.buildParentPom(maven, repository, artifacts, ParentPOM.VDOC_SUITE);

    }

    /**
     * used to build a parent pom from ftl file
     *
     * @param maven
     * @param repository
     * @param artifacts
     * @param pom        the pom file to generate.
     * @throws MojoExecutionException
     * @throws org.apache.maven.plugin.MojoFailureException
     */
    protected void buildParentPom(Maven maven, Repository repository, Set<Artifact> artifacts, ParentPOM pom) throws MojoExecutionException, MojoFailureException {
        LOGGER.info("Create the " + pom + " pom file");
        try {

            ParentPOMGenerator generator = new ParentPOMGeneratorImpl(maven, pom, artifacts, this.targetVersion);

            File pomFile = generator.generate();

            Artifact artifact = new Artifact(pomFile, pom.getArtifactId(), this.targetVersion, "com.vdoc.engineering");
            artifact.setPackaging("pom");
            DeployFileConfiguration deployFileConfiguration = new DeployFileConfiguration(maven, repository, artifact);
            deployFileConfiguration.call();

            pomFile.renameTo(new File(this.mavenHome, pom.getArtifactId() + ".xml"));

        } catch (IOException | PomGenerationException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected Set<Artifact> deployFiles(Maven maven, Repository repository, Set<File> fileSet) throws MojoExecutionException, MojoFailureException {
        Validate.notEmpty(fileSet);

        LOGGER.info("Prepare all file deployer");
        Set<DeployFileConfiguration> deployFileSet = new HashSet<>(fileSet.size());
        for (File jar : fileSet) {
            String artifactId = StringUtils.substringBefore(FilenameUtils.getBaseName(jar.getName()), "-suite");
            Artifact artifact = new Artifact(jar, artifactId, this.targetVersion, this.targetGroupId);
            DeployFileConfiguration deployFileConfiguration = new DeployFileConfiguration(maven, repository, artifact);
            LOGGER.debug("Artifact deployer created for : " + artifact);
            deployFileSet.add(deployFileConfiguration);
        }

        Set<Artifact> artifactSet = new HashSet<>(fileSet.size());
        // If only parent pom we do not execute
        if (this.onlyParentPom) {
            LOGGER.debug("Artifact deployment is disable with onlyParentPom option");
            for (DeployFileConfiguration deployFileConfiguration : deployFileSet) {
                artifactSet.add(deployFileConfiguration.getArtifact());
            }
        } else {
            ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            // use ExecutorCompletionService to take result when it's possible
            CompletionService<Artifact> completionService = new ExecutorCompletionService<>(pool);
            // join all deployment.
            try {
                for (DeployFileConfiguration deployFileConfiguration : deployFileSet) {
                    completionService.submit(deployFileConfiguration);
                }

                for (int i = 0; i < deployFileSet.size(); i++) {
                    Future<Artifact> future = completionService.take();
                    Artifact finished = future.get();
                    artifactSet.add(finished);
                    LOGGER.info("{} successfully deploy", finished);
                }
            } catch (InterruptedException | ExecutionException e) {
                // if 1 thread fail we must stop all
                for (DeployFileConfiguration deployFileConfiguration : deployFileSet) {
                    switch (deployFileConfiguration.getState()) {
                        case STARTED:
                        case FINISHED:
                        case CANCELED:
                            LOGGER.error("Artifact possibly deployed : {}", deployFileConfiguration.getArtifact());
                            break;
                        case FAILED:
                        case NONE:
                        default:
                    }
                }
                throw new MojoFailureException("an artifact deploy have fail!");
            } finally {
                pool.shutdown();
                if (this.deleteSplittedJar) {
                    for (DeployFileConfiguration deployFileConfiguration : deployFileSet) {
                        deployFileConfiguration.close();
                    }
                }
            }
        }

        return artifactSet;
    }

    public MavenProject getProject() {
        return this.project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public MavenSession getSession() {
        return this.session;
    }

    public void setSession(MavenSession session) {
        this.session = session;
    }

    public File getVdocHome() {
        return this.vdocHome;
    }

    public void setVdocHome(File vdocHome) {
        this.vdocHome = vdocHome;
    }

    public boolean isDeleteSplittedJar() {
        return this.deleteSplittedJar;
    }

    public void setDeleteSplittedJar(boolean deleteSplittedJar) {
        this.deleteSplittedJar = deleteSplittedJar;
    }

    public String getTargetVersion() {
        return this.targetVersion;
    }

    public void setTargetVersion(String targetVersion) {
        this.targetVersion = targetVersion;
    }

    public String getTargetGroupId() {
        return this.targetGroupId;
    }

    public void setTargetGroupId(String targetGroupId) {
        this.targetGroupId = targetGroupId;
    }

    public String getRepositoryId() {
        return this.repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getRepositoryUrl() {
        return this.repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public File getMavenHome() {
        return this.mavenHome;
    }

    public void setMavenHome(File mavenHome) {
        this.mavenHome = mavenHome;
    }

    public boolean isOnlyParentPom() {
        return this.onlyParentPom;
    }

    public void setOnlyParentPom(boolean onlyParentPom) {
        this.onlyParentPom = onlyParentPom;
    }
}
