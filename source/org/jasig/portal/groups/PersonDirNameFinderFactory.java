/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

 package org.jasig.portal.groups;

 import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Factory for creating <code>PersonDirNameFinders</code>.
 * @author Alex Vigdor
 * @version $Revision$
 */

public class PersonDirNameFinderFactory implements IEntityNameFinderFactory {
    private static final Log log = LogFactory.getLog(PersonDirNameFinderFactory.class);
    
/**
 * ReferencePersonNameFinderFactory constructor comment.
 */
public PersonDirNameFinderFactory() {
        super();
}
/**
 * Return a finder instance.
 * @return org.jasig.portal.groups.IEntityNameFinder
 * @exception org.jasig.portal.groups.GroupsException
 */
public IEntityNameFinder newFinder() throws GroupsException
{
    try
        { return PersonDirNameFinder.singleton(); }
    catch ( SQLException sqle )
    {
        log.error( "ReferencePersonNameFinderFactory.newFinder(): " + sqle);
        throw new GroupsException(sqle.getMessage());
    }
}
}
