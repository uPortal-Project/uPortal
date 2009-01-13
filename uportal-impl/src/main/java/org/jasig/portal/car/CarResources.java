/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.car;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ServletContextAware;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Provides access to resources stored in channel archive files or CARs for
 * short. 
 * @author Mark Boyd  {@link <a href="mailto:mark.boyd@engineer.com">mark.boyd@engineer.com</a>}
 * @version $Revision$
 */
public class CarResources implements ServletContextAware, InitializingBean {

    // static, class variables
    private static final Log log = LogFactory.getLog(CarResources.class);
    private static CarResources instance = null;
    private static CarClassLoader loader = null;

    public final static String RCS_ID = "@(#) $Header$";

    static final String DEPLOYMENT_DESCRIPTOR = "META-INF/comp.xml";
    private static final String WELL_KNOWN_DIR = "/WEB-INF/cars";
    private static final String CAR_DIR_PROP_NAME = "org.jasig.portal.car.CarResources.directory";

    public static final String CAR_WORKER_ID = "carRsrc";
    public static final String CAR_RESOURCE_PARM = CAR_WORKER_ID;

    // instance variables
    private ServletContext servletContext;

    private final Map<String, JarFile> resourceJars = new Hashtable<String, JarFile>();
    private final Map<JarFile, File> carsByJars = new Hashtable<JarFile, File>();
    private final Map<File, List<String>> carContents = new Hashtable<File, List<String>>();
    private final Map<String, File> carsByPath = new Hashtable<String, File>();

    private final SAX2BufferImpl services = new SAX2BufferImpl();
    private final Properties workers = new Properties();
    private boolean carsLoaded = false;

    private final List<JarFile> jarsWithDescriptors = new Vector<JarFile>();

    private String carDirPath = null;

    /**
       A fileFilter for obtaining a list of CARs.
     */
    private final FileFilter carFilter = new FileFilter() {
        public boolean accept(File path) {
            return path.getName().endsWith(".car");
        }
    };

    /**
       A fileFilter for obtaining a list of directories.
     */
    private final FileFilter dirFilter = new FileFilter() {
        public boolean accept(File file) {
            return file.isDirectory();
        }
    };

    /**
       Instantiate a CarResources object and load information about all CARs
       and their contained resources.
     */
    public void afterPropertiesSet() throws Exception {
        loader = new CarClassLoader(this);
        this.processDescriptors();
        
        try {
            this.loadCars();
        }
        catch (final Exception e) {
            log.error("An Exception occurred while loading channel archives. Any channels "
                    + "deployed via CARs will not be available.", e);
        }
        
        synchronized (CarResources.class) {
            instance = this;
            CarResources.class.notifyAll();
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Process the descriptors of the channel archives if any.
     */
    private void processDescriptors() {
        if (this.carsLoaded == true) {
            for (final JarFile jarFile : this.jarsWithDescriptors) {
                try {
                    final DescriptorHandler handler = new DescriptorHandler(jarFile);
                    handler.getWorkers(this.workers);
                    handler.getServices(this.services);
                }
                catch (final Exception e) {
                    log.error("An Exception occurred while processing deployment descriptor "
                            + DEPLOYMENT_DESCRIPTOR + " in " + jarFile.getName() + ". ", e);
                }
            }
        }
    }

    /**
     * Return the single instance of CarResources.
     * @deprecated Use the Spring managed 'carResources' bean instead
     */
    @Deprecated
    public static CarResources getInstance() {
        if (instance == null) {
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            instance = (CarResources)applicationContext.getBean("carResources", CarResources.class);
        }
        
        return instance;
    }

    /**
       Return the single instance of CarClassLoader.
     */
    public ClassLoader getClassLoader() {
        return loader;
    }

    /**
       Return a File object representing the well-known channel archive base
       directory '/WEB-INF/cars' where channel archives are located.
     */
    private File getWellKnownDir() {

        if (this.servletContext == null) {
            log.error("Channel Archives will not be  loaded. Unable to aquire the real path to '"
                    + WELL_KNOWN_DIR + "' due to no ServletCotnext being available. Alternatively, "
                    + "you can specify a fully qualified path as the value of a '"
                    + CAR_DIR_PROP_NAME + "' property in portal.properties.");
            return null;
        }

        final String carDirRealPath = this.servletContext.getRealPath(WELL_KNOWN_DIR);
        
        if (carDirRealPath == null) {
            log.error("Channel Archives will not be  loaded. Unable to aquire the real path to '"
                    + WELL_KNOWN_DIR + "'. This can occur if the portal is deployed "
                    + "as a WAR and directories can not be created within its directory "
                    + "structure. Alternatively, you can specify a fully qualified path as the value of a '"
                    + CAR_DIR_PROP_NAME + "' property in portal.properties.");
            return null;
        }

        final File carDir = new File(carDirRealPath);

        if (!carDir.exists()) {
            if (log.isInfoEnabled()) {
                log.info("Channel Archives can not be  loaded. CAR directory '" + carDirRealPath
                        + "' does not exist.");
            }
            return null;
        }
        this.carDirPath = carDirRealPath;
        return carDir;
    }

    /**
       Return a File object representing the channel archive base
       directory whose fully-qualified path is specified by the
       'org.jasig.portal.car.CarResources.directory' property in
       portal.properties.
     */
    private File getPropertySpecifiedDir() {
        String carDirPath = null;
        File carDir = null;

        try {
            carDirPath = PropertiesManager.getProperty(CAR_DIR_PROP_NAME);
            carDir = new File(carDirPath);
        }
        catch (final RuntimeException re) {
            if (log.isInfoEnabled()) {
                log.info("CAR directory property '" + CAR_DIR_PROP_NAME + "' not specified. Defaulting to "
                        + "well-known directory '" + WELL_KNOWN_DIR + "'.");
            }
            return null;
        }

        if (!carDir.exists()) {
            log.error("CAR directory '" + carDirPath + "' specified by property '" + CAR_DIR_PROP_NAME
                    + "' does not exist. Channel Archives can not be loaded from this directory.");
            return null;
        }
        this.carDirPath = carDirPath;
        return carDir;
    }

    /**
       Load information about all installed CARs and their contained resources.
     */
    private void loadCars() {
        File carDir = this.getPropertySpecifiedDir();

        if (carDir == null) {
            carDir = this.getWellKnownDir();
        }

        if (carDir != null) {
            this.scanDir(carDir);
            if (log.isInfoEnabled()) {
                log.info("Channel Archives Loaded: " + this.carsByPath.size() + " from '" + this.carDirPath + "'");
            }
        }
        this.carsLoaded = true;
    }

    /**
       Scan the passed in directory loading any cars there-in and calling
       this method for any nested directories.
     */
    private void scanDir(File dir) {

        // first get all of the cars in this directory
        final File[] cars = dir.listFiles(this.carFilter);

        if (cars != null && cars.length != 0) {
            for (final File car : cars) {
                this.loadCarEntries(car);
            }
        }

        // now get all of the sub-directories to be scanned
        final File[] dirs = dir.listFiles(this.dirFilter);

        if (dirs != null && dirs.length != 0) {
            for (final File dir2 : dirs) {
                this.scanDir(dir2);
            }
        }
    }

    /**
       Load information about the passed in CAR and any contained resources.
     */
    private void loadCarEntries(File car) {
        JarFile jar = null;

        try {
            jar = new JarFile(car);
        }
        catch (final IOException ioe) {
            log.error("CAR " + this.getCarPath(car) + " could not be loaded. Details: "
                    + (ioe.getMessage() != null ? ioe.getMessage() : ioe.getClass().getName()), ioe);
            return;
        }
        final Vector<String> entryList = new Vector<String>();
        this.carsByJars.put(jar, car);
        this.carsByPath.put(this.getCarPath(car), car);

        final Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();

            if (!entry.isDirectory()) {
                final String name = entry.getName();

                if (name.equals(DEPLOYMENT_DESCRIPTOR)) {
                    this.jarsWithDescriptors.add(jar);
                }
                else {
                    // add to map of which jar holds this resource
                    this.resourceJars.put(name, jar);
                }

                // add to list of contents for this car
                entryList.add(name);
            }
        }
        this.carContents.put(car, entryList);
    }

    /**
       Push into the passed in properties object workers defined 
       in any component archive's deployment descriptor.
     */
    public void getWorkers(Properties workers) {
        for (final Object element : this.workers.entrySet()) {
            final Map.Entry<Object, Object> entry = (Entry<Object, Object>) element;
            if (!workers.containsKey(entry.getKey())) {
                workers.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
       Returns true if any archive included a deployment descriptor.
     */
    public boolean hasDescriptors() {
        return this.jarsWithDescriptors.size() > 0;
    }

    /**
       Push into the passed in content handler events for any services declared
       in any component archive's deployment descriptor.
     */
    public void getServices(ContentHandler contentHandler) throws SAXException {
        this.services.outputBuffer(contentHandler);
    }

    /**
       Return an input stream for reading the raw bytes making up the resource
       contained in one of the installed CARs. Returns null if the resource
       is not found.

     */
    public InputStream getResourceAsStream(String resource) throws PortalException {
        final JarFile jar = this.resourceJars.get(resource);

        if (jar == null) {
            return null;
        }

        final ZipEntry entry = jar.getEntry(resource);

        if (entry == null) {
            return null;
        }

        try {
            return jar.getInputStream(entry);
        }
        catch (final IOException ioe) {
            throw new PortalException("Unable to get input stream for " + resource);
        }
    }

    /**
       Return the size of the indicated resource or -1 if the resource is not
       found or its size is unknown.
     */
    public long getResourceSize(String resource) {
        final JarFile jar = this.resourceJars.get(resource);

        if (jar == null) {
            return -1;
        }

        final ZipEntry entry = jar.getEntry(resource);

        if (entry == null) {
            return -1;
        }
        return entry.getSize();
    }

    /**
       Returns a URL to the requested entry if found in one of the installed
       CARs or null if not found.
     */
    public URL findResource(String entry) {
        if (entry == null) {
            return null;
        }

        // resolve entries that refer to a parent directory
        // using a regular expression.
        entry = this.resolveRegExpr(entry);

        final JarFile jar = this.resourceJars.get(entry);
        if (jar == null) {
            return null;
        }
        final File carFile = this.carsByJars.get(jar);
        if (carFile == null) {
            return null;
        }
        final String url = "jar:file:" + carFile.getAbsolutePath() + "!/" + entry;
        try {
            return new URL(url);
        }
        catch (final java.net.MalformedURLException me) {
        }
        return null;
    }

    /**
       Returns the path of the CAR containing the indicated resource. This
       path is relative to the CAR directory configured via the property in
       portal.properties. If a CAR for that entry is not found it
       returns null.
     */
    public String getContainingCarPath(String entry) {
        if (entry == null) {
            return null;
        }
        final JarFile jar = this.resourceJars.get(entry);
        if (jar == null) {
            return null;
        }
        final File carFile = this.carsByJars.get(jar);
        if (carFile == null) {
            return null;
        }
        return this.getCarPath(carFile);
    }

    /**
       Returns true if the indicated resource is available, false otherwise.
       The resource is identified by its complete path within the CAR file.
     */
    public boolean containsResource(String resource) {
        return this.resourceJars.containsKey(resource);
    }

    /**
       Returns a String array of car file paths relative to the car directory
       specified via the property in portal.properties.
     */
    public String[] listCars() {

        final File[] carFiles = this.carsByJars.values().toArray(new File[this.carsByJars.size()]);
        final String[] carNames = new String[carFiles.length];

        for (int i = 0; i < carFiles.length; i++) {
            carNames[i] = this.getCarPath(carFiles[i]);
        }
        return carNames;
    }

    /**
       Returns a list of resources available in the car identified by the
       passed in relative car file path name. This name is the path to the
       car file relative to the car directory. If no car file is found for
       the passed-in path then null is returned.
     */
    public String[] listCarResources(String carPath) {
        final File car = this.carsByPath.get(carPath);
        if (car == null) {
            return null;
        }

        final List<String> contents = this.carContents.get(car);

        if (contents == null) {
            return null; // should never happen
        }

        return contents.toArray(new String[contents.size()]);
    }

    /**
       Return the path of a car file relative to the car directory.
     */
    private String getCarPath(File car) {
        final String carPath = car.getAbsolutePath();
        return carPath.substring(this.carDirPath.length() + 1);
    }

    /**
       Returns an enumeration of String objects each containing the path of a
       resource available from the installed CARs.
     */
    public String[] listAllResources() {
        return this.resourceJars.keySet().toArray(new String[resourceJars.size()]);
    }

    /**
     * Home-grown version of the String replace method.  This one replaces
     * the supplied String (generally a regular expression '../') with the
     * supplied replacement.  It returns the original String as is if no
     * matches were found or a modified version of it if matches were found.
     *
     * @param entry  the String to search for the regExpr.
     * @param regExpr the regular expression to find and replace
     * @param replacement the String to replace the regExpr with
     * @return A modified String of match(es) were found, otherwise the
     *         original String unmodified.
     **/
    private String replace(String entry, String regExpr, String replacement) {
        String copy = entry;
        final int beginIdx = 0;
        int endIdx = copy.indexOf(regExpr);
        final StringBuffer buff = new StringBuffer();

        while (endIdx != -1) {
            // grab portion of the copied string up to the
            // reg expr.
            final String newStr = copy.substring(beginIdx, endIdx);

            // replace original version of copy(ed) string with
            // only a substring from the reg expr (+3 for reg expr
            // length) to the end of the string
            copy = copy.substring(endIdx + 3, copy.length());

            // append the string taken up to the reg expr to the
            // buffer and add a replacement character to replace
            // the reg expr.
            buff.append(newStr).append(replacement);

            // see if another reg expr exists in the remaining
            // copy(ed) string.
            endIdx = copy.indexOf(regExpr);

            // if there are no more reg expr in the copy(ed) string,
            // append the copy and call it good.
            if (endIdx == -1) {
                buff.append(copy);
            }
        }

        // if there was a reg expr in the original entry, then the
        // buffer wouldn't be 0 length.
        if (buff.toString().length() > 0) {
            entry = buff.toString();
        }

        if (log.isDebugEnabled()) {
            log.debug("CarResources replace() - returned entry is: " + entry);
        }
        return entry;
    }

    /**
     * Resolves the String entry and removes any regular expression
     * patterns that would indicate a directory move (i.e. '../').
     * The returned String is the supplied 'entry' String minus the
     * '../' pattern and the directory directly preceding it if any.
     *
     * @param entry the String entry to resolve
     * @return the modified String minus the reg expr.
     **/
    private String resolveRegExpr(String entry) {
        // first it's necessary to replace any reg expr '../'
        // with a different character, in this case a '~'.
        // this allows the StringTokenizer to parse the
        // entry into the appropriate tokens.
        final String replacement = "~";
        entry = this.replace(entry, "../", replacement);

        // now the real fun starts.  If the entry had been modified,
        // (i.e. had a reg expr), then it will now be tokenized so
        // that a new String can be constructed.
        if (entry.indexOf(replacement) != -1) {
            final String delim = "/";
            final StringBuffer sb = new StringBuffer();

            if (log.isDebugEnabled()) {
                log.debug("CarResources resolveRegExpr() -  Parsing resource name: " + entry);
            }

            final StringTokenizer st = new StringTokenizer(entry, replacement);
            final int tokens = st.countTokens();
            int count = 1;

            while (st.hasMoreTokens()) {
                // parse each token separately to correctly climb back
                // up a directory
                final String token = st.nextToken();

                if (log.isDebugEnabled()) {
                    log.debug("CarResources resolveRegExpr() - Token is now: " + token);
                }

                final StringTokenizer st1 = new StringTokenizer(token, delim);
                final int childTokens = st1.countTokens();
                int childCount = 1;

                while (st1.hasMoreTokens()) {
                    final String childToken = st1.nextToken();

                    if (log.isDebugEnabled()) {
                        log.debug("CarResources resolveRegExpr() - Child token is: " + childToken);
                    }

                    // if there are more child tokens, then add the most
                    // recent one to the buffer along with the delimiter
                    if (childCount < childTokens) {
                        sb.append(childToken);
                        sb.append(delim);
                    }
                    else if (count == tokens) {
                        // if the original entry began with '../', like
                        // ( ../somedir ),
                        // then this would basically remove the ../ and
                        // return the rest of the string unchanged.
                        sb.append(childToken);
                    }
                    else {
                        // ignore last token
                        break;
                    }
                    childCount++;
                }
                count++;
            }
            entry = sb.toString();
        }

        if (log.isDebugEnabled()) {
            log.debug("CarResources resolveRegExpr() - resolved entry is: " + entry);
        }
        return entry;
    }
}
