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

import java.util.HashMap;
import java.util.Map;

import javax.naming.Name;

/**
 * Reference implementation of IComponentGroupService.
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class ReferenceComponentGroupService implements IComponentGroupService 
{
  /**
   * The services contained by this component, keyed on the name of the service 
   * as it is known within this component.  Used to assemble the composite.
   */
    protected Map componentServices = new HashMap();

  /**
   * The fully-qualified <code>Name</code> of the service, which may not be 
   * known until the composite service is fully assembled.  
   */
    protected Name serviceName;
/**
 * ReferenceComponentGroupService constructor comment.
 */
public ReferenceComponentGroupService() {
	super();
}
/**
 * Returns a <code>Map</code> of the services contained by this component,
 * keyed on the name of the service WITHIN THIS COMPONENT.  
 */
public Map getComponentServices() {
	return componentServices;
}
/**
 * Returns the FULLY-QUALIFIED <code>Name</code> of the service, which 
 * may not be known until the composite service is assembled.  
 */
public javax.naming.Name getServiceName() {
	return serviceName;
}
/**
 * Answers if this service is a leaf in the composite; a service that
 * actually operates on groups.
 */
public boolean isLeafService() {
    return false;
}
/**
 * Sets the name of the service to the new value. 
 */
public void setServiceName(Name newServiceName) 
{
    serviceName = newServiceName;
}
}
