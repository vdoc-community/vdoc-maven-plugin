package com.vdoc.maven.plugin;

import com.vdoc.maven.plugin.as.ApplicationServerContext;
import com.vdoc.maven.plugin.as.ApplicationServerContextFactory;
import java.io.File;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Created by famaridon on 19/05/2014.
 */
public abstract class AbstractDeployerVDocMojo extends AbstractVDocMojo {

    /**
     * the VDoc home folder if set the apps is copied into apps folder.
     */
    @Parameter(required = false)
    private File vdocHome;


    protected ApplicationServerContext findApplicationServerContext() {
        return ApplicationServerContextFactory.newInstance(this.findProjectContext());
    }

    /**
     * get {@link AbstractDeployerVDocMojo#vdocHome} property
     *
     * @return get the vdocHome property
     **/
    public File getVdocHome() {
        return vdocHome;
    }

    /**
     * set {@link AbstractDeployerVDocMojo#vdocHome} property
     *
     * @param vdocHome set the vdocHome property
     **/
    public void setVdocHome(File vdocHome) {
        this.vdocHome = vdocHome;
    }
}
