package com.vdoc.maven.plugin.watch.listener.impl.deployer;

import com.vdoc.maven.plugin.as.ApplicationServerContext;
import com.vdoc.maven.plugin.project.ProjectContext;
import com.vdoc.maven.plugin.watch.listener.FolderEventListener;

public class DeployerEventListenerFactory {

  private DeployerEventListenerFactory() {
  }

  public static FolderEventListener newInstance(ApplicationServerContext applicationServerContext,
      ProjectContext projectContext) {
    return new VDocDeployerEventListener(applicationServerContext, projectContext);
  }
}
