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

package org.jasig.portal.portlet.container.services;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.portal.concurrency.FunctionWithoutResult;
import org.jasig.portal.concurrency.locking.IClusterLockService;
import org.jasig.portal.portlet.dao.IPortletCookieDao;
import org.jasig.portal.portlet.om.IPortalCookie;
import org.jasig.portal.portlet.om.IPortletCookie;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.util.WebUtils;

/**
 * {@link Service} bean to encapsulate business logic regarding portlet cookie persistence.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("portletCookieService")
public class PortletCookieServiceImpl implements IPortletCookieService, ServletContextAware {
    
	/**
	 * Name of the {@link HttpSession} attribute used for storing a concurrent map of portlet cookies that do not need to be persisted.
	 */
	static final String SESSION_ATTRIBUTE__SESSION_ONLY_COOKIE_MAP = PortletCookieServiceImpl.class.getName() + ".SESSION_ONLY_COOKIE_MAP";
	/**
	 * Name of the {@link HttpSession} attribute used to track the value of the {@link IPortalCookie} (useful if the client does not accept cookies).
	 */
	static final String SESSION_ATTRIBUTE__PORTAL_COOKIE_ID = PortletCookieServiceImpl.class.getName() + ".PORTAL_COOKIE_ID";
	
	private static final String PURGE_LOCK_NAME = PortletCookieServiceImpl.class.getName() + ".PURGE_LOCK";
    
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	private IPortletCookieDao portletCookieDao;
	private IClusterLockService clusterLockService;
    
    protected static final int DEFAULT_MAX_AGE = (int)TimeUnit.DAYS.toSeconds(365);
    private String cookieName = DEFAULT_PORTAL_COOKIE_NAME;
    private String comment = DEFAULT_PORTAL_COOKIE_COMMENT;
    private String domain = null;
    private String path = "/";
    private int maxAge = DEFAULT_MAX_AGE;
    private long maxAgeUpdateInterval = TimeUnit.MINUTES.toMillis(5);
    private boolean portalCookieAlwaysSecure = false;
    
    @Autowired
    public void setPortletCookieDao(IPortletCookieDao portletCookieDao) {
        this.portletCookieDao = portletCookieDao;
    }

    @Autowired
    public void setClusterLockService(IClusterLockService clusterLockService) {
        this.clusterLockService = clusterLockService;
    }



    /* (non-Javadoc)
	 * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
	 */
	@Override
	public void setServletContext(ServletContext servletContext) {
		this.path = servletContext.getContextPath() + "/";
	}

	/**
     * @param maxAge The max number of seconds the portal cookie should live for. Defaults to 365 days. 
     */
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    /**
     * @param cookieName The name of the cookie to set on the browser. Defaults to {@link #DEFAULT_PORTAL_COOKIE_NAME}
     * WARNING if you change this in an existing deployment all existing portal cookies will be orphaned.
     */
    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    /**
     * @param comment The comment for the cookie that is set. Defaults to {@link #DEFAULT_PORTAL_COOKIE_COMMENT}
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @param domain The domain to set, it is recommended to leave this null.
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * @param maxAgeUpdateInterval How frequently (in ms) the maxAge date on the portal cookie should be updated. Defaults to 5 minutes.
     * Only portal cookies older than 5 minutes will be updated in the client's browser and the db with a new maxAge
     */
    public void setMaxAgeUpdateInterval(long maxAgeUpdateInterval) {
        this.maxAgeUpdateInterval = maxAgeUpdateInterval;
    }

    /**
	 * @param portalCookieAlwaysSecure Set a value of true to set the portal cookie's secure flag to 'true' regardless of the request's secure flag.
	 */
	public void setPortalCookieAlwaysSecure(boolean portalCookieAlwaysSecure) {
		this.portalCookieAlwaysSecure = portalCookieAlwaysSecure;
	}

	/**
     * 
     * (non-Javadoc)
     * @see org.jasig.portal.portlet.container.services.IPortletCookieService#updatePortalCookie(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void updatePortalCookie(HttpServletRequest request, HttpServletResponse response) {
        //Get the portal cookie object
        final IPortalCookie portalCookie = this.getOrCreatePortalCookie(request);
        
        //Create the browser cookie
        final Cookie cookie = this.convertToCookie(portalCookie, this.portalCookieAlwaysSecure || request.isSecure());
        
        //Update the expiration date of the portal cookie stored in the DB if the update interval has passed
        final Date expires = portalCookie.getExpires();
        if ((System.currentTimeMillis() - this.maxAgeUpdateInterval) > (expires.getTime() - TimeUnit.SECONDS.toMillis(this.maxAge))) {
            this.portletCookieDao.updatePortalCookieExpiration(portalCookie, cookie.getMaxAge());
            
            // Update expiration dates of portlet cookies stored in session
            removeExpiredPortletCookies(request);
        }
        //Update the cookie in the users browser
        response.addCookie(cookie);
    }
   
   /**
    * Remove expired session only portlet cookies.
    * 
    * @param request
    */
   protected void removeExpiredPortletCookies(HttpServletRequest request) {
	   final Date now = new Date();
	   Map<String, SessionOnlyPortletCookieImpl> sessionOnlyCookies = getSessionOnlyPortletCookieMap(request);
	   for(Entry<String, SessionOnlyPortletCookieImpl> entry: sessionOnlyCookies.entrySet()) {
		   String key = entry.getKey();
		   SessionOnlyPortletCookieImpl sessionOnlyCookie = entry.getValue();
		   if(sessionOnlyCookie.getExpires().before(now)){
			   sessionOnlyCookies.remove(key);
		   }
	   }
   }
    
    @Override
    public Cookie[] getAllPortletCookies(HttpServletRequest request, IPortletWindowId portletWindowId) {
    	final IPortalCookie portalCookie = this.getPortalCookie(request);
    	 
    	//Get the cookies from the servlet request
        Cookie[] servletCookies = request.getCookies();
        if (servletCookies == null) {
            servletCookies = new Cookie[0];
        } else if(portalCookie != null) {
        	for(int i=0; i< servletCookies.length; i++) {
        		if(servletCookies[i].getName().equals(this.cookieName)) {
        			// replace cookie in the array with converted IPortalCookie (so secure, domain, path, maxAge are set)
        			servletCookies[i] = convertToCookie(portalCookie, this.portalCookieAlwaysSecure || request.isSecure());
        		}
        	}
        }
        
        //Get cookies that have been set by portlets, suppressing expired
        Set<IPortletCookie> portletCookies = new HashSet<IPortletCookie>();
        if(portalCookie != null) {
        	Date now = new Date();
        	for(IPortletCookie portletCookie: portalCookie.getPortletCookies()) {
        		if(portletCookie.getExpires().after(now)) {
        			portletCookies.add(portletCookie);
        		}
        	}
        }
        
        // finally get portlet cookies from session (all maxAge -1)
        Map<String, SessionOnlyPortletCookieImpl> sessionOnlyPortletCookieMap = getSessionOnlyPortletCookieMap(request);
        Collection<SessionOnlyPortletCookieImpl> sessionOnlyCookies = sessionOnlyPortletCookieMap.values();
        
        //Merge into a single array
        final Cookie[] cookies = new Cookie[servletCookies.length + portletCookies.size() + sessionOnlyCookies.size()];
        System.arraycopy(servletCookies, 0, cookies, 0, servletCookies.length);

        int cookieIdx = servletCookies.length;
        for (final IPortletCookie portletCookie : portletCookies) {
            final Cookie cookie = portletCookie.toCookie(); 
            cookies[cookieIdx++] = cookie;
        }
        for(SessionOnlyPortletCookieImpl sessionOnlyCookie: sessionOnlyCookies) {
        	cookies[cookieIdx++] = sessionOnlyCookie.toCookie();
        }
       
        
        return cookies;
    }
    
    @Override
    public void addCookie(HttpServletRequest request, IPortletWindowId portletWindowId, Cookie cookie) {
        final IPortalCookie portalCookie = this.getOrCreatePortalCookie(request);
        if(cookie.getMaxAge() < 0) {
        	// persist only in the session
            Map<String, SessionOnlyPortletCookieImpl> sessionOnlyPortletCookies = getSessionOnlyPortletCookieMap(request);
            SessionOnlyPortletCookieImpl sessionOnlyCookie = new SessionOnlyPortletCookieImpl(cookie);
            sessionOnlyPortletCookies.put(cookie.getName(), sessionOnlyCookie);
        } else if (cookie.getMaxAge() == 0) {
        	// delete the cookie from the session, if present
        	Map<String, SessionOnlyPortletCookieImpl> sessionOnlyPortletCookies = getSessionOnlyPortletCookieMap(request);
        	SessionOnlyPortletCookieImpl existing = sessionOnlyPortletCookies.remove(cookie.getName());
        	if(null == existing) {
        		// returning null from map#remove means cookie wasn't in the session, trigger portletCookieDao update
        		this.portletCookieDao.addOrUpdatePortletCookie(portalCookie, cookie);
        	}
        } else {
        	Map<String, SessionOnlyPortletCookieImpl> sessionOnlyPortletCookies = getSessionOnlyPortletCookieMap(request);
        	sessionOnlyPortletCookies.remove(cookie.getName());
        	// update the portletCookieDao regardless
        	this.portletCookieDao.addOrUpdatePortletCookie(portalCookie, cookie);
        }
        
    }
    
    @Override
    public boolean purgeExpiredCookies() {
        try {
            this.clusterLockService.doInTryLock(PURGE_LOCK_NAME, new FunctionWithoutResult<String>() {
                @Override
                protected void applyWithoutResult(String input) {
                    portletCookieDao.purgeExpiredCookies();
                }
            });
            return true;
        }
        catch (InterruptedException e) {
            logger.warn("Interrupted while purging expired cookies", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Get the {@link Map} of {@link SessionOnlyPortletCookieImpl}s stored in the {@link HttpSession} specifically
     * used for storing {@link SessionOnlyPortletCookieImpl}s with a maxAge equal to -1.
     * Will create the map if it doesn't yet exist.
     * 
     * @param request
     * @return
     */
    @SuppressWarnings("unchecked")
	protected Map<String, SessionOnlyPortletCookieImpl> getSessionOnlyPortletCookieMap(final HttpServletRequest request) {
    	final HttpSession session = request.getSession();
        synchronized(WebUtils.getSessionMutex(session)) {
        	Map<String, SessionOnlyPortletCookieImpl> sessionOnlyPortletCookies = (Map<String, SessionOnlyPortletCookieImpl>) session.getAttribute(SESSION_ATTRIBUTE__SESSION_ONLY_COOKIE_MAP);
        	if(sessionOnlyPortletCookies == null) {
        		sessionOnlyPortletCookies = new ConcurrentHashMap<String, SessionOnlyPortletCookieImpl>();
        		session.setAttribute(SESSION_ATTRIBUTE__SESSION_ONLY_COOKIE_MAP, sessionOnlyPortletCookies);	
        	}
        	return sessionOnlyPortletCookies;
        }
    }
    /**
     * Convert the {@link IPortalCookie} into a servlet {@link Cookie}.
     * 
     * @param portalCookie
     * @return
     */
    protected Cookie convertToCookie(IPortalCookie portalCookie, boolean secure) {
    	final Cookie cookie = new Cookie(this.cookieName, portalCookie.getValue());

    	//Set the cookie's fields
    	cookie.setComment(this.comment);
    	cookie.setMaxAge(this.maxAge);
    	cookie.setSecure(secure);
    	if (this.domain != null) {
    		cookie.setDomain(this.domain);
    	}

    	cookie.setPath(this.path);

    	return cookie;
    }
    
    /**
     * Check the {@link HttpSession} for the ID of the Portal Cookie.
     * This is useful if the customer does not wish to accept cookies.
     * 
     * @param session
     * @return
     */
    protected IPortalCookie locatePortalCookieInSession(HttpSession session) {
    	synchronized(WebUtils.getSessionMutex(session)) {
    		final String portalCookieId = (String) session.getAttribute(SESSION_ATTRIBUTE__PORTAL_COOKIE_ID);
    		if(portalCookieId == null) {
    			return null;
    		}
    		IPortalCookie portalCookie = this.portletCookieDao.getPortalCookie(portalCookieId);
    		return portalCookie;
    	}
    }
    
    /**
     * Locate the existing {@link IPortalCookie} with the request, or create a new one.
     * 
     * @param request
     * @return the {@link IPortalCookie} - never null
     */
    protected IPortalCookie getOrCreatePortalCookie(HttpServletRequest request) {
    	IPortalCookie result = null;
    	
    	// first check in request
        final Cookie cookie = this.getCookieFromRequest(this.cookieName, request);
        if (cookie != null) {
        	// found a potential cookie, call off to the dao
        	final String value = cookie.getValue();
        	result = this.portletCookieDao.getPortalCookie(value);
        } 
        
        // still null? check in the session
        if(result == null) {
        	result = locatePortalCookieInSession(request.getSession());
        }
        // if by this point we still haven't found the portal cookie, create one
        if(result == null) {
        	result = this.portletCookieDao.createPortalCookie(this.maxAge);
        	// store the portal cookie value value in the session 
        	HttpSession session = request.getSession();
        	synchronized(WebUtils.getSessionMutex(session)) {
        		session.setAttribute(SESSION_ATTRIBUTE__PORTAL_COOKIE_ID, result.getValue());
        	}
        }
        
        return result;
    }
    
    /**
     * Get THE {@link IPortalCookie} from the {@link HttpServletRequest}, if it exists.
     * Gracefully returns null if not in the request.
     * 
     * @param request
     * @return
     */
    protected IPortalCookie getPortalCookie(HttpServletRequest request) {
        final Cookie cookie = this.getCookieFromRequest(this.cookieName, request);
        if (cookie == null) {
        	// check the session
        	IPortalCookie portalCookieInSession = locatePortalCookieInSession(request.getSession());
        	if(null != portalCookieInSession) {
        		return  portalCookieInSession;
        	}

    		return null;
        }
        
        final String value = cookie.getValue();
        return this.portletCookieDao.getPortalCookie(value);
    }
    
    /**
     * Attempts to retrieve the {@link Cookie} with the specified name from the 
     * {@link HttpServletRequest}.
     * 
     * Returns the {@link Cookie} if a match is found in the request, otherwise
     * gracefully returns null.
     * 
     * @param name
     * @param request
     * @return
     */
    protected Cookie getCookieFromRequest(String name, HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) { // getCookies() returns null if there aren't any
            return null;
        }
        
        for (final Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }
        
        return null;
    }
    
}
