/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.portlets.dynamicskin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.portlet.PortletContext;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.io.IOUtils;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.lesscss.LessSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * File system based implementation of services for the Skin Manager.
 *
 * @since 4.1.0
 * @author James Wennmacher, jwennmacher@unicon.net
 */
@Service
public class FileSystemDynamicSkinService implements DynamicSkinService {

    private static final String DYNASKIN_DEFAULT_ROOT_FOLDER = "/media/skins/respondr";
    private static final String DYNASKIN_TEMPLATE_INCLUDE_FILE = "{0}/{1}.less";
    private static final String DYNASKIN_INCLUDE_FILE = "{0}/configuredSkin-{1}.less";
    private static final String LESS_CSS_JAVASCRIPT_URL = "/media/skins/common/javascript/less/less-1.6.2.js";

    private final Logger log = LoggerFactory.getLogger(getClass());
    private String rootFolder = DYNASKIN_DEFAULT_ROOT_FOLDER;
    private MessageFormat skinTemplateIncludeFile = new MessageFormat(DYNASKIN_TEMPLATE_INCLUDE_FILE);
    private MessageFormat skinIncludeFile = new MessageFormat(DYNASKIN_INCLUDE_FILE);
    private String lessCssJavascriptUrlPath = LESS_CSS_JAVASCRIPT_URL;

    /**
     *  Set of skinFilePath values for skin files that currently exists on file
     *  system.  Thread-safe for concurrent reads and inserts.
     */
    private Set<String> compiledCssFilepaths = new CopyOnWriteArraySet<String>();

    @Autowired
    @Qualifier(value = "org.jasig.portal.skinManager.failureCache")
    private Cache cssSkinFailureCache;

    public void setLessCssJavascriptUrlPath(String lessCssJavascriptUrlPath) {
        this.lessCssJavascriptUrlPath = lessCssJavascriptUrlPath;
    }

    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public void setSkinTemplateIncludeFile(String skinTemplateIncludeFile) {
        this.skinTemplateIncludeFile = new MessageFormat(skinTemplateIncludeFile);
    }

    public void setSkinIncludeFile(String skinIncludeFile) {
        this.skinIncludeFile = new MessageFormat(skinIncludeFile);
    }

    /**
     * Return true if the filePathname already exists on the file system.  Check memory first in a concurrent manner
     * to allow multiple threads to check simultaneously.
     *
     * @param filePathname Fully-qualified file path name of the .css file
     * @return True if file exists on the file system.
     */
    @Override
    public boolean skinFileExists(String filePathname) {
        // Check the existing filepaths map first since it is faster than accessing the file system.
        if (compiledCssFilepaths.contains(filePathname)) {
            return true;
        }
        // If file already exists on file system, add it to the existing filepaths map in a thread-safe manner.
        // This situation would occur when the portal servers are restarted and the in-memory map is empty.
        boolean exists = new File(filePathname).exists();
        if (exists) {
            compiledCssFilepaths.add(filePathname);
        }
        return exists;
    }

    @Override
    public void generateSkinCssFile(PortletRequest request, String filePathname, String skinToken,
                                    String lessfileBaseName) {

        synchronized(filePathname) {
            if (compiledCssFilepaths.contains(filePathname)) {
                /*
                 * Two or more threads needing the same CSS file managed to invoke
                 * this method.  An earlier thread has already generated the file
                 * we need.  Concurrency features of the CopyOnWriteArraySet
                 * (compiledCssFilepaths) guarantee that we will enter this if {}
                 * block (and exit) for a filePathname that's been successfully
                 * generated by another thread.
                 */
                return;
            }
            try {
                if (!cssSkinFailureCache.getKeysWithExpiryCheck().contains(filePathname)) {
                    PortletContext ctx = request.getPortletSession().getPortletContext();

                    String templateRelativePath =
                            skinTemplateIncludeFile.format(new Object[] {rootFolder, lessfileBaseName});
                    String templateFilepath = ctx.getRealPath(templateRelativePath);

                    String includeRelativePath = skinIncludeFile.format(new Object[] {rootFolder, skinToken});
                    String includeFilepath = ctx.getRealPath(includeRelativePath);

                    createLessIncludeFile(request.getPreferences(), includeFilepath, templateFilepath);

                    URL lessCssJavascriptUrl = ctx.getResource(lessCssJavascriptUrlPath);
                    processLessFile(includeFilepath, filePathname, lessCssJavascriptUrl);
                    compiledCssFilepaths.add(filePathname);
                } else {
                    // Though this should never happen except when developers are modifying the LESS files and make a mistake,
                    // if we previously tried to create the CSS file and failed for some reason, don't try to compile it
                    // again for a bit since the process is so processor intensive. It would virtually hang the uPortal
                    // service trying to compile a bad LESS file repeatedly on different threads.
                    log.warn("Skipping generation of CSS file {} due to previous LESS compilation failures", filePathname);
                }
            } catch (Exception e) {
                cssSkinFailureCache.put(new Element(filePathname, filePathname));
                throw new RuntimeException("Error compiling the following LESS file:  " + filePathname, e);
            }
        }

    }

    /**
     * Create the less include file by appending the configurable preference definitions (minus the configuration
     * prefix string) to the end of the template file; e.g. portlet preference name
     * PREFcolor1 is written to the less file as @color1:prefValue
     *
     * @param prefs Portlet preferences
     * @param filename name of the less include file to create
     * @param templateFile template less include file
     * @throws IOException
     */
    private void createLessIncludeFile(PortletPreferences prefs, String filename, String templateFile) throws IOException {
        // Create a set of less variable assignments.
        StringBuilder str = new StringBuilder();
        Enumeration<String> prefNames =  prefs.getNames();
        while (prefNames.hasMoreElements()) {
            String prefName = prefNames.nextElement();
            if (prefName.startsWith(DynamicSkinService.CONFIGURABLE_PREFIX)) {
                String nameWithoutPrefix = prefName.substring(DynamicSkinService.CONFIGURABLE_PREFIX.length());
                str.append("@").append(nameWithoutPrefix).append(": ").append(prefs.getValue(prefName, "")).append(";\n");
            }
        }

        // Create byte[]s of the template and preferences content
        byte[] prefsContent = str.toString().getBytes();
        File f = new File(templateFile);
        byte[] templateContent = IOUtils.toByteArray(f.toURI());

        // Create a less include file by appending the less variable definitions to the end of the template less
        // include file.  Insure there is a newline at the end of the template content or the first preference
        // value will be lost.
        byte[] newline = "\n".getBytes();
        byte[] fileContent = new byte[templateContent.length + newline.length + prefsContent.length];
        System.arraycopy(templateContent, 0, fileContent, 0, templateContent.length);
        System.arraycopy(newline, 0, fileContent, templateContent.length, newline.length);
        System.arraycopy(prefsContent, 0, fileContent, templateContent.length + newline.length, prefsContent.length);
        File lessInclude = new File(filename);
        IOUtils.write(fileContent, new FileOutputStream(lessInclude));
    }

    /**
     * Less compile the include file into a temporary css file.  When done rename the temporary css file to the
     * correct output filename.  Since the less compilation phase takes several seconds, this insures the
     * output css file is does not exist on the filesystem until it is complete.
     *
     * @param lessIncludeFilepath less include file that includes all dependencies
     * @param outputFilepath name of the output css file
     * @param lessCssJavascriptUrl lessCssJavascript compiler url
     * @throws IOException
     * @throws LessException
     */
    private void processLessFile(String lessIncludeFilepath, String outputFilepath, URL lessCssJavascriptUrl)
            throws IOException, LessException {
        LessSource lessSource = new LessSource(new File(lessIncludeFilepath));
        if (log.isDebugEnabled()) {
            String result = lessSource.getNormalizedContent();
            File lessSourceOutput = new File(outputFilepath + ".lesssource");
            IOUtils.write(result, new FileOutputStream(lessSourceOutput));
            log.debug("Full Less source from include file {0}, using lessCssJavascript at {1}"
                    + ", is at {2}, output css will be written to {3}",
                    lessIncludeFilepath, lessCssJavascriptUrl.toString(), lessSourceOutput, outputFilepath);
        }
        LessCompiler compiler = new LessCompiler();
        compiler.setLessJs(lessCssJavascriptUrl);
        compiler.setCompress(true);
        File tempOutputFile = new File(outputFilepath+"tmp");
        compiler.compile(lessSource, tempOutputFile);
        tempOutputFile.renameTo(new File(outputFilepath));
    }

    @Override
    public String calculateTokenForCurrentSkin(PortletRequest request) {
        int hash = 0;
        PortletPreferences preferences = request.getPreferences();

        // Add the list of preference names to an ordered list so we can get reliable hashcode calculations.
        Map<String, String[]> prefs = preferences.getMap();
        TreeSet<String> orderedNames = new TreeSet<String>(prefs.keySet());
        Iterator<String> iterator = orderedNames.iterator();
        while (iterator.hasNext()) {
            String preferenceName = iterator.next();
            if (preferenceName.startsWith(DynamicSkinService.CONFIGURABLE_PREFIX)) {
                hash = hash * 31 + preferences.getValue(preferenceName, "").trim().hashCode();
            }
        }
        return Integer.toString(hash);
    }

    @Override
    /**
     * Returns the set of skins to use.  This implementation parses the skinList.xml file and returns the set of
     * skin-key element values.  If there is an error parsing the XML file, return an empty set.
     */
    public SortedSet<String> getSkinNames(PortletRequest request) {
        // Context to access the filesystem
        PortletContext ctx = request.getPortletSession().getPortletContext();

        // Determine the full path to the skins directory
        String skinsFilepath = ctx.getRealPath(rootFolder + "/skinList.xml");

        // Create File object to access the filesystem
        File skinList = new File(skinsFilepath);

        TreeSet<String> skins = new TreeSet<>();
        try {
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(skinList);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("skin-key");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                org.w3c.dom.Element element = (org.w3c.dom.Element) nList.item(temp);
                String skinName = element.getTextContent();
                log.debug("Found skin-key value {}", skinName);
                skins.add(skinName);
            }
        } catch (SAXException | ParserConfigurationException | IOException e) {
            log.error("Error processing skinsFilepath {}", skinsFilepath, e);
        }

        return skins;
    }

}
