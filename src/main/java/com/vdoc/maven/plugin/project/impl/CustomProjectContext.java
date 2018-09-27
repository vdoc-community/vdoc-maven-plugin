package com.vdoc.maven.plugin.project.impl;

import com.vdoc.maven.plugin.project.ProjectContext;
import com.vdoc.maven.plugin.project.ProjectContextFactory;
import org.apache.maven.project.MavenProject;

public class CustomProjectContext extends AbstractProjectContext implements ProjectContext {

  public CustomProjectContext(MavenProject mavenProject) {
    super(mavenProject);
  }

  @Override
  protected String findMinimumRuntimeVersion() {
    return this.findMinimumRuntimeVersion(this.mavenProject);
  }

  private String findMinimumRuntimeVersion(MavenProject project) {
    if (project.getParent() != null) { // can be an Apps
      if (ProjectContextFactory.COM_VDOC_ENGINEERING_GROUP_ID
          .equals(project.getParent().getGroupId())
          && ProjectContextFactory.SDK_ADVANCED_ARTIFACT_ID
          .equals(project.getParent().getArtifactId())) {
        return project.getVersion();
      } else {
        return findMinimumRuntimeVersion(project.getParent());
      }
    } else {
      throw new IllegalStateException(
          "Can't find the project minimum runtime version this project must have this parent "
              + ProjectContextFactory.COM_VDOC_ENGINEERING_GROUP_ID + ":"
              + ProjectContextFactory.SDK_ADVANCED_ARTIFACT_ID);
    }
  }
}
