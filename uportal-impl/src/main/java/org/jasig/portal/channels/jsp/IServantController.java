/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.jsp;

/**
 * An interface that an instance of IController for the jsp channel type must
 * implement to be used within the ServantChannel subclass of the jsp channel
 * type and hence can provide servant capabilities. This interface provides the
 * same functionality for the jsp channel type that IServant does for IChannel.
 * 
 * @author Mark Boyd
 * @version $Revision$
 */
public interface IServantController 
{

    /**
     * Enables the containing channel to change configuration of the servant if
     * supported by the servant channel at any point in time. If this method is 
     * called the servant must discard its current state and reconfigure itself
     * according to the information passed in via this method. The type of 
     * object allowed for a given servant must be information obtained from 
     * documentation or other means.
     * 
     * @param o
     */
    public void configure(Object o);

    /**
     * Allows the Master Channel to ascertain if the Servant has accomplished
     * the requested task (Note that the way which a certain task is requested
     * can be specified by this interface or through some particular
     * configuration paramaters used to initialize the servant.)
     * 
     * @return boolean
     */    
    public boolean isFinished();
    
    /**
     * Many servant channels will fulfill their function by providing some set of
     * 1 or more Objects to the Master Channel.
     * 
     * @return Object[] the expected Object type should be documented by the
     *         IServant implementation
     */    
    public Object[] getResults();
}

