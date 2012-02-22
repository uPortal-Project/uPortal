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

package org.jasig.portal.layout.dlm;

import org.apache.commons.lang.Validate;

/**
 * Uniquely identifies a node within some DLM layout for use in external data 
 * XML.  A Pethref is the opposite (counterpart) of a {@link Noderef}.  In other 
 * words, a {@link Noderef} identifies internally what a Pathref identifies 
 * externally.  The need for two formats stems from the fact that {@link Noderef}s 
 * use database identifiers in their format, which are not (always) stable across 
 * migrations.<br/>
 * 
 * Pathrefs contain either 2 or 3 elements:
 * <ul>
 *   <li>username of the fragment owner (e.g. 'admin-lo')</li>
 *   <li>XPath that uniquely identifies a layout node (e.g. '/layout/folder/folder[3]')</li>
 *   <li><strong>SOMETIMES</strong> a portlet fname (layout nodes referring to portlets only)</li>
 * </ul>
 * 
 * @author awills
 */
public final class Pathref {
    
    private static final String TOKEN_DELIMITER = ":";
    
    // Instance Members
    private final String layoutOwnerUsername;
    private final String uniquePath;
    private final String portletFname;
    
    public Pathref(String layoutOwnerUsername, String uniquePath) {
        this(layoutOwnerUsername, uniquePath, null);
    }
    
    public Pathref(String layoutOwnerUsername, String uniquePath, String portletFname) {
        
        Validate.notNull(layoutOwnerUsername, "Argument 'layoutOwnerUsername' cannot be null.");
        Validate.notNull(uniquePath, "Argument 'uniquePath' cannot be null.");
        // NB:  portletFname can and SHOULD be null for Pathrefs that refer to non-portlet nodes
        
        this.layoutOwnerUsername = layoutOwnerUsername;
        this.uniquePath = uniquePath;
        this.portletFname = portletFname;
        
    }
    
    public String getLayoutOwnerUsername() {
        return layoutOwnerUsername;
    }

    public String getUniquePath() {
        return uniquePath;
    }

    public String getPortletFname() {
        return portletFname;
    }
    
    @Override
    public String toString() {
        return layoutOwnerUsername + TOKEN_DELIMITER + uniquePath;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((layoutOwnerUsername == null) ? 0 : layoutOwnerUsername
                        .hashCode());
        result = prime * result
                + ((portletFname == null) ? 0 : portletFname.hashCode());
        result = prime * result
                + ((uniquePath == null) ? 0 : uniquePath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pathref other = (Pathref) obj;
        if (layoutOwnerUsername == null) {
            if (other.layoutOwnerUsername != null)
                return false;
        } else if (!layoutOwnerUsername.equals(other.layoutOwnerUsername))
            return false;
        if (portletFname == null) {
            if (other.portletFname != null)
                return false;
        } else if (!portletFname.equals(other.portletFname))
            return false;
        if (uniquePath == null) {
            if (other.uniquePath != null)
                return false;
        } else if (!uniquePath.equals(other.uniquePath))
            return false;
        return true;
    }

}
