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

package org.jasig.portal.channels.error;

import org.jasig.portal.IPermissible;

/**
 * Conveys the single permission available from CError that if granted allows
 * users to see the stack trace button. This allows PermissionsManager to show
 * the permission available from CError and grant it to other users or groups.
 * 
 * @author Mark Boyd, mboyd@sungardsct.com
 * @since uPortal 2.6.
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class SupportedPermissions implements IPermissible
{

    static final String OWNER = "UP_ERROR_CHAN";
    static final String VIEW_ACTIVITY = "VIEW";
    static final String DETAILS_TARGET = "DETAILS";
    
    /* (non-Javadoc)
     * @see org.jasig.portal.IPermissible#getActivityTokens()
     */
    public String[] getActivityTokens()
    {
        return new String[] {VIEW_ACTIVITY}; 
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IPermissible#getActivityName(java.lang.String)
     */
    public String getActivityName(String token)
    {
        return "View";
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IPermissible#getTargetTokens()
     */
    public String[] getTargetTokens()
    {
        return new String[] {DETAILS_TARGET};
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IPermissible#getTargetName(java.lang.String)
     */
    public String getTargetName(String token)
    {
        return "Details";
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IPermissible#getOwnerToken()
     */
    public String getOwnerToken()
    {
        return OWNER;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IPermissible#getOwnerName()
     */
    public String getOwnerName()
    {
        return "CError Channel";
    }
}
