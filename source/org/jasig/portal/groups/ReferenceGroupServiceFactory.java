/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates the reference implemetation of <code>IGroupService</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 * @deprecated
 */

public class ReferenceGroupServiceFactory implements IGroupServiceFactory {
    
    private static final Log log = LogFactory.getLog(ReferenceGroupServiceFactory.class);
    
/**
 * ReferenceGroupServiceFactory constructor.
 */
public ReferenceGroupServiceFactory() {
        super();
}
/**
 * Return an instance of the service implementation.
 * @return org.jasig.portal.groups.IGroupService
 * @exception org.jasig.portal.groups.GroupsException
 */
public IGroupService newGroupService() throws GroupsException
{
    try
        { return ReferenceGroupService.singleton(); }
    catch ( GroupsException ge )
    {
        log.error(ge.getMessage(), ge);
        throw new GroupsException(ge);
    }
}
}
