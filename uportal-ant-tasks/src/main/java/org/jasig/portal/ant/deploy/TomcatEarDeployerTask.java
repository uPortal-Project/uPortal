/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license distributed with this
 * file and available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.ant.deploy;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.jasig.portal.tools.deployer.tomcat.TomcatDeployerConfig;
import org.jasig.portal.tools.deployer.tomcat.TomcatEarDeployer;

/**
 * Ant task that exposes the TomcatEarDeployer functionality
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TomcatEarDeployerTask extends Task {
    public static final String DEFAULT_WEBAPPS_DIR = "webapps";
    public static final String DEFAULT_JAR_DIR = "shared/lib";
    
    private String webAppsDir;
    private String jarDir;
    
    private File catalinaBase;
    private File ear;
    private boolean extractWars = false;
    private boolean removeExistingDirectories = false;


    public String getWebAppsDir() {
        if (this.webAppsDir == null) {
            return DEFAULT_WEBAPPS_DIR;
        }

        return this.webAppsDir;
    }
    public void setWebAppsDir(String webAppsDir) {
        this.webAppsDir = webAppsDir;
    }

    public String getJarDir() {
        if (this.jarDir == null) {
            return DEFAULT_JAR_DIR;
        }

        return this.jarDir;
    }
    public void setJarDir(String jarDir) {
        this.jarDir = jarDir;
    }

    public File getCatalinaBase() {
        return this.catalinaBase;
    }
    public void setCatalinaBase(File catalinaShared) {
        this.catalinaBase = catalinaShared;
    }

    public File getEar() {
        return this.ear;
    }
    public void setEar(File ear) {
        this.ear = ear;
    }

    public boolean isExtractWars() {
        return this.extractWars;
    }
    public void setExtractWars(boolean extractWars) {
        this.extractWars = extractWars;
    }

    public boolean isRemoveExistingDirectories() {
        return this.removeExistingDirectories;
    }
    public void setRemoveExistingDirectories(boolean removeExistingDirectories) {
        this.removeExistingDirectories = removeExistingDirectories;
    }

    @Override
    public void execute() throws BuildException {
        this.validateArgs();

        final TomcatDeployerConfig config = new TomcatDeployerConfig();

        final File catalinaBase = this.getCatalinaBase();
        
        final File fullJarDir = new File(catalinaBase, this.getJarDir());
        config.setJarDir(fullJarDir);
        
        final File fullWebAppsDir = new File(catalinaBase, this.getWebAppsDir());
        config.setWebAppsDir(fullWebAppsDir);
        
        config.setEarLocation(this.getEar());
        config.setExtractWars(this.isExtractWars());
        config.setRemoveExistingDirectories(this.isRemoveExistingDirectories());

        final TomcatEarDeployer deployer = new TomcatEarDeployer();
        try {
            deployer.deployEar(config);
        }
        catch (IOException e) {
            throw new BuildException(e);
        }
    }

    private void validateArgs() throws BuildException {
        if (this.ear == null) {
            throw new BuildException("ear is a required property");
        }
        if (!this.ear.exists()) {
            throw new BuildException("ear '" + this.ear.getAbsolutePath() + "' does not exist");
        }
        if (this.catalinaBase == null) {
            throw new BuildException("catalinaBase is a required property");
        }
    }
}
