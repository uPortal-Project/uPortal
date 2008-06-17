/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

import javax.naming.Name;

/**
 * A composite key that identifies a component group service.
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class CompositeServiceIdentifier extends CompositeEntityIdentifier {
/**
 * CompositeServiceIdentifier.
 * @param serviceKey java.lang.String
 * @exception org.jasig.portal.groups.GroupsException
 */
public CompositeServiceIdentifier(String serviceKey) throws GroupsException 
{
    super(serviceKey, org.jasig.portal.EntityTypes.GROUP_ENTITY_TYPE);
}
/**
 * CompositeServiceIdentifier.
 * @param entityKey java.lang.String
 * @param entityType java.lang.Class
 * @exception org.jasig.portal.groups.GroupsException
 */
public CompositeServiceIdentifier(String entityKey, Class entityType) throws GroupsException 
{
    super(entityKey, entityType);
}
/**
 * The service name is the entire key.
 * @return javax.naming.Name
 */
public Name getServiceName() 
{
    return getCompositeKey();
} 
/**
 * Returns a String that represents the value of this object.
 * @return java.lang.String
 */
public String toString() {
    return "CompositeServiceIdentifier (" + getKey() + ")";

}
}
