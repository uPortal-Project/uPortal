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
package org.apereo.portal.layout.dlm.pluggable;

import org.apereo.portal.layout.dlm.IUserView;
import org.apereo.portal.layout.dlm.OwnerLayoutUserView;
import org.apereo.portal.security.IPerson;
import org.w3c.dom.Document;

/**
 * This {@link IUserView} implementation provides fragment content based on Spring-managed beans.
 *
 * @since 5.1
 */
public class PluggableBeanUserView extends OwnerLayoutUserView {

    @Override
    public Document getFragmentContentForUser(IPerson user) {
        throw new UnsupportedOperationException("WIP");
    }

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
