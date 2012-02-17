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
 * Uniquely identifies a node within some DLM layout for use in the internal 
 * model of a layout.  A Noderef is the opposite (counterpart) of a {@link Pathref}.
 * In other words, a Noderef identifies internally what a {@link Pathref} identifies 
 * externally.  The need for two formats stems from the fact that Noderefs use 
 * database identifiers in their format, which are not (always) stable across 
 * migrations.
 * 
 * Noderefs contain 3 elements:
 * 
 * <ul>
 *   <li>userId of the fragment owner (e.g. 'u13')</li>
 *   <li>layoutId of the fragment layout (currently this will always be 'l1')</li>
 *   <li>structureId of the node within the fragment layout (e.g. 's2' but the preceeding character is not always an 's')</li>
 * </ul>
 * 
 * Example Noderef:  u13l1s2
 * 
 * @author awills
 */
public final class Noderef {
    
    private static final String USER_ID_PREFIX = "u";
    private static final String LAYOUT_ID_PREFIX = "l";

    // Instance Members
    private final int userId;
    private final int layoutId;
    private final String structureId;
    
    /**
     * Creates an intra-layout Noderef (refers to a node in one's own layout).
     * 
     * @param structureId
     */
    public Noderef(String structureId) {
        this(0, 0, structureId);
    }

    /**
     * Creates an extra-layout Noderef (refers to a node in a DLM fragment).
     */
    public Noderef(int userId, int layoutId, String structureId) {

        boolean userAndLayoutIdsAreValid = (userId > 0 && layoutId > 0) || 
                                        (userId == 0 && layoutId == 0);
        Validate.isTrue(userAndLayoutIdsAreValid, "Arguments 'userId' and " +
        		            "'layoutId' must either both be zero or both " +
        		            "greater than zero.");
        Validate.notNull(structureId, "Argument 'structureId' cannot be null.");

        this.userId = userId;
        this.layoutId = layoutId;
        this.structureId = structureId;

    }

    public int getUserId() {
        return userId;
    }
    
    public int getLayoutId() {
        return layoutId;
    }
    
    public String getStructureId() {
        return structureId;
    }
    
    @Override
    public String toString() {
        String rslt = null;
        if (userId != 0 && layoutId !=0) {
            // An intra-layout Noderef
            StringBuilder sb = new StringBuilder();
            sb.append(USER_ID_PREFIX).append(userId).append(LAYOUT_ID_PREFIX)
                                .append(layoutId).append(structureId);
            rslt = sb.toString();
        } else {
            // An extr-layout Noderef
            rslt = structureId;
        }
        return rslt;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + layoutId;
        result = prime * result
                + ((structureId == null) ? 0 : structureId.hashCode());
        result = prime * result + userId;
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
        Noderef other = (Noderef) obj;
        if (layoutId != other.layoutId)
            return false;
        if (structureId == null) {
            if (other.structureId != null)
                return false;
        } else if (!structureId.equals(other.structureId))
            return false;
        if (userId != other.userId)
            return false;
        return true;
    }
    
}
