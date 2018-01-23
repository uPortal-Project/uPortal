/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.layout.dlm;

import org.w3c.dom.Document;

/**
 * Strategy for content of a DLM fragment based on the fragment owner's persisted layout.  This is
 * the original approach to DLM;  previous to 5.1, it was the exclusive strategy.
 *
 * @since 5.1
 */
public class OwnerLayoutUserView implements IUserView {

    private int userId;
    private Document layout = null;
    private int layoutId = 0;
    private int profileId = 1;

    @Override
    public int getUserId() {
        return this.userId;
    }

    @Override
    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public Document getLayout() {
        return layout;
    }

    @Override
    public void setLayout(Document layout) {
        this.layout = layout;
    }

    @Override
    public int getLayoutId() {
        return layoutId;
    }

    @Override
    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    @Override
    public int getProfileId() {
        return profileId;
    }

    @Override
    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }
}
