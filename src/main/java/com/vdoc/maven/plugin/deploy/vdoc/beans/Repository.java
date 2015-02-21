package com.vdoc.maven.plugin.deploy.vdoc.beans;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this will be used to describe a repository and it's configuration.
 * Created by famaridon on 21/02/15.
 */
public class Repository {

    private static final Logger LOGGER = LoggerFactory.getLogger(Repository.class);

    /**
     * Server Id to map on the <id> under <server> section of settings.xml In most cases, this parameter will be required for authentication.
     * Default value is: remote-repository.
     * User property is: repositoryId.
     */
    protected String repositoryId;
    /**
     * Parameter used to control how many times a failed deployment will be retried before giving up and failing. If a value outside the range 1-10 is specified it will be pulled to the nearest value within the range 1-10.
     * Default value is: 1.
     * User property is: retryFailedDeploymentCount.
     */
    protected int retryFailedDeploymentCount = 1;

    /**
     * Whether to deploy snapshots with a unique version or not.
     * Default value is: true.
     * User property is: uniqueVersion.
     */
    protected boolean uniqueVersion = true;
    /**
     * Parameter used to update the metadata to make the artifact as release.
     * Default value is: false.
     * User property is: updateReleaseInfo.
     */
    protected boolean updateReleaseInfo = true;
    /**
     * URL where the artifact will be deployed.
     * ie ( file:///C:/m2-repo or scp://host.com/path/to/repo )
     * User property is: url.
     */
    protected String url;

    private Repository(String repositoryId, int retryFailedDeploymentCount, boolean uniqueVersion, boolean updateReleaseInfo, String url) {
        super();
        this.repositoryId = repositoryId;
        this.retryFailedDeploymentCount = retryFailedDeploymentCount;
        this.uniqueVersion = uniqueVersion;
        this.updateReleaseInfo = updateReleaseInfo;
        this.url = Validate.notBlank(url);
    }

    public static Repository createRepository(String repositoryId, int retryFailedDeploymentCount, boolean uniqueVersion, boolean updateReleaseInfo, String url) {
        return new Repository(repositoryId, retryFailedDeploymentCount, uniqueVersion, updateReleaseInfo, url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (this.getClass() != o.getClass())) {
            return false;
        }

        Repository that = (Repository) o;

        if (!this.url.equals(that.url)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return this.url.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("url", this.url)
                .toString();
    }

    public String getRepositoryId() {
        return this.repositoryId;
    }

    public int getRetryFailedDeploymentCount() {
        return this.retryFailedDeploymentCount;
    }

    public boolean isUniqueVersion() {
        return this.uniqueVersion;
    }

    public boolean isUpdateReleaseInfo() {
        return this.updateReleaseInfo;
    }

    public String getUrl() {
        return this.url;
    }
}
