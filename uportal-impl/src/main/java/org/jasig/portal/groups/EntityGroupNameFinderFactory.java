/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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



