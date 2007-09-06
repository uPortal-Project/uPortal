/* Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.tools.deployer.tomcat;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.jasig.portal.tools.deployer.AbstractEarDeployer;
import org.jasig.portal.tools.deployer.DeployerConfig;
import org.jasig.portal.tools.deployer.WebModule;

/**
 * Encapsulates Tomcat specific logic for deploying WARs and JARs from the uPortal3 EAR.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TomcatEarDeployer extends AbstractEarDeployer {
    
    /**
     * Writes the WAR to Tomcat's webapps directory, as specified by {@link TomcatDeployerConfig#getCatalinaWebapps()}.
     * 
     * @see org.jasig.portal.tools.deployer.AbstractEarDeployer#deployWar(org.jasig.portal.tools.deployer.WebModule, java.util.jar.JarFile, org.jasig.portal.tools.deployer.DeployerConfig)
     */
    @Override
    protected final void deployWar(WebModule webModule, JarFile earFile, DeployerConfig deployerConfig) throws IOException {
        final TomcatDeployerConfig tomcatDeployerConfig = this.getTomcatDeployerConfig(deployerConfig);
        
        final String webUri = webModule.getWebUri();
        final JarEntry warEntry = earFile.getJarEntry(webUri);
        final File catalinaWebappsDir = tomcatDeployerConfig.getCatalinaWebapps();
        
        String contextName = webModule.getContextRoot();
        if (contextName.endsWith(".war")) {
            contextName = contextName.substring(contextName.length() - 4);
        }
        if (contextName.startsWith("/")) {
            contextName = contextName.substring(1);
        }
        
        if (tomcatDeployerConfig.isRemoveExistingDirectories()) {
            final File contextDir = new File(catalinaWebappsDir, contextName);
            
            if (contextDir.exists()) {
                FileUtils.deleteDirectory(contextDir);
            }
        }
        
        if (tomcatDeployerConfig.isExtractWars()) {
            final File contextDir = new File(catalinaWebappsDir, contextName);
            this.extractWar(earFile, warEntry, contextDir);
        }
        else {
            final String warName = contextName += ".war";
            final File warDest = this.createSafeFile(catalinaWebappsDir, warName);
            this.copyAndClose(warEntry, earFile, warDest);
        }
    }

    /**
     * Writes the JAR to Tomcat's shared/lib directory, as specified by {@link TomcatDeployerConfig#getCatalinaShared()}.
     * 
     * @see org.jasig.portal.tools.deployer.AbstractEarDeployer#deployJar(java.util.jar.JarEntry, java.util.jar.JarFile, org.jasig.portal.tools.deployer.DeployerConfig)
     */
    @Override
    protected final void deployJar(JarEntry jarEntry, JarFile earFile, DeployerConfig deployerConfig) throws IOException {
        final TomcatDeployerConfig tomcatDeployerConfig = this.getTomcatDeployerConfig(deployerConfig);
        
        final String jarName = jarEntry.getName();
        final File sharedLib = new File(tomcatDeployerConfig.getCatalinaShared(), "lib");
        final File jarDest = this.createSafeFile(sharedLib, jarName);
        
        this.copyAndClose(jarEntry, earFile, jarDest);
    }

    /**
     * Does a type check and converts a generic {@link DeployerConfig} to a {@link TomcatDeployerConfig}.
     * 
     * @param deployerConfig The config to try and cast
     * @return A TomcatDeployerConfig.
     */
    protected TomcatDeployerConfig getTomcatDeployerConfig(DeployerConfig deployerConfig) {
        if (!(deployerConfig instanceof TomcatDeployerConfig)) {
            throw new IllegalArgumentException("deployerConfig must be of type " + TomcatDeployerConfig.class);
        }
        final TomcatDeployerConfig tomcatDeployerConfig = (TomcatDeployerConfig)deployerConfig;
        return tomcatDeployerConfig;
    }
}
