package com.vdoc.maven.plugin.as;

import com.vdoc.maven.plugin.project.ProjectContext;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApplicationServerContextFactory {

  private ApplicationServerContextFactory() {

  }

  public static ApplicationServerContext newInstance(ProjectContext projectContext) {

    // TODO check for vdoc home or VDOC_HOMES

    // an home is forced in properties.
    projectContext.getMavenProject().getProperties().getProperty("vdocHome");
    return null;
  }
}
