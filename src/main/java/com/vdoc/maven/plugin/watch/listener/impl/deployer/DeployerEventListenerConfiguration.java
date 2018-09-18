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
	private final Path targetPrefix;
	
	public DeployerEventListenerConfiguration(ApplicationServer applicationServer, DeployementType deployementType, Path source, Path target, Path targetPrefix)
	{
		this.applicationServer = applicationServer;
		this.deployementType = deployementType;
		this.source = source;
		this.target = target;
		this.targetPrefix = targetPrefix;
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
	
	public Path getTargetPrefix()
	{
		return this.targetPrefix;
	}
}
