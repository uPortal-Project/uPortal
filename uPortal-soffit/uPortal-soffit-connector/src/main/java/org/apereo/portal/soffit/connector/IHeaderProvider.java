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
package org.apereo.portal.soffit.connector;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.apache.http.Header;

/**
 * Concrete implementations of this interface know how to produce the headers (and their values)
 * sent by the {@link SoffitConnectorController} to the remote Soffit.
 *
 * @since 5.0
 */
public interface IHeaderProvider {

    /**
     * Prepares an appropriate HTTP header for inclusion in the outbound request to the remote
     * soffit. May return <code>null</code>, in which case the header should be ignored.
     *
     * @param renderRequest The current <code>RenderRequest</code>
     * @param renderResponse The current <code>RenderResponse</code>
     * @return Ann appropriate HTTP header or <code>null</code>
     */
    Header createHeader(RenderRequest renderRequest, RenderResponse renderResponse);
}
