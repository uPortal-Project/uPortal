/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import org.jasig.portal.car.CarResources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.oreilly.servlet.multipart.Part;

/**
 * A set of runtime data accessible by a channel.
 *
 * @author <a href="mailto:pkharchenko@unicon.net">Peter Kharchenko</a>
 * @version $Revision$
 */
public class ChannelRuntimeData extends Hashtable implements Cloneable {
    
    private static final Log log = LogFactory.getLog(ChannelRuntimeData.class);
    
    private BrowserInfo binfo=null;
    private Locale[] locales = null;
    private UPFileSpec channelUPFile;
    private String baseActionURL = null;
    private String httpRequestMethod=null;
    private String remoteAddress=null;
    private String keywords=null;
    private boolean renderingAsRoot=false;
    private boolean targeted = false;
    private static final String TRADITIONAL_MEDIA_BASE = "media/";
    public static final String CAR_BASE = "/CAR_BASE";
    public static final String WEB_APP_BASE = null;

    /**
     * Default empty constructor
     */
    public ChannelRuntimeData() {
      super();
      channelUPFile = new UPFileSpec();
    }

    /**
     * Create a new instance of ourself
     * Used by the CError channel
     * @return crd the cloned ChannelRuntimeData object
     */
    public Object clone() {
      ChannelRuntimeData crd = new ChannelRuntimeData();
      crd.binfo = binfo;
      crd.locales = locales;
      crd.channelUPFile = channelUPFile;
      crd.baseActionURL = baseActionURL;
      crd.httpRequestMethod = httpRequestMethod;
      crd.keywords = keywords;
      crd.renderingAsRoot = renderingAsRoot;
      crd.targeted = targeted;
      crd.putAll(this);
      return crd;
    }

    /**
     * Set a UPFileSpec which will be used to produce
     * baseActionURL and workerActionURL.
     * @param upfs the UPFileSpec
     */
    public void setUPFile(UPFileSpec upfs) {
        channelUPFile = upfs;
    }

    /**
     * Get the UPFileSpec
     * @return channelUPFile the UPFileSpec
     */
    public UPFileSpec getUPFile() {
        return this.channelUPFile;
    }

    /**
     * Set the HTTP Request method.
     *
     * @param method a <code>String</code> value
     */
    public void setHttpRequestMethod(String method) {
        this.httpRequestMethod=method;
    }

    /**
     * Get HTTP request method (i.e. GET, POST)
     *
     * @return a <code>String</code> value
     */
    public String getHttpRequestMethod() {
        return this.httpRequestMethod;
    }

    /**
     * Sets the base action URL.  This was added back in for the benefit
     * of web services.  Not sure if it is going to stay this way.
     * @param baseActionURL the base action URL
     */
    public void setBaseActionURL(String baseActionURL) {
        this.baseActionURL = baseActionURL;
    }

    /**
     * Sets whether or not the channel is rendering as the root of the layout.
     * @param rar <code>true</code> if channel is rendering as the root, otherwise <code>false</code>
     */
    public void setRenderingAsRoot(boolean rar) {
        renderingAsRoot = rar;
    }

    /**
     * Sets whether or not the channel is currently targeted.  A channel is targeted
     * if an incoming request specifies the channel's subscribe ID as the targeted node ID.
     * @param targeted <code>true</code> if channel is targeted, otherwise <code>false</code>
     */
    public void setTargeted(boolean targeted) {
        this.targeted = targeted;
    }

    /**
     * Setter method for browser info object.
     *
     * @param bi a browser info associated with the current request
     */
    public void setBrowserInfo(BrowserInfo bi) {
        this.binfo = bi;
    }

    /**
     * Provides information about a user-agent associated with the current request/response.
     *
     * @return a <code>BrowserInfo</code> object ecapsulating various user-agent information.
     */
    public BrowserInfo getBrowserInfo() {
        return  binfo;
    }

    /**
     * Setter method for array of locales. A channel should
     * make an effort to render itself according to the
     * order of the locales in this array.
     * @param locales an ordered list of locales
     */
    public void setLocales(Locale[] locales) {
        this.locales = locales;
    }

    /**
     * Accessor method for ordered set of locales.
     * @return locales an ordered list of locales
     */
    public Locale[] getLocales() {
        return locales;
    }

    /**
     * A convenience method for setting a whole set of parameters at once.
     * The values in the Map must be object arrays. If (name, value[]) is in
     * the Map, then a future call to getParameter(name) will return value[0].
     * @param params a <code>Map</code> of parameter names to parameter value arrays.
     */
    public void setParameters(Map params) {
      this.putAll(params); // copy a Map
    }

    /**
     * A convenience method for setting a whole set of parameters at once.
     * The Map should contain name-value pairs.  The name should be a String
     * and the value should be either a String or a Part.
     * If (name, value) is in the Map then a future call to getParameter(name)
     * will return value.
     * @param params a <code>Map</code> of parameter names to parameter value arrays.
     */
    public void setParametersSingleValued(Map params) {
        if (params != null) {
            java.util.Iterator iter = params.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String)iter.next();
                Object value = params.get(key);
                if (value instanceof String)
                    setParameter(key, (String)value);
                else if (value instanceof Part)
                    setParameter(key, (Part)value);
            }
        }
    }

    /**
     * Sets multi-valued parameter.
     *
     * @param pName parameter name
     * @param values an array of parameter values
     * @return an array of parameter values
     */
    public String[] setParameterValues(String pName, String[] values) {
        return  (String[])super.put(pName, values);
    }

    /**
     * Establish a parameter name-value pair.
     *
     * @param pName parameter name
     * @param value parameter value
     */
    public  void setParameter(String pName, String value) {
        String[] valueArray = new String[1];
        valueArray[0] = value;
        super.put(pName, valueArray);
    }

    public  com.oreilly.servlet.multipart.Part[] setParameterValues(String pName, com.oreilly.servlet.multipart.Part[] values) {
        return  (com.oreilly.servlet.multipart.Part[])super.put(pName, values);
    }

    public synchronized void setParameter(String key, Part value) {
        Part[] valueArray = new Part[1];
        valueArray[0] = value;
        super.put(key, valueArray);
    }

    /**
     * Returns a baseActionURL - parameters of a request coming in on the baseActionURL
     * will be placed into the ChannelRuntimeData object for channel's use.
     *
     * @return a value of URL to which parameter sequences should be appended.
     */
    public String getBaseActionURL() {
        return this.getBaseActionURL(false);
    }

    /**
     * Returns a baseActionURL - parameters of a request coming in on the baseActionURL
     * will be placed into the ChannelRuntimeData object for channel's use.
     *
     * @param idempotent a <code>boolean</code> value specifying if a given URL should be idepotent.
     * @return a value of URL to which parameter sequences should be appended.
     */
    public String getBaseActionURL(boolean idempotent) {
        // If the base action URL was explicitly set, use it
        // peterk: we should probably introduce idepotent version of this one as well, at some point
        if (baseActionURL != null) {
          return baseActionURL;
        }

        String url=null;
        try {
            if(idempotent) {
                UPFileSpec upfs=new UPFileSpec(channelUPFile);
                upfs.setTagId(PortalSessionManager.IDEMPOTENT_URL_TAG);
                url=upfs.getUPFile();
            } else {
                url=channelUPFile.getUPFile();
            }
        } catch (Exception e) {
            log.error("ChannelRuntimeData::getBaseActionURL() : unable to construct a base action URL!");
        }
        return url;
    }

    /**
     * Returns the URL to invoke one of the workers specified in PortalSessionManager.
     * Typically the channel that is invoked with the worker will have to implement an
     * interface specific for that worker.
     * @param worker - Worker string must be a UPFileSpec.xxx value.
     * @return URL to invoke the worker.
     */
    public String getBaseWorkerURL(String worker) {
        // todo: propagate the exception
        String url=null;
        try {
            url=getBaseWorkerURL(worker,false);
        } catch (Exception e) {
            log.error("ChannelRuntimeData::getBaseWorkerURL() : unable to construct a worker action URL for a worker \""+worker+"\".");
        }
        return url;
    }

    /**
       Returns a media base appropriate for web-visible resources used by and
       deployed with the passed in object. If the class of the passed in
       object was loaded from a CAR then a URL appropriate for accessing
       images in CARs is returned. Otherwise, a URL to the base media
       in the web application's document root is returned.
     */
    public String getBaseMediaURL( Object aChannelObject )
    throws PortalException
    {
        return getBaseMediaURL( aChannelObject.getClass() );
    }

    /**
       Returns a media base appropriate for web-visible resources used by and
       deployed with the passed in class. If the class of the passed in
       object was loaded from a CAR then a URL appropriate for accessing
       images in CARs is returned. Otherwise, a URL to the base media
       in the web application's document root is returned.
     */
    public String getBaseMediaURL( Class aChannelClass )
    throws PortalException
    {
        if (aChannelClass == null)
            return TRADITIONAL_MEDIA_BASE;
        if (aChannelClass == CarResources.class)
            return createBaseCarMediaURL();
        if ( aChannelClass.getClassLoader() ==
             CarResources.getInstance().getClassLoader() )
            return createBaseCarMediaURL();
        return TRADITIONAL_MEDIA_BASE;
    }

    /**
       Returns a media base appropriate for the resource path passed in. The
       resource path is the path to the resource within its channel archive.
       (See org.jasig.portal.car.CarResources class for more information.)
       If the passed in resourcePath matches that of a resource loaded from
       CARs then this method returns a URL appropriate to obtain CAR
       deployed, web-visible resources. Otherwise it returns a URL to the
       traditional media path under the uPortal web application's document
       root.
     */
    public String getBaseMediaURL( String resourcePath )
    throws PortalException
    {
        if ( resourcePath == null || resourcePath.equals(WEB_APP_BASE))
            return TRADITIONAL_MEDIA_BASE;
        if ( resourcePath.equals(CAR_BASE))
            return createBaseCarMediaURL();
        if ( CarResources.getInstance().containsResource( resourcePath ) )
            return createBaseCarMediaURL();
        return TRADITIONAL_MEDIA_BASE;
    }

    /**
     * Creates the CAR media base URL.
     */
    private String createBaseCarMediaURL() throws PortalException {
        String url = getBaseWorkerURL( CarResources.CAR_WORKER_ID, true );
        return url.concat( "?" + CarResources.CAR_RESOURCE_PARM + "=" );
    }


    /**
     * Returns the URL to invoke one of the workers specified in PortalSessionManager.
     * Typically the channel that is invoked with the worker will have to implement an
     * interface specific for that worker.
     * @param worker - Worker string must be a UPFileSpec.xxx value.
     * @param idempotent a <code>boolean</code> value sepcifying if a URL should be idempotent
     * @return URL to invoke the worker.
     * @exception PortalException if an error occurs
     */
    public String getBaseWorkerURL(String worker, boolean idempotent) throws PortalException {
        String url=null;
        UPFileSpec upfs=new UPFileSpec(channelUPFile);
        upfs.setMethod(UPFileSpec.WORKER_METHOD);
        upfs.setMethodNodeId(worker);
        if(idempotent) {
            upfs.setTagId(PortalSessionManager.IDEMPOTENT_URL_TAG);
        }

        url=upfs.getUPFile();

        return url;
    }

    /**
     * Tells whether or not the channel is rendering as the root of the layout.
     * @return <code>true</code> if channel is rendering as the root, otherwise <code>false</code>
     */
    public boolean isRenderingAsRoot() {
      return renderingAsRoot;
    }
    
    /**
     * Tells whether or not the channel is currently targeted.  A channel is targeted
     * if an incoming request specifies the channel's subscribe ID as the targeted node ID.
     * @return <code>true</code> if channel is targeted, otherwise <code>false</code>
     */
    public boolean isTargeted() {
        return targeted;
    }

    /**
     * Get a parameter value. If the parameter has multiple values, only the first value is returned.
     *
     * @param pName parameter name
     * @return parameter value
     */
    public String getParameter(String pName) {
        String[] value_array = this.getParameterValues(pName);
        if ((value_array != null) && (value_array.length > 0))
            return  value_array[0];
        else
            return  null;
    }

    /**
     * Obtain an <code>Object</code> parameter value. If the parameter has multiple values, only the first value is returned.
     *
     * @param pName parameter name
     * @return parameter value
     */
    public Object getObjectParameter(String pName) {
        Object[] value_array = this.getObjectParameterValues(pName);
        if ((value_array != null) && (value_array.length > 0)) {
            return  value_array[0];
        } else {
            return  null;
        }
    }

    /**
     * Obtain all values for a given parameter.
     *
     * @param pName parameter name
     * @return an array of parameter string values
     */
    public String[] getParameterValues(String pName) {
      Object[] pars = (Object[])super.get(pName);
      if (pars instanceof String[]) {
        return  (String[])pars;
      } else {
        return  null;
      }
    }

    /**
     * Obtain all values for a given parameter as <code>Object</code>s.
     *
     * @param pName parameter name
     * @return a vector of parameter <code>Object[]</code> values
     */
    public Object[] getObjectParameterValues(String pName) {
        return  (Object[])super.get(pName);
    }

    /**
     * Get an enumeration of parameter names.
     *
     * @return an <code>Enumeration</code> of parameter names.
     */
    public Enumeration getParameterNames() {
        return  (Enumeration)super.keys();
    }

    /**
     * Get the parameters as a Map
     * @return a Map of parameter name-value pairs
     */
    public Map getParameters() {
        Map params = new java.util.HashMap(this.size());
        Enumeration e = this.getParameterNames();
        while (e.hasMoreElements()) {
          String name = (String)e.nextElement();
          String value = this.getParameter(name);
          params.put(name, value);
        }
        return params;
    }

    /**
     * Sets the keywords
     * @param keywords a String of keywords
     */
    public void setKeywords(String keywords) {
      this.keywords = keywords;
    }

    /**
     * Returns the keywords
     * @return a String of keywords, null if there were none
     */
    public String getKeywords() {
      return keywords;
    }
    
	/**
	 * @return the remote address
	 */
	public String getRemoteAddress() {
		return remoteAddress;
	}
    
	/**
	 * @param string
	 */
	public void setRemoteAddress(String string) {
		remoteAddress = string;
	}	
}
