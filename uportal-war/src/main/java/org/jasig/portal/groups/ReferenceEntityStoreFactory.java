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
 * Creates an instance of the reference <code>IEntityStore</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class ReferenceEntityStoreFactory implements IEntityStoreFactory {
    
    private static final Log log = LogFactory.getLog(ReferenceEntityStoreFactory.class);
    
/**
 * ReferenceGroupServiceFactory constructor.
 */
public ReferenceEntityStoreFactory() {
    super();
}
/**
 * Return an instance of the entity store implementation.
 * @return IEntityStore
 * @exception GroupsException
 */
public IEntityStore newEntityStore() throws GroupsException
{
    return newInstance();
}
/**
 * Return an instance of the entity store implementation.
 * @return IEntityStore
 * @exception GroupsException
 */
public IEntityStore newInstance() throws GroupsException
{
    try
        { return new RDBMEntityStore(); }
    catch ( Exception ex )
    {
        log.error( "ReferenceEntityStoreFactory.newInstance(): " + ex);
        throw new GroupsException(ex);
    }
}
}
