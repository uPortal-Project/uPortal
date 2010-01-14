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

package org.jasig.portal.groups;
 
/**
 * A GroupsException describes a problem in the groups structure or in
 * the groups store.  An example of a structural problem is an attempt 
 * to create a circular reference.  If the problem arises retrieving or 
 * updating the groups store, the GroupsException should wrap an Exception 
 * specific to the store, probably a java.sql.SQLException or a 
 * javax.naming.NamingException.
 *
 * @author Dan Ellentuck
 * @version $Revision$ $Date$ 
 */
public class GroupsException extends org.jasig.portal.PortalException {
    
    /**
     * Instantiate a GroupsException with the given cause.
     * @param cause Throwable that caused the problem
     */
    public GroupsException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiate a GroupsException with the given message.
     * @param msg message describing problem
     */
    public GroupsException(String msg) {
        super(msg);
    }
    
    /**
     * Instantiate a GroupsException with the given message and underlying cause.
     * @param msg message describing problem
     * @param cause underlying cause
     */
    public GroupsException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
