/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al.common.node;

/**
 * A class describing folder type constants
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 */
public class FolderType {
    private final String name;
    
    public final static FolderType REGULAR=new FolderType("regular");
    public final static FolderType FOOTER=new FolderType("footer");
    public final static FolderType HEADER=new FolderType("header");
    
    public FolderType(String name) {
        this.name=name;
    }
    
    
    /**
     * @return Returns the folder type name.
     */
    public String getName() {
        return name;
    }
}
