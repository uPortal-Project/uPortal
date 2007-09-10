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
    private static final String WEBAPPS_DIR_NAME = "webapps";
    private static final String COMMON_DIR_NAME = "common";
    private static final String SHARED_DIR_NAME = "shared";
    private static final String LIB_DIR_NAME = "lib";

    /**
     * Writes the WAR to Tomcat's webapps directory, as specified by {@link TomcatDeployerConfig#getCatalinaWebapps()}.
     * 
     * @see org.jasig.portal.tools.deployer.AbstractEarDeployer#deployWar(org.jasig.portal.tools.deployer.WebModule, java.util.jar.JarFile, org.jasig.portal.tools.deployer.DeployerConfig)
     */
    @Override
    protected final void deployWar(WebModule webModule, JarFile earFile, TomcatDeployerConfig tomcatDeployerConfig) throws IOException {
        final String webUri = webModule.getWebUri();
        final JarEntry warEntry = earFile.getJarEntry(webUri);
        final File tomcatBaseDir = tomcatDeployerConfig.getTomcatBase();
        final File webappsDir = new File(tomcatBaseDir, WEBAPPS_DIR_NAME);
        
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
        final String[] jarNameParts = this.getJarNameParts(jarName);
        
        final File jarDestDir;
        if (SHARED_DIR_NAME.equals(jarNameParts[0])) {
            final File tomcatBaseDir = tomcatDeployerConfig.getTomcatBase();
            final File sharedDir = new File(tomcatBaseDir, SHARED_DIR_NAME);
            jarDestDir = new File(sharedDir, LIB_DIR_NAME);
        }
        else if (COMMON_DIR_NAME.equals(jarNameParts[0])) {
            final File tomcatHomeDir = tomcatDeployerConfig.getTomcatHome();
            final File commonDir = new File(tomcatHomeDir, COMMON_DIR_NAME);
            jarDestDir = new File(commonDir, LIB_DIR_NAME);
        }
        else {
            throw new IllegalArgumentException("No configured destination for JARs in '" + jarNameParts[0] + "' directory. ('" + jarName + "')");
        }
        
        final File jarDest = this.createSafeFile(jarDestDir, jarNameParts[1]);
        
        this.copyAndClose(jarEntry, earFile, jarDest);
    }

    /**
     * Splits the jarName string into the path and fileName parts.
     */
    private String[] getJarNameParts(final String jarName) {
        final String[] jarNameParts = new String[2];
        
        final int lastSlash = jarName.lastIndexOf('/');
        if (lastSlash < 0) {
            jarNameParts[0] = null;
            jarNameParts[1] = jarName;
        }
        else {
            jarNameParts[0] = jarName.substring(0, lastSlash);
            jarNameParts[1] = jarName.substring(lastSlash + 1);
        }

        return jarNameParts;
    }
}
