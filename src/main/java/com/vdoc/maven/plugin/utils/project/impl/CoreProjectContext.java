package com.vdoc.maven.plugin.utils.project.impl;

import com.vdoc.maven.plugin.utils.project.ProjectContext;
import com.vdoc.maven.plugin.watch.WatchableSource;
import com.vdoc.maven.plugin.watch.listener.impl.WebappWatchableSource;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

public class CoreProjectContext extends AbstractProjectContext implements ProjectContext {

  public CoreProjectContext(MavenProject mavenProject, MavenSession session) {
    super(mavenProject, session);
  }

  @Override
  protected String findMinimumRuntimeVersion() {
    // each module and submodules have the same version number
    return this.mavenProject.getVersion();
  }

  @Override
  public Iterable<WatchableSource> getWatchableSources() {
    Set<WatchableSource> watchableSources = new HashSet<>();
    for (MavenProject project : this.session.getAllProjects()) {
      if(project.getArtifactId().equals("VDocWAR")) {
        Path webapp = Paths.get(project.getBasedir().toURI()).resolve("src/main/webapp");
        watchableSources.add(new WebappWatchableSource(webapp));
      }
    }
    return watchableSources;
  }
}
