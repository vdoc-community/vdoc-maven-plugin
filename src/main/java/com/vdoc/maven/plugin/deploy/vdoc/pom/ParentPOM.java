package com.vdoc.maven.plugin.deploy.vdoc.pom;

/**
 * Created by famaridon on 15/02/15.
 */
public enum ParentPOM {
    SDK("sdk"), SDK_ADVANCED("sdk.advanced");

    private final String ftlName;
    private final String artifactId;

    ParentPOM(String artifactId) {
        this.artifactId = artifactId;
        this.ftlName = artifactId + ".ftl";
    }

    public String getFtlName() {
        return this.ftlName;
    }

    public String getArtifactId() {
        return this.artifactId;
    }
}
