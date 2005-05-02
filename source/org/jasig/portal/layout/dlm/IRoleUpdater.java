/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import java.util.List;

/**
 * @author mboyd
 *
 * Interface added solely to get around threads needing to be tagged when 
 * updating group membership information from within the uPortal code base.
 * Fragment owners do not have proper roles. To fix this problem bug 19794
 * was opened and this interface enables schools to declare which roles a
 * fragment owner should have and propagate that configuration into LDAP using
 * our groups manager. But we can't have any compile time dependancies so this
 * interface allows us to place the implementing code over in the cp tree and
 * instantiate the class via the class name and call it via its implementation
 * of this interface.
 */
public interface IRoleUpdater 
{
    /**
     * Changes the roles for this owner to be only those indicated in the 
     * passed in List.
     * 
     * @param owner
     * @param roles
     */
    public void setFragmentOwnerRoles( String owner, List roles );
}
