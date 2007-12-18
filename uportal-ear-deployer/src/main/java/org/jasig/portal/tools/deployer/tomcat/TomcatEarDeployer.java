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
import org.jasig.portal.tools.deployer.WebModule;

/**
 * Encapsulates Tomcat specific logic for deploying WARs and JARs from the uPortal3 EAR.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TomcatEarDeployer extends AbstractEarDeployer<TomcatDeployerConfig> {
    /**
     * Writes the WAR to Tomcat's webapps directory, as specified by {@link TomcatDeployerConfig#getCatalinaWebapps()}.
     * 
     * @see org.jasig.portal.tools.deployer.AbstractEarDeployer#deployWar(org.jasig.portal.tools.deployer.WebModule, java.util.jar.JarFile, org.jasig.portal.tools.deployer.DeployerConfig)
     */
    @Override
    protected final void deployWar(WebModule webModule, JarFile earFile, TomcatDeployerConfig tomcatDeployerConfig) throws IOException {
        final String webUri = webModule.getWebUri();
        final JarEntry warEntry = earFile.getJarEntry(webUri);
        final File webappsDir = tomcatDeployerConfig.getWebAppsDir();
        
        String contextName = webModule.getContextRoot();
        if (contextName.endsWith(".war")) {
            contextName = contextName.substring(contextName.length() - 4);
        }
        if (contextName.startsWith("/")) {
            contextName = contextName.substring(1);
        }
        
        if (tomcatDeployerConfig.isRemoveExistingDirectories()) {
            final File contextDir = new File(webappsDir, contextName);
            
            if (contextDir.exists()) {
                FileUtils.deleteDirectory(contextDir);
            }
        }
        
        if (tomcatDeployerConfig.isExtractWars()) {
            final File contextDir = new File(webappsDir, contextName);
            this.extractWar(earFile, warEntry, contextDir);
        }
        else {
            final String warName = contextName += ".war";
            final File warDest = this.createSafeFile(webappsDir, warName);
            this.copyAndClose(warEntry, earFile, warDest);
        }
    }

    /**
     * Writes the JAR to Tomcat's shared/lib directory, as specified by {@link TomcatDeployerConfig#getCatalinaShared()}.
     * 
     * @see org.jasig.portal.tools.deployer.AbstractEarDeployer#deployJar(java.util.jar.JarEntry, java.util.jar.JarFile, org.jasig.portal.tools.deployer.DeployerConfig)
     */
    @Override
    protected final void deployJar(JarEntry jarEntry, JarFile earFile, TomcatDeployerConfig tomcatDeployerConfig) throws IOException {
        final String jarName = jarEntry.getName();
        
        if (jarName.contains("/")) {
            throw new IllegalArgumentException("The EAR contains a JAR entry in a folder, this is not supported. Bad Jar: '" + jarName + "'");
        }
        
        final File sharedLibDir = tomcatDeployerConfig.getJarDir();
        final File jarDest = this.createSafeFile(sharedLibDir, jarName);
        
        this.copyAndClose(jarEntry, earFile, jarDest);
    }
}
