package org.jasig.portal.channels.jsp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.car.CarResources;
import org.jasig.portal.properties.PropertiesManager;


/**
 * Determines if resources in a CAR containing JSPs need to be extracted and
 * placed in a configurable location external to the CAR or to update resources 
 * already there. This is necessary to enable the web server to compile the 
 * JSPs and for the JSPs to be able to access classes and property files 
 * included in the CAR upon which they depend.
 * 
 * @author Mark Boyd
 */
public class Deployer
{
    private static final Log LOG = LogFactory.getLog(Deployer.class);
    public static final String CLASSES_DEPLOY_DIR_PROP
    = "org.jasig.portal.channels.jsp.Deployer.context.relative.classesPath";
    public static final String JSP_DEPLOY_DIR_PROP
    = "org.jasig.portal.channels.jsp.Deployer.context.relative.jspPath";
    private String mCarFilePath;
    private String mClassFilePath;
    private String mClassName;
    private boolean mDeploy;
    private boolean mPurgeOldResources;
    private boolean mDeployedAsCar;

    private static String cClassesDirPath = null;
    private static String cJspDirPath = null;
    
    private File mCarFile = null;

    /**
     * @param classname
     */
    void deployResources(String classname)
        throws PortalException
    {
        this.mClassName = classname;
        determineDeploymentNeeds();

        if (mDeploy)
            extractNewResources();
    }
    /**
     * Passes through the resources found in the CAR containing the class 
     * passed to this deployer and places any contained .jsp or .class files
     * into appropriate directories determined by configuration and tells the 
     * deployment specification this information so that it can update its 
     * references.
     */
    private void extractNewResources() throws PortalException
    {
        getClassesPath();
        getJspPath();
        JarFile jar = getCar();
        List resources = new ArrayList();

        purgePreviousResourcesForCar();

        for (Enumeration entries = jar.entries(); entries.hasMoreElements();)
        {
            ZipEntry z = (ZipEntry) entries.nextElement();
            String resource = z.getName();
            if (resource.endsWith(".jsp") ||
                resource.endsWith(".jspf") ||
                resource.endsWith(".jsf") ||
                resource.endsWith(".class") ||
                resource.endsWith(".properties"))
            {
                extractResource(z, jar);
                resources.add(resource);
            }
        }
        DeploymentSpec.getInstance().addDeploymentFor(mCarFilePath, resources);
    }
    
    /**
     * Removes old entries for the CAR being deployed if any and returns the
     * corresponding property referring to the car's info.
     * 
     * @return
     * @throws PortalException
     */
    private void purgePreviousResourcesForCar() throws PortalException
    {
        // first remove from the deployment spec
        List oldResources = DeploymentSpec.getInstance().removeEntriesFor(
                mCarFilePath);

        // now purge file paths and the files at those destinations as needed
        if (mPurgeOldResources)
        {
            for (Iterator itr = oldResources.iterator(); itr.hasNext();)
            {
                String resource = (String) itr.next();
                String filePath = mClassFilePath + "/" + resource;

                File theFile = new File(filePath);
                if (theFile.exists())
                    theFile.delete();
            }
        }
    }

    /**
     * Writes the bytes for the jar file zip entry out of the jar file and 
     * into a file in the configurable classpath accessible destination area.
     * 
     * @param z
     */
    private void extractResource(ZipEntry z, JarFile jar)
        throws PortalException
    {
        File destination = getOutputFileFor(z);
        OutputStream out = null;
            try
        {
            if (LOG.isDebugEnabled())
                LOG.debug("Attempting to open '"
                    + destination.getAbsolutePath()
                    + "' for extracting a CAR resource.");
            out = new FileOutputStream(destination);
        }
        catch (Exception e)
        {
            throw new PortalException(
                "Unable to open file '" + destination.getAbsolutePath() + "'.",
                e);
        }
        byte[] bytes = new byte[4096];
        int bytesRead;

        try
        {
            InputStream in = jar.getInputStream(z);
            bytesRead = in.read(bytes);
            while (bytesRead != -1)
            {
                out.write(bytes, 0, bytesRead);
                bytesRead = in.read(bytes);
            }
            in.close();
            out.flush();
            out.close();
        }
        catch (IOException e)
        {
            throw new PortalException(
                "Unable to extract content of '"
                    + z.getName()
                    + "' in CAR '"
                    + jar.getName()
                    + "'.",
                e);
        }
    }

    /**
     * @param z
     */
    private File getOutputFileFor(ZipEntry z)
        throws PortalException
    {
        int lastSeparator = z.getName().lastIndexOf('/');
        String fullName = "";
        String strDirPath = cClassesDirPath;
        
        if (z.getName().endsWith(".jsp")||
            z.getName().endsWith(".jspf")||
            z.getName().endsWith(".jsf"))
        {
            strDirPath = cJspDirPath;
        }
            
        fullName = strDirPath + "/" + z.getName();
        
        String filePath = null;

        if (lastSeparator == -1) // no path only filename
        {
            filePath = strDirPath;
        }
        else // strip off terminating filename
        {
            filePath = 
                strDirPath + "/" + z.getName().substring(0, lastSeparator);                
        }
        
        File dirPath = new File(filePath);
        if (! dirPath.exists())
            dirPath.mkdirs();
            
        try
        {
            if (LOG.isDebugEnabled())
                LOG.debug("Attempting to open '"
                    + fullName
                    + "' for extracting a CAR resource.");
            return new File(fullName);
        }
        catch (Exception e)
        {
            throw new PortalException(
                "Unable to open file '" + filePath + "'.",
                e);
        }
    }

    /**
     * Returns a JarFile object representing the CAR that contains the class
     * file passed into this deployer for deployment.
     * 
    * @return JarFile representing the CAR containing the class file.
    */
    private JarFile getCar() throws PortalException
    {
        File car = getCarFile();
        String carFileFullPath = car.getAbsolutePath();
        JarFile jar;
        try
        {
            jar = new JarFile(car);
        }
        catch (IOException e)
        {
            throw new PortalException(
                "Unable to open CAR '" + carFileFullPath + "'." + e);
        }
        return jar;
    }

    /**
     * Returns a File object representing the CAR file containing the 
     * controller class.
     * @return
     * @throws PortalException
     */
    private File getCarFile() throws PortalException
    {
        if (mCarFile == null)
        {
            CarResources cRes = CarResources.getInstance();
            URL classUrl = cRes.findResource(mClassFilePath);
            if (classUrl == null)
                throw new PortalException(
                        "Unable to locate CAR containing compiled " +
                        "controller class file '" + mClassFilePath + "'.");
            String carPath = classUrl.toExternalForm();
            carPath = carPath.substring("jar:file:".length());
            carPath = carPath.substring(0, carPath.indexOf('!'));
            mCarFile = new File(carPath);

            if (!mCarFile.exists())
                throw new PortalException("Unable to locate CAR '" + carPath
                        + "'. Resources can't be deployed.");
        }
        return mCarFile;
    }
    
    private String getClassesPath() throws PortalException
    {
        if (cClassesDirPath == null)
            cClassesDirPath = getRealPath(CLASSES_DEPLOY_DIR_PROP);

        return cClassesDirPath;
    }
    
    private String getRealPath(String relativePath)
    {
        ServletContext ctx =
            PortalSessionManager.getInstance().getServletContext();
        String ctxRelativePath 
            = PropertiesManager.getProperty(relativePath, "/WEB-INF/classes");
        if (ctxRelativePath.endsWith("/") || ctxRelativePath.endsWith("\\"))
            ctxRelativePath = ctxRelativePath.substring(0, ctxRelativePath
                    .length() - 1);
        String realPath = ctx.getRealPath(ctxRelativePath);
        if (realPath == null)
            throw new PortalException(
                "Unable to locate directory " + ctxRelativePath);
        return realPath;
    }
    
    /**
     * Returns the path to which jsps are deployed.  NOTE:
     * They are currently deployed in the same location as the
     * rest of the car's resources, but this may change.
     * 
     * @return The path for deploying the jsp files.
     * @throws PortalException
     */
    private String getJspPath() throws PortalException
    {
        if (cJspDirPath == null)
            cJspDirPath = getRealPath(JSP_DEPLOY_DIR_PROP);

        return cJspDirPath;
    }
    
    /**
     * @param classname
     * @return
     */
    private void determineDeploymentNeeds() throws PortalException
    {
        mClassFilePath = mClassName.replace('.', '/') + ".class";
        CarResources cRes = CarResources.getInstance();

        mCarFilePath = cRes.getContainingCarPath(mClassFilePath);
        boolean classInCar = mCarFilePath != null;
        boolean classInDir = classInDir();
        boolean depldInDir = DeploymentSpec.getInstance()
                .isDeployInfoAvailableFor(mClassFilePath);
        boolean carIsNewer = depldInDir && isCarNewer();

        if (LOG.isDebugEnabled())
            LOG.debug("classInCar: " + classInCar +
                      " classInDir: " + classInDir +
                      " depldInDir: " + depldInDir +
                      " carIsNewer: " + carIsNewer );
        
        if (!classInDir && classInCar && !depldInDir)
        {
            mDeployedAsCar = true;
            mPurgeOldResources = false;
            mDeploy = true;
        }
        else if (classInDir && !classInCar && !depldInDir)
        {
            mDeployedAsCar = false;
            mPurgeOldResources = false;
            mDeploy = false;
        }
        else if (classInDir && classInCar && !depldInDir)
        {
            mDeployedAsCar = true;
            mPurgeOldResources = false;
            mDeploy = true;
        }
        else if (
            (!classInDir && classInCar && depldInDir)
                || (classInDir && !classInCar && depldInDir && carIsNewer)
                || (classInDir && classInCar && depldInDir && carIsNewer)
                || (!classInDir && classInCar && depldInDir && !carIsNewer))
        {
            mDeployedAsCar = true;
            mPurgeOldResources = true;
            mDeploy = true;
        }
        else if ( classInDir && classInCar && depldInDir && !carIsNewer )
        {
            mDeployedAsCar = true;
            mPurgeOldResources = false;
            mDeploy = false;
        }
    }

    /**
     * Determines if the resource identified by the passed in classFilePath, 
     * a value like <code>a/b/c/some.class</code>, is found on the classpath
     * in a directory or within a Jar file. If found within a Jar the URL
     * returned will be in Jar URL format like <code>jar:file:/D:/...</code>.
     * If located as a regular file in a directory somewhere it will have a
     * file URL format like <code>file:/D:/...</code>. It is important not
     * to instantiate the class to make this 
     * determination since we don't want the class instantiated until its new
     * version has been extracted from the CAR if needed. Once instantiated 
     * by loading from one location by a classloader it will not be loaded 
     * again without replacing that class loader instance.
     * 
     * @param classFilePath
     * @return
     */
    private boolean classInDir() throws PortalException
    {
        CarResources cRes = CarResources.getInstance();
        ClassLoader cLdr = cRes.getClassLoader();

        URL classUrl = cLdr.getResource(mClassFilePath);

        // if null then not in CAR or on classpath
        if (classUrl == null)
            throw new PortalException(
                "Class file, '" + mClassFilePath + "' not found on classpath.");

        String classUrlPath = classUrl.toExternalForm();
        if (classUrlPath.startsWith("file:"))
        {
            return true;
        }
        return false;
    }

    /**
     * Determines if the timestamp on the CAR from which deployment may have
     * taken place is later than when deployment took place as defined in
     * the deployment info file. If later, then redployment is needed since a 
     * new CAR appears to have been installed.
     * 
     * @return
     */
    private boolean isCarNewer() throws PortalException
    {
        long depDate = DeploymentSpec.getInstance()
                .getTimeOfDeploymentForResource(mClassFilePath);
        File car = getCarFile();
        long carModDate = car.lastModified();
        return carModDate > depDate;
    }

    /**
     * @return
     */
    boolean isDeployedInCar()
    {
        return mDeployedAsCar;
    }
}
