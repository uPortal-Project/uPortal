/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license distributed with this
 * file and available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.tools.deployer.tomcat;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TomcatEarDeployerTest extends TestCase {
    public void testDeployDirect() throws Exception {
        final TomcatDeployerConfig config = new TomcatDeployerConfig();

        final URL testEarUrl = this.getClass().getResource("/org/jasig/portal/tools/deployer/test.ear");
        final File testEar = new File(testEarUrl.getFile());
        config.setEarLocation(testEar);

        final File sharedDir = this.getTempDir("shared", "dir");
        config.setCatalinaShared(sharedDir);

        final File webappsDir = this.getTempDir("webapps", "dir");
        config.setCatalinaWebapps(webappsDir);
        
        try {
            final TomcatEarDeployer deployer = new TomcatEarDeployer();
            deployer.deployEar(config);


            final File expectedPlutoJar = new File(new File(sharedDir, "/lib"), "portlet-api-1.0.jar");
            assertTrue("Expected file '" + expectedPlutoJar + "' does not exist.", expectedPlutoJar.exists());

            final File expectedWar1 = new File(webappsDir, "WarDeployerTestPortlet1.war");
            assertTrue("Expected file '" + expectedWar1 + "' does not exist.", expectedWar1.exists());

            final File expectedWar2 = new File(webappsDir, "WarDeployerTestPortlet2.war");
            assertTrue("Expected file '" + expectedWar2 + "' does not exist.", expectedWar2.exists());
        }
        finally {
            try { FileUtils.deleteDirectory(sharedDir); } catch (Exception e) { }
            try { FileUtils.deleteDirectory(webappsDir); } catch (Exception e) { }
        }
    }
    
    public void testDeployDirectAndRemoveExistingDirs() throws Exception {
        final TomcatDeployerConfig config = new TomcatDeployerConfig();
        
        config.setRemoveExistingDirectories(true);

        final URL testEarUrl = this.getClass().getResource("/org/jasig/portal/tools/deployer/test.ear");
        final File testEar = new File(testEarUrl.getFile());
        config.setEarLocation(testEar);

        final File sharedDir = this.getTempDir("shared", "dir");
        config.setCatalinaShared(sharedDir);

        final File webappsDir = this.getTempDir("webapps", "dir");
        config.setCatalinaWebapps(webappsDir);
        
        final File oldWarDir1 = new File(webappsDir, "WarDeployerTestPortlet1");
        FileUtils.forceMkdir(oldWarDir1);
        
        final File oldWarDir2 = new File(webappsDir, "WarDeployerTestPortlet2");
        FileUtils.forceMkdir(oldWarDir2);
        
        try {
            final TomcatEarDeployer deployer = new TomcatEarDeployer();
            deployer.deployEar(config);


            final File expectedPlutoJar = new File(new File(sharedDir, "/lib"), "portlet-api-1.0.jar");
            assertTrue("Expected file '" + expectedPlutoJar + "' does not exist.", expectedPlutoJar.exists());

            final File expectedWar1 = new File(webappsDir, "WarDeployerTestPortlet1.war");
            assertTrue("Expected file '" + expectedWar1 + "' does not exist.", expectedWar1.exists());
            assertFalse("Directory '" + oldWarDir1 + "' should not exist.", oldWarDir1.exists());

            final File expectedWar2 = new File(webappsDir, "WarDeployerTestPortlet2.war");
            assertTrue("Expected file '" + expectedWar2 + "' does not exist.", expectedWar2.exists());
            assertFalse("Directory '" + oldWarDir2 + "' should not exist.", oldWarDir2.exists());
        }
        finally {
            try { FileUtils.deleteDirectory(sharedDir); } catch (Exception e) { }
            try { FileUtils.deleteDirectory(webappsDir); } catch (Exception e) { }
        }
    }
    
    public void testDeployExtracted() throws Exception {
        final TomcatDeployerConfig config = new TomcatDeployerConfig();
        
        config.setExtractWars(true);

        final URL testEarUrl = this.getClass().getResource("/org/jasig/portal/tools/deployer/test.ear");
        final File testEar = new File(testEarUrl.getFile());
        config.setEarLocation(testEar);

        final File sharedDir = this.getTempDir("shared", "dir");
        config.setCatalinaShared(sharedDir);

        final File webappsDir = this.getTempDir("webapps", "dir");
        config.setCatalinaWebapps(webappsDir);
        
        try {
            final TomcatEarDeployer deployer = new TomcatEarDeployer();
            deployer.deployEar(config);


            final File expectedPlutoJar = new File(new File(sharedDir, "/lib"), "portlet-api-1.0.jar");
            assertTrue("Expected file '" + expectedPlutoJar + "' does not exist.", expectedPlutoJar.exists());

            final File expectedWarDir1 = new File(webappsDir, "WarDeployerTestPortlet1");
            assertTrue("Expected file '" + expectedWarDir1 + "' does not exist.", expectedWarDir1.exists());
            final File expectedPortletXml1 = new File(expectedWarDir1, "/WEB-INF/portlet.xml");
            assertTrue("Expected file '" + expectedPortletXml1 + "' does not exist.", expectedPortletXml1.exists());

            final File expectedWarDir2 = new File(webappsDir, "WarDeployerTestPortlet2");
            assertTrue("Expected file '" + expectedWarDir2 + "' does not exist.", expectedWarDir2.exists());
            final File expectedPortletXml2 = new File(expectedWarDir2, "/WEB-INF/portlet.xml");
            assertTrue("Expected file '" + expectedPortletXml2 + "' does not exist.", expectedPortletXml2.exists());
        }
        finally {
            try { FileUtils.deleteDirectory(sharedDir); } catch (Exception e) { }
            try { FileUtils.deleteDirectory(webappsDir); } catch (Exception e) { }
        }
    }

    private File getTempDir(String prefix, String suffix) throws IOException {
        final File tempFile = File.createTempFile(prefix, suffix);
        tempFile.delete();
        tempFile.mkdirs();
        return tempFile;
    }
}
