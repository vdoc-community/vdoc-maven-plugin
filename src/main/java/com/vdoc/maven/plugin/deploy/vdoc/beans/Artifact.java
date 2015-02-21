package com.vdoc.maven.plugin.deploy.vdoc.beans;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by famaridon on 21/02/15.
 */
public class Artifact {
    public static final String VALIDATE_NOT_BLANK_MESSAGE = "%s must not be blank!";
    private static final Logger LOGGER = LoggerFactory.getLogger(Artifact.class);
    /**
     * File to be deployed.
     * <b>User property is</b>: file.
     */
    protected final File file;

    /**
     * ArtifactId of the artifact to be deployed. Retrieved from POM file if specified.
     * User property is: artifactId.
     */
    protected final String artifactId;
    /**
     * Version of the artifact to be deployed. Retrieved from POM file if specified.
     * User property is: version.
     */
    protected final String version;
    /**
     * GroupId of the artifact to be deployed. Retrieved from POM file if specified.
     * User property is: groupId.
     */
    protected final String groupId;
    /**
     * Add classifier to the artifact
     * User property is: classifier.
     */
    protected String classifier;
    /**
     * Type of the artifact to be deployed. Retrieved from the <packaging> element of the POM file if a POM file specified. Defaults to the file extension if it is not specified via command line or POM.
     * Maven uses two terms to refer to this datum: the <packaging> element for the entire POM,
     * and the <type> element in a dependency specification.
     * User property is: packaging.
     */
    protected String packaging;
    /**
     * Description passed to a generated POM file (in case of generatePom=true)
     * User property is: generatePom.description.
     */
    protected String description;

    public Artifact(File file, String artifactId, String version, String groupId) {
        super();
        Path filePath = file.toPath();
        if (!Files.isRegularFile(filePath)) {
            throw new IllegalArgumentException("file is not a regular file!");
        }
        if (!Files.isReadable(filePath)) {
            throw new IllegalArgumentException("file is not readable!");
        }
        this.file = file;
        this.artifactId = Validate.notBlank(artifactId, VALIDATE_NOT_BLANK_MESSAGE, "artifactId");
        this.version = Validate.notBlank(version, VALIDATE_NOT_BLANK_MESSAGE, "version");
        this.groupId = Validate.notBlank(groupId, VALIDATE_NOT_BLANK_MESSAGE, "groupId");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (this.getClass() != o.getClass())) {
            return false;
        }

        Artifact artifact = (Artifact) o;

        if (!this.artifactId.equals(artifact.artifactId)) {
            return false;
        }
        if ((this.classifier != null) ? !this.classifier.equals(artifact.classifier) : (artifact.classifier != null)) {
            return false;
        }
        if (!this.groupId.equals(artifact.groupId)) {
            return false;
        }
        if (!this.version.equals(artifact.version)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = this.artifactId.hashCode();
        result = (31 * result) + this.version.hashCode();
        result = (31 * result) + this.groupId.hashCode();
        result = (31 * result) + ((this.classifier != null) ? this.classifier.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("file", this.file)
                .append("artifactId", this.artifactId)
                .append("version", this.version)
                .append("groupId", this.groupId)
                .toString();
    }

    public String getArtifactId() {
        return this.artifactId;
    }

    public String getVersion() {
        return this.version;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public String getClassifier() {
        return this.classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getPackaging() {
        return this.packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public File getFile() {
        return this.file;
    }
}
