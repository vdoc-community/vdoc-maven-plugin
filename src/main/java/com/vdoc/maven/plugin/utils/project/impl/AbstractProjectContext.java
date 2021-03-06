package com.vdoc.maven.plugin.utils.project.impl;

import com.vdoc.maven.plugin.utils.project.ProjectContext;
import com.vdoc.maven.plugin.utils.versions.Version;
import com.vdoc.maven.plugin.utils.versions.VersionParser;
import java.text.ParseException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

public abstract class AbstractProjectContext implements ProjectContext {

  protected final MavenProject mavenProject;
  protected final MavenSession session;
  protected final Version minimumRuntimeVersion;

  protected AbstractProjectContext(MavenProject mavenProject, MavenSession session) {
    this.mavenProject = mavenProject;
    this.session = session;
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
