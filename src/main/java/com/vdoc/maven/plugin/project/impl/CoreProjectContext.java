package com.vdoc.maven.plugin.project.impl;

import com.vdoc.maven.plugin.project.ProjectContext;
import com.vdoc.maven.plugin.versions.Version;
import java.text.ParseException;
import org.apache.maven.project.MavenProject;

public class CoreProjectContext extends AbstractProjectContext implements ProjectContext {

  public CoreProjectContext(MavenProject mavenProject) {
    super(mavenProject);
  }

  @Override
  protected String findMinimumRuntimeVersion() {
    // each module and submodules have the same version number
    return this.mavenProject.getVersion();
  }
}
