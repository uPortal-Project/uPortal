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

package org.jasig.portal.channels.adminnav;

import org.jasig.portal.ICacheable;
import org.jasig.portal.IChannel;
import org.jasig.portal.security.IAuthorizationPrincipal;

/**
 * Represents a pluggable facility for implementing the administrative navigation
 * channel's functionality.
 *
 * @author mboyd@sungardsct.com
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface INavigationModel extends ICacheable, IChannel, ILinkRegistrar
{
    /**
     * Answers true if the user represented by the passed-in authorization
     * principal can access any of the channels pointed
     *
     * @param ap
     * @return boolean
     */
    public boolean canAccess(IAuthorizationPrincipal ap);
}
