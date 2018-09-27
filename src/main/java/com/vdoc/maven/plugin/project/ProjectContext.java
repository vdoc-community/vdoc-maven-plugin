package com.vdoc.maven.plugin.project;

import com.vdoc.maven.plugin.versions.Version;
import org.apache.maven.project.MavenProject;

public interface ProjectContext {

  MavenProject getMavenProject();
  Version getMinimumRuntimeVersion();

}
