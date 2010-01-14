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

/**
 * This class began its life as a public inner class of RDBMUserLayoutStore.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5 before 2.5 this was an inner class of org.jasig.portal.RDBMUserLayoutStore.
 */
public class StructureParameter {
    // TODO: provide an intelligent Type comment for this object.

    /**
     * The parameter name.
     */
    private final String name;

    /**
     * The parameter value.
     */
    private final String value;

    /**
     * Create a new StructureParameter instance representing the
     * given name, value pair.
     * @param name the name of the parameter
     * @param value the value for the parameter
     */
    public StructureParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Get the name of this parameter.
     * @return the name of this parameter.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the value of this parameter.
     * @return the value of the parameter.
     */
    public String getValue() {
        return this.value;
    }
}

