package com.vdoc.maven.plugin.watch.listener.impl;

import com.vdoc.maven.plugin.watch.listener.FolderEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Created by famaridon on 15/03/2017.
 */
public class LoggerEventListener implements FolderEventListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LoggerEventListener.class);
	
	@Override
	public void onCreate(Path path) {
		LOGGER.info(" >> a : {}", path);
	}
	
	@Override
	public void onDelete(Path path) {
		LOGGER.info(" >> d : {}", path);
	}
	
	@Override
	public void onModify(Path path) {
		LOGGER.info(" >> m : {}", path);
	}
}