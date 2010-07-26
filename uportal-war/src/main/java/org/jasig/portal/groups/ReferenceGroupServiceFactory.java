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
 * Creates the reference implemetation of <code>IGroupService</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 * @deprecated
 */

@Deprecated
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
