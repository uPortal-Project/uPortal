/**
 * Copyright © 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer.
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. Redistributions of any form whatsoever must retain the following
*    acknowledgment:
*    "This product includes software developed by the JA-SIG Collaborative
*    (http://www.jasig.org/)."
*
* THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
* EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
* PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
* NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
* OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/


package  org.jasig.portal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.channels.portlet.CPortletAdapter;
import org.jasig.portal.container.services.information.PortletStateManager;
import org.jasig.portal.jndi.JNDIManager;
import org.jasig.portal.properties.PropertiesManager;
import org.jasig.portal.utils.ResourceLoader;


/**
 * This is an entry point into the uPortal.
 * @author Peter Kharchenko <pkharchenko@interactivebusiness.com>
 * @version $Revision$
 */
public class PortalSessionManager extends HttpServlet {

    private static final Log log = LogFactory.getLog(PortalSessionManager.class);
    
  public static final String INTERNAL_TAG_VALUE=Long.toHexString((new Random()).nextLong());
  public static final String IDEMPOTENT_URL_TAG="idempotent";

  private static boolean initialized = false;
  private static ServletContext servletContext = null;
  private static PortalSessionManager instance = null;
  private static boolean fatalError = false;
  
  public static final ErrorID initPortalContext = new ErrorID("config","JNDI","Cannot initialize JNDI context");

  /**
   * Provides access to the servlet instance ultimately to provide access
   * to the servlet context of the portal.
   * @return instance, the PortalSessionManager servlet instance
   */
  public static final PortalSessionManager getInstance() {
     return instance;
  }

  // Following flag allows to disable features that prevent
  // repeated requests from going through. This is useful
  // when debugging and typing things in on a command line.
  // Otherwise, the flag should be set to false.
  private static final boolean ALLOW_REPEATED_REQUESTS = getAllowRepeatedRequestsValue();

  // random number generator
  private static final Random randomGenerator = new Random();
  
  private static boolean getAllowRepeatedRequestsValue() {
  	try {
  	    return 	PropertiesManager.getPropertyAsBoolean("org.jasig.portal.PortalSessionManager.allow_repeated_requests");
  	} catch ( RuntimeException re ) {
  		return false;
  	}
  }

  static {
    log.info( "uPortal started");
  }

  /**
   * Initialize the PortalSessionManager servlet
   * @throws ServletException
   */
  public void init() throws ServletException {
    if(!initialized) {
      instance = this;
      // Retrieve the servlet configuration object from the servlet container
      // and make sure it's available
      ServletConfig sc = getServletConfig();
      if (sc == null) {
        throw new ServletException("PortalSessionManager.init(): ServletConfig object was returned as null");
      }
      
      // Supply PortletContainer with ServletConfig
      CPortletAdapter.setServletConfig(sc);
      
      servletContext = sc.getServletContext();

      try {
          JNDIManager.initializePortalContext();
      } catch (Exception pe) {
      	  ExceptionHelper.genericTopHandler(initPortalContext,pe);
      	  fatalError=true;
      }
      
      // Turn off URL caching if it has been requested
      if (!PropertiesManager.getPropertyAsBoolean("org.jasig.portal.PortalSessionManager.url_caching")) {
         // strangely, we have to instantiate a URLConnection to turn off caching, so we'll get something we know is there
         try {
            URL url = ResourceLoader.getResourceAsURL(PortalSessionManager.class, "/properties/portal.properties");
            URLConnection conn = url.openConnection();
            conn.setDefaultUseCaches(false);
         } catch (Exception e) {
            log.warn("PortalSessionManager.init(): Caught Exception trying to disable URL Caching");
         }
      }

      // Log orderly shutdown time
      Runtime.getRuntime().addShutdownHook(new Thread("uPortal shutdown hook") {
          public void run() {
            log.info( "uPortal stopped");
          }
        });


      // Flag that the portal has been initialized
      initialized = true;

      // Get the SAX implementation
      if (System.getProperty("org.xml.sax.driver") == null) {
          System.setProperty("org.xml.sax.driver", PropertiesManager.getProperty("org.xml.sax.driver"));
      }
    }
  }



    /**
     * Process HTTP POST request
     *
     * @param req an incoming <code>HttpServletRequest</code> value
     * @param res an outgoing <code>HttpServletResponse</code> value
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res)  {
        doGet(req, res);
    }

    /**
     * Process HTTP GET request.
     *
     * @param req an incoming <code>HttpServletRequest</code>
     * @param res an outgoing <code>HttpServletResponse</code>
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) {
        // Send the uPortal version in a header
        res.setHeader("uPortal-version", Version.getProduct() + "_" + Version.getReleaseTag());
        
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
            req.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException uee) {
            log.error("Unable to set UTF-8 character encoding!", uee);
        }
        
        HttpSession session = req.getSession(false);

        if (session != null) {
            Set requestTags=null;
            boolean request_verified=false;

            if(!ALLOW_REPEATED_REQUESTS) {
                // obtain a tag table
                synchronized(session) {
                    requestTags=(Set)session.getAttribute("uP_requestTags");
                    if(requestTags==null) {
                        requestTags=Collections.synchronizedSet(new HashSet());
                        session.setAttribute("uP_requestTags",requestTags);
                    }
                }
                // determine current tag
                UPFileSpec upfs=new UPFileSpec(req);

                String tag=upfs.getTagId();
                
                // see if the tag was registered
                if ( tag != null ) {
                    request_verified = true;
                    requestTags.remove(tag);
                }

                log.debug("PortalSessionManager::doGet() : request verified: "+request_verified);
            }

            try {
                UserInstance userInstance = null;
                try {
                    // Retrieve the user's UserInstance object
                    userInstance = UserInstanceManager.getUserInstance(req);
                } catch(Exception e) {
                	ExceptionHelper.genericTopHandler(Errors.bug,e);
                	ExceptionHelper.generateErrorPage(res,e);
                	return;
                }


                // fire away
                if(ALLOW_REPEATED_REQUESTS) {
                    userInstance.writeContent(new RequestParamWrapper(req,true),res);
                } else {
                    // generate and register a new tag
                    String newTag=Long.toHexString(randomGenerator.nextLong());
                    log.debug("PortalSessionManager::doGet() : generated new tag \""+newTag+"\" for the session "+session.getId());
                    // no need to check for duplicates :) we'd have to wait a lifetime of a universe for this time happen
                    if(!requestTags.add(newTag)) {
                        log.error("PortalSessionManager::doGet() : a duplicate tag has been generated ! Time's up !");
                    }
                    
                    RequestParamWrapper wrappedRequest = new RequestParamWrapper(req,request_verified);
					wrappedRequest.getParameterMap().putAll(PortletStateManager.getURLDecodedParameters(wrappedRequest));

                    userInstance.writeContent(wrappedRequest, new ResponseSubstitutionWrapper(res,INTERNAL_TAG_VALUE,newTag));
                }
            } catch (Exception e) {
            	ExceptionHelper.genericTopHandler(Errors.bug,e);
				ExceptionHelper.generateErrorPage(res,e);
            	return;
           }

        } else {
           try {	
             //throw new ServletException("Session object is null !");
        	 res.sendRedirect(req.getContextPath() + "/Login" );
           } catch (Exception e) {
        	  ExceptionHelper.genericTopHandler(Errors.bug,e);
			  ExceptionHelper.generateErrorPage(res,e);
        	  return;
             } 	
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
    if (!resource.startsWith("/"))
      resource = "/" + resource;

    return servletContext.getResourceAsStream(resource);
  }

}



