package com.vdoc.maven.plugin.deploy.vdoc;

import com.vdoc.maven.plugin.deploy.vdoc.beans.Artifact;
import com.vdoc.maven.plugin.deploy.vdoc.beans.Maven;
import com.vdoc.maven.plugin.deploy.vdoc.beans.Repository;
import com.vdoc.maven.plugin.deploy.vdoc.spliter.JarSplitter;
import com.vdoc.maven.plugin.deploy.vdoc.spliter.JarSplitterImpl;
import com.vdoc.maven.plugin.utils.ProcessOutputGobbler;
import com.vdoc.maven.plugin.utils.impl.SLF4JLoggerAdapter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.plugin.MojoFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by famaridon on 30/06/2014.
 */
public class DeployFileConfiguration implements Callable<Artifact>, AutoCloseable, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeployFileConfiguration.class);

    /**
     * the repository to deploy
     */
    protected final Repository repository;
    /**
     * the artifact to deploy
     */
    protected final Artifact artifact;
    /**
     * the maven installation to use;
     */
    private final Maven maven;

    /**
     * The bundled API docs for the artifact.
     * User property is: javadoc.
     */
    protected File javadoc;

    /**
     * The bundled sources for the artifact.
     * User property is: sources.
     */
    protected File sources;

    /**
     * Upload a POM for this artifact. Will generate a default POM if none is supplied with the pomFile argument.
     * Default value is: true.
     * User property is: generatePom.
     */
    protected boolean generatePom = true;

    /**
     * Location of an existing POM file to be deployed alongside the main artifact, given by the ${file} parameter.
     * User property is: pomFile.
     */
    protected File pomFile;

    /**
     * A comma separated list of files for each of the extra side artifacts to deploy. If there is a mis-match in the number of entries in types or classifiers, then an error will be raised.
     * User property is: files.
     */
    protected String files;
    /**
     * A comma separated list of classifiers for each of the extra side artifacts to deploy. If there is a mis-match in the number of entries in files or types, then an error will be raised.
     * User property is: classifiers.
     */
    protected String classifiers;
    /**
     * A comma separated list of types for each of the extra side artifacts to deploy. If there is a mis-match in the number of entries in files or classifiers, then an error will be raised.
     * User property is: types.
     */
    protected String types;

    private DeploymentState state = DeploymentState.NONE;

    public DeployFileConfiguration(Maven maven, Repository repository, Artifact artifact) {
        super();
        this.maven = maven;
        this.repository = repository;
        this.artifact = artifact;
    }

    @Override
    public Artifact call() throws MojoFailureException {
        this.state = DeploymentState.STARTED;
        if ("jar".equalsIgnoreCase(FilenameUtils.getExtension(this.getArtifact().getFile().getName()))) {
            LOGGER.info("Start splitting jar file.");
            try (JarSplitter jarSplitter = new JarSplitterImpl(this.getArtifact().getFile())) {

                jarSplitter.split();
                if (jarSplitter.getJar().exists()) {
                    this.setJavadoc(jarSplitter.getJar());
                }
                if (jarSplitter.getSource().exists()) {
                    this.setSources(jarSplitter.getSource());
                }
            } catch (IOException e) {
                throw new MojoFailureException(e.getMessage(), e);
            }
        }

        // stop this thread correctly
        if (Thread.currentThread().isInterrupted()) {
            this.state = DeploymentState.CANCELED;
            LOGGER.info("Start deploying artifact : {}", this.getArtifact());
            return this.getArtifact();
        }

        try {
            List<String> cmd = this.toDeployCmd();
            LOGGER.info("Start deploying artifact : {}", this.getArtifact());
            LOGGER.debug("Process command : {}", cmd);

            ProcessBuilder builder = new ProcessBuilder(cmd);
            Process process = builder.start();

            ProcessOutputGobbler processOutputGobbler = new ProcessOutputGobbler(process, new SLF4JLoggerAdapter(LOGGER, this.getArtifact().getArtifactId()));
            // start gobblers
            processOutputGobbler.start();

            int code = process.waitFor();
            if (code != 0) {
                this.state = DeploymentState.FAILED;
                LOGGER.error("Deployment fail for : {}", this.getArtifact());
                throw new MojoFailureException("Artifact deployment fail : " + this.getArtifact());
            }

        } catch (IOException | InterruptedException e) {
            this.state = DeploymentState.FAILED;
            LOGGER.error("Deployment fail for : {}", this.getArtifact());
            throw new MojoFailureException("Artifact deployment fail : " + this.getArtifact());
        }
        this.state = DeploymentState.FINISHED;
        return this.getArtifact();
    }

    /**
     * close only delete javadoc and source jar
     *
     * @throws IOException
     */
    @Override
    public void close() {
        LOGGER.debug("Delete javadoc and source jar");
        if (this.getJavadoc() != null) {
            this.getJavadoc().delete();
        }

        if (this.getSources() != null) {
            this.getSources().delete();
        }
    }

    protected List<String> toDeployCmd() {
        List<String> cmd = new ArrayList<>(2);

        cmd.add(this.maven.getMvn().getAbsolutePath());
        if (this.maven.getSession().getRequest().getLoggingLevel() == MavenExecutionRequest.LOGGING_LEVEL_DEBUG) {
            cmd.add("-X");
        }
        cmd.add("deploy:deploy-file");

        // Repository
        this.appendCmdString(cmd, "retryFailedDeploymentCount", Integer.toString(this.getRepository().getRetryFailedDeploymentCount()));
        this.appendCmdString(cmd, "uniqueVersion", Boolean.toString(this.getRepository().isUniqueVersion()));
        this.appendCmdString(cmd, "updateReleaseInfo", Boolean.toString(this.getRepository().isUpdateReleaseInfo()));
        this.appendCmdString(cmd, "url", this.getRepository().getUrl());
        this.appendCmdString(cmd, "repositoryId", this.getRepository().getRepositoryId());

        // Artifact
        this.appendCmdString(cmd, "artifactId", this.getArtifact().getArtifactId());
        this.appendCmdString(cmd, "groupId", this.getArtifact().getGroupId());
        this.appendCmdString(cmd, "version", this.getArtifact().getVersion());
        this.appendCmdFile(cmd, "file", this.getArtifact().getFile());
        this.appendCmdString(cmd, "classifier", this.getArtifact().getClassifier());
        this.appendCmdString(cmd, "description", this.getArtifact().getDescription());
        this.appendCmdString(cmd, "packaging", this.getArtifact().getPackaging());

        this.appendCmdFile(cmd, "javadoc", this.javadoc);
        this.appendCmdFile(cmd, "sources", this.sources);

        // files
        this.appendCmdString(cmd, "files", this.files);
        this.appendCmdString(cmd, "classifiers", this.classifiers);
        this.appendCmdString(cmd, "types", this.types);

        // POM
        if (!"pom".equalsIgnoreCase(this.getArtifact().getPackaging())) {
            this.appendCmdString(cmd, "generatePom", Boolean.toString(this.generatePom));
            if (!this.generatePom) {
                this.appendCmdFile(cmd, "pomFile", this.pomFile);
            }
        }

        return cmd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (this.getClass() != o.getClass())) {
            return false;
        }
        DeployFileConfiguration that = (DeployFileConfiguration) o;
        return this.artifact.equals(that.artifact);
    }

    @Override
    public int hashCode() {
        return this.artifact.hashCode();
    }

    protected void appendCmdString(List<String> strings, String parameterName, String value) {
        if (StringUtils.isNotEmpty(value)) {
            strings.add("-D" + parameterName + '=' + value);
        }
    }

    protected void appendCmdFile(List<String> strings, String parameterName, File value) {
        if (value != null) {
            strings.add("-D" + parameterName + '=' + value.getAbsolutePath());
        }
    }

    public String getClassifiers() {
        return this.classifiers;
    }

    public void setClassifiers(String classifiers) {
        this.classifiers = classifiers;
    }

    public String getFiles() {
        return this.files;
    }

    public void setFiles(String files) {
        this.files = files;
    }

    public boolean isGeneratePom() {
        return this.generatePom;
    }

    public void setGeneratePom(boolean generatePom) {
        this.generatePom = generatePom;
    }

    public File getJavadoc() {
        return this.javadoc;
    }

    public void setJavadoc(File javadoc) {
        this.javadoc = javadoc;
    }

    public File getPomFile() {
        return this.pomFile;
    }

    public void setPomFile(File pomFile) {
        this.pomFile = pomFile;
    }

    public File getSources() {
        return this.sources;
    }

    public void setSources(File sources) {
        this.sources = sources;
    }

    public String getTypes() {
        return this.types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public Repository getRepository() {
        return this.repository;
    }

    public Artifact getArtifact() {
        return this.artifact;
    }

    public DeploymentState getState() {
        return this.state;
    }
}
