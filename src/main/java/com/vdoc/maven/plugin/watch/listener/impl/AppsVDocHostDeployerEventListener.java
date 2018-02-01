package com.vdoc.maven.plugin.watch.listener.impl;

import com.vdoc.maven.plugin.watch.listener.FolderEventListener;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;

/**
 * Created by famaridon on 15/03/2017.
 */
public class AppsVDocHostDeployerEventListener extends AbstractVDocEventListener implements FolderEventListener {

	public AppsVDocHostDeployerEventListener(Path watchedFolder, Path vdocHome) {
		super(watchedFolder, vdocHome);
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
