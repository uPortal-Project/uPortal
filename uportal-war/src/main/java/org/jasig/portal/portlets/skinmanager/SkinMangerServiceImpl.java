/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.portlets.skinmanager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.portlet.PortletContext;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.jasig.portal.utils.FileUtil;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.lesscss.LessSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Services for the Skin Manager
 *
 * @author James Wennmacher, jwennmacher@unicon.net
 */

@Service
public class SkinMangerServiceImpl implements SkinManagerService {
    private static final String SKINMGR_ROOT_FOLDER = "/media/skins/respondr";
    private static final String SKINMGR_SKIN_TEMPLATE_INCLUDE_FILE = "{0}/defaultSkin.less";
    private static final String SKINMGR_LESS_VARIABLE_FILE = "{0}/defaultSkin/less/variables.less";
    private static final String SKINMGR_VARIABLE_REGEX = "^[\\s]*@{0}[\\s]*:[\\s]*(.+);";
    private static final String SKINMGR_SKIN_INCLUDE_FILE = "{0}/configuredSkin-{1}.less";
    private static final String LESS_CSS_JAVASCRIPT_URL = "/media/skins/common/javascript/less/less-1.6.2.js";

    private Cache cssSkinFailureCache;

    private final Logger log = LoggerFactory.getLogger(getClass());
    private String rootFolder = SKINMGR_ROOT_FOLDER;
    private MessageFormat skinTemplateIncludeFile = new MessageFormat(SKINMGR_SKIN_TEMPLATE_INCLUDE_FILE);
    private MessageFormat skinIncludeFile = new MessageFormat(SKINMGR_SKIN_INCLUDE_FILE);
    private MessageFormat lessVariableFile = new MessageFormat(SKINMGR_LESS_VARIABLE_FILE);
    private String variableRegex = SKINMGR_VARIABLE_REGEX;
    private String lessCssJavascriptUrlPath = LESS_CSS_JAVASCRIPT_URL;
    private int cssConcurrentHashMapInitialSize = 16;
    // Map of <skinFilepath, skinFilepath> where value = skinFilepath when skin file exists on file system. This map
    // is thread-safe for concurrent reads and inserts.
    private ConcurrentHashMap<String, String> compiledCssFilepaths;
    private static final Set<String> locks = Collections.synchronizedSet(new HashSet<String>());

    @PostConstruct
    private void init() {
        compiledCssFilepaths = new ConcurrentHashMap<String, String>(cssConcurrentHashMapInitialSize);
    }



    @Autowired
    @Qualifier(value = "org.jasig.portal.skinManager.failureCache")
    @Required
    public void setCssSkinFailureCache(Cache cssSkinFailureCache) {
        this.cssSkinFailureCache = cssSkinFailureCache;
    }

    public int getCssConcurrentHashMapInitialSize() {
        return cssConcurrentHashMapInitialSize;
    }

    @Value(value = "${org.jasig.portal.skin.manager.cssfile.cache.size:16}")
    public void setCssConcurrentHashMapInitialSize(int cssConcurrentHashMapInitialSize) {
        this.cssConcurrentHashMapInitialSize = cssConcurrentHashMapInitialSize;
    }

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

    public void setSkinmgrLessVariableFile(String skinmgrLessVariableFile) {
        this.lessVariableFile = new MessageFormat(skinmgrLessVariableFile);
    }

    public void setVariableRegex(String variableRegex) {
        this.variableRegex = variableRegex;
    }

    /**
     * Return true if the filePathname already exists on the file system.  Check memory first in a concurrent manner
     * to allow multiple threads to check simultaneously.
     * @param filePathname
     * @return
     */
    @Override
    public boolean skinFileExists(String filePathname) {
        // Check the existing filepaths map first since it is faster than accessing the file system.
        if (compiledCssFilepaths.get(filePathname) != null) {
            return true;
        }
        // If file already exists on file system, add it to the existing filepaths map in a thread-safe manner.
        // This situation would occur when the portal servers are restarted and the in-memory map is empty.
        boolean exists = FileUtil.fileExists(filePathname);
        if (exists) {
            compiledCssFilepaths.put(filePathname, filePathname);
        }
        return exists;
    }

    /**
     * Create the skin css file in a thread-safe manner that allows multiple different skin files to be created
     * simultaneously to handle large tenant situations where all the custom CSS files were cleared away after a
     * uPortal deploy.  Since the less compilation phase is fairly slow (several seconds) and intensive,
     * allow multiple threads to process different less compilations at the same time but insure the same
     * output file will not be created multiple times. Also we should not let a consistent LESS compilation
     * failure completely take down the portal due to trying repeatedly compile a bad LESS file.
     * After a period of time, allow LESS compilation failures to retry to limit performance impacts.
     * @param request
     * @param filePathname
     * @param uniqueString
     */
    @Override
    public void createSkinCssFile(PortletRequest request, String filePathname, String uniqueString) {
        // Wait until we can get a lock for the particular filePathname
        while(locks.contains(filePathname))
        {
            try { Thread.sleep(100); } catch (InterruptedException ie) { }
        }

        // Multiple threads may potentially reach this point simultaneously for the same filename.
        // If we successfully added the filePathname to the locks (e.g. it wasn't already present in the list),
        // create the LESS include file from the template and compile it to create the CSS file.
        if (locks.add(filePathname)) {
            try {
                // Though this should never happen except when developers are modifying the LESS files and make a mistake,
                // if we previously tried to create the CSS file and failed for some reason, don't try to compile it
                // again for a bit since the process is so processor intensive. It would virtually hang the uPortal
                // service trying to compile a bad LESS file repeatedly on different threads.
                if (cssSkinFailureCache.get(filePathname) != null) {
                    log.warn("Temporarily won't attempt to create CSS file {} due to previous LESS compilation failures",
                            filePathname);
                } else if (!skinFileExists(filePathname)) {
                    PortletContext portletContext = request.getPortletSession().getPortletContext();

                    String templateRelativePath = skinTemplateIncludeFile.format(new Object[]{rootFolder});
                    String templateFilepath = portletContext.getRealPath(templateRelativePath);

                    String includeRelativePath = skinIncludeFile.format(new Object[]{rootFolder, uniqueString});
                    String includeFilepath = portletContext.getRealPath(includeRelativePath);

                    createLessIncludeFile(request.getPreferences(), includeFilepath, templateFilepath);

                    URL lessCssJavascriptUrl = portletContext.getResource(lessCssJavascriptUrlPath);
                    processLessFile(includeFilepath, filePathname, lessCssJavascriptUrl);
                    compiledCssFilepaths.put(filePathname, filePathname);
                }
            } catch (IOException e) {
                addFilenameToFailureList(filePathname);
                throw new SkinManagerException(e);
            } catch (LessException e) {
                addFilenameToFailureList(filePathname);
                throw new SkinManagerException(e);
            } finally {
                locks.remove(filePathname);
            }
        } else {
            // It's possible two threads exited the while loop around the same time, but only one can add the filename
            // to the locks.  If this thread wasn't able to add the filename to the locks, retry the process.
            // It's rare this path will execute, but we don't want two threads to attempt to create the same files on
            // the file system in the exceptionally rare case it did happen.
            createSkinCssFile(request, filePathname, uniqueString);
        }
    }

    private void addFilenameToFailureList(String filePathname) {
        cssSkinFailureCache.put(new Element(filePathname, filePathname));
    }

    /**
     * Create the less include file by appending the preference definitions to the end of the template file.
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
            str.append("@").append(prefName).append(": ").append(prefs.getValue(prefName, "")).append(";\n");
        }

        // Create byte[]s of the template and preferences content
        byte[] prefsContent = str.toString().getBytes();
        byte[] templateContent = FileUtil.read(templateFile);

        // Create a less include file by appending the less variable definitions to the end of the template less
        // include file.  Insure there is a newline at the end of the template content or the first preference
        // value will be lost.
        byte[] newline = "\n".getBytes();
        byte[] fileContent = new byte[templateContent.length + newline.length + prefsContent.length];
        System.arraycopy(templateContent, 0, fileContent, 0, templateContent.length);
        System.arraycopy(newline, 0, fileContent, templateContent.length, newline.length);
        System.arraycopy(prefsContent, 0, fileContent, templateContent.length + newline.length, prefsContent.length);
        FileUtil.write(filename, fileContent);
    }

    /**
     * Less compile the include file into a temporary css file.  When done rename the temporary css file to the
     * correct output filename.  Since the less compilation phase takes several seconds, this insures the
     * output css file is does not exist on the filesystem until it is complete.
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
            String lessSourceOutput = outputFilepath + ".lesssource";
            FileUtil.write(lessSourceOutput, result);
            log.debug("Full Less source from include file {0}, using lessCssJavascript at {1}"
                    + ", is at {2}, output css will be written to {3}",
                    lessIncludeFilepath, lessCssJavascriptUrl.toString(), lessSourceOutput, outputFilepath);
        }
        LessCompiler compiler = new LessCompiler();
        compiler.setLessJs(lessCssJavascriptUrl);
        compiler.setCompress(true);
        File tempOutputFile = new File("tmp"+outputFilepath);
        compiler.compile(lessSource, tempOutputFile);
        tempOutputFile.renameTo(new File(outputFilepath));
    }

    /**
     * Return a String hashcode of the portlet preference values in a repeatable fashion by calculating them based
     * on sorted portlet preference names.  Though hashcode does not guarantee uniqueness, from a practical
     * perspective we'll have so few different values we can reasonably assume preference value
     * combinations will be unique.
     *
     * This calculation process must duplicate computeDefaultHashcode.
     *
     * @param request Portlet request
     * @return Hashcode of portlet preference configuration values.
     */
    @Override
    public String calculateSkinHash(PortletRequest request) {
        int hash = 0;
        PortletPreferences preferences = request.getPreferences();

        // Add the the list of preference names to an ordered list so we can get reliable hashcode calculations.
        Map<String, String[]> prefs = preferences.getMap();
        TreeSet<String> orderedNames = new TreeSet<String>(prefs.keySet());
        Iterator<String> iterator = orderedNames.iterator();
        while (iterator.hasNext()) {
            String preferenceName = iterator.next();
            hash = hash * 31 + preferences.getValue(preferenceName, "").trim().hashCode();
        }
        return Integer.toString(hash);
    }

    /**
     * Return a String hashcode of the values in the lessVariableFile that match the variable names from portlet
     * preferences in a repeatable fashion based on sorted portlet preference names.  Though hashcode does not
     * guarantee uniqueness, from a practical perspective we'll have so few different values we can
     * reasonably assume preference value combinations will be unique.
     *
     * This calculation process must duplicate calculateSkinHash.
     *
     * @param request Portlet request
     * @return Hashcode of portlet preference configuration values.
     */
    @Override
    public String computeDefaultHashcode(PortletRequest request) throws IOException {
        String lessVariableFilename = request.getPortletSession().getPortletContext()
                .getRealPath(lessVariableFile.format(new Object[]{rootFolder}));
        byte[] lessVariablesFileContent = FileUtil.read(lessVariableFilename);
        String lessVariables = new String(lessVariablesFileContent);

        int hash = 0;
        PortletPreferences preferences = request.getPreferences();

        // Add the the list of preference names to an ordered list so we can get reliable hashcode calculations.
        Map<String, String[]> prefs = preferences.getMap();
        TreeSet<String> orderedNames = new TreeSet<String>(prefs.keySet());
        Iterator<String> iterator = orderedNames.iterator();
        // For each preference name, search for it within the less variable file and hash its value.
        while (iterator.hasNext()) {
            String preferenceName = iterator.next();
            String regex = variableRegex.replace("{0}", preferenceName);
            Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            Matcher m = pattern.matcher(lessVariables);
            if (m.find()) {
                hash = hash * 31 + m.group(1).trim().hashCode();
            } else {
                log.warn("Unable to find less variable {} in file {}", preferenceName, lessVariableFilename);
            }
        }
        return Integer.toString(hash);
    }
}
