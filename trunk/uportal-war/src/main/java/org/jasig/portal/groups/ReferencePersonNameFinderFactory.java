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

 import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Factory for creating <code>ReferencePersonNameFinders</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class ReferencePersonNameFinderFactory implements IEntityNameFinderFactory {
    private static final Log log = LogFactory.getLog(ReferencePersonNameFinderFactory.class);
    
/**
 * ReferencePersonNameFinderFactory constructor comment.
 */
public ReferencePersonNameFinderFactory() {
        super();
}
/**
 * Return a finder instance.
 * @return org.jasig.portal.groups.IEntityNameFinder
 * @exception org.jasig.portal.groups.GroupsException
 */
public IEntityNameFinder newFinder() throws GroupsException
{
    try
        { return ReferencePersonNameFinder.singleton(); }
    catch ( SQLException sqle )
    {
        log.error(sqle.getMessage(), sqle);
        throw new GroupsException(sqle);
    }
}
}
