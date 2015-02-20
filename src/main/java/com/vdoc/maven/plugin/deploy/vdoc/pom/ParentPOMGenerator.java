package com.vdoc.maven.plugin.deploy.vdoc.pom;

import com.vdoc.maven.plugin.deploy.vdoc.pom.exception.PomGenerationException;

import java.io.File;

/**
 * Created by famaridon on 15/02/15.
 */
public interface ParentPOMGenerator {

    /**
     * generate the pom file
     *
     * @return
     */
    public File generate() throws PomGenerationException;

}
