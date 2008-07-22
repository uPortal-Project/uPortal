/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.events.EventPublisherLocator;
import org.jasig.portal.events.support.UserLoggedOutPortalEvent;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;
import org.jasig.portal.security.PersonManagerFactory;
import org.jasig.portal.utils.ResourceLoader;

/**
 * Simple servlet to handle user logout. When a user
 * logs out, their session gets invalidated and they
 * are returned to the guest page.
 * @author Ken Weiner, kweiner@unicon.net
 * @author Don Fracapane, df7@columbia.edu
 * @version $Revision$
 */
public class LogoutServlet extends HttpServlet {
    
    private static final Log log = LogFactory.getLog(LogoutServlet.class);
    
   private static boolean INITIALIZED = false;
   private static String DEFAULT_REDIRECT;
   private static HashMap REDIRECT_MAP;

   /**
    * Initialize the LogoutServlet
    * @throws ServletException
    */
   public void init () throws ServletException {
      if (!INITIALIZED) {
         String upFile = UPFileSpec.RENDER_URL_ELEMENT + UPFileSpec.PORTAL_URL_SEPARATOR
               + UPFileSpec.USER_LAYOUT_ROOT_NODE + UPFileSpec.PORTAL_URL_SEPARATOR
               + UPFileSpec.PORTAL_URL_SUFFIX;
         HashMap rdHash = new HashMap(1);
         try {
            upFile = UPFileSpec.buildUPFile(UPFileSpec.RENDER_METHOD, UPFileSpec.USER_LAYOUT_ROOT_NODE,
                  null, null);
            // We retrieve the redirect strings for each context
            // from the security properties file.
            String key;
            Properties props = ResourceLoader.getResourceAsProperties(LogoutServlet.class,
                  "/properties/security.properties");
            Enumeration propNames = props.propertyNames();
            while (propNames.hasMoreElements()) {
               String propName = (String)propNames.nextElement();
               String propValue = props.getProperty(propName);
               if (propName.startsWith("logoutRedirect.")) {
                  key = propName.substring(15);
                  key = (key.startsWith("root.") ? key.substring(5) : key);
                  if (log.isDebugEnabled()) {
                      log.debug("LogoutServlet::initializer()" +
                              " Redirect key = " + key);
                      log.debug("LogoutServlet::initializer()" +
                              " Redirect value = " + propValue);
                  }
  
                  rdHash.put(key, propValue);
               }
            }
         } catch (PortalException pe) {
            log.error( "LogoutServlet::static ", pe);
         } catch (IOException ioe) {
            log.error( "LogoutServlet::static", ioe);
         }
         REDIRECT_MAP = rdHash;
         DEFAULT_REDIRECT = upFile;
         INITIALIZED = true;
      }
   }

  /**
   * Process the incoming request and response.
   * @param request HttpServletRequest object
   * @param response HttpServletResponse object
   * @throws ServletException
   * @throws IOException
   */
  public void doGet (HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
    init();
    String redirect = getRedirectionUrl(request);
    HttpSession session = request.getSession(false);

    if (session != null) {
        // Record that an authenticated user is requesting to log out
        try {
            IPerson person = PersonManagerFactory.getPersonManagerInstance().getPerson(request);
            if (person != null && person.getSecurityContext().isAuthenticated()) {
            	EventPublisherLocator.getApplicationEventPublisher().publishEvent(new UserLoggedOutPortalEvent(this, person));
            }
        } catch (Exception e) {
            log.error("Exception recording logout " +
                    "associated with request " + request, e);
        }
        
        final String originalUid = (String)session.getAttribute(LoginServlet.SWAP_ORIGINAL_UID);
        //Logging out from a swapped user, just redirect to the Login servlet
        if (originalUid != null) {
            redirect = request.getContextPath() + "/Login";
        }
        else {
            // Clear out the existing session for the user
            try {
                session.invalidate();
            } catch (IllegalStateException ise) {
            	// IllegalStateException indicates session was already invalidated.
            	// This is fine.  LogoutServlet is looking to guarantee the logged out session is invalid;
            	// it need not insist that it be the one to perform the invalidating.
            	if (log.isTraceEnabled()) {
            		log.trace("LogoutServlet encountered IllegalStateException invalidating a presumably already-invalidated session.", ise);
            	}
            }
        }
    }

    // Send the user back to the guest page
    response.sendRedirect(redirect);
  }

  /**
   * The redirect is determined based upon the context that passed authentication
   * The LogoutServlet looks at each authenticated context and determines if a
   * redirect exists for that context in the REDIRECT_MAP variable (loaded from
   * security.properties file). The redirect is returned for the first authenticated
   * context that has an associated redirect string. If such a context is not found,
   * we use the default DEFAULT_REDIRECT that was originally setup.
   *
   * NOTE:
   * This will work or not work based upon the logic in the root context. At this time,
   * all known security contexts extend the ChainingSecurityContext class. If a context
   * has the variable stopWhenAuthenticated set to false, the user may be logged into
   * multiple security contexts. If this is the case, the logout process currently
   * implemented does not accommodate multiple logouts. As a reference implemention,
   * the current implementation assumes only one security context has been authenticated.
   * Modifications to perform multiple logouts should be considered when a concrete
   * need arises and can be handled by this class or through a change in the
   * ISecurityConext API where a context knows how to perform it's own logout.
   *
   * @param request
   * @return String representing the redirection URL
   */
   private String getRedirectionUrl (HttpServletRequest request) {
      String redirect = null;
      String defaultRedirect = request.getContextPath() + '/' + DEFAULT_REDIRECT;
      IPerson person = null;
      if (REDIRECT_MAP == null) {
         return  defaultRedirect;
      }
      try {
         // Get the person object associated with the request
         person = PersonManagerFactory.getPersonManagerInstance().getPerson(request);
         // Retrieve the security context for the user
         ISecurityContext securityContext = person.getSecurityContext();
         if (securityContext.isAuthenticated()) {
             if (log.isDebugEnabled())
                 log.debug("LogoutServlet::getRedirectionUrl()" +
                     " Looking for redirect string for the root context");
            redirect = (String)REDIRECT_MAP.get("root");
            if (redirect != null && !redirect.equals("")) {
               return redirect;
            }
         }
         Enumeration subCtxNames = securityContext.getSubContextNames();
         while (subCtxNames.hasMoreElements()) {
            String subCtxName = (String)subCtxNames.nextElement();
            if (log.isDebugEnabled())
                log.debug("LogoutServlet::getRedirectionUrl() " +
                        " subCtxName = " + subCtxName);
            // strip off "root." part of name
            ISecurityContext sc = securityContext.getSubContext(subCtxName);
            if (log.isDebugEnabled())
                log.debug("LogoutServlet::getRedirectionUrl()" +
                        " subCtxName isAuth = " + sc.isAuthenticated());
            if (sc.isAuthenticated()) {
                if (log.isDebugEnabled())
                    log.debug("LogoutServlet::getRedirectionUrl()" +
                            " Looking for redirect string for subCtxName = " + subCtxName);
               redirect = (String)REDIRECT_MAP.get(subCtxName);
               if (redirect != null && !redirect.equals("")) {
                   if (log.isDebugEnabled())
                       log.debug("LogoutServlet::getRedirectionUrl()" +
                               " subCtxName redirect = " + redirect);
                  break;
               }
            }
         }
      } catch (Exception e) {
         // Log the exception
         log.error( "LogoutServlet::getRedirectionUrl() Error:", e);
      }
      if (redirect == null) {
         redirect = defaultRedirect;
      }
      if (log.isDebugEnabled())
          log.debug("LogoutServlet::getRedirectionUrl()" +
                  " redirectionURL = " + redirect);
      return  redirect;
   }
}
