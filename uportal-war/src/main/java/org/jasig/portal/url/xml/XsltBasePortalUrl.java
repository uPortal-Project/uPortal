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

import org.jasig.portal.url.IBasePortalUrl;

/**
 * Wrapper around a {@link IBasePortalUrl} that makes it easier to use via XSLTC
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XsltBasePortalUrl {
    IBasePortalUrl basePortalUrl;

    XsltBasePortalUrl() {
        this.basePortalUrl = null;
    }
    XsltBasePortalUrl(IBasePortalUrl basePortalUrl) {
        this.basePortalUrl = basePortalUrl;
    }

    public void setPortalParameter(String name, String value) {
        if (this.basePortalUrl == null) {
            return;
        }
        this.basePortalUrl.setPortalParameter(name, value);
    }
    
    public void addPortalParameter(String name, String value) {
        if (this.basePortalUrl == null) {
            return;
        }
        this.basePortalUrl.addPortalParameter(name, value);
    }

    public String getUrlString() {
        if (this.basePortalUrl == null) {
            return "";
        }
        return this.basePortalUrl.getUrlString();
    }

    @Override
    public String toString() {
        if (this.basePortalUrl == null) {
            return "";
        }
        return this.basePortalUrl.toString();
    }

    @Override
    public int hashCode() {
        if (this.basePortalUrl == null) {
            return 0;
        }
        return this.basePortalUrl.hashCode();
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
        XsltBasePortalUrl other = (XsltBasePortalUrl) obj;
        if (this.basePortalUrl == null) {
            if (other.basePortalUrl != null) {
                return false;
            }
        }
        else if (!this.basePortalUrl.equals(other.basePortalUrl)) {
            return false;
        }
        return true;
    }
}
