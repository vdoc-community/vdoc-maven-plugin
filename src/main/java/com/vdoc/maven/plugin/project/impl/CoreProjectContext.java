package com.vdoc.maven.plugin.project.impl;

import com.vdoc.maven.plugin.project.ProjectContext;
import org.apache.maven.project.MavenProject;

public class CoreProjectContext extends AbstractProjectContext implements ProjectContext {

  public CoreProjectContext(MavenProject mavenProject) {
    super(mavenProject);
  }
}
