package org.jasig.portal.layout.dlm;

import org.jasig.portal.PortalException;

/**
 * Represents a layout processing action that should be applied to a user's
 * layout and contains the ability to apply that specific action. Node changes
 * that need to be made to the ILF and PLF of a user must not be made until all
 * changes have been identified and reviewed to see if they are allowed by an
 * owning fragment and additionally in the case of channels by the channel
 * definition. After all such actions have been identified and approved then
 * each implementation of this interface applies its changes in an appropriate
 * manner based on whether the node is incorporated from a fragment or owned by
 * the user.
 * 
 * @author mboyd@sungardsct.com
 */
public interface ILayoutProcessingAction
{
    public void perform() throws PortalException;
}
