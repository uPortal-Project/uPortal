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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides a static wrapper around an actual {@link IXalanAuthorizationHelper}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service
public class XalanAuthorizationHelper {
    private static IXalanAuthorizationHelper authorizationHelper;
    
    @Autowired
    public void setAuthorizationHelper(IXalanAuthorizationHelper authorizationHelper) {
        XalanAuthorizationHelper.authorizationHelper = authorizationHelper;
    }

    /**
     * @see org.jasig.portal.security.xslt.IXalanAuthorizationHelper#canRender(java.lang.String, java.lang.String)
     */
    public static boolean canRender(final String userName, final String channelFName) {
        return authorizationHelper.canRender(userName, channelFName);
    }
}
