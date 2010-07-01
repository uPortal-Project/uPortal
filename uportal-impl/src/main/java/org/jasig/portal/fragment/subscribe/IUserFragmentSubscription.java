/**
 * 
 */
package org.jasig.portal.fragment.subscribe;

import org.jasig.portal.IBasicEntity;

/**
 * IUserFragmentSubscription represents a fragment subscription for an 
 * individual user.  Subscriptions are used to track preferences for
 * pulled DLM fragments.
 * 
 * @author Mary Hunt
 * @version $Revision$ $Date$
 */
public interface IUserFragmentSubscription extends IBasicEntity {

    /**
     * Get the internal unique user ID for the end user with which this
     * subscription is associated.
     * 
     * @return
     */
    public int getUserId();

    /**
     * Get the unique string username of the owner of the subscribed-to
     * fragment.
     * 
     * @return
     */
    public String getFragmentOwner();

    /**
     * Return <code>true</code> if this fragment subscription is active, 
     * <code>false</code> if inactive/deleted.
     * 
     * @return
     */
    public boolean isActive();

    /**
     * Mark this fragment as inactive/deleted.
     */
    public void setInactive();

    /**
     * Set a flag indicating whether this fragment is currently active.
     * 
     * @param active
     */
    public void setActive(boolean active);

    /**
     * Get the unique ID associated with this subscription.
     * 
     * @return
     */
    public long getId();

}
