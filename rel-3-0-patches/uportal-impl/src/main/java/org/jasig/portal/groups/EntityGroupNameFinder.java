/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.groups;

import java.util.HashMap;
import java.util.Map;

import org.jasig.portal.services.GroupService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Reference implementation of <code>IEntityNameFinder</code> for <code>IEntityGroup</code>.
 * @author Alex Vigdor
 * @version $Revision$
 */
public class EntityGroupNameFinder
        implements IEntityNameFinder {
    private static final Log log = LogFactory.getLog(EntityGroupNameFinder.class);
    private static IEntityNameFinder _instance = null;
    private Class type = null;

    protected EntityGroupNameFinder () {
        try {
            type = Class.forName("org.jasig.portal.groups.IEntityGroup");
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    public synchronized static IEntityNameFinder singleton () {
        if (_instance == null) {
            _instance = new EntityGroupNameFinder();
        }
        return  _instance;
    }

    /**
     * Given the key, returns the entity's name.
     * @param key java.lang.String
     */
    public String getName (String key) throws Exception {
        IEntityGroup g = GroupService.findGroup(key);
        return  g.getName();
    }

    /**
     * Given an array of keys, returns the names of the entities.
     * @param keys java.lang.String[]
     */
    public Map getNames (String[] keys) throws Exception {
        HashMap names = new HashMap();
        for (int i = 0; i < keys.length; i++) {
            names.put(keys[i], getName(keys[i]));
        }
        return  names;
    }

    /**
     * Returns the entity type for this <code>IEntityFinder</code>.
     * @return java.lang.Class
     */
    public Class getType () {
        return  type;
    }
}



