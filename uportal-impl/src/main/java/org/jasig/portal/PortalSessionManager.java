/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/


package  org.jasig.portal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Random;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.rendering.IPortalRenderingPipeline;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.security.PersonManagerFactory;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.jasig.portal.tools.versioning.Version;
import org.jasig.portal.tools.versioning.VersionsManager;
import org.jasig.portal.url.IWritableHttpServletRequest;
import org.jasig.portal.url.PortalHttpServletRequest;
import org.jasig.portal.url.processing.IRequestParameterProcessorController;
import org.jasig.portal.utils.ResourceLoader;
import org.springframework.context.ApplicationContext;

/**
 * This is an entry point into the uPortal.
 * @author Peter Kharchenko <pkharchenko@interactivebusiness.com>
 * @version $Revision$
 */
public class PortalSessionManager extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(PortalSessionManager.class);

    /**
     * Default value for whether to cache URLs.
     * This value will be used when the corresponding property cannot be loaded.
     */
    private static final boolean DEFAULT_URL_CACHING = true;

    /**
     * Default SAX driver name.
     * This will be used when the System property is not set and
     * the corresponding portal.properties property is not set.
     */
    private static final String DEFAULT_SAX_DRIVER = "org.apache.xerces.parsers.SAXParser";

  private static boolean initialized = false;
  private static ServletContext servletContext = null;
  private static PortalSessionManager instance = null;
  private static boolean fatalError = false;
  private static int unauthenticatedUserSessionTimeout = 0;
  private static final IPersonManager personManager =
      PersonManagerFactory.getPersonManagerInstance();

  public static final ErrorID initPortalContext = new ErrorID("config","JNDI","Cannot initialize JNDI context");

  private static final ThreadGroup threadGroup = new ThreadGroup("uPortal");
  public static ThreadGroup getThreadGroup() {
	  return threadGroup;
  }

  /**
   * Provides access to the servlet instance ultimately to provide access
   * to the servlet context of the portal.
   * @return instance, the PortalSessionManager servlet instance
   */
  public static final PortalSessionManager getInstance() {
     return instance;
  }

  // random number generator
  private static final Random randomGenerator = new Random();

  public static Date STARTED_AT = new Date(0); // Loaded, but not initialized
  /**
   * Initialize the PortalSessionManager servlet
   * @throws ServletException
   */
  @Override
public void init() throws ServletException {
    if(!initialized) {
      STARTED_AT = new Date();

      instance = this;
      // Retrieve the servlet configuration object from the servlet container
      // and make sure it's available
      ServletConfig sc = getServletConfig();
      if (sc == null) {
        throw new ServletException("PortalSessionManager.init(): ServletConfig object was returned as null");
      }

      servletContext = sc.getServletContext();

      // Turn off URL caching if it has been requested
      if (!PropertiesManager.getPropertyAsBoolean("org.jasig.portal.PortalSessionManager.url_caching", DEFAULT_URL_CACHING)) {
         // strangely, we have to instantiate a URLConnection to turn off caching, so we'll get something we know is there
         try {
            URL url = ResourceLoader.getResourceAsURL(PortalSessionManager.class, "/properties/portal.properties");
            URLConnection conn = url.openConnection();
            conn.setDefaultUseCaches(false);
         } catch (Exception e) {
             if (log.isWarnEnabled())
                 log.warn("PortalSessionManager.init(): " +
                        "Caught Exception trying to disable URL Caching", e);
         }
      }


      // Get the SAX implementation
      if (System.getProperty("org.xml.sax.driver") == null) {
          System.setProperty("org.xml.sax.driver", PropertiesManager.getProperty("org.xml.sax.driver", DEFAULT_SAX_DRIVER));
      }

      // try to set the unauthenticated user's timeout, defaulting to the current server setting
      unauthenticatedUserSessionTimeout = PropertiesManager.getPropertyAsInt(
          PortalSessionManager.class.getName()+".unauthenticatedUserSessionTimeout", 0);

      // anything less than -1 is undefined, so
      // set it to no timeout
      if (unauthenticatedUserSessionTimeout < -1) {
          unauthenticatedUserSessionTimeout = -1;
      }

      // Flag that the portal has been initialized
      initialized = true;
      log.info( "uPortal started");
    }
  }


    @Override
    public void destroy() {
        // Log orderly shutdown time
        log.info("uPortal stopped");
    }
    
    /**
     * Process HTTP POST request
     *
     * @param req an incoming <code>HttpServletRequest</code> value
     * @param res an outgoing <code>HttpServletResponse</code> value
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)  {
        doGet(req, res);
    }

    /**
     * Process HTTP GET request.
     *
     * @param req an incoming <code>HttpServletRequest</code>
     * @param res an outgoing <code>HttpServletResponse</code>
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse res) {
        final IWritableHttpServletRequest writableRequest = new PortalHttpServletRequest(request, personManager);
        this.doGetInternal(writableRequest, res);
    }
    
    /**
     * Internal method just to clarify that the writableRequest should be used from here on out.
     */
    protected void doGetInternal(IWritableHttpServletRequest writableRequest, HttpServletResponse res) {
        // Send the uPortal version in a header
        final VersionsManager versionManager = VersionsManager.getInstance();
        final Version version = versionManager.getVersion(IPermission.PORTAL_FRAMEWORK);
        res.setHeader("uPortal-version", "uPortal_rel-" + version.getMajor() + "-" + version.getMinor() + "-" + version.getMicro());

        //Check if the servlet failed to initialize
        if (fatalError) {
            try {
                res.sendRedirect("error/fatal.htm");
            } catch (IOException e) {
                ExceptionHelper.genericTopHandler(Errors.bug,e);
            }
            return;
        }

        // Call to setCharacterEncoding method should be done before any call to req.getParameter() method.
        try {
            writableRequest.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            log.error("Unable to set UTF-8 character encoding!", uee);
        }

        //Get the user's session
        final HttpSession session = writableRequest.getSession(false);
        if (session == null) {
            try {
                //Session is null, redirect to Login servlet
                res.sendRedirect(writableRequest.getContextPath() + "/Login");
            }
            catch (Exception e) {
                ExceptionHelper.genericTopHandler(Errors.bug, e);
                ExceptionHelper.generateErrorPage(res, e);
            }
            
            return;
        }

        // Update the session timeout for an unauthenticated user.
        final IPerson person = personManager.getPerson(writableRequest);
        if (person != null && !person.getSecurityContext().isAuthenticated()) {

            if (unauthenticatedUserSessionTimeout != 0) {
                session.setMaxInactiveInterval(unauthenticatedUserSessionTimeout);
                
                if (log.isDebugEnabled()) {
                    log.debug("Unauthenticated user session timeout set to: " + unauthenticatedUserSessionTimeout);
                }
            }
        }
        
        try {
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            
            final IRequestParameterProcessorController requestProcessorController = (IRequestParameterProcessorController)applicationContext.getBean("requestParameterProcessorController", IRequestParameterProcessorController.class);
            requestProcessorController.processParameters(writableRequest, res);

            // Retrieve the user's UserInstance object
            final IUserInstance userInstance = UserInstanceManager.getUserInstance(writableRequest);
            
            // fire away
            final IPortalRenderingPipeline portalRenderingPipeline = (IPortalRenderingPipeline)applicationContext.getBean("portalRenderingPipeline", IPortalRenderingPipeline.class);
            portalRenderingPipeline.renderState(writableRequest, res, userInstance);
        }
        catch (Exception e) {
            ExceptionHelper.genericTopHandler(Errors.bug, e);
            ExceptionHelper.generateErrorPage(res, e);
            return;
        }
    }

  /**
   * Gets a URL associated with the named resource.
   * Call this to access files with paths relative to the
   * document root.  Paths should begin with a "/".
   * @param resource relative to the document root
   * @return a URL associated with the named resource or null if the URL isn't accessible
   */
  public static URL getResourceAsURL(String resource) {
    //Make sure resource string starts with a "/"
    if (!resource.startsWith("/"))
      resource = "/" + resource;

    URL url = null;

    try {
      url = servletContext.getResource(resource);
    } catch (java.net.MalformedURLException murle) {
      // if the URL is bad, just return null
    }
    return url;
  }

  /**
   * Gets an input stream associated with the named resource.
   * Call this to access files with paths relative to the
   * document root. Paths should begin with a "/".
   * @param resource relative to the document root
   * @return an input stream assosiated with the named resource
   */
  public static java.io.InputStream getResourceAsStream(String resource) {

    //Make sure resource string starts with a "/"
    if (!resource.startsWith("/")) {
      resource = "/" + resource;
    }

    if (servletContext != null) {
      return servletContext.getResourceAsStream(resource);
	} else {
      throw new IllegalStateException("Unable to load resource '" + resource +"' because the servlet context has not been initialized yet");
	}
  }

  /**
   * See if our servlet context is available
   * @return boolean if so
   */
  public static boolean isServletContext() {
      return servletContext != null;
  }
  

  /**
   *Accessor for the fatalError member.
   *@return a boolean value indicating if a fatal error occured duing init
   */
  public boolean getFatalError(){
    return fatalError;
  }

  /**
   *Accessor for the Random number generator instance
   *@return an instance of a random number generator instantiated
   *        by the portal session manager.
   */
  public Random getRandom(){
    return randomGenerator;
  }

}



