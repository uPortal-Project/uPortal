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

package org.jasig.portal.url.xml;

import org.jasig.portal.url.ILayoutPortalUrl;

/**
 * Wrapper around a {@link ILayoutPortalUrl} to make use from XSLT easier
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XsltLayoutPortalUrl extends XsltBasePortalUrl {
    private final ILayoutPortalUrl layoutPortalUrl;

    XsltLayoutPortalUrl(ILayoutPortalUrl layoutPortalUrl) {
        super(layoutPortalUrl);
        this.layoutPortalUrl = layoutPortalUrl;
    }

    /**
     * @see ILayoutPortalUrl#setAction(boolean)
     */
    public void setAction(String action) {
        if (action != null && (action = action.trim()).length() > 0) {
            this.layoutPortalUrl.setAction(Boolean.parseBoolean(action));
        }
    }
    
    /**
     * @see ILayoutPortalUrl#setLayoutParameter(String, String...)
     */
    public void setLayoutParameter(String name, String value) {
        this.layoutPortalUrl.setLayoutParameter(name, value);
    }

    /**
     * @see ILayoutPortalUrl#addLayoutParameter(String, String...)
     */
    public void addLayoutParameter(String name, String value) {
        this.layoutPortalUrl.addLayoutParameter(name, value);
    }

    @Override
    public int hashCode() {
        return this.layoutPortalUrl.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        XsltLayoutPortalUrl other = (XsltLayoutPortalUrl) obj;
        if (this.layoutPortalUrl == null) {
            if (other.layoutPortalUrl != null) {
                return false;
            }
        }
        else if (!this.layoutPortalUrl.equals(other.layoutPortalUrl)) {
            return false;
        }
        return true;
    }
}
