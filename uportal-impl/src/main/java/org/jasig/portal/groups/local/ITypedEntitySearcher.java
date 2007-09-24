/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
 
package org.jasig.portal.groups.local;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IGroupConstants;

/**
 * An API for a searcher that knows about a single type, used
 * by EntitySearcherImpl
 *
 * @author Alex Vigdor
 * @version $Revision$
 */


public interface ITypedEntitySearcher extends IGroupConstants{
    public EntityIdentifier[] searchForEntities(String query, int method) throws GroupsException;
    public Class getType();
}