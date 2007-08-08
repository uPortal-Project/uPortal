/*
 * Created on Aug 25, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jasig.portal.channels.jsp;

import java.io.File;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ICacheable;
import org.jasig.portal.ICharacterChannel;
import org.jasig.portal.IDirectResponse;
import org.jasig.portal.IPrivileged;
import org.jasig.portal.IPrivilegedChannel;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.car.CarResources;
import org.jasig.portal.channels.BaseChannel;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.properties.PropertiesManager;

/**
 * @author Mark Boyd
 *
 **/
public class Channel
    extends BaseChannel
    implements ICacheable, ICharacterChannel, IPrivilegedChannel, 
    IDirectResponse
{
    private static final Log LOG = LogFactory.getLog(Channel.class);

    /**
     * The property to look for to determine if we should serialize rendering
     * of jsps for a single http request.
     */
    private static final String SINGLE_THREAD_CFG_PROPERTY
        = Channel.class.getName() + ".serializeJspRendering";
    
    /**
     * Indicates whether serialization of rendering is configured.
     */
    private static final boolean cSerializeJspRendering = loadRenderingCfg();
    
    private static final String PREFS_PREFIX="JSP.";    
    protected String mControllerClassname = null;
    private Map mObjects;
    private Properties mJspMap = new Properties();
    private HttpServletResponse mResponse;
    private HttpServletRequest mRequest;
    private static Set mLoaded = new HashSet();
    private static Map mDeploymentApproach = new HashMap();
    protected IController mController;
    private HttpSession mSession = null;
    
    private static String CONTROLLER_KEY = "controllerClass";
    public static final Object CAR_DEPLOYMENT = new Object();
    public static final Object TRADITIONAL_DEPLOYMENT = new Object();
    private String cJspContextPath = null;
    
    /**
     * Default constructor used by Channel Manager to instiate this channel. 
     * This approach delegates to ChannelStaticData to find a property key of
     * "controllerClass" with a value of a class name the implements the 
     * IContoller interface.
     *
     */
    public Channel()
    {
    }
    
    /**
     * Reads configuration to determine if we are serializing rendering of 
     * jsps in a single http request for a user.
     * @return
     */
    private static boolean loadRenderingCfg()
    {
        boolean serialize = PropertiesManager
            .getPropertyAsBoolean(SINGLE_THREAD_CFG_PROPERTY, false);
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Serializing JSP Request Rendering: " + serialize);
        }
        return serialize; 
    }

    /**
     * Return a cache key for indicating if cached output is stale. This 
     * call is delegated to the contained IController which extends ICacheable.
     * 
     * @see org.jasig.portal.ICacheable#generateKey()
     */
    public ChannelCacheKey generateKey()
    {
        if (mController != null) {
            return mController.generateKey();
        }
        return null;
    }

    /**
     * Indicate whether cached output is still valid for the passed in 
     * validity object. The call is delegated to the contained IController 
     * which extends ICacheable.
     * 
     * @see org.jasig.portal.ICacheable#isCacheValid(java.lang.Object)
     */
    public boolean isCacheValid(Object validity)
    {
        if (mController != null) {
            return mController.isCacheValid(validity);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.ICharacterChannel#renderCharacters(java.io.PrintWriter)
     */
    public void renderCharacters(PrintWriter pw) throws PortalException
    {
        if (mController == null) {
            return;
        }
            
        // 1) determine what JSP to forward to
        String jspId = mController.getJspToRender();
        if (jspId == null) {
                throw new PortalException("No JSP id returned by controller.");
        }
                
        String jsp = mJspMap.getProperty(jspId);
        boolean relativeToController = false;
        
        if (jsp == null) {
            throw new PortalException("No mapping available for JSP id '"
                    + jspId + "'.");
        }
            
        if (! jsp.startsWith("/"))
        {
            relativeToController = true;
            jsp =
                mController.getClass().getPackage().getName().replace('.', '/')
                    + "/"
                    + jsp;
        }

        String prefix = "";
        // jsps deployed from a car are deployed in configurable location.
        if (relativeToController ||
            mDeploymentApproach.get(mControllerClassname) == CAR_DEPLOYMENT)
        {
            prefix = getJspContextPath();
        }
        
        // 2) create req/res wrappers and push in model objects returned 
        // from controller.
        
        HttpRequestFacade reqF = new HttpRequestFacade(mRequest);
        HttpResponseFacade respF = new HttpResponseFacade(mResponse);

        if (mObjects != null)
        {
            for (Iterator itr = mObjects.entrySet().iterator(); itr.hasNext();)
            {
                Map.Entry entry = (Entry) itr.next();
                reqF.setAttribute((String) entry.getKey(), entry.getValue());
            }
        }
        
        // 3) Add baseActionUrl and baseMediaUrl to the request object.
        // Since classes and JSPs get deployed in some context relative 
        // classpath accessible area as determined by the Deployer's 
        // getRealPath() the classes will always be loaded by the non-car class
        // loader. Therefore, we can't use traditional ways of determining
        // if the channel was deployed via CAR and the images are still
        // embedded therein. So we use this map which is updated during
        // deployment checks to tell in which way the channel is deployed.
        
        if (mControllerClassname != null)
        {
                reqF.setAttribute(
                    "baseMediaUrl",
                    runtimeData.getBaseMediaURL(mControllerClassname));
            reqF.setAttribute("baseActionUrl", runtimeData.getBaseActionURL(true));
            reqF.setAttribute("userLocale", runtimeData.getLocales()[0]);
        }
        
        // 4 render the JSP for the channel
        String jspPath = prefix + jsp;
        /*
         * When we converted to tomcat we started seeing jsp channel content
         * being swapped and some jsp's not getting their expected models 
         * resulting in exceptions. The problem was traced to dispatching 
         * somehow getting crossed. Therefore, we synchronize on the request
         * to serialized all jsp rendering for a single user request if 
         * configured to do so.
         */
        if (cSerializeJspRendering)
        {
            synchronized(mRequest)
            {
                renderJsp(jspPath, reqF, respF);
            }
        }
        else
        {
            renderJsp(jspPath, reqF, respF);
        }
        
        // 5) if rendering successful, extract characters and use for our UI.
        if (respF.isSuccessful())
        {
            pw.print(respF.getCharacters());
        }
        else
        {
        throw new PortalException(
            "A problem occurred rendering JSP '"
                + jspPath
                + "'. Response Error Code: "
                + respF.getErrorCode()
                + (respF.getErrorMessage() != null
                    ? ", Response Error Message '"
                        + respF.getErrorMessage()
                        + "'"
                   : ""));
        }
    }
    
    /**
     * Obtains dispatcher to JSP and forwards request to it to be rendered.
     * Upon returning, respF should contain the output of the JSP.
     * 
     * @param jspPath
     * @param reqF
     * @param respF
     */
    private void renderJsp(String jspPath, 
            HttpRequestFacade reqF, HttpResponseFacade respF)
    {
        // 4.a) get the request dispatcher for the JSP
        RequestDispatcher dispatch = mRequest.getRequestDispatcher(jspPath);

        if (LOG.isDebugEnabled())
        {
        if (dispatch == null)
            {
                LOG.debug("\n\n Jsp Channel Type with:\n" +
                        "- controller: '" +
                        mController.getClass().getName() + "'\n" +
                        "-     called: getRequestDispatcher()'\n" +
                        "-         on:" + mRequest.getClass().getName() + 
                        ".'\n" +
                        "-   hashcode: " + mRequest.hashCode() + "\n" +
                        "-    passing: " + jspPath
                        + "'\n" +
                        "-   received: NULL");
                LOG.debug("\n" +
                        "-       FROM:\n", new Throwable("STACK"));
            }   
            else
            {
                LOG.debug("\n\n Jsp Channel Type with:\n" +
                        "- controller: '" +
                        mController.getClass().getName() + "'\n" +
                        "-     called: getRequestDispatcher()'\n" +
                        "-         on:" + mRequest.getClass().getName() + 
                        ".'\n" +
                        "-   hashcode: " + mRequest.hashCode() + "\n" +
                        "-    passing: " + jspPath
                        + "'\n" +
                        "-   received: " + dispatch.getClass().getName() + ".");
                LOG.debug("\n" +
                        "-       FROM:\n", new Throwable("STACK"));
            }
        }
        if (dispatch == null) {
            throw new PortalException(
                "Unable to delegate to JSP '" + jspPath + "'. " +
                mRequest.getClass().getName() + ".getRequestDispatch('" +
                jspPath + "') returned NULL.");
        }
            
        // 4.b) now render the JSP view
        try
        {
            dispatch.forward(reqF, respF);
        }
        catch (IllegalStateException e)
        {
            throw new PortalException(
                "A problem occurred rendering JSP '" + jspPath + "'",
                e);
        }
        catch (ServletException e)
        {
            throw new PortalException(
                "A problem occurred rendering JSP '" + jspPath + "'",
                e);
        }
        catch (Exception e)
        {
            throw new PortalException(
                "A problem occurred rendering JSP '" + jspPath + "'",
                e);
        }
    }
    
    private String getJspContextPath()
    {
        if (cJspContextPath == null)
        {
            String ctxRelativePath 
            = PropertiesManager.getProperty(Deployer.JSP_DEPLOY_DIR_PROP,
                    "/WEB-INF/classes");
            if (!ctxRelativePath.endsWith("/")
                    && !ctxRelativePath.endsWith("\\")) {
                ctxRelativePath = ctxRelativePath + File.separatorChar;
            }
            cJspContextPath = ctxRelativePath;
        }
        return cJspContextPath;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#receiveEvent(org.jasig.portal.PortalEvent)
     */
    public void receiveEvent(PortalEvent ev)
    {
        if (mController != null)
        {
            mController.receiveEvent(ev);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#setRuntimeData(org.jasig.portal.ChannelRuntimeData)
     */
    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException
    {
    	
    	if (log.isTraceEnabled()) {
    		log.trace("JSP Channel received setRuntimeData [" + rd + "]");
    	}
        if (mController != null)
        {
            super.setRuntimeData(new MediaResolver(rd));
            mObjects = mController.processRuntimeData(runtimeData, mSession);
        }
        else
            super.setRuntimeData(rd);

        // Now indicate to the JSPs via the HttpRequest which locale is in use.
        java.util.Locale locale = runtimeData.getLocales()[0];

        // To avoid the compile-time dependency, use the published
        // strings instead of linking directly to the JSTL Config
        // class.
        mRequest.setAttribute(
            "javax.servlet.jsp.jstl.fmt.locale",
            locale
            );
                                    
        // To avoid the compile-time dependency, use the published
        // strings instead of linking directly to the JSTL Config
        // class.
        mRequest.setAttribute(
            "javax.servlet.jsp.jstl.fmt.locale" + ".request",
            locale
            );

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#setStaticData(org.jasig.portal.ChannelStaticData)
     */
    public void setStaticData(ChannelStaticData sd) throws PortalException
    {
        super.setStaticData(sd);
        getController();

        if (mController != null)
        {
            // strip channel-type specific prefix from those parameters that
            // are specific to the JSPs.
            // Take all parameters whose names start with "JSP." and remove them
            // from the channel static data, then reinsert them w/out the prefix.
            Enumeration allKeys = sd.keys();
            while (allKeys.hasMoreElements()) {
                String p = (String)allKeys.nextElement();
                if (p.startsWith(PREFS_PREFIX)) {
                    String name = p.substring(PREFS_PREFIX.length());
                    String value = sd.getParameter(p);
                    sd.setParameter(name,value);
                    // remove old parameter
                    sd.remove(p);
                }
            }
                    
            mController.setStaticData(sd);
            loadJspMap();
        }
    }

    /**
     * 
     */
    private void loadJspMap()
    {
        Map jsps = mController.getJspMap();
        if ( null == jsps ) {
            return;
        }
        
        for (Iterator itr = jsps.entrySet().iterator(); itr.hasNext();)
        {
            Map.Entry entry = (Entry) itr.next();
            String key = (String) entry.getKey();
            mJspMap.put(key, entry.getValue());
        }
    }

    /**
     * 
     */
    private void getController() throws PortalException
    {
        if (mControllerClassname == null) {
        	mControllerClassname = this.staticData.getParameter(CONTROLLER_KEY);
        }
        	
        if (mControllerClassname == null) {
            throw new PortalException("No implementation of " +
                    "org.jasig.portal.channels.jsp.IController " +
                    "specified on ChannelStaticData.");
        }
        
        syncDeploymentOfResources(mControllerClassname);

        if (mControllerClassname == null) {
            throw new PortalException("No '" + CONTROLLER_KEY + "' specified.");
        }
            
        // now lets load and instantiate the handler
        Class c = null;
        Object obj = null;

        try
        {
            CarResources cRes = CarResources.getInstance();
            ClassLoader cl = cRes.getClassLoader();
            c = cl.loadClass(mControllerClassname);
        }
        catch (Exception e)
        {
            throw new PortalException(
                "Class '"
                    + mControllerClassname
                    + "' specified in parameter '"
                    + CONTROLLER_KEY
                    + "' could not be loaded.",e);
        }
        catch( NoClassDefFoundError e)
        {
            throw new PortalException(
                    "Class '"
                    + mControllerClassname
                    + "' specified in parameter '"
                    + CONTROLLER_KEY
                    + "' could not be loaded.",e);
        }
        
        try
        {
            obj = c.newInstance();
        }
        catch (Exception e)
        {
            throw new PortalException(
                "Unable to instantiate class '"
                    + mControllerClassname
                    + "' specified in parameter '"
                    + CONTROLLER_KEY
                    + "'.",
                e);
        }
        try
        {
            mController = (IController) obj;
        }
        catch (ClassCastException cce)
        {
            throw new PortalException(
                "Class '"
                    + mControllerClassname
                    + "' specified in parameter '"
                    + CONTROLLER_KEY
                    + "' does not implement "
                    + IController.class.getName());
        }
    }

    /**
     * The purpose of this method is to see if resources for this controller are
     * currently being deployed and wait until they are done. The first thread
     * in here for a specific controller class will take care of deploying while
     * all others will wait until it is done. This is done to keep that class
     * from being loaded by the classloader until it is sitting in a proper
     * non-car class loader dependant location so that it and any other classes
     * potentially in a CAR can be used by the JSPs. If left in and loaded from
     * the CARs the classloader used by the JSPs will not be able to find them.
     * 
     * @param classname
     */
    private void syncDeploymentOfResources(String classname)
    {
        String resource = classname.replace('.', '/') + ".class";
        CarResources cRes = CarResources.getInstance();
        String car = cRes.getContainingCarPath(resource);
        
        if (car == null) // class not found in car, no deployment necessary
        {
            mDeploymentApproach.put(classname, Channel.TRADITIONAL_DEPLOYMENT);
            return;
        }
        if( !mLoaded.contains( car ) )
        {
            synchronized( Channel.class )
            {
                if( !mLoaded.contains( car ) )
                {
                    Deployer deployer = new Deployer();
                    deployer.deployResources(classname);
                    mDeploymentApproach.put(
                        classname,
                        (deployer.isDeployedInCar()
                            ? Channel.CAR_DEPLOYMENT
                            : Channel.TRADITIONAL_DEPLOYMENT));
                    mLoaded.add(car);
                }
            }
        }
    }

    //////////////// Implementation of IPrivileged ///////////////////

    /** 
     * Extracts the HttpServletRequest, HttpServletResponse, and 
     * HttpSession for use in delegated JSPs.
     * 
     * @see org.jasig.portal.IPrivileged#setPortalControlStructures(org.jasig.portal.PortalControlStructures)
     */
    public void setPortalControlStructures(PortalControlStructures pcs)
        throws PortalException
    {
        mSession = pcs.getHttpSession();
        mRequest = pcs.getHttpServletRequest();
        mResponse = pcs.getHttpServletResponse();

        if (mController != null && mController instanceof IPrivileged)
        {
            ((IPrivileged)mController).setPortalControlStructures(pcs);
        }
    }

    //////////////// Implementation of IDirectResponse ///////////////////

    /**
     * Serves up any type of file to the browser as dictated by the controller
     * if the controller supports IDirectResponse.
     * 
     * @see org.jasig.portal.IDirectResponse#setResponse(javax.servlet.http.HttpServletResponse)
     */
    public void setResponse(HttpServletResponse response)
    {
        if (mController != null && mController instanceof IDirectResponse) {
        	((IDirectResponse)mController).setResponse(response);
        } else {
            throw new UnsupportedOperationException("JSP Controller " +
                    mControllerClassname + " does not implement " +
                    IDirectResponse.class.getName() + ".");
        }
    }
}
