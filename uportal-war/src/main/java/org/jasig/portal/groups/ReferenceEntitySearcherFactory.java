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

import org.jasig.portal.groups.local.EntitySearcherImpl;
import org.jasig.portal.groups.local.ITypedEntitySearcher;
import org.jasig.portal.groups.local.searchers.RDBMChannelDefSearcher;
import org.jasig.portal.groups.local.searchers.RDBMPersonSearcher;

/**
 * Creates an instance of the reference <code>IEntitySearcher</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class ReferenceEntitySearcherFactory implements IEntitySearcherFactory {
/**
 * ReferenceGroupServiceFactory constructor.
 */
public ReferenceEntitySearcherFactory() {
    super();
}
/**
 * Return an instance of the entity searcher implementation.
 * @return IEntitySearcher
 * @exception GroupsException
 */
public IEntitySearcher newEntitySearcher() throws GroupsException
{
    return newInstance();
}
/**
 * Return an instance of the entity searcher implementation.
 * @return IEntitySearcher
 * @exception GroupsException
 */
public IEntitySearcher newInstance() throws GroupsException
{
    ITypedEntitySearcher[] tes = new ITypedEntitySearcher[2];
    tes[0]=new RDBMChannelDefSearcher();
    tes[1]=new RDBMPersonSearcher();
    IEntitySearcher entitySearcher = new EntitySearcherImpl(tes);
    return entitySearcher;
}
}
