package org.jasig.portal;


/*
 * An interface that allows "special" channels gain
 * control over most internal portal functions.
 * Use with great caution. 
 * Allow for publishing/subscriptions of such channels
 * if and only if the following hold:
 * <ul>
 *  <li> Special channel is an integral part of the uPortal framework.<br/>
 * (such as CLayoutManager, CPublish, etc.) </li>
 * <li> You understand 100% what the channel is doing and certain that
 * it will not cause harm (that implies understanding of portal architecture) </li>
 * <li> There is no other way to circumvent the problem, and access to internal structures
 * is absolutely necessary. </li>
 * </ul>
 * @author Peter Kharchenko
 * @version $Revision$
 */ 
public interface ISpecialChannel extends IChannel {

    /*
     * Passes portal control structure to the channel.
     * @see PortalControlStructures
     */
    public void setPortalControlStructures(PortalControlStructures pcs);

}
    
