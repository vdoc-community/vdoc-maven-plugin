package com.vdoc.maven.plugin.project.impl;

import com.vdoc.maven.plugin.project.ProjectContext;
import org.apache.maven.project.MavenProject;

public abstract class AbstractProjectContext implements ProjectContext {

  protected final MavenProject mavenProject;

  protected AbstractProjectContext(MavenProject mavenProject) {
    this.mavenProject = mavenProject;
  }

  @Override
  public MavenProject getMavenProject() {
    return this.mavenProject;
  }
}
