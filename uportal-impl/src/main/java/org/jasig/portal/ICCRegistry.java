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
 * A proxy class that allows channels to contribute to inter channel
 * communication registry.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenk
</a>
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class ICCRegistry {
    private ChannelManager cm;
    private String currentChannelSubscribeId;

    /**
     * Creates a new <code>IICRegistry</code> instance.
     *
     * @param cm a <code>ChannelManager</code> value
     * @param currentChannelSubscribeId a <code>String</code> value
     */
    public ICCRegistry(ChannelManager cm,String currentChannelSubscribeId) {
        this.cm=cm;
        this.currentChannelSubscribeId=currentChannelSubscribeId;
    }

    /**
     * Add a listener channel
     *
     * @param channelSubscribeId a <code>String</code> value
     */
    public void addListenerChannel(String channelSubscribeId) {
        cm.registerChannelDependency(channelSubscribeId,this.currentChannelSubscribeId);
    }

    /**
     * Remove a listener channel
     *
     * @param channelSubscribeId a <code>String</code> value
     */
    public void removeListenerChannel(String channelSubscribeId) {
        cm.removeChannelDependency(channelSubscribeId,this.currentChannelSubscribeId);
    }

    /**
     * Add an instructor channel (to which the current channel will listen)
     *
     * @param channelSubscribeId a <code>String</code> value
     */
    public void addInstructorChannel(String channelSubscribeId) {
        cm.registerChannelDependency(this.currentChannelSubscribeId,channelSubscribeId);        
    }

    /**
     * Remove an instructor channel
     *
     * @param channelSubscribeId a <code>String</code> value
     */
    public void removeInstructorChannel(String channelSubscribeId){
        cm.removeChannelDependency(this.currentChannelSubscribeId,channelSubscribeId);
    }
}
