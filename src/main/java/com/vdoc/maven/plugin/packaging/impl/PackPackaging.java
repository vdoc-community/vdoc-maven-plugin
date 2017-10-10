package com.vdoc.maven.plugin.packaging.impl;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;

import java.util.HashSet;
import java.util.Set;

public class PackPackaging extends AppsPackaging {
	
	@Override
	protected Set<Artifact> getArtifacts() {
		Set<Artifact> artifacts = super.getArtifacts();
		Set<Artifact> newArtifacts = new HashSet<>();
		Set<String> artifactIds = new HashSet<>();
		for (Dependency dependency : getProject().getDependencyManagement().getDependencies()) {
			artifactIds.add(dependency.getArtifactId());
		}
		for (Artifact artifact : artifacts) {
			if (!artifactIds.contains(artifact.getArtifactId())) {
				newArtifacts.add(artifact);
			}
		}
		return newArtifacts;
	}
}
