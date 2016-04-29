/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.layout.dlm;

import org.w3c.dom.Document;

/**
 * @since uPortal 2.5
 */
/* package-private */ class UserView {

    private final int userId;
    private Document layout = null;
    private int layoutId = 0;
    private int profileId = 1;

    UserView (int fragmentOwnerUserId) {
        this.userId = fragmentOwnerUserId;
    }

    public int getUserId() {
        return this.userId;
    }

    public Document getLayout() {
        return layout;
    }

    public void setLayout(Document layout) {
        this.layout = layout;
    }

    public int getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

}
