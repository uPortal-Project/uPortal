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

package org.jasig.portal.layout.dlm.processing;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.UserPreferences;
import org.jasig.portal.layout.dlm.DistributedLayoutManager;
import org.jasig.portal.security.IPerson;

/**
 * Represents an object that can process layout parameters submitted to the
 * portal as part of a request and/or alter the SAX stream representing the
 * user's layout. Implementations of this interface can take part in acting on
 * parameters submitted to the portal dealing with layout manipulation and can
 * also take part in altering or filtering the SAX events of the raw user
 * layout. Implementations of this class enable the decoupling of
 * DistributedLayoutManager from processing needed to handle URL syntax for a
 * specific Structure and/or Theme transformation stylesheet combination. These
 * implementations are made available to the DistributedLayoutManager via
 * properties/context/layoutContext.xml.
 *
 * @author Mark Boyd
 */
public interface IParameterProcessor
{
    /**
     * Sets the IPerson and DistributedLayoutManager objects for use by this
     * object in the implementation of its characteristics.
     *
     * @param person
     * @param dlm
     */
    public void setResources(IPerson person, DistributedLayoutManager dlm);

    /**
     * Called from DistributedLayoutManager's processLayoutParameters() method
     * allowing this processor to modify the DistributedLayoutManager's state
     * or the state of this object. During rendering of a request for a user
     * the UserInstance object's renderState() method synchronizes access to
     * this call and the call that pushes layout SAX events through this object.
     * Therefore, instance variables can be set on this class as a result of
     * this method being called and then used during handling of the
     * ContentHandler event calls.
     *
     * @param prefs
     * @param request
     */
    public void processParameters(UserPreferences prefs,
            HttpServletRequest request);
}
