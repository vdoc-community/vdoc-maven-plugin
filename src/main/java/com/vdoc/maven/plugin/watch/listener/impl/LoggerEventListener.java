package com.vdoc.maven.plugin.watch.listener.impl;

import com.vdoc.maven.plugin.watch.listener.FolderEventListener;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by famaridon on 15/03/2017.
 */
public class LoggerEventListener implements FolderEventListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(LoggerEventListener.class);

  @Override
  public void onCreate(Path parent, Path relativePath) {
    LOGGER.info(" >> a : {}", relativePath);
  }

  @Override
  public void onDelete(Path parent, Path relativePath) {
    LOGGER.info(" >> d : {}", relativePath);
  }

  @Override
  public void onModify(Path parent, Path relativePath) {
    LOGGER.info(" >> m : {}", relativePath);
  }
}