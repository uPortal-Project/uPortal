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

package org.jasig.portal.portlet.container;

import javax.portlet.CacheControl;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CacheControlImpl implements CacheControl {
    private String eTag;
    private int expirationTime;
    private boolean publicScope;
    private boolean cachedContent;
    
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
        this.eTag = token;
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
        this.cachedContent = useCachedContent;
    }

    /* (non-Javadoc)
     * @see javax.portlet.CacheControl#useCachedContent()
     */
    @Override
    public boolean useCachedContent() {
        return this.cachedContent;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.cachedContent ? 1231 : 1237);
        result = prime * result + ((this.eTag == null) ? 0 : this.eTag.hashCode());
        result = prime * result + this.expirationTime;
        result = prime * result + (this.publicScope ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CacheControlImpl other = (CacheControlImpl) obj;
        if (this.cachedContent != other.cachedContent) {
            return false;
        }
        if (this.eTag == null) {
            if (other.eTag != null) {
                return false;
            }
        }
        else if (!this.eTag.equals(other.eTag)) {
            return false;
        }
        if (this.expirationTime != other.expirationTime) {
            return false;
        }
        if (this.publicScope != other.publicScope) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CacheControlImpl [cachedContent=" + this.cachedContent + ", eTag=" + this.eTag + ", expirationTime=" + this.expirationTime + ", publicScope=" + this.publicScope + "]";
    }
}
