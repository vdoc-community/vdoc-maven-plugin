package com.vdoc.maven.plugin.project;

import com.vdoc.maven.plugin.project.impl.CustomProjectContext;
import com.vdoc.maven.plugin.project.impl.CoreProjectContext;
import org.apache.maven.project.MavenProject;

public class ProjectContextFactory {

  public static final String MOOVAPPS_PROCESS_PARENT_GROUP_ID = "com.axemble.vdoc";
  public static final String MOOVAPPS_PROCESS_PARENT_ARTIFACT_ID = "moovapps-process-parent";
  public static final String COM_VDOC_ENGINEERING_GROUP_ID = "com.vdoc.engineering";
  public static final String SDK_ADVANCED_ARTIFACT_ID = "sdk.advanced";

  private ProjectContextFactory() {
  }

  public static ProjectContext getInstance(MavenProject mavenProject) {
    if (isCoreModule(mavenProject)) {
      return new CoreProjectContext(mavenProject);
    } else if (isCoreModule(mavenProject)) {
      return new CustomProjectContext(mavenProject);
    } else {
      throw new IllegalStateException("Project context can't found!");
    }
  }

  private static boolean isAppsModule(MavenProject mavenProject) {
    if (mavenProject.getParent() != null) { // can be an Apps
      if (COM_VDOC_ENGINEERING_GROUP_ID.equals(mavenProject.getParent().getGroupId())
          && SDK_ADVANCED_ARTIFACT_ID.equals(mavenProject.getParent().getArtifactId())) {
        return true;
      } else {
        return isAppsModule(mavenProject.getParent());
      }
    } else {
      return false;
    }
  }

  private static boolean isCoreModule(MavenProject mavenProject) {
    if (MOOVAPPS_PROCESS_PARENT_GROUP_ID.equals(mavenProject.getGroupId())) { // can be core
      if (MOOVAPPS_PROCESS_PARENT_ARTIFACT_ID
          .equals(mavenProject.getArtifactId())) { // the root core module
        return true;
      } else if (mavenProject.getParent() != null && MOOVAPPS_PROCESS_PARENT_ARTIFACT_ID
          .equals(mavenProject.getParent().getArtifactId())) { // a sub module
        return true;
      }
    }
    return false;

  }

}
