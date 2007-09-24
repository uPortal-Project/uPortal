/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.groups;

import java.util.HashMap;
import java.util.Map;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.IChannelRegistryStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Reference implementation of <code>IEntityNameFinder</code> for <code>Channels</code>.
 * @author Alex Vigdor
 * @version $Revision$
 */
public class ReferenceChannelNameFinder
        implements IEntityNameFinder {
    
    private static final Log log = LogFactory.getLog(ReferenceChannelNameFinder.class);
    
    private static IEntityNameFinder _instance = null;
    private Class type = null;

    protected ReferenceChannelNameFinder () {
        try {
            type = Class.forName("org.jasig.portal.ChannelDefinition");
        } catch (Exception e) {
            log.error("Exception instantiating ReferenceChannelNameFinder.", e);
        }
    }

    public static synchronized IEntityNameFinder singleton () {
        if (_instance == null) {
            _instance = new ReferenceChannelNameFinder();
        }
        return  _instance;
    }

    /**
     * Given the key, returns the entity's name.
     * @param key java.lang.String
     */
    public String getName (String key) throws Exception {
        IChannelRegistryStore crs = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl();
        ChannelDefinition cd = crs.getChannelDefinition(Integer.parseInt(key));
        return  cd.getName();
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



