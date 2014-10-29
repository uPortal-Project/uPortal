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

package org.jasig.portal.portlet.container.cache;

import javax.portlet.CacheControl;

import org.apache.commons.lang.StringUtils;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CacheControlImpl implements CacheControl {
	private String eTag;
    // PLT.22.1 default expiration time is 0 ("always expired")
    private int expirationTime = 0;
    // PLT.22.1 cache scope is assumed private by default
    private boolean publicScope = false;
    private boolean useCachedContent = false;
    
	/* (non-Javadoc)
     * @see javax.portlet.CacheControl#getETag()
     */
    @Override
    public String getETag() {
        return this.eTag;
    }

    /* (non-Javadoc)
     * @see javax.portlet.CacheControl#getExpirationTime()
     */
    @Override
    public int getExpirationTime() {
        return this.expirationTime;
    }

    /* (non-Javadoc)
     * @see javax.portlet.CacheControl#isPublicScope()
     */
    @Override
    public boolean isPublicScope() {
        return this.publicScope;
    }

    /* (non-Javadoc)
     * @see javax.portlet.CacheControl#setETag(java.lang.String)
     */
    @Override
    public void setETag(String token) {
        this.eTag = StringUtils.trimToNull(token);
    }

    /* (non-Javadoc)
     * @see javax.portlet.CacheControl#setExpirationTime(int)
     */
    @Override
    public void setExpirationTime(int time) {
        this.expirationTime = time;
    }

    /* (non-Javadoc)
     * @see javax.portlet.CacheControl#setPublicScope(boolean)
     */
    @Override
    public void setPublicScope(boolean publicScope) {
        this.publicScope = publicScope;
    }

    /* (non-Javadoc)
     * @see javax.portlet.CacheControl#setUseCachedContent(boolean)
     */
    @Override
    public void setUseCachedContent(boolean useCachedContent) {
        this.useCachedContent = useCachedContent;
    }

    /* (non-Javadoc)
     * @see javax.portlet.CacheControl#useCachedContent()
     */
    @Override
    public boolean useCachedContent() {
        return this.useCachedContent;
    }

    @Override
    public String toString() {
        return "CacheControlImpl [eTag=" + eTag + ", expirationTime=" + expirationTime + ", publicScope=" + publicScope
                + ", useCachedContent=" + useCachedContent + "]";
    }
}
