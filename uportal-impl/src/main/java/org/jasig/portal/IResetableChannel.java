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
 * Describes an IChannel that can handle being refereshed or reset by the container
 * after an error.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface IResetableChannel extends IChannel {
    /**
     * Prepare to be refereshed after an error. Information about the last
     * request should be cleared and the channel should be in a renderable
     * state when this method returns.
     * 
     * If the channel also implements {@link IPrivileged} {@link IPrivileged#setPortalControlStructures(PortalControlStructures)}
     * must be called before this method. {@link IChannel#setRuntimeData(ChannelRuntimeData)} must also be called before this method.
     */
    public void prepareForRefresh();
    
    /**
     * Prepare to be reset after an error. This channel instance may be destroyed
     * and re-created during the reset process so this method gives the old channel
     * a chance to clean up before the reset. State information stored anywhere outside
     * of {@link ChannelRuntimeData} should be cleared.
     * 
     * If the channel also implements {@link IPrivileged} {@link IPrivileged#setPortalControlStructures(PortalControlStructures)}
     * must be called before this method. {@link IChannel#setRuntimeData(ChannelRuntimeData)} must also be called before this method.
     */
    public void prepareForReset();
}
