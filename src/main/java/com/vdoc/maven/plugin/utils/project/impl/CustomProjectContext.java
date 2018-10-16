package com.vdoc.maven.plugin.utils.project.impl;

import com.vdoc.maven.plugin.utils.project.ProjectContext;
import com.vdoc.maven.plugin.utils.project.ProjectContextFactory;
import com.vdoc.maven.plugin.watch.WatchableSource;
import com.vdoc.maven.plugin.watch.listener.impl.CustomWatchableSource;
import com.vdoc.maven.plugin.watch.listener.impl.WebappWatchableSource;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;

public class CustomProjectContext extends AbstractProjectContext implements ProjectContext {

  public CustomProjectContext(MavenProject mavenProject, MavenSession session) {
    super(mavenProject, session);
  }

  @Override
  protected String findMinimumRuntimeVersion() {
    return this.findMinimumRuntimeVersion(this.mavenProject);
  }

  private String findMinimumRuntimeVersion(MavenProject project) {
    if (project.getParent() == null) {
      // we are on root level project  without finding on SDK parent
      throw new IllegalStateException(
          "Can't find the project minimum runtime version this project must have this parent "
              + ProjectContextFactory.COM_VDOC_ENGINEERING_GROUP_ID + ":"
              + ProjectContextFactory.SDK_ADVANCED_ARTIFACT_ID);
    }
    if (ProjectContextFactory.COM_VDOC_ENGINEERING_GROUP_ID
        .equals(project.getParent().getGroupId())
        && ProjectContextFactory.SDK_ADVANCED_ARTIFACT_ID
        .equals(project.getParent().getArtifactId())) {
      return project.getParent().getVersion();
    } else {
      return findMinimumRuntimeVersion(project.getParent());
    }
  }

  @Override
  public Iterable<WatchableSource> getWatchableSources() {
    Set<WatchableSource> watchableSources = new HashSet<>();
    for (MavenProject project : this.session.getAllProjects()) {
      for (Resource r : project.getResources()) {
        File resourcesDirectory = new File(r.getDirectory());
        File customFolder = new File(resourcesDirectory.getParentFile(), "custom");
        if (customFolder.isDirectory()) {
          Path customFolderPath = Paths.get(customFolder.toURI());
          watchableSources.add(new CustomWatchableSource(customFolderPath));
          watchableSources.add(new WebappWatchableSource(customFolderPath.resolve("webapp")));
        }
      }
    }
    return watchableSources;
  }
}
