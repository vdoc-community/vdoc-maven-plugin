package com.vdoc.maven.plugin.utils.as;

import com.vdoc.maven.plugin.utils.as.impl.JBossApplicationServerContext;
import com.vdoc.maven.plugin.utils.as.impl.WildflyApplicationServerContext;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationServerContextFactory {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ApplicationServerContextFactory.class);

  private ApplicationServerContextFactory() {

  }

  public static ApplicationServerContext newInstance(File path) {
    Path home = Paths.get(path.toURI());
    Path launcherJar = home.resolve(Paths.get("wildfly","bin","launcher.jar"));
    if(Files.exists(launcherJar)) {
      return new WildflyApplicationServerContext(home);
    }

    Path runJar = home.resolve(Paths.get("JBoss","bin","run.jar"));
    if(Files.exists(runJar)) {
      return new JBossApplicationServerContext(home);
    }

    throw new NotImplementedException("Not supported application server!");
  }

}
