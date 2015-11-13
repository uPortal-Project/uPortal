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

package org.jasig.portal.permission.target;

/**
 * IPermissionTarget represents a valid target for a permission.  Examples
 * of permission targets might include a uPortal group or a static string.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
public interface IPermissionTarget {

    /**
     * Indicates the nature of the target you're dealing with.  Allows code that
     * does things with permissions (esp. checking) to approach its work
     * intelligently.
     *
     * @author drewwills
     * @since 4.3
     */
    public enum TargetType {

        /**
         * This target is a person and no other type.
         */
        PERSON,

        /**
         * This target is a group <em>of people</em> and no other type.
         */
        GROUP,

        /**
         * This target is a portlet and no other type.
         */
        PORTLET,

        /**
         * This target is a group <em>of portlets</em> (category) and no other
         * type.
         */
        CATEGORY,

        /**
         * This target is a type of portlet (i.e. a CPD) and no other type.
         */
        PORTLET_TYPE,

        /**
         * This target is an attribute users may have (e.g. telephoneNumber) and
         * no other type.
         */
        USER_ATTRIBUTE,

        /**
         * This target is a type of entity (data type) recognized by
         * Import/Export (e.g. portlet-definition) and no other type.
         */
        DATA_TYPE,

        /**
         * This target is not any of the other things listed in this enumeration.
         */
        OTHER;

        /**
         * Allows other code to make important connections with internal data structures
         */
        @Override
        public String toString() {
            return this.name().toLowerCase();
        }

    }

    /**
     * Get the key of this permission target.  This is the String against which
     * permissions tests are made within {@link IAuthorizationService};  it must
     * match the TARGET column on UP_PERMISSION.
     */
    public String getKey();

    /**
     * Get the human-readable name of this permission target.
     * 
     * @return
     */
    public String getName();

    /**
     * Learn the nature of this target.  E.g. is it a portlet?
     */
    public TargetType getTargetType();

}
