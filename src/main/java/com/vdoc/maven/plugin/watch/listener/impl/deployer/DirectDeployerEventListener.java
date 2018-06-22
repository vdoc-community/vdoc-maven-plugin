package com.vdoc.maven.plugin.watch.listener.impl.deployer;

import com.vdoc.maven.plugin.watch.listener.FolderEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Created by famaridon on 15/03/2017.
 */
public class DirectDeployerEventListener extends AbstractVDocDeployerEventListener implements FolderEventListener
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectDeployerEventListener.class);
	
	public DirectDeployerEventListener(DeployerEventListenerConfiguration configuration)
	{
		super(configuration);
	}
	
	@Override
	protected Path getTo(Path path)
	{
		return this.ear.resolve(path);
	}
}
