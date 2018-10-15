package com.vdoc.maven.plugin.watch.listener.impl.deployer;

import com.vdoc.maven.plugin.as.ApplicationServerContext;
import com.vdoc.maven.plugin.project.ProjectContext;
import com.vdoc.maven.plugin.watch.WatchableSource;
import com.vdoc.maven.plugin.watch.listener.FolderEventListener;

public class DeployerEventListenerFactory {

  private DeployerEventListenerFactory() {
  }

  public static FolderEventListener newInstance(ApplicationServerContext applicationServerContext,
      WatchableSource watchableSource) {
    return new VDocDeployerEventListener(applicationServerContext, watchableSource);
  }
}
