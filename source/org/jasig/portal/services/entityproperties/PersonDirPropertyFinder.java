/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.services.entityproperties;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.services.PersonDirectory;
import org.jasig.portal.services.persondir.support.IPersonAttributeDao;
import org.jasig.portal.utils.SoftHashMap;

/**
 * A finder implementation to provide IPerson properties derived from the
 * PersonDirectory
 *
 * @author Alex Vigdor av317@columbia.edu
 * @version $Revision$
 */
public class PersonDirPropertyFinder
        implements IEntityPropertyFinder {
    
    private static final Log log = LogFactory.getLog(PersonDirPropertyFinder.class);
    
    private Class person = org.jasig.portal.security.IPerson.class;
    private IPersonAttributeDao pa;
    private SoftHashMap cache;

    public PersonDirPropertyFinder() {
        pa = PersonDirectory.getPersonAttributeDaoInstance();
        cache = new SoftHashMap(120);
    }

    public String[] getPropertyNames(EntityIdentifier entityID) {
        String[] r = new String[0];
        if (entityID.getType().equals(person)) {
            r = (String[])getPropertiesHash(entityID).keySet().toArray(r);
        }
        return  r;
    }

    public String getProperty(EntityIdentifier entityID, String name) {
        String r = null;
        if (entityID.getType().equals(person)) {
            Object o = getPropertiesHash(entityID).get(name);
            if (o instanceof String) {
                r = (String)o;
            } else if (o instanceof List) {
                StringBuffer sb = new StringBuffer();
                List values = (List)o;
                for (Iterator iter = values.iterator(); iter.hasNext();) {
                    Object value = (Object)iter.next();
                    sb.append(value.toString());
                    if (iter.hasNext()) {
                        sb.append(", ");
                    }
                }
                r = sb.toString();
            }
        }
        return  r;
    }
    protected Hashtable getPropertiesHash(EntityIdentifier entityID) {
        Map ht;
        if ((ht = (Map)cache.get(entityID.getKey())) == null) {
            ht = new Hashtable(0);
            try {
                ht = pa.getUserAttributes(entityID.getKey());
            } catch (Exception e) {
                log.error("Error getting properties hash for entityID [" + entityID + "]", e);
            }
            cache.put(entityID.getKey(), ht);
        }
        return  new Hashtable(ht);
    }

}



