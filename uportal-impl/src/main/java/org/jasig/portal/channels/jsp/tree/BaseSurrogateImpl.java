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
 * A base class for surrogates providing default functionality that can be used
 * by subclasses if appropriate and alleviating them from having to implement 
 * identical methods to conform to the ISurrogate interface.
 * 
 * @author Mark Boyd
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public abstract class BaseSurrogateImpl implements ISurrogate
{

    /**
     * Returns null meaning that the domain object has no aspects.
     */
    public Object[] getAspects(Object o)
    {
        return null;
    }
    /**
     * Returns null meaning that the domain object has no children.
     */
    public Object[] getChildren(Object o)
    {
        return null;
    }
    /**
     * Returns null meaning that the domain object has no object specific label 
     * data for use in custome rendering.
     */
    public Object getLabelData(Object o)
    {
        return null;
    }
    /**
     * Returns false meaning that the domain object has no aspects.
     */
    public boolean hasAspects(Object o)
    {
        return false;
    }
    /**
     * Returns false meaning that the domain object has no children.
     */
    public boolean hasChildren(Object o)
    {
        return false;
    }
    /**
     * Returns false meaning that the domain object can't contain children.
     */
    public boolean canContainChildren(Object o)
    {
        return false;
    }
}
