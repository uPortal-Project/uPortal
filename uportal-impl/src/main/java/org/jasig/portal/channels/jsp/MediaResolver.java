package org.jasig.portal.channels.jsp;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import org.jasig.portal.BrowserInfo;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.PortalException;
import org.jasig.portal.UPFileSpec;

/**
 * Handles resolving base media URL for jsp channel types since the controller
 * class can't be used to tell if the channel was loaded via CAR or 
 * traditionally since it is extracted and residing in WEB-INF/classes so that
 * it and its other objects can be accessible to JSPs deployed in the CAR.
 * Therefore, its classloader won't be the CarClassloader.
 * 
 * @author Mark Boyd
 *
 */
public class MediaResolver extends ChannelRuntimeData
{
    private ChannelRuntimeData runtimedata;
    
    MediaResolver(ChannelRuntimeData rd)
    {
        this.runtimedata = rd;
    }
    public String getBaseMediaURL(Class aChannelClass) throws PortalException
    {
        String resource = aChannelClass.getName().replace('.', '/') + ".class";
        DeploymentSpec spec = DeploymentSpec.getInstance();
        
        if (spec.isDeployInfoAvailableFor(resource))
            return getBaseMediaURL(ChannelRuntimeData.CAR_BASE);
        
        return super.getBaseMediaURL(aChannelClass);
    }
    public String getBaseMediaURL(Object aChannelObject) throws PortalException
    {
        return getBaseMediaURL(aChannelObject.getClass());
    }
    public String getBaseMediaURL(String resourcePath) throws PortalException
    {
        DeploymentSpec spec = DeploymentSpec.getInstance();
        
        if (spec.isDeployInfoAvailableFor(resourcePath))
            return getBaseMediaURL(ChannelRuntimeData.CAR_BASE);
        
        return super.getBaseMediaURL(resourcePath);
    }
    
    public Object clone()
    {
        super.clone();
        
        return runtimedata.clone();
    }
    public String getBaseActionURL()
    {
        return runtimedata.getBaseActionURL();
    }
    public String getBaseActionURL(boolean idempotent)
    {
        return runtimedata.getBaseActionURL(idempotent);
    }
    public String getBaseWorkerURL(String worker, boolean idempotent)
            throws PortalException
    {
        return runtimedata.getBaseWorkerURL(worker, idempotent);
    }
    public String getBaseWorkerURL(String worker)
    {
        return runtimedata.getBaseWorkerURL(worker);
    }
    public BrowserInfo getBrowserInfo()
    {
        return runtimedata.getBrowserInfo();
    }
    public String getFnameActionURL(String fname)
    {
        return runtimedata.getFnameActionURL(fname);
    }
    public String getHttpRequestMethod()
    {
        return runtimedata.getHttpRequestMethod();
    }
    public String getKeywords()
    {
        return runtimedata.getKeywords();
    }
    public Locale[] getLocales()
    {
        return runtimedata.getLocales();
    }
    public Object getObjectParameter(String pName)
    {
        return runtimedata.getObjectParameter(pName);
    }
    public Object[] getObjectParameterValues(String pName)
    {
        return runtimedata.getObjectParameterValues(pName);
    }
    public String getParameter(String pName)
    {
        return runtimedata.getParameter(pName);
    }
    public Enumeration getParameterNames()
    {
        return runtimedata.getParameterNames();
    }
    public Map getParameters()
    {
        return runtimedata.getParameters();
    }
    public String[] getParameterValues(String pName)
    {
        return runtimedata.getParameterValues(pName);
    }
    public String getRemoteAddress()
    {
        return runtimedata.getRemoteAddress();
    }
    public UPFileSpec getUPFile()
    {
        return runtimedata.getUPFile();
    }
    public boolean isRenderingAsRoot()
    {
        return runtimedata.isRenderingAsRoot();
    }
    public boolean isTargeted()
    {
        return runtimedata.isTargeted();
    }
    public void setBaseActionURL(String baseActionURL)
    {
        runtimedata.setBaseActionURL(baseActionURL);
    }
    public void setBrowserInfo(BrowserInfo bi)
    {
        runtimedata.setBrowserInfo(bi);
    }
    public void setHttpRequestMethod(String method)
    {
        runtimedata.setHttpRequestMethod(method);
    }
    public void setKeywords(String keywords)
    {
        runtimedata.setKeywords(keywords);
    }
    public void setLocales(Locale[] locales)
    {
        runtimedata.setLocales(locales);
    }
    public void setParameter(String pName, String value)
    {
        runtimedata.setParameter(pName, value);
    }
    public void setParameters(Map params)
    {
        runtimedata.setParameters(params);
    }
    public void setParametersSingleValued(Map params)
    {
        runtimedata.setParametersSingleValued(params);
    }
    public String[] setParameterValues(String pName, String[] values)
    {
        return runtimedata.setParameterValues(pName, values);
    }
    public void setRemoteAddress(String string)
    {
        runtimedata.setRemoteAddress(string);
    }
    public void setRenderingAsRoot(boolean rar)
    {
        runtimedata.setRenderingAsRoot(rar);
    }
    public void setTargeted(boolean targeted)
    {
        runtimedata.setTargeted(targeted);
    }
    public void setUPFile(UPFileSpec upfs)
    {
        runtimedata.setUPFile(upfs);
    }
    public String toString()
    {
        return runtimedata.toString();
    }
}
