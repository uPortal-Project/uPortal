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

package org.jasig.portal.portlets.permissionsadmin;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;

public class Assignment implements Comparable<Assignment>, Serializable {
    
    private static final long serialVersionUID = 1L;

    // Instance Members.
    private final String principalId;
    private final JsonEntityBean principal;
    private Type type;
    private final Set<Assignment> children = new TreeSet<Assignment>();

    /*
     * Public API.
     */
    
    public enum Type {
        
        INHERIT_GRANT,
        
        INHERIT_DENY,
        
        GRANT,
        
        DENY
        
    }

    /**
     * Creates a new {@see Assignment} of type {@see Type.INHERIT}.
     * 
     * @param principal User or group to which this permissions record applies
     */
    public Assignment(String principalId, JsonEntityBean principal) {
        this(principalId, principal, Type.INHERIT_DENY);
    }

    /**
     * Creates a new {@see Assignment} of the specified {@see Type}.
     * 
     * @param principal User or group to which this permissions record applies
     * @param type Either {@see Type.INHERIT}, {@see Type.GRANT}, or {@see Type.DENY}
     */
    public Assignment(String principalId, JsonEntityBean principal, Type type) {

        // Assertions.
        if (principal == null) {
            String msg = "Argument 'principal' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        if (type == null) {
            String msg = "Argument 'type' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        
        this.principalId = principalId;
        this.principal = principal;
        this.type = type;

    }
    
    public Assignment addChild(Assignment a) {

        // Assertions.
        if (a == null) {
            String msg = "Argument 'a' [Assignment] cannot be null";
            throw new IllegalArgumentException(msg);
        }

        children.add(a);
        return this;

    }
    
    @Override
    public int compareTo(Assignment a) {
        return this.principal.getName().compareTo(a.principal.getName());
    }

    public Set<Assignment> getChildren() {
        return new TreeSet<Assignment>(children);
    }
    
    public JsonEntityBean getPrincipal() {
        return principal;
    }
    
    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
    
    public Assignment findDecendentOrSelfIfExists(JsonEntityBean principal) {
        
        // Assertions.
        if (principal == null) {
            String msg = "Argument 'principal' cannot be null";
            throw new IllegalArgumentException(msg);
        }

        Assignment rslt = null;  // default...
        
        if (principal.getId().equals(this.principal.getId()) 
                && principal.getEntityTypeAsString().equals(this.principal.getEntityTypeAsString())) {
            rslt = this;
        } else {
            for (Assignment a : children) {
                rslt = a.findDecendentOrSelfIfExists(principal);
                if (rslt != null) {
                    break;
                }
            }
        }
        
        return rslt;
        
    }

    public String getPrincipalId() {
        return principalId;
    }

}
