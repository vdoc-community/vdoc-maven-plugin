package com.vdoc.maven.plugin.as;

import com.vdoc.maven.plugin.project.ProjectContext;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApplicationServerContextFactory {

  private ApplicationServerContextFactory() {

  }

  public static ApplicationServerContext newInstance(ProjectContext projectContext) {
    projectContext.getMavenProject().getProperties().getProperty("vdocHome");
    return null;
  }
}
