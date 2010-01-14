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

package org.jasig.portal.channels.jsp.tree;


/**
 * Represents a trivial default implementation of IDomainActionSet that provides
 * no supported actions for the tree.
 *  
 * @author Mark Boyd
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class DefaultDomainActionSet implements IDomainActionSet
{

    /**
     * Returns null indicating that no domain specific actions are supported 
     * for objects in the tree.
     */
    public String[] getSupportedActions()
    {
        return null;
    }

    /**
     * Returns null since it will never get called due to no domain actions 
     * being supported by this class.
     */
    public Object getLabelData(String action, Object domainObject)
    {
        return null;
    }

}
