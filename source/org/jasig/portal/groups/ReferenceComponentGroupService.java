/* Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
