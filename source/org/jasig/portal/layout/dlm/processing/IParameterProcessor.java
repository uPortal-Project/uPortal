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
 * Represents an object that can process query or post parameters submitted to 
 * the portal as part of an HTTP request. Implementations of this class enable the decoupling of
 * DistributedLayoutManager from processing needed to handle URL syntax for a
 * specific Structure and/or Theme transformation stylesheet combination. These
 * implementations are made available to the DistributedLayoutManager via
 * properties/dlmContext.xml.
 * 
 * @author mark.boyd@sungardhe.com
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
     * @param person
     * @param prefs
     * @param request
     * @param dlm
     */
    public void processParameters(UserPreferences prefs,
            HttpServletRequest request);
}
