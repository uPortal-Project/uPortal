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

package org.jasig.portal.layout.dlm;

import org.jasig.portal.UserProfile;
import org.w3c.dom.Document;


/**
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
class UserView
{
    public static final String RCS_ID = "@(#) $Header$";

    private int userId = -1;
    DistributedUserPreferences structUserPrefs = null;
    DistributedUserPreferences themeUserPrefs = null;
    Document layout = null;
    int layoutId = 0;
    int profileId = 1;
    String profileFname = "default"; /* Was 1 when profileId was the key. */
    int structureStylesheetId = 0;
    int themeStylesheetId = 0;

    UserView (int fragmentOwnerUserId)
    {
        this.userId = fragmentOwnerUserId;
    }
    
    UserView(int fragmentOwnerUserId, UserProfile profile, Document layout,
            DistributedUserPreferences ssup, DistributedUserPreferences tsup)
    {
        this.userId = fragmentOwnerUserId;
        layoutId = profile.getLayoutId();
        profileId = profile.getProfileId();
        profileFname = profile.getProfileFname();
        structureStylesheetId = profile.getStructureStylesheetId();
        themeStylesheetId = profile.getThemeStylesheetId();
        this.layout = layout;
        structUserPrefs = ssup;
        themeUserPrefs = tsup;
    }
    
    public int getUserId() {
        return this.userId;
    }
    
}
