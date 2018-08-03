package com.vdoc.maven.plugin.project.impl;

import com.vdoc.maven.plugin.project.ProjectContext;
import org.apache.maven.project.MavenProject;

public class CustomProjectContext extends AbstractProjectContext implements ProjectContext {

  public CustomProjectContext(MavenProject mavenProject) {
    super(mavenProject);
  }
}
