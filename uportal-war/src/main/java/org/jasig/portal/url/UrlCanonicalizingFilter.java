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

package org.jasig.portal.url;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.NumberUtils;
import org.jasig.portal.user.IUserInstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Sets request info headers and forces canonical portal URLs
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class UrlCanonicalizingFilter extends OncePerRequestFilter {
    private static final String COOKIE_NAME = "UrlCanonicalizingFilter.REDIRECT_COUNT";
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private IUrlSyntaxProvider urlSyntaxProvider;
    private IUserInstanceManager userInstanceManager;
    private int maximumRedirects = 5;
    

    @Autowired
    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }
    @Autowired    
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }
    /**
     * Maximum number of consecutive redirects that are allowed. Defaults to 5.
     */
    public void setMaximumRedirects(int maximumRedirects) {
        this.maximumRedirects = maximumRedirects;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if ("GET".equals(request.getMethod())) {
            final String canonicalUrl = this.urlSyntaxProvider.getCanonicalUrl(request);
            
            final String canonicalUri;
            final int queryStringIndex = canonicalUrl.indexOf("?");
            if (queryStringIndex < 0) {
                canonicalUri = canonicalUrl;
            }
            else {
                canonicalUri = canonicalUrl.substring(0, queryStringIndex);
            }
            
            final String requestURI = request.getRequestURI();
            
            final int redirectCount = this.getRedirectCount(request);
            if (!canonicalUri.equals(requestURI)) {
                if (redirectCount < this.maximumRedirects) {
                    this.setRedirectCount(request, response, redirectCount + 1);
                    
                    final String encodeCanonicalUrl = response.encodeRedirectURL(canonicalUrl);
                    response.sendRedirect(encodeCanonicalUrl);
                    logger.debug("Redirecting from {} to canonicalized URL {}, redirect {}", new Object[] {requestURI, canonicalUri, redirectCount});
                    return;
                }

                this.clearRedirectCount(request, response);
                logger.debug("Not redirecting from {} to canonicalized URL {} due to limit of {} redirects", new Object[] {requestURI, canonicalUri, redirectCount});
            }
            else if (redirectCount > 0) {
                this.clearRedirectCount(request, response);
            }
        }
        
        final IPortalRequestInfo portalRequestInfo = this.urlSyntaxProvider.getPortalRequestInfo(request);
        final UrlType urlType = portalRequestInfo.getUrlType();
        final UrlState urlState = portalRequestInfo.getUrlState();
        
        final PortalHttpServletResponseWrapper httpServletResponseWrapper = new PortalHttpServletResponseWrapper(response);
        final PortalHttpServletRequestWrapper httpServletRequestWrapper = 
            new PortalHttpServletRequestWrapper(request, httpServletResponseWrapper, this.userInstanceManager);
        
        httpServletRequestWrapper.setHeader(IPortalRequestInfo.URL_TYPE_HEADER, urlType.toString());
        httpServletRequestWrapper.setHeader(IPortalRequestInfo.URL_STATE_HEADER, urlState.toString());

        //Hack to make PortalController work in light of https://jira.springsource.org/secure/attachment/18283/SPR7346.patch
        httpServletRequestWrapper.setHeader(IPortalRequestInfo.URL_TYPE_HEADER + "." + urlType, Boolean.TRUE.toString());
        httpServletRequestWrapper.setHeader(IPortalRequestInfo.URL_STATE_HEADER + "." + urlState, Boolean.TRUE.toString());
        
        filterChain.doFilter(httpServletRequestWrapper, httpServletResponseWrapper);
    }
    
    protected void clearRedirectCount(HttpServletRequest request, HttpServletResponse response) {
        final Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath(request.getContextPath());
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
    
    protected void setRedirectCount(HttpServletRequest request, HttpServletResponse response, int count) {
        final Cookie cookie = new Cookie(COOKIE_NAME, Integer.toString(count));
        cookie.setPath(request.getContextPath());
        cookie.setMaxAge(30);
        response.addCookie(cookie);
    }
    
    protected int getRedirectCount(HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return 0;
        }
        
        for (final Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                final String value = cookie.getValue();
                return NumberUtils.toInt(value, 0);
            }
        }
        
        return 0;
    }
}
