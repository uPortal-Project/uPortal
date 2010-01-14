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

/**
 * Channels implementing this interface are considered "privileged" because
 * they will have access to internal portal control structures such as the
 * HttpServletRequest, HttpServletReponse, and UserPreferencesManager.  IPrivilegedChannel
 * is intended for channels that are integral to the framework such as those that
 * manage user preferences, channel publishing, and channel subscription.
 * IPrivilegedChannel is NOT intended for "normal" channels.  Channels
 * should normally implement {@link IChannel}.
 *
 * Portal administrators should only allow publishing/subscibing of channels
 * implementing IPrivilegedChannel if the following are true:
 *
 * <ul>
 * <li>The channel is an integral part of the uPortal framework, e.g. {@link org.jasig.portal.channels.CLogin}.</li>
 * <li>The channel is well-understood and will not cause harm.  An understanding of the
 * portal architecture is necessary to determine if this is true.</li>
 * <li>There is no way to implement the channel as an IChannel because access to
 * internal structures is absolutely necessary.</li>
 * </ul>
 * @author Peter Kharchenko, pkharchenko@interactivebusiness.com
 * @version $Revision$
 * @see IChannel
 * @see PortalControlStructures
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface IPrivilegedChannel extends IChannel, IPrivileged {}

