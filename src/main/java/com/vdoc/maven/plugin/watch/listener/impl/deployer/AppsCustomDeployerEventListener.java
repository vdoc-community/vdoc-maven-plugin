package com.vdoc.maven.plugin.watch.listener.impl.deployer;

import com.vdoc.maven.plugin.watch.listener.FolderEventListener;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;

/**
 * Created by famaridon on 15/03/2017.
 */
public class AppsCustomDeployerEventListener extends AbstractVDocDeployerEventListener implements FolderEventListener
{
	
	protected final Path custom;
	
	public AppsCustomDeployerEventListener(DeployerEventListenerConfiguration configuration)
	{
		super(configuration);
		this.custom = this.war.resolve("WEB-INF/storage/custom");
	}
	
	protected Path getTo(Path path)
	{
		if (FilenameUtils.wildcardMatch(FilenameUtils.separatorsToUnix(path.toString()), "*webapp/*")) {
			return war.resolve(path.subpath(1, path.getNameCount()));
		}
		else {
			return custom.resolve(path);
		}
	}
}
