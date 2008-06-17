/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm.processing;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.UserPreferences;
import org.jasig.portal.layout.dlm.DistributedLayoutManager;
import org.jasig.portal.security.IPerson;

/**
 * Represents an object that can process layout parameters submitted to the
 * portal as part of a request and/or alter the SAX stream representing the
 * user's layout. Implementations of this interface can take part in acting on
 * parameters submitted to the portal dealing with layout manipulation and can
 * also take part in altering or filtering the SAX events of the raw user
 * layout. Implementations of this class enable the decoupling of
 * DistributedLayoutManager from processing needed to handle URL syntax for a
 * specific Structure and/or Theme transformation stylesheet combination. These
 * implementations are made available to the DistributedLayoutManager via
 * properties/context/layoutContext.xml.
 *
 * @author Mark Boyd
 */
public interface IParameterProcessor
{
    /**
     * Sets the IPerson and DistributedLayoutManager objects for use by this
     * object in the implementation of its characteristics.
     *
     * @param person
     * @param dlm
     */
    public void setResources(IPerson person, DistributedLayoutManager dlm);

    /**
     * Called from DistributedLayoutManager's processLayoutParameters() method
     * allowing this processor to modify the DistributedLayoutManager's state
     * or the state of this object. During rendering of a request for a user
     * the UserInstance object's renderState() method synchronizes access to
     * this call and the call that pushes layout SAX events through this object.
     * Therefore, instance variables can be set on this class as a result of
     * this method being called and then used during handling of the
     * ContentHandler event calls.
     *
     * @param prefs
     * @param request
     */
    public void processParameters(UserPreferences prefs,
            HttpServletRequest request);
}
