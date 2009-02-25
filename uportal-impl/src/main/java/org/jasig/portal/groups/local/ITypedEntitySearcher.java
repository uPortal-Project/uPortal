/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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