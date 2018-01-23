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

import org.apereo.portal.security.IPerson;
import org.w3c.dom.Document;

/**
 * Represents a ready-to-use DLM fragment and supports pluggable strategies for layout content.  The
 * UserView concept has existed since the dawn of DLM, but this interface is a recent
 * addition.  Originally DLM supported only one strategy for layout content:  the layout owner's
 * persisted layout.  Refactoring the <code>UserView</code> into an interface-based abstraction
 * opens the door to pluggable strategies, such as metadata matching.
 *
 * <p>Concrete implementations must provide a no-arg constructor.
 *
 * @since 5.1
 */
public interface IUserView {

    /**
     * Obtains the userId (numeric) of the fragment owner account.
     */
    int getUserId();

    /**
     * Updates the userId (numeric) of the fragment owner account.
     */
    void setUserId(int userId);

    /**
     * Obtains the "fragmentized" (processed) layout Document of the fragment owner account.
     */
    Document getLayout();

    /**
     * Updates the "fragmentized" (processed) layout Document of the fragment owner account.
     */
    void setLayout(Document layout);

    /**
     * Obtains the id of the fragment owner's layout associated with the fragment.
     */
    int getLayoutId();

    /**
     * Updates the id of the fragment owner's layout associated with the fragment.
     */
    void setLayoutId(int layoutId);

    /**
     * Obtains the id of the fragment owner's profile associated with the fragment.
     */
    int getProfileId();

    /**
     * Updates the id of the fragment owner's profile associated with the fragment.
     */
    void setProfileId(int profileId);

    /**
     * Obtains the content stemming from this fragment that will be applied to the specified user's
     * layout.  The returned <code>Document</code> may or may not be the same as the value of
     * <code>getLayout</code>.
     *
     * @since 5.1
     */
    Document getFragmentContentForUser(IPerson user);

//    Sample XML Output for getFragmentContentForUser()
//
//    <?xml version="1.0"?>
//    <layout xmlns:dlm="http://www.uportal.org/layout/dlm" ID="u25l1">
//      <folder ID="u25l1s1" dlm:fragment="0" dlm:precedence="80.0" hidden="false" immutable="false" locale="en_US" name="Root folder" type="root" unremovable="true">
//        <folder ID="u25l1s100" dlm:fragment="0" dlm:precedence="80.0" hidden="false" immutable="true" locale="en_US" name="Page Top folder" type="page-top" unremovable="true">
//          <channel ID="u25l1n110" chanID="41" description="Compiles Bootstrap LESS dynamically, allowing administrators to make some skin configuration choices in a UI.  Supports the optional 'dynamic' strategy for Respondr." dlm:fragment="0" dlm:precedence="80.0" fname="dynamic-respondr-skin" hidden="false" immutable="false" locale="en_US" name="Dynamic Respondr Skin" timeout="30000" title="Dynamic Respondr Skin" typeID="3" unremovable="false">
//            <parameter name="mobileIconUrl" value="/ResourceServingWebapp/rs/tango/0.8.90/32x32/apps/preferences-desktop-theme.png"/>
//            <parameter name="iconUrl" value="/ResourceServingWebapp/rs/tango/0.8.90/32x32/apps/preferences-desktop-theme.png"/>
//            <parameter name="disableDynamicTitle" value="true"/>
//            <parameter name="configurable" value="true"/>
//          </channel>
//        </folder>
//      </folder>
//    </layout>
}
