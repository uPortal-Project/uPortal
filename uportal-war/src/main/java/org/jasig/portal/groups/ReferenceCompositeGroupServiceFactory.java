/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.groups;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates an instance of the reference <code>ICompositeGroupService</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class ReferenceCompositeGroupServiceFactory implements ICompositeGroupServiceFactory {
    private static final Log log = LogFactory.getLog(ReferenceCompositeGroupServiceFactory.class);
    
/**
 * ReferenceGroupServiceFactory constructor.
 */
public ReferenceCompositeGroupServiceFactory() {
    super();
}
/**
 * Return an instance of the service implementation.
 * @return ICompositeGroupService
 * @exception GroupsException
 */
public ICompositeGroupService newGroupService() 
throws GroupsException
{
    try
    { 
	   ReferenceCompositeGroupService service =  new ReferenceCompositeGroupService();
	   service.initializeComponentServices();
	   return service; 
    }
    catch ( Exception ex )
    {
        log.error( "ReferenceCompositeGroupServiceFactory.newGroupService(): " + ex);
        throw new GroupsException(ex);
    }
}
}
