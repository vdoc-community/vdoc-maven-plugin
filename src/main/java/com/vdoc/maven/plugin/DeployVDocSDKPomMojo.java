package com.vdoc.maven.plugin;

import com.vdoc.maven.plugin.deploy.vdoc.DeployFileConfiguration;
import com.vdoc.maven.plugin.deploy.vdoc.beans.Artifact;
import com.vdoc.maven.plugin.deploy.vdoc.beans.Maven;
import com.vdoc.maven.plugin.deploy.vdoc.beans.Repository;
import com.vdoc.maven.plugin.deploy.vdoc.pom.ParentPOM;
import com.vdoc.maven.plugin.deploy.vdoc.pom.ParentPOMGenerator;
import com.vdoc.maven.plugin.deploy.vdoc.pom.ParentPOMGeneratorImpl;
import com.vdoc.maven.plugin.deploy.vdoc.pom.exception.PomGenerationException;
import java.io.File;
import java.io.IOException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this task can deploy all VDoc jars into a repository
 */
@Mojo(name = "deploy-vdoc-sdk-pom", threadSafe = false, requiresProject = false, requiresDirectInvocation = true, defaultPhase = LifecyclePhase.DEPLOY)
public class DeployVDocSDKPomMojo extends AbstractVDocMojo {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeployVDocSDKPomMojo.class);

  /**
   * the maven home folder
   */
  @Parameter(property = "mavenHome", required = false, defaultValue = "${env.M2_HOME}")
  protected File mavenHome;

  /**
   * Used for attaching the artifact in the project.
   */
  @Component
  private MavenProjectHelper projectHelper;
  private Repository deploymentRepository;
  private Maven maven;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (this.project.getArtifact().isSnapshot()) {
      deploymentRepository = Repository
          .createRepository(this.project.getDistributionManagement().getSnapshotRepository());
    } else {
      deploymentRepository = Repository
          .createRepository(this.project.getDistributionManagement().getRepository());
    }

    maven = new Maven(this.session, this.project,
        (PluginDescriptor) this.getPluginContext().get("pluginDescriptor"), this.mavenHome);

    LOGGER.debug("Start building engineering parent pom");
    this.buildParentPom(ParentPOM.SDK);
    this.buildParentPom(ParentPOM.SDK_ADVANCED);
    this.buildParentPom(ParentPOM.VDOC_SUITE);
  }

  /**
   * used to build a parent pom from ftl file
   *
   * @param pom the pom file to generate.
   */
  protected void buildParentPom(ParentPOM pom) throws MojoExecutionException, MojoFailureException {
    LOGGER.info("Create the " + pom + " pom file");
    try {
      ParentPOMGenerator generator = new ParentPOMGeneratorImpl(this.maven, pom,
          this.getProject().getVersion());
      File pomFile = generator.generate();

      Artifact artifact = new Artifact(pomFile, pom.getArtifactId(), this.getProject().getVersion(),
          "com.vdoc.engineering");
      artifact.setPackaging("pom");
      DeployFileConfiguration deployFileConfiguration = new DeployFileConfiguration(maven,
          deploymentRepository, artifact);
      deployFileConfiguration.call();

      pomFile.delete();

    } catch (IOException | PomGenerationException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

}
