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
package org.apereo.portal.io.xml.layout;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apereo.portal.IUserIdentityStore;
import org.apereo.portal.IUserProfile;
import org.apereo.portal.UserProfile;
import org.apereo.portal.io.xml.crn.AbstractDom4jExporter;
import org.apereo.portal.layout.IUserLayoutStore;
import org.apereo.portal.security.provider.BrokenSecurityContext;
import org.apereo.portal.security.provider.PersonImpl;
import org.apereo.portal.utils.Tuple;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;

/**
 */
public class LayoutExporter extends AbstractDom4jExporter {
    private final Cache<Tuple<String, String>, UserProfile> layoutCache =
            CacheBuilder.newBuilder().maximumSize(1000).<Tuple<String, String>, UserProfile>build();
    private final Cache<Tuple<String, String>, Document> profileCache =
            CacheBuilder.newBuilder().maximumSize(1000).<Tuple<String, String>, Document>build();

    private IUserLayoutStore userLayoutStore;
    private IUserIdentityStore userIdentityStore;

    @Autowired
    public void setUserLayoutStore(IUserLayoutStore userLayoutStore) {
        this.userLayoutStore = userLayoutStore;
    }

    @Autowired
    public void setUserIdentityStore(IUserIdentityStore userIdentityStore) {
        this.userIdentityStore = userIdentityStore;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.io.xml.crn.AbstractDom4jExporter#exportDataElement(java.lang.String)
     */
    @Override
    protected Element exportDataElement(String userName) {
        final Integer userId = this.userIdentityStore.getPortalUserId(userName);
        if (userId == null) {
            this.logger.warn("No user " + userName + " found, no layout will be exported");
            return null;
        }

        //Setup empty IPerson used to interact with the layout store
        final PersonImpl person = new PersonImpl();
        person.setUserName(userName);
        person.setID(userId);
        person.setSecurityContext(new BrokenSecurityContext());

        try {
            this.userLayoutStore.setProfileImportExportCache(layoutCache);
            this.userLayoutStore.setLayoutImportExportCache(profileCache);

            final IUserProfile userProfile =
                    userLayoutStore.getUserProfileByFname(
                            person, UserProfile.DEFAULT_PROFILE_FNAME);
            final Element layoutElement = userLayoutStore.exportLayout(person, userProfile);

            return layoutElement;
        } finally {
            this.userLayoutStore.setProfileImportExportCache(null);
            this.userLayoutStore.setLayoutImportExportCache(null);
        }
    }
}
