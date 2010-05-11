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

package org.jasig.portal.portlet.url;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.container.ResourceURLProvider;
import org.jasig.portal.portlet.om.IPortletWindow;

/**
 * Simple handling for resource URL generation
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ResourceUrlProviderImpl implements ResourceURLProvider {
    private final IPortletWindow portletWindow;
    private final HttpServletRequest containerRequest;
    private final HttpServletResponse containerResponse;
    
    private String path = null;
    
   public ResourceUrlProviderImpl(IPortletWindow portletWindow, HttpServletRequest containerRequest, HttpServletResponse containerResponse) {
        this.portletWindow = portletWindow;
        this.containerRequest = containerRequest;
        this.containerResponse = containerResponse;
    }

    /*
    * (non-Javadoc)
    * @see org.apache.pluto.container.ResourceURLProvider#setAbsoluteURL(java.lang.String)
    */
    @Override
    public void setAbsoluteURL(String path) {
        this.path = path;
    }

   /*
    * (non-Javadoc)
    * @see org.apache.pluto.container.ResourceURLProvider#setFullPath(java.lang.String)
    */
    @Override
    public void setFullPath(String path) {
        this.path = path;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.path;
    }
}
