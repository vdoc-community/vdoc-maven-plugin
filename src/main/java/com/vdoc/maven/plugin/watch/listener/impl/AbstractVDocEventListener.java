package com.vdoc.maven.plugin.watch.listener.impl;

import com.vdoc.maven.plugin.watch.listener.FolderEventListener;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Created by famaridon on 15/03/2017.
 */
public abstract class AbstractVDocEventListener implements FolderEventListener {

	protected final Path watchedFolder;
	protected final Path vdocHome;
	protected final Path ear;
	protected final Path war;
	protected final Path custom;
	
	public AbstractVDocEventListener(Path watchedFolder, Path vdocHome) {
		this.watchedFolder = watchedFolder;
		this.vdocHome = vdocHome;
		this.ear = vdocHome.resolve("JBoss/server/all/deploy/vdoc.ear/");
		this.war = ear.resolve("vdoc.war");
		this.custom = war.resolve("WEB-INF/storage/custom");
	}
	
	protected Path getTo(Path path) {
		if (FilenameUtils.wildcardMatch(FilenameUtils.separatorsToUnix(path.toString()), "*webapp/*")) {
			return war.resolve(path.subpath(1, path.getNameCount()));
		}
		else {
			return custom.resolve(path);
		}
	}
}