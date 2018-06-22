package com.vdoc.maven.plugin.watch.listener.impl.deployer;

import com.vdoc.maven.plugin.ApplicationServer;

import java.nio.file.Path;

public class DeployerEventListenerConfigurationBuilder
{
	private ApplicationServer applicationServer;
	private DeployementType deployementType;
	private Path source;
	private Path target;
	
	public DeployerEventListenerConfigurationBuilder setApplicationServer(ApplicationServer applicationServer)
	{
		this.applicationServer = applicationServer;
		return this;
	}
	
	public DeployerEventListenerConfigurationBuilder setDeployementType(DeployementType deployementType)
	{
		this.deployementType = deployementType;
		return this;
	}
	
	public DeployerEventListenerConfigurationBuilder setSource(Path source)
	{
		this.source = source;
		return this;
	}
	
	public DeployerEventListenerConfigurationBuilder setTarget(Path target)
	{
		this.target = target;
		return this;
	}
	
	public DeployerEventListenerConfiguration createDeployerEventListenerConfiguration()
	{
		return new DeployerEventListenerConfiguration(this.applicationServer, this.deployementType, this.source, this.target);
	}
}