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

package org.jasig.portal;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net </a>
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class MultithreadedPrivilegedCacheableDirectResponseCharacterChannelAdapter extends
        MultithreadedPrivilegedCacheableCharacterChannelAdapter implements IDirectResponse {

    public MultithreadedPrivilegedCacheableDirectResponseCharacterChannelAdapter(
            IMultithreadedCharacterChannel channel, String uid) throws PortalException {

        super(channel, uid);

        if (!(channel instanceof IMultithreadedDirectResponse)) {
            throw (new PortalException(
                    "MultithreadedPrivilegedCacheableDirectResponseCharacterChannelAdapter: Cannot adapt "
                            + channel.getClass().getName()));
        }
    }

    /**
     * @see org.jasig.portal.IMultithreadedDirectResponse#setResponse(String, javax.servlet.http.HttpServletResponse)
     */
    public void setResponse(HttpServletResponse response) {
        ((IMultithreadedDirectResponse) channel).setResponse(uid, response);
    }
}