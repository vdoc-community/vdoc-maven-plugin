package com.vdoc.maven.plugin.project.impl;

import com.vdoc.maven.plugin.project.ProjectContext;
import com.vdoc.maven.plugin.versions.Version;
import com.vdoc.maven.plugin.versions.VersionParser;
import java.text.ParseException;
import org.apache.maven.project.MavenProject;

public abstract class AbstractProjectContext implements ProjectContext {

  protected final MavenProject mavenProject;
  protected final Version minimumRuntimeVersion;

  protected AbstractProjectContext(MavenProject mavenProject) {
    this.mavenProject = mavenProject;
    this.minimumRuntimeVersion = this.parseMinimumRuntimeVersion();
  }

  @Override
  public MavenProject getMavenProject() {
    return this.mavenProject;
  }

  private final Version parseMinimumRuntimeVersion(){
    String minimumRuntimeVersion = this.findMinimumRuntimeVersion();
    try {
      VersionParser versionParser = new VersionParser();
      return versionParser.parse(minimumRuntimeVersion);
    } catch (ParseException e) {
      throw new IllegalStateException(
          "Project minimum runtime is not a valid version " + minimumRuntimeVersion);
    }
  }

  protected abstract String findMinimumRuntimeVersion();

  @Override
  public final Version getMinimumRuntimeVersion() {
    return this.minimumRuntimeVersion;
  }


}
