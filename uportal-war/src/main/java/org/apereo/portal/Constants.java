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
package org.apereo.portal;

/**
 * Houses constants used in the portal code base. Developers, any constants added here should be
 * clearly documented.
 *
 */
public class Constants {

    /**
     * The query parameter name that can be appended to a baseActionUrl along with its value, the
     * fname of a channel, to cause an instance of that channel to appear in focused mode without
     * the user having to subcribe to that channel. Examples are infrastructure channels like
     * CChannelManager and CUserPreferences. They don't reside in a user's layout but are merged in
     * via the fname functionality as needed.
     */
    public static final String FNAME_PARAM = "uP_fname";

    /**
     * The request parameter name that can be appended to a baseActionURL along with its value, the
     * locales to which the portal should assign priority. These locales will be a priority for the
     * remainder of a user's session. The value of this parameter should be a comma-delimited list
     * of locale codes. For example, en_US,ja_JP,de_DE
     */
    public static final String LOCALES_PARAM = "uP_locales";

    /**
     * The name of a category into which automatically published channels from a channel archive are
     * placed. It is expected to be found in the root category and if not found during publishing is
     * automatically created.
     */
    public static final String AUTO_PUBLISH_CATEGORY = "Auto-Published";

    /**
     * The default functional name of the administrative links channel. This is used by channels
     * designed for being delegated to from the administrative links channel that return to that
     * channel when the user is finished with their functionality. Returning to the links channel is
     * accomplished through the use of a URL obtained from ChannelRuntimeData.getFnameActionUrl
     * passing a functional name. The value of this field can be passed to that method to obtain a
     * URL that will bring that channel into focus.
     */
    public static final String NAVIGATION_CHAN_FNAME = "admin_navigation_links";

    /**
     * The prefix of DTDs owned by uPortal version 2.0. This is used to allow local resolution of
     * uPortal specific DTDs.
     */
    public static final String UPORTAL_2_DTD_PREFIX =
            "http://www.ja-sig.org/products/uportal/2/dtds/";

    /**
     * The attribute used to convey a custom template account from which to copy the initial layout
     * for a user.
     */
    public static final String TEMPLATE_USER_NAME_ATT = "uPortalTemplateUserName";
}
