package org.jasig.portal;

import java.util.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*; 
import org.xml.sax.DocumentHandler;


/** 
 * An interface presented by a channel to a portal.
 * @author Peter Kharchenko
 * @version $Revision$
 */

public interface IChannel
{


    // following section allows portal to pass parameter/settings information to the channel
    
    /**
     * Passes ChannelStaticData to the channel.
     * This is done during channel instantiation time.
     * see org.jasig.portal.StaticData
     * @param sd channel static data
     * @see ChannelStaticData
     */
    public void setStaticData (ChannelStaticData sd);
    
    /**
     * Passes ChannelRuntimeData to the channel.
     * This function is called prior to the renderXML() call.
     * @param rd channel runtime data
     * @see ChannelRuntimeData
     */
    public void setRuntimeData(ChannelRuntimeData rd);

    /**
     * Passes an outside event to a channel.
     * Events should normally come from the LayoutBean.
     * @param ev LayoutEvent object
     * @see LayoutEvent
     */
    public void receiveEvent(LayoutEvent ev);


    // following section allows channel to pass parameter/settings information to the portal
    
    /**
     * Acquires ChannelSubscriptionProperties from the channel.
     * This function should be called at the Publishing/Subscription time. 
     * @see ChannelSubscriptionProperties
     */
    public ChannelSubscriptionProperties getSubscriptionProperties();

    /**
     * Acquires ChannelRuntimeProperites from the channel.
     * This function may be called by the portal framework throughout the session.
     * @see ChannelRuntimeProperties
     */
    public ChannelRuntimeProperties getRuntimeProperties();
    


    // rendering Layer

    /**
     * Ask channel to render its content.
     * @param out the SAX DocumentHandler to output content to
     */
    public void renderXML (DocumentHandler out);    

}
