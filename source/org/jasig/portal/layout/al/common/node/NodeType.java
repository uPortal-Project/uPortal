/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al.common.node;

/**
 * Node type constant class
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 */
public class NodeType {
	
    private final String name;
    
    public final static NodeType CHANNEL=new NodeType("channel");
    public final static NodeType FOLDER=new NodeType("folder");
    
    public NodeType(String name) {
        this.name=name;
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
}
