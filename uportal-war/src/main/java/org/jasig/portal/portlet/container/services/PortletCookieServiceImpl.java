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
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.portal.portlet.dao.IPortletCookieDao;
import org.jasig.portal.portlet.om.IPortalCookie;
import org.jasig.portal.portlet.om.IPortletCookie;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class PortletCookieServiceImpl implements IPortletCookieService {
    
	private static final String SESSION_ATTRIBUTE__SESSION_ONLY_COOKIE_MAP = PortletCookieServiceImpl.class.getName() + ".SESSION_ONLY_COOKIE_MAP";
	
    private IPortletCookieDao portletCookieDao;
    
    private String cookieName = DEFAULT_PORTAL_COOKIE_NAME;
    private String comment = DEFAULT_PORTAL_COOKIE_COMMENT;
    private String domain = null;
    private String path = null;
    private int maxAge = (int)TimeUnit.DAYS.toSeconds(365);
    
    @Autowired
    public void setPortletCookieDao(IPortletCookieDao portletCookieDao) {
        this.portletCookieDao = portletCookieDao;
    }
    
    /**
     * @param maxAge The max number of seconds the portal cookie should live for. Defaults to 365 days. 
     */
    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

   @Override
    public void updatePortalCookie(HttpServletRequest request, HttpServletResponse response) {
        //Get the portal cookie object
        final IPortalCookie portalCookie = this.getOrCreatePortalCookie(request);
        
        //Create the browser cookie
        final Cookie cookie = this.createCookie(portalCookie, request.isSecure());
        
        //Update the expiration date of the portal cookie stored in the DB
        this.portletCookieDao.updatePortalCookieExpiration(portalCookie, cookie.getMaxAge());
        
        //Update the cookie in the users browser
        response.addCookie(cookie);
    }
    
    @Override
    public Cookie[] getAllPortletCookies(HttpServletRequest request, IPortletWindowId portletWindowId) {
        //Get the cookies from the servlet request
        Cookie[] servletCookies = request.getCookies();
        if (servletCookies == null) {
            servletCookies = new Cookie[0];
        }
        
        //Get cookies that have been set by portlets
        final IPortalCookie portalCookie = this.getPortalCookie(request);
        Set<IPortletCookie> portletCookies = Collections.emptySet();
        if(portalCookie != null) {
        	portletCookies = portalCookie.getPortletCookies();
        }
        
        // finally get portlet cookies from session (all maxAge -1)
        Map<String, Cookie> sessionOnlyPortletCookieMap = getSessionOnlyPortletCookieMap(request);
        Collection<Cookie> sessionOnlyCookies = sessionOnlyPortletCookieMap.values();
        
        //Merge into a single array
        final Cookie[] cookies = new Cookie[servletCookies.length + portletCookies.size() + sessionOnlyCookies.size()];
        System.arraycopy(servletCookies, 0, cookies, 0, servletCookies.length);

        int cookieIdx = servletCookies.length;
        for (final IPortletCookie portletCookie : portletCookies) {
            final Cookie cookie = portletCookie.toCookie(); 
            cookies[cookieIdx++] = cookie;
        }
        for(Cookie sessionOnlyCookie: sessionOnlyCookies) {
        	cookies[cookieIdx++] = sessionOnlyCookie;
        }
       
        
        return cookies;
    }
    
    @Override
    public void addCookie(HttpServletRequest request, IPortletWindowId portletWindowId, Cookie cookie) {
        final IPortalCookie portalCookie = this.getOrCreatePortalCookie(request);
        if(cookie.getMaxAge() == -1) {
        	// persist only in the session
            Map<String, Cookie> sessionOnlyPortletCookies = getSessionOnlyPortletCookieMap(request);
            sessionOnlyPortletCookies.put(cookie.getName(), cookie);
            
        } else {
        	this.portletCookieDao.addOrUpdatePortletCookie(portalCookie, cookie);
        }
        
    }
    
    /**
     * Get the {@link Map} of {@link Cookie}s stored in the {@link HttpSession} specifically
     * used for storing {@link Cookie}s with a maxAge equal to -1.
     * Will create the map if it doesn't yet exist.
     * 
     * @param request
     * @return
     */
    @SuppressWarnings("unchecked")
	protected Map<String, Cookie> getSessionOnlyPortletCookieMap(final HttpServletRequest request) {
    	final HttpSession session = request.getSession();
        synchronized(WebUtils.getSessionMutex(session)) {
        	Map<String, Cookie> sessionOnlyPortletCookies = (Map<String, Cookie>) session.getAttribute(SESSION_ATTRIBUTE__SESSION_ONLY_COOKIE_MAP);
        	if(sessionOnlyPortletCookies == null) {
        		sessionOnlyPortletCookies = new ConcurrentHashMap<String, Cookie>();
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
    protected Cookie createCookie(IPortalCookie portalCookie, boolean secure) {
        final Cookie cookie = new Cookie(this.cookieName, portalCookie.getValue());
        
        //Set the cookie's feilds
        cookie.setComment(this.comment);
        cookie.setMaxAge(this.maxAge);
        cookie.setSecure(secure);
        if (this.domain != null) {
            cookie.setDomain(this.domain);
        }
        if (this.path != null) {
            cookie.setPath(this.path);
        }
        
        return cookie;
    }
    
    protected IPortalCookie getOrCreatePortalCookie(HttpServletRequest request) {
        final Cookie cookie = this.getCookie(this.cookieName, request);
        if (cookie == null) {
            return this.portletCookieDao.createPortalCookie(this.maxAge);
        }
        
        final String value = cookie.getValue();
        final IPortalCookie portalCookie = this.portletCookieDao.getPortalCookie(value);
        if (portalCookie == null) {
            return this.portletCookieDao.createPortalCookie(this.maxAge);
        }
        
        return portalCookie;
    }
    
    protected IPortalCookie getPortalCookie(HttpServletRequest request) {
        final Cookie cookie = this.getCookie(this.cookieName, request);
        if (cookie == null) {
            return null;
        }
        
        final String value = cookie.getValue();
        return this.portletCookieDao.getPortalCookie(value);
    }
    
    protected Cookie getCookie(String name, HttpServletRequest request) {
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
