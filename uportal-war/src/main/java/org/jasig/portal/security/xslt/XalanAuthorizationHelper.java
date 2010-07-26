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

package org.jasig.portal.security.xslt;

import org.jasig.portal.spring.locator.XalanAuthorizationHelperLocator;

/**
 * Provides a Spring locating facade in front of an actual {@link IXalanAuthorizationHelper} since the Xalan
 * elements can only instantiate classes directly.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XalanAuthorizationHelper implements IXalanAuthorizationHelper {
    private final IXalanAuthorizationHelper authorizationHelper;
    
    public XalanAuthorizationHelper() {
        this.authorizationHelper = XalanAuthorizationHelperLocator.getXalanAuthorizationHelper();
    }

    /**
     * @see org.jasig.portal.security.xslt.IXalanAuthorizationHelper#canRender(java.lang.String, java.lang.String)
     */
    public boolean canRender(final String userName, final String channelFName) {
        return this.authorizationHelper.canRender(userName, channelFName);
    }
}
