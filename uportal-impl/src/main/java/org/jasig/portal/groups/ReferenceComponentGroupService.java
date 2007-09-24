/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
