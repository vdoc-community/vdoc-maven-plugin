package com.vdoc.maven.plugin.watch.listener.impl.deployer;

import com.vdoc.maven.plugin.ApplicationServer;

import java.nio.file.Path;
import java.util.List;

public class DeployerEventListenerConfiguration
{
	private final ApplicationServer applicationServer;
	private final DeployementType deployementType;
	private final Path source;
	private final Path target;
	
	public DeployerEventListenerConfiguration(ApplicationServer applicationServer, DeployementType deployementType, Path source, Path target)
	{
		this.applicationServer = applicationServer;
		this.deployementType = deployementType;
		this.source = source;
		this.target = target;
	}
	
	public ApplicationServer getApplicationServer()
	{
		return this.applicationServer;
	}
	
	public DeployementType getDeployementType()
	{
		return this.deployementType;
	}
	
	public Path getSource()
	{
		return this.source;
	}
	
	public Path getTarget()
	{
		return this.target;
	}
}
