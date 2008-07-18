/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.services.entityproperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.PersonDirectory;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;

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
    
    private Class<IPerson> person = org.jasig.portal.security.IPerson.class;
    private IPersonAttributeDao pa;
    private Map<String, Map<String, List<Object>>> cache;

    public PersonDirPropertyFinder() {
        pa = PersonDirectory.getPersonAttributeDao();
        cache = new ReferenceMap(ReferenceMap.HARD, ReferenceMap.SOFT, 120, .75f, true);
    }

    public String[] getPropertyNames(EntityIdentifier entityID) {
        String[] r = new String[0];
        if (entityID.getType().equals(person)) {
            final Map<String, List<Object>> properties = getPropertiesHash(entityID);
            final Set<String> propertyNames = properties.keySet();
            r = propertyNames.toArray(r);
        }
        return  r;
    }

    public String getProperty(EntityIdentifier entityID, String name) {
        String r = null;
        if (entityID.getType().equals(person)) {
            final Map<String, List<Object>> properties = getPropertiesHash(entityID);
            final List<Object> values = properties.get(name);
            
            final StringBuilder sb = new StringBuilder();
            for (Iterator<Object> iter = values.iterator(); iter.hasNext();) {
                Object value = iter.next();
                sb.append(value);
                if (iter.hasNext()) {
                    sb.append(", ");
                }
            }
            r = sb.toString();
        }
        return  r;
    }
    protected Map<String, List<Object>> getPropertiesHash(EntityIdentifier entityID) {
        final String entityIdKey = entityID.getKey();
        Map<String, List<Object>> ht = cache.get(entityIdKey);
        if (ht == null) {
            try {
                final IPersonAttributes personAttributes = pa.getPerson(entityIdKey);
                ht = personAttributes.getAttributes();
            } catch (Exception e) {
                log.error("Error getting properties hash for entityID [" + entityID + "]", e);
                ht = Collections.emptyMap();
            }
            cache.put(entityIdKey, ht);
        }
        return  new HashMap<String, List<Object>>(ht);
    }

}



