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

package org.jasig.portal.layout;

import org.jasig.portal.layout.node.IUserLayoutNodeDescription;

/**
 * A layout event involving old parent reference. 
 * Used to related "move" and "delete" node events.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version 1.0
 */
public class LayoutMoveEvent extends LayoutEvent {
    private static final long serialVersionUID = 1L;

    protected final IUserLayoutNodeDescription oldParentNode;

    /**
     * Construct a <code>LayoutMoveEvent</code> from a given source,
     * node, and node's old parent.
     *
     * @param source an <code>Object</code> that generated the event
     * @param node an <code>UserLayoutNodeDescription</code> of the node that was involved
     * @param oldParentNodeId a <code>String</code> value of an old parent id
     */
    public LayoutMoveEvent(Object source, IUserLayoutNodeDescription parentNode,  IUserLayoutNodeDescription node, IUserLayoutNodeDescription oldParentNode) {
        super(source, parentNode, node);
        this.oldParentNode=oldParentNode;
    }
    
    /**
     * Obtain the OLD parent node. This is the node
     * to which a given node used to be attached.
     * 
     * @return the old parent node
     */
    public IUserLayoutNodeDescription getOldParentNode() {
        return this.oldParentNode;
    }
    
    /**
     * Obtain an id of an OLD parent node.  This is the node
     * to which a given node used to be attached.
     *
     * @return a <code>String</code> node id
     */
    public String getOldParentNodeId() {
        return this.oldParentNode.getId();
    }
}

