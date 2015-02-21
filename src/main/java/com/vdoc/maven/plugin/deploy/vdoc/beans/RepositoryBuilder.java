package com.vdoc.maven.plugin.deploy.vdoc.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryBuilder.class);

    private String repositoryId;
    private int retryFailedDeploymentCount = 1;
    private boolean uniqueVersion = true;
    private boolean updateReleaseInfo = true;
    private String url;

    public RepositoryBuilder setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
        return this;
    }

    public RepositoryBuilder setRetryFailedDeploymentCount(int retryFailedDeploymentCount) {
        this.retryFailedDeploymentCount = retryFailedDeploymentCount;
        return this;
    }

    public RepositoryBuilder setUniqueVersion(boolean uniqueVersion) {
        this.uniqueVersion = uniqueVersion;
        return this;
    }

    public RepositoryBuilder setUpdateReleaseInfo(boolean updateReleaseInfo) {
        this.updateReleaseInfo = updateReleaseInfo;
        return this;
    }

    public RepositoryBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public Repository createRepository() {
        return Repository.createRepository(this.repositoryId, this.retryFailedDeploymentCount, this.uniqueVersion, this.updateReleaseInfo, this.url);
    }
}