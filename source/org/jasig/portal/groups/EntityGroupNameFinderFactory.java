/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.groups;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Factory for creating <code>EntityGroupNameFinder</code>.
 * @author Alex Vigdor
 * @version $Revision$
 */
public class EntityGroupNameFinderFactory
        implements IEntityNameFinderFactory {
    private static final Log log = LogFactory.getLog(EntityGroupNameFinderFactory.class);
    public EntityGroupNameFinderFactory () {
    }

    public IEntityNameFinder newFinder () throws GroupsException {
        try {
            return  EntityGroupNameFinder.singleton();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new GroupsException(e);
        }
    }
}



