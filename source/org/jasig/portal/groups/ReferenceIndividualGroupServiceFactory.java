/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates an instance of the reference <code>IIndividualGroupService</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class ReferenceIndividualGroupServiceFactory implements IComponentGroupServiceFactory {
    
    private static final Log log = LogFactory.getLog(ReferenceIndividualGroupServiceFactory.class);
    
/**
 * ReferenceGroupServiceFactory constructor.
 */
public ReferenceIndividualGroupServiceFactory() {
    super();
}
/**
 * Return an instance of the service implementation.
 * @return IIndividualGroupService
 * @exception GroupsException
 */
public IComponentGroupService newGroupService() throws GroupsException
{
    return newGroupService(new ComponentGroupServiceDescriptor());
}
/**
 * Return an instance of the service implementation.
 * @return IIndividualGroupService
 * @exception GroupsException
 */
public IComponentGroupService newGroupService(ComponentGroupServiceDescriptor svcDescriptor) 
throws GroupsException
{
    try
        { return new ReferenceIndividualGroupService(svcDescriptor); }
    catch ( GroupsException ge )
    {
        log.error( "ReferenceIndividualGroupServiceFactory.newGroupService(): " + ge);
        throw new GroupsException(ge.getMessage());
    }
}
}
