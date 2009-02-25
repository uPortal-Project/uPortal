/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.groups;

import org.jasig.portal.groups.local.EntitySearcherImpl;
import org.jasig.portal.groups.local.ITypedEntitySearcher;
import org.jasig.portal.groups.local.searchers.RDBMChannelDefSearcher;
import org.jasig.portal.groups.local.searchers.RDBMPersonSearcher;

/**
 * Creates an instance of the reference <code>IEntitySearcher</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class ReferenceEntitySearcherFactory implements IEntitySearcherFactory {
/**
 * ReferenceGroupServiceFactory constructor.
 */
public ReferenceEntitySearcherFactory() {
    super();
}
/**
 * Return an instance of the entity searcher implementation.
 * @return IEntitySearcher
 * @exception GroupsException
 */
public IEntitySearcher newEntitySearcher() throws GroupsException
{
    return newInstance();
}
/**
 * Return an instance of the entity searcher implementation.
 * @return IEntitySearcher
 * @exception GroupsException
 */
public IEntitySearcher newInstance() throws GroupsException
{
    ITypedEntitySearcher[] tes = new ITypedEntitySearcher[2];
    tes[0]=new RDBMChannelDefSearcher();
    tes[1]=new RDBMPersonSearcher();
    IEntitySearcher entitySearcher = new EntitySearcherImpl(tes);
    return entitySearcher;
}
}
