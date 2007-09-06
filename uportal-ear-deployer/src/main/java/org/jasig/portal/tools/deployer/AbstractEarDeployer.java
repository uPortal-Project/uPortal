/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license distributed with this
 * file and available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.tools.deployer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Base class for taking uPortal3 and portlets, packaged as an EAR, and deploying it to a container.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractEarDeployer {
    private static final String DESCRIPTOR_PATH        = "META-INF/application.xml";
    private static final String WEB_MODULE_XPATH       = "//application/module/web";
    private static final String WEB_URI_NODE_NAME      = "web-uri";
    private static final String CONTEXT_ROOT_NODE_NAME = "context-root";

    protected final Log         logger                 = LogFactory.getLog(this.getClass());


    /**
     * Deployes an EAR to the container specified in the DeployerConfig. The EAR's
     * applicationContext.xml is parsed and the module/web entries are deployed using
     * {@link #deployWar(WebModule, JarFile, DeployerConfig)}. Then all JARs in the EAR are
     * deployed using {@link #deployJar(JarEntry, JarFile, DeployerConfig)}.
     * 
     * @param deployerConfig
     * @throws Exception
     */
    public final void deployEar(DeployerConfig deployerConfig) throws IOException {
        final JarFile earFile = this.getEarFile(deployerConfig);
        final Document descriptorDom = this.getDescriptorDom(earFile);
        final NodeList webModules = this.getWebModules(descriptorDom);

        //Iterate through the WebModules, deploying each
        for (int index = 0; index < webModules.getLength(); index++) {
            final Node webModuleNode = webModules.item(index);
            final WebModule webModuleInfo = this.getWebModuleInfo(webModuleNode);

            this.deployWar(webModuleInfo, earFile, deployerConfig);
        }

        //Iterate through all the entries in the EAR, deploying each that ends in .jar
        for (final Enumeration<JarEntry> earEntries = earFile.entries(); earEntries.hasMoreElements();) {
            final JarEntry entry = earEntries.nextElement();

            if (entry.getName().endsWith(".jar")) {
                this.deployJar(entry, earFile, deployerConfig);
            }
        }
    }

    /**
     * Sub-classes must implement this to deploy the specified WAR file from the EAR to the appropriate
     * location for the container.
     * 
     * @param webModule Information about the WAR to deploy.
     * @param earFile The EAR.
     * @param deployerConfig Deployer configuration, sub-classes will likely us a DeployerConfig sub-class to pass container specific information
     * @throws IOException If an IO related error occures while deploying the WAR.
     */
    protected abstract void deployWar(WebModule webModule, JarFile earFile, DeployerConfig deployerConfig) throws IOException;

    /**
     * Sub-classes must implement this to deploy the specified JAR file from the EAR to the appropriate
     * location for the container.
     * 
     * @param jarEntry The entry in the EAR that contains the JAR to deploy.
     * @param earFile The EAR.
     * @param deployerConfig Deployer configuration, sub-classes will likely us a DeployerConfig sub-class to pass container specific information
     * @throws IOException If an IO related error occures while deploying the JAR.
     */
    protected abstract void deployJar(JarEntry jarEntry, JarFile earFile, DeployerConfig deployerConfig) throws IOException;

    /**
     * Gets the EAR from the configuration in the {@link DeployerConfig}.
     * 
     * @param deployerConfig The configuration with information about the EAR
     * @return The JarFile for the EAR.
     * @throws IOException If there was a problem finding or opening the EAR.
     */
    protected JarFile getEarFile(DeployerConfig deployerConfig) throws IOException {
        final JarFile earFile = new JarFile(deployerConfig.getEarLocation());
        return earFile;
    }

    /**
     * Gets the EAR descriptor from the {@link JarFile}.
     * 
     * @param earFile The EAR to get the descriptor from.
     * @return The descriptor DOM for the EAR.
     * @throws IOException If there is any problem reading the descriptor from the EAR.
     */
    protected Document getDescriptorDom(final JarFile earFile) throws IOException {
        final ZipEntry descriptorEntry = earFile.getEntry(DESCRIPTOR_PATH);
        if (descriptorEntry == null) {
            throw new IllegalArgumentException("JarFile '" + earFile + "' does not contain a descriptor at '" + DESCRIPTOR_PATH + "'");
        }
        
        final InputStream descriptorStream = earFile.getInputStream(descriptorEntry);

        try {
            final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            
            final DocumentBuilder docBuilder;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            }
            catch (ParserConfigurationException pce) {
                throw new RuntimeException("Failed to create DocumentBuilder to parse EAR descriptor.", pce);
            }
            
            final Document descriptorDom;
            try {
                descriptorDom = docBuilder.parse(descriptorStream);
                return descriptorDom;
            }
            catch (SAXException e) {
                throw new RuntimeException("Failed to parse descriptor '" + DESCRIPTOR_PATH + "' from EAR '" + earFile.getName() + "'", e);
            }
        }
        finally {
            IOUtils.closeQuietly(descriptorStream);
        }
    }

    /**
     * Gets a {@link NodeList} of {@link Node}s that contain information about the web
     * modules in the EAR.
     * 
     * @param descriptorDom The EAR descriptor.
     * @return A NodeList of web module nodes.
     */
    protected NodeList getWebModules(Document descriptorDom) {
        final XPathFactory xpathFactory = XPathFactory.newInstance();
        final XPath xpath = xpathFactory.newXPath();

        final XPathExpression xpathExpr;
        try {
            xpathExpr = xpath.compile(WEB_MODULE_XPATH);
        }
        catch (XPathExpressionException xpee) {
            throw new RuntimeException("Failed to compile XPathExpression from '" + WEB_MODULE_XPATH + "'", xpee);
        }

        try {
            final NodeList nodes = (NodeList)xpathExpr.evaluate(descriptorDom, XPathConstants.NODESET);

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Found " + nodes.getLength() + " '" + WEB_MODULE_XPATH + "' nodes in descriptor.");
            }

            return nodes;
        }
        catch (XPathExpressionException xpee) {
            throw new RuntimeException("Failed to evaluate XPathExpression='" + xpathExpr + "'", xpee);
        }
    }

    
    /**
     * Creates a {@link WebModule} from a {@link Node} from the descriptor. The {@link #WEB_URI_NODE_NAME}
     * and {@link #CONTEXT_ROOT_NODE_NAME} child nodes are used to populate the respective properties on
     * the {@link WebModule}.
     * 
     * @param webModuleNode The 'web' Node that has the information needed to create a WebModule.
     * @return A WebModule that represents the data contained in the passed Node.
     */
    protected WebModule getWebModuleInfo(Node webModuleNode) {
        if (!"web".equals(this.getNodeName(webModuleNode))) {
            throw new IllegalArgumentException("webModuleNode must be a 'web' Node");
        }
        
        String webUri = null;
        String contextRoot = null;

        //Iterate through the children looking for needed elements
        final NodeList childNodes = webModuleNode.getChildNodes();
        for (int index = 0; index < childNodes.getLength() && (webUri == null || contextRoot == null); index++) {
            final Node node = childNodes.item(index);
            final String nodeName = this.getNodeName(node);

            if (WEB_URI_NODE_NAME.equals(nodeName)) {
                webUri = StringUtils.strip(node.getTextContent());
            }
            else if (CONTEXT_ROOT_NODE_NAME.equals(nodeName)) {
                contextRoot = StringUtils.strip(node.getTextContent());
            }
        }
        
        //Check that the node had all the right info
        if (webUri == null || contextRoot == null) {
            throw new IllegalArgumentException("Node '" + webModuleNode + "' did not contain the nessesary information to create a WebModule. webUri='" + webUri + "', contextRoot='" + contextRoot + "'");
        }
        
        //Create the WebModule object
        final WebModule webModule = new WebModule();
        webModule.setWebUri(webUri);
        webModule.setContextRoot(contextRoot);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Found WebModule='" + webModule + "'");
        }

        return webModule;
    }

    /**
     * Creates a File for the specified directory and name, if a file already exists
     * at the location it is deleted and all parent directories are verfied to exist. 
     * 
     * @param baseDir The directory to base the file in
     * @param fileName The name for the file
     * @return A new File object that has its parent directories and an existing file deleted. 
     */
    protected File createSafeFile(final File baseDir, final String fileName) throws IOException {
        final File safeFile = new File(baseDir, fileName);
        if (safeFile.exists()) {
            safeFile.delete();
        }
        else {
            FileUtils.forceMkdir(baseDir);
        }
        
        return safeFile;
    }

    /**
     * Reads the specified {@link JarEntry} from the {@link JarFile} and writes its contents
     * to the specified {@link File}.
     * 
     * @param earEntry The JarEntry for the file to read from the archive.
     * @param earFile The JarFile to get the {@link InputStream} for the file from.
     * @param destinationFile The File to write to, all parent directories should exist and no file should already exist at this location.
     * @throws IOException If the copying of data from the JarEntry to the File fails.
     */
    protected void copyAndClose(JarEntry earEntry, JarFile earFile, File destinationFile) throws IOException {
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Copying EAR entry '" + earFile.getName() + "!" + earEntry.getName() + "' to '" + destinationFile + "'");
        }

        final InputStream jarEntryStream = earFile.getInputStream(earEntry);
        try {
            final OutputStream jarOutStream = new FileOutputStream(destinationFile);
            try {
                IOUtils.copy(jarEntryStream, jarOutStream);
            }
            finally {
                IOUtils.closeQuietly(jarOutStream);
            }
        }
        finally {
            IOUtils.closeQuietly(jarEntryStream);
        }
    }

    /**
     * Reads the specified {@link JarEntry} from the {@link JarFile} assuming that the
     * entry represents another a JAR file. The files in the {@link JarEntry} will be
     * extracted using the contextDir as the base directory. 
     * 
     * @param earFile The JarEntry for the JAR to read from the archive.
     * @param earEntry The JarFile to get the {@link InputStream} for the file from.
     * @param contextDir The directory to extract the JAR to.
     * @throws IOException If the extracting of data from the JarEntry fails.
     */
    protected void extractWar(JarFile earFile, final JarEntry earEntry, final File contextDir) throws IOException {
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Extracting EAR entry '" + earFile.getName() + "!" + earEntry.getName() + "' to '" + contextDir + "'");
        }
        
        if (!contextDir.exists()) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Creating context directory entry '" + contextDir + "'");
            }

            FileUtils.forceMkdir(contextDir);
        }
        
        final JarInputStream warInputStream = new JarInputStream(earFile.getInputStream(earEntry));
        try {
            //TODO write manifest
            
            JarEntry warEntry;
            while ((warEntry = warInputStream.getNextJarEntry()) != null) {
                final File warEntryFile = new File(contextDir, warEntry.getName());
                
                if (warEntry.isDirectory()) {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("Creating WAR directory entry '" + earEntry.getName() + "!" + warEntry.getName() + "' as '" + warEntryFile + "'");
                    }
                    
                    FileUtils.forceMkdir(warEntryFile);
                }
                else {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("Extracting WAR entry '" + earEntry.getName() + "!" + warEntry.getName() + "' to '" + warEntryFile + "'");
                    }
                    
                    FileUtils.forceMkdir(warEntryFile.getParentFile());
                    
                    final FileOutputStream jarEntryFileOutputStream = new FileOutputStream(warEntryFile);
                    try {
                        IOUtils.copy(warInputStream, jarEntryFileOutputStream);
                    }
                    finally {
                        IOUtils.closeQuietly(jarEntryFileOutputStream);
                    }
                }
            }
        }
        finally {
            IOUtils.closeQuietly(warInputStream);
        }
    }

    private String getNodeName(Node node) {
        if (node.getNamespaceURI() == null) {
            return node.getNodeName();
        }
        else {
            return node.getLocalName();
        }
    }
}
