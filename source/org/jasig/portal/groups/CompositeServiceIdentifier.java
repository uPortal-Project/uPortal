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
