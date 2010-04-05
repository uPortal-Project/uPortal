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

package org.jasig.portal;

import java.util.Set;

/**
 *
 *  This interface represents the set of questions any channel or service ("Owner") must answer
 *  if it wants to delegate the responsibility of assigning and viewing permissions
 *  to the Permissions Manager channel.
 *
 *  Owners will be registered by the IPermissible classname that represents them.
 *  These classnames will be stored in a database "UP_PERMISSIBLE"
 *
 * @author  Alex vigdor av317@columbia.edu
 * @version $Revision$
 */
public interface IPermissible {

    public Set<String> getTargetTokens();
    
    /** Return the human readable name of a target
     */    
    public String getTargetName(String token);

}

