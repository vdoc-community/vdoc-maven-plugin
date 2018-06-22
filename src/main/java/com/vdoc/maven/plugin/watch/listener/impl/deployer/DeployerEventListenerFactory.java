package com.vdoc.maven.plugin.watch.listener.impl.deployer;

import com.vdoc.maven.plugin.watch.listener.FolderEventListener;
import org.apache.commons.lang3.NotImplementedException;

public class DeployerEventListenerFactory
{
	
	private DeployerEventListenerFactory()
	{
	}
	
	public static FolderEventListener newInstance(DeployerEventListenerConfiguration configuration)
	{
		switch (configuration.getDeployementType()){
			case DIRECT:
				return new DirectDeployerEventListener(configuration);
			case APPS_CUSTOM:
				return new AppsCustomDeployerEventListener(configuration);
			default:
				throw new NotImplementedException("No event listener found for this configuration");
		}
	}
}
