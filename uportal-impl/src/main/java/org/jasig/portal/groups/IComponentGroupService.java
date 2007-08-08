/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import java.util.Map;

import javax.naming.Name;

/**
 * Defines a component group service that is part of a composite groups system.
 * A component contains individual group services, which may themselves be
 * components.
 * <p> 
 * The component is only used in the process of composing the composite
 * service, so it does not define any operations on groups, only on other
 * components.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 *
 */
 public interface IComponentGroupService {

  /**
   * Returns a <code>Map</code> of the services contained by this component,
   * keyed on the name of the service WITHIN THIS COMPONENT.  
   */
  public Map getComponentServices();
  /**
   * Returns the FULLY-QUALIFIED <code>Name</code> of the service, which 
   * may not be known until the composite service is assembled.  
   */
  public Name getServiceName();
  /**
   * Answers if this service is a leaf in the composite; a service that
   * actually operates on groups.
   */
  public boolean isLeafService();
  /**
   * Sets the name of the service to the new value. 
   */
  public void setServiceName(Name newServiceName);
}
