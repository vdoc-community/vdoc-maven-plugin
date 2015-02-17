package com.vdoc.maven.plugin;

import com.vdoc.maven.plugin.beans.DeployFileConfiguration;
import com.vdoc.maven.plugin.pom.ParentPOM;
import com.vdoc.maven.plugin.pom.ParentPOMGenerator;
import com.vdoc.maven.plugin.pom.ParentPOMGeneratorImpl;
import com.vdoc.maven.plugin.pom.exception.PomGenerationException;
import com.vdoc.maven.plugin.spliter.JarSplitter;
import com.vdoc.maven.plugin.spliter.JarSplitterImpl;
import com.vdoc.maven.plugin.utils.OSUtils;
import com.vdoc.maven.plugin.utils.StreamGobbler;
import com.vdoc.maven.plugin.utils.impl.SLF4JLoggerAdapter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
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
import java.util.ArrayList;
import java.util.List;

/**
 * this task can deploy all VDoc jars into a repository
 */
@Mojo(name = "deploy-vdoc", threadSafe = false, requiresProject = false, requiresDirectInvocation = true)
public class DeployVDocMojo extends AbstractVDocMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployVDocMojo.class);

    /**
     * the current running plugin description
     */
    protected PluginDescriptor pluginDescriptor;

    /**
     * the VDoc ear folder (prefer the configurator's ear to avoid upload not needed jar)
     */
    @Parameter(property = "earFolder", required = true)
    protected File earFolder;

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
     * snapshot must used unique version
     */
    @Parameter(property = "uniqueVersion", required = false, defaultValue = "true")
    protected boolean uniqueVersion;

    /**
     * the maven home folder
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

    protected List<DeployFileConfiguration> dependencies = new ArrayList<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.pluginDescriptor = (PluginDescriptor) this.getPluginContext().get("pluginDescriptor");

        if (this.mavenHome == null) {
            String mavenEnv = System.getenv("M2_HOME");
            Validate.notEmpty(mavenEnv, "M2_HOME is not set you can used the maven-home configuration!");
            this.mavenHome = new File(mavenEnv);
        }

        if (!this.mavenHome.exists()) {
            throw new IllegalArgumentException("maven home (M2_HOME or maven-home configuration) is set to bad location : " + this.mavenHome.getAbsolutePath());
        }

        OrFileFilter prefixFileFilter = new OrFileFilter();
        prefixFileFilter.addFileFilter(new PrefixFileFilter("VDoc"));
        prefixFileFilter.addFileFilter(new PrefixFileFilter("VDP"));

        AndFileFilter fileFilter = new AndFileFilter();
        fileFilter.addFileFilter(prefixFileFilter);
        fileFilter.addFileFilter(new SuffixFileFilter(".jar"));

        File[] earFiles = this.earFolder.listFiles((FileFilter) fileFilter);
        LOGGER.info("Scan the vdoc.ear folder");
        this.deployFiles(earFiles);
        LOGGER.info("Scan the vdoc.ear/lib folder");
        File[] earLibFiles = new File(this.earFolder, "lib").listFiles((FileFilter) fileFilter);
        this.deployFiles(earLibFiles);

        this.buildParentPom(ParentPOM.SDK);
        this.buildParentPom(ParentPOM.SDK_ADVANCED);

    }

    /**
     * used to build a parent pom from ftl file
     *
     * @param pom the pom file to generate.
     * @throws MojoExecutionException
     */
    protected void buildParentPom(ParentPOM pom) throws MojoExecutionException {
        LOGGER.info("Create the " + pom + " pom file");
        try {

            ParentPOMGenerator generator = new ParentPOMGeneratorImpl(this.mavenHome, pom, this);

            File pomFile = generator.generate();

            DeployFileConfiguration deployFileConfiguration = new DeployFileConfiguration(pomFile, this.repositoryId);
            deployFileConfiguration.setArtifactId(pom.getArtifactId());
            deployFileConfiguration.setGroupId("com.vdoc.engineering");
            deployFileConfiguration.setVersion(this.targetVersion);
            deployFileConfiguration.setUniqueVersion(this.uniqueVersion);
            deployFileConfiguration.setUrl(this.repositoryUrl);
            deployFileConfiguration.setPackaging("pom");
            this.deployFileToNexus(deployFileConfiguration);

            pomFile.renameTo(new File(this.mavenHome, pom.getArtifactId() + ".xml"));


        } catch (IOException | PomGenerationException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected void deployFiles(File[] vdocFiles) throws MojoExecutionException {
        if (vdocFiles == null) {
            return;
        }
        for (File jar : vdocFiles) {
            LOGGER.debug("parsing file : " + jar.getName());
            DeployFileConfiguration deployFileConfiguration = new DeployFileConfiguration(jar, this.repositoryId);
            deployFileConfiguration.setArtifactId(StringUtils.substringBefore(FilenameUtils.getBaseName(jar.getName()), "-suite"));
            deployFileConfiguration.setGroupId(this.targetGroupId);
            deployFileConfiguration.setVersion(this.targetVersion);
            deployFileConfiguration.setUniqueVersion(this.uniqueVersion);
            deployFileConfiguration.setUrl(this.repositoryUrl);

            LOGGER.debug("search javadoc");
            try {
                this.splitJar(deployFileConfiguration);
                this.dependencies.add(deployFileConfiguration);
                if (!this.onlyParentPom) {
                    this.deployFileToNexus(deployFileConfiguration);
                }

            } finally {
                if (this.deleteSplittedJar) {
                    LOGGER.debug("delete javadoc jar");
                    if (deployFileConfiguration.getJavadoc() != null) {
                        deployFileConfiguration.getJavadoc().delete();
                    }

                    if (deployFileConfiguration.getSources() != null) {
                        deployFileConfiguration.getSources().delete();
                    }
                }
            }
        }
    }

    /**
     * // org.apache.maven.plugins:maven-deploy-plugin:2.8.1:deploy-file
     *
     * @param deployFileConfiguration
     * @throws MojoExecutionException
     */
    protected void deployFileToNexus(DeployFileConfiguration deployFileConfiguration) throws MojoExecutionException {
        try {
            List<String> cmd = deployFileConfiguration.toCmd();
            cmd.add(0, "deploy:deploy-file");
            cmd.add(0, "-X");
            cmd.add(0, new File(this.mavenHome, "/bin/mvn" + (OSUtils.isWindows() ? ".bat" : "")).getAbsolutePath());

            LOGGER.info(cmd.toString());

            ProcessBuilder builder = new ProcessBuilder(cmd);
            Process process = builder.start();

            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), new SLF4JLoggerAdapter(LOGGER, "error"), deployFileConfiguration.getArtifactId());

            StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), new SLF4JLoggerAdapter(LOGGER, "debug"), deployFileConfiguration.getArtifactId());

            // start gobblers
            outputGobbler.start();
            errorGobbler.start();

            int code = process.waitFor();
            if (code != 0) {
                throw new MojoExecutionException(deployFileConfiguration.toCmd().toString());
            }

        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }

    /**
     * split jar into source and javadoc jar
     *
     * @param deployFileConfiguration the file descriptor to split
     * @throws MojoExecutionException
     */
    protected void splitJar(DeployFileConfiguration deployFileConfiguration) throws MojoExecutionException {
        try (JarSplitter jarSplitter = new JarSplitterImpl(deployFileConfiguration.getFile())) {

            jarSplitter.split();
            if (jarSplitter.getJar().exists()) {
                deployFileConfiguration.setJavadoc(jarSplitter.getJar());
            }
            if (jarSplitter.getSource().exists()) {
                deployFileConfiguration.setSources(jarSplitter.getSource());
            }

        } catch (IOException e) {
            throw new MojoExecutionException("Jar can't be splitted!", e);
        }


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

    public File getEarFolder() {
        return this.earFolder;
    }

    public void setEarFolder(File earFolder) {
        this.earFolder = earFolder;
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

    public boolean isUniqueVersion() {
        return this.uniqueVersion;
    }

    public void setUniqueVersion(boolean uniqueVersion) {
        this.uniqueVersion = uniqueVersion;
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

    public List<DeployFileConfiguration> getDependencies() {
        return this.dependencies;
    }

    public void setDependencies(List<DeployFileConfiguration> dependencies) {
        this.dependencies = dependencies;
    }

    public PluginDescriptor getPluginDescriptor() {
        return this.pluginDescriptor;
    }

    public void setPluginDescriptor(PluginDescriptor pluginDescriptor) {
        this.pluginDescriptor = pluginDescriptor;
    }

    public boolean isOnlyParentPom() {
        return this.onlyParentPom;
    }

    public void setOnlyParentPom(boolean onlyParentPom) {
        this.onlyParentPom = onlyParentPom;
    }
}
