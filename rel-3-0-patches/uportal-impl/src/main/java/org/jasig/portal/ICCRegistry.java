/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * A proxy class that allows channels to contribute to inter channel
 * communication registry.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenk
</a>
 * @version $Revision$
 */
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
