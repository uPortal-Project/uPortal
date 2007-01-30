/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.sax.SAXTransformerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.SAXReader;
import org.jasig.portal.tools.BuildXsltcTranslets;
import org.springframework.beans.factory.InitializingBean;

public class XsltcTransletManager implements IXsltcTransletManager, InitializingBean {
    
    private static XsltcTransletManager __instance = new XsltcTransletManager();
    
    private final Log log = LogFactory.getLog(XsltcTransletManager.class);
    private boolean activated;
    private boolean debug;
    private String packageName;
    private String namespaceUri;
    private SAXTransformerFactory transformerFactoryImpl;
    private Set blacklist;
    private String deployedDirectory;
    private final Hashtable stylesheetLocks = new Hashtable();
    private final Hashtable stylesheetIncludeMap = new Hashtable();

    private QName[] importTags;
    
    public static XsltcTransletManager getInstance() {
        return __instance;
    }
    
    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getNamespaceUri() {
        return namespaceUri;
    }

    public void setNamespaceUri(String namespaceUri) {
        this.namespaceUri = namespaceUri;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public String getDeployedDirectory() {
        return deployedDirectory;
    }

    public void setDeployedDirectory(String deployedDirectory) {
        this.deployedDirectory = deployedDirectory;
    }

    public void setTransformerFactoryClassName(String transformerFactoryClassName) {
        try {
	        this.transformerFactoryImpl = (SAXTransformerFactory)Class.forName(transformerFactoryClassName).newInstance();
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("SAXTransformerFactory impl class not found: " + transformerFactoryClassName);
        } catch (Exception e) {
            throw new RuntimeException("Error instantiating SAXTransformerFactory impl: " + transformerFactoryClassName, e);
        }
    }
    
    public String getTransformerFactoryClassName() {
        if (transformerFactoryImpl != null) {
	        return transformerFactoryImpl.getClass().getName();
        }
        return null;
    }
    
    public SAXTransformerFactory getTransformerFactoryImpl() {
        return transformerFactoryImpl;
    }    
    
    public void afterPropertiesSet() throws Exception {
        File f = new File(deployedDirectory);
        if (!f.exists()) {
            final String msg =
                "Invalid deployed directory. Directory does not exist: " +
                deployedDirectory;
            throw new IllegalArgumentException(msg);
        }
        
        importTags = new QName[2];
        importTags[0] = new QName("include", new Namespace("xsl", namespaceUri));
        importTags[1] = new QName("import", new Namespace("xsl", namespaceUri));
    }

    public Set getBlacklist() {
        Set s = new HashSet(blacklist.size());
        Iterator itr = blacklist.iterator();
        while (itr.hasNext()) {
            s.add(((Pattern)itr.next()).pattern());
        }
        return s;
    }

    public void setBlacklist(Set blacklistExpressions) {
        blacklist = new HashSet(blacklistExpressions.size());
        Iterator itr = blacklistExpressions.iterator();
        while (itr.hasNext()) {
	        addToBlacklist((String)itr.next());
        }
    }
    
    private boolean resourceExists(File f) {
        if (f != null && f.exists()) {
            return true; 
        }   
        String urlStr = null;
        final String fileSpec = "file:";
        try { 
            urlStr = fileSpec + f.getAbsolutePath();
            return resourceExists(new URL(urlStr));
        } catch (MalformedURLException e) {
            log.error("Failed to create url: " + urlStr, e);
        }   
        return false;
    }   
    
    private boolean resourceExists(URL url) {
        File f = new File(url.getPath());
        if (f != null && f.exists()) {
            return true; 
        }   
        
        try {
            url.openStream().close();
        } catch (IOException ioe) {
            return false;
        }   
        return true;
    }   
    
    public boolean transletExists(String uriStr) {
      try { 
        if (activated && uriStr != null) {
          log.info("Checking existence of " + uriStr);
          URI uri = new URI(uriStr);
          if ("file".equals(uri.getScheme())) {
          
            if (!resourceExists(uri.toURL())) {
                throw new RuntimeException("Stylesheet does not exist: " + uriStr);
            }

            String uriPath = uri.getPath();
            File uriFile = new File(uriPath);
            if (!isStylesheetBlacklisted(uriFile)) {
                int pos1 = uriPath.lastIndexOf('/');
                int pos2 = uriPath.indexOf(".xsl");
                if (pos1 >= 0 && pos2 >= 0) {
                  StringBuffer transletPath = new StringBuffer(uriPath.substring(0,pos1+1));
                  transletPath.append(packageName).append('/');
                  transletPath.append(uriPath.substring(pos1+1, pos2));
                  transletPath.append(".class");
                  File f = new File(transletPath.toString());
                  recompileIfNecessary(uriFile, f);
                  return resourceExists(f);
                }
            }
          }
        }
      } catch (Exception e) {
        log.error("invalid xsl url: " + uriStr, e);
      }
      return false;
    }
    
    private void addToBlacklist(String s) {
        if (log.isDebugEnabled()) {
            log.debug("Adding to blacklist [" + s + "]");
        }
        s = s.replaceAll("\\\\",".").replaceAll("/", ".");
        log.info("Blacklisting: " + s);
        Pattern p = Pattern.compile(s, Pattern.CASE_INSENSITIVE);
        blacklist.add(p);
    }

    private boolean isStylesheetBlacklisted(File f) {
        if (log.isDebugEnabled()) {
            log.debug("Determining if stylesheet of file [" + f + "] is blacklisted.");
        }

        boolean blacklisted = false;
        Iterator itr = blacklist.iterator();
        while (!blacklisted && itr.hasNext()) {
            Pattern p = (Pattern)itr.next();
            Matcher m = p.matcher(getStylesheetIncludeKey(f));
            blacklisted = m.matches();
            if (log.isDebugEnabled()) {
                log.debug("checking " + f.getAbsolutePath() + " against " + p.pattern() + ": " + blacklisted);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Stylesheet of file [" + f + "] blacklisted status: " + blacklisted);
        }

        return blacklisted;
    }

    private void recompileIfNecessary(File stylesheet, File translet) {
        try {
            Object lock = getStylesheetLock(stylesheet);
            synchronized (lock) {
                if (!stylesheetIncludesExists(stylesheet)) {
                    setStylesheetIncludes(stylesheet, new HashSet(), true);
                }
                if (log.isDebugEnabled()) {
                    log.debug("checking if stylesheet is newer: " +
                        stylesheet.getAbsolutePath() +
                        "(" + stylesheet.lastModified() + "), " +
                        translet.getAbsolutePath() +
                        "(" + translet.lastModified() + ")");
                }
                if (!resourceExists(translet) || stylesheet.lastModified() > translet.lastModified()) {
                    if (log.isDebugEnabled()) {
                        log.debug("stylesheet newer, recompiling: " +
                        stylesheet.getAbsolutePath() +
                        "(" + stylesheet.lastModified() + "), " +
                        translet.getAbsolutePath() +
                        "(" + translet.lastModified() + ")");                    }
                    setStylesheetIncludes(stylesheet, new HashSet(), true);
                    try {
                    compileStylesheet(stylesheet);
                    } catch (Exception e) {
                        log.warn("Translet compilation failed. Moving to blacklist: " +
                            stylesheet.getAbsolutePath());
                        addToBlacklist(".*"+getStylesheetIncludeKey(stylesheet));
                    }
                } else {
                    Iterator itr = getStylesheetIncludes(stylesheet).iterator();
                    boolean compiled = false;
                    while (!compiled && itr.hasNext()) {
                        File includedStylesheet = (File)itr.next();
                        if (includedStylesheet == null || !resourceExists(includedStylesheet)) {
                            throw new RuntimeException(
                                "Included stylesheet does not exists: " +
                                stylesheet.getAbsolutePath() + ", " +
                                includedStylesheet.getAbsolutePath());
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("checking if included stylesheet is newer: " +
                                includedStylesheet.getAbsolutePath() +
                                "(" + stylesheet.lastModified() + "), " +
                                translet.getAbsolutePath() +
                                "(" + translet.lastModified() + ")");
                        }
                        if (!resourceExists(translet) || includedStylesheet.lastModified() > translet.lastModified()) {
                            if (log.isDebugEnabled()) {
                                log.debug("stylesheet newer, recompiling: " +
                                stylesheet.getAbsolutePath() +
                                "(" + stylesheet.lastModified() + "), " +                                translet.getAbsolutePath() +
                                "(" + translet.lastModified() + ")");
                            }
                            compileStylesheet(stylesheet);
                            compiled = true;
                        }
                    }
                }
                removeStylesheetLock(stylesheet);
            }
        } catch (Throwable t) {
            removeStylesheetLock(stylesheet);
            throw new RuntimeException(t);
        }
    }

    private void setStylesheetIncludes(File f, Set traversedSet, boolean reset) throws DocumentException {
        String fileKey = getStylesheetIncludeKey(f);
        if (traversedSet.contains(fileKey)) {
            return;
        }

        traversedSet.add(fileKey);

        File parentDir = f.getParentFile();
        Document doc = parseStylesheet(f);
        Element root = doc.getRootElement();
        Set s = getStylesheetIncludes(f);

        if (reset) {
               s.clear();
        }
        StringBuffer sb = null;

        if (log.isDebugEnabled()) {
            sb = new StringBuffer();
            sb.append("reading included stylesheets for: ");
            sb.append(f.getAbsoluteFile());
            sb.append(" : ");
        }

        for (int i=0; i<importTags.length; i++) {
               for ( Iterator itr = root.elementIterator(importTags[i]); itr.hasNext(); ) {
                   Element el = (Element) itr.next();
                   if (log.isDebugEnabled()) {
                      log.debug("TAG: " + el.getName() + ", " + el.getNamespacePrefix() + ", " + el.getNamespaceURI());
                   }
                   File includedFile = new File(parentDir, el.attributeValue("href"));
                   if (includedFile == null || !resourceExists(includedFile)) {
                       throw new RuntimeException(
                           "Included stylesheet does not exist: " +
                           f.getAbsolutePath() + ", " +
                           includedFile.getAbsolutePath());
                   }
                   if (true || log.isDebugEnabled()) {
                       sb.append(includedFile.getAbsoluteFile()).append(", ");
                   }
                   s.add(includedFile);
                setStylesheetIncludes(includedFile, traversedSet, false);
               }
        }

        if (log.isDebugEnabled()) {
            log.debug(sb);
        }

    }

    private Document parseStylesheet(File url)
    throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(url);
        return document;
    }   

    private void compileStylesheet(File f) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Compiling stylesheet of file [" + f + "]");
        }


        if (log.isDebugEnabled()) {
            log.debug("compileStylesheet : " + f.getParent() + ", " +
                deployedDirectory + ", " + packageName);
        }
        BuildXsltcTranslets buildTranslets =
            new BuildXsltcTranslets(f.getParent(), deployedDirectory,
                packageName, false, false);
        buildTranslets.buildFile(f.getParentFile(), f.getName());
    }

    private Set getStylesheetIncludes(File filename) {
        String key = getStylesheetIncludeKey(filename);
        Set s = (Set)stylesheetIncludeMap.get(key);
        if (s == null) {
            s = new HashSet();
            stylesheetIncludeMap.put(key, s);
        }
        return s;
    }

    private boolean stylesheetIncludesExists(File f) {
        return stylesheetIncludeMap.get(getStylesheetIncludeKey(f)) != null;
    }

    private String getStylesheetIncludeKey(File f) {
        return f.getAbsolutePath();
    }

    private synchronized Object getStylesheetLock(File stylesheet) {
        String filename = stylesheet.getAbsolutePath();
        Object lock = stylesheetLocks.get(filename);
        if (lock == null) {
            lock = new Object();
            stylesheetLocks.put(filename, lock);
        }
        return lock;
    }

    private synchronized void removeStylesheetLock(File stylesheet) {
        String filename = stylesheet.getAbsolutePath();
        stylesheetLocks.remove(filename);
    }

    private XsltcTransletManager() {
    }
    
}
