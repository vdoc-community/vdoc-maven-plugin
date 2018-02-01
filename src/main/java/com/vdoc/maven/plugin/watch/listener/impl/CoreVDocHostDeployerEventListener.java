package com.vdoc.maven.plugin.watch.listener.impl;

import com.vdoc.maven.plugin.watch.listener.FolderEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Created by famaridon on 15/03/2017.
 */
public class CoreVDocHostDeployerEventListener extends AbstractVDocEventListener implements FolderEventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(CoreVDocHostDeployerEventListener.class);

	public CoreVDocHostDeployerEventListener(Path watchedFolder, Path vdocHome) {
		super(watchedFolder, vdocHome);
	}

	@Override
	protected Path getTo(Path path) {
		return war.resolve(path);
	}
}
