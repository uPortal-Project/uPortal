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
 * Implementations of this class represent an object that knows how to acquire
 * specific pieces of information needed to present a node in a hierarchical 
 * tree view.
 * 
 * @author Mark Boyd
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface ISurrogate
{
    /**
     * Determines if the passed in Object is an instance of an object for which
     * this class can act as a surrogate.
     * @param o
     * @return true if this class can act as a surrogate for the passed in object.
     */
    public boolean canResolve(Object o);
    
    /**
     * Gets the unique id for the passed in object. This id will be used in as
     * the value of query parameter, jsp_tree_item, in the event that this 
     * item supports any actions and the link for that action for thit item in
     * the view was selected by a user. It also is the value passed to the label
     * resolver to acquire the UI presentable label to represent the passed in
     * object in the view.
     * 
     * @param o
     * @return the id for o.
     */
    public String getId(Object o);
    
    /**
     * Returns information needed for rendering the UI representation of this 
     * node. Since label rendering is pluggable the object returned should be 
     * known and understood by the plugged-in rendering JSP.
     * @param o
     * @return the UI presentable label for o.
     */
    public Object getLabelData(Object o);
    
    /**
     * Indicates if this object has aspects. Aspects act like children
     * but do not include a line from them to the parent-to-child branch and
     * can be exposed without expanding the parent to show the children. Unlike
     * children, aspects can not have any contained nodes. They are only meant
     * to portray some information of the parent node.
     * 
     * @param o
     * @return
     */
    public boolean hasAspects(Object o);
    
    /**
     * Returns the aspects of the passed in object. This method is only called
     * hasAspects() returns true. Aspects act like children but do not include a
     * line from them to the parent-to-child branch and can be exposed without
     * expanding the parent to show the children. Unlike children, aspects can
     * not have any containing nodes. They are only meant to portray some
     * information of the parent node.
     * 
     * @param o
     * @return
     */
    public Object[] getAspects(Object o);
    
    public boolean canContainChildren(Object o);

    public boolean hasChildren(Object o);
    
    public Object[] getChildren(Object o);
}
