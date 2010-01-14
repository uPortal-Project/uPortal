/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

/** An interface that a Servant Channel must implement.  A Servant Channel is capable of providing some type of
 * interactive service within the flow of another Channel's use.  Originally designed for CGroupsManager
 * and CPermissionsManager, which can function both as standalone channels and provide the functions of 
 * selecting groups and people, and assigning permissions to them (respectively) for other channels
 *
 * @author Alex Vigdor - av317@columbia.edu
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface IServant extends IChannel{

    /** Allows the Master Channel to ascertain if the Servant has accomplished the requested task 
     * (Note that the way which a certain task is requested is not specified by this interface; 
     * normally it will be documented by a particular IServant and require some particular 
     * configuration paramaters used to initialize the servant)
     * @return boolean
     */    
    public boolean isFinished();
    
    /** Many servant channels will fulfil their function
     * by providing some set of 1 or more Objects to the Master
     * Channel.  
     * @return Object[]
     *  the expected Object type should be documented by the IServant implementation
     */    
    public Object[] getResults();
    
}

