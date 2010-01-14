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
 * Provides default resolution of domain objects for presenting in a tree. The
 * class uses object hashCodes for the identifiers of objects and the results of 
 * toString() for label data.
 * 
 * @author Mark Boyd
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class DefaultSurrogate extends BaseSurrogateImpl
{
    /**
     * Always returns true since it can resolve any domain object using basic
     * java language constructs for representing the objects in the tree.
     */
    public boolean canResolve(Object o)
    {
        return true;
    }

    /**
     * Returns the String version of the hash code of the passed-in object.
     */
    public String getId(Object o)
    {
        return "" + o.hashCode();
    }

    /**
     * Returns the results of calling toString() on the passed in object.
     */
    public Object getLabelData(Object o)
    {
        return null;
    }
}
