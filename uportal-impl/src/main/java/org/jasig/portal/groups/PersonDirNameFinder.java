/* Copyright 2002, 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/


package  org.jasig.portal.groups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.portal.services.persondir.IPersonAttributeDao;
import org.jasig.portal.utils.SoftHashMap;


/**
 * Implementation of <code>IEntityNameFinder</code> for <code>IPersons</code> by 
 * looking up displayName from an <code>IPersonAttributeDao</code>.
 * @author Alex Vigdor
 * @version $Revision$
 */
public class PersonDirNameFinder
        implements IEntityNameFinder {
    
    /**
     * Data Access Object backing this name finder.
     */
    private IPersonAttributeDao paDao;
    
    /** Our cache of entity names: */
    private Map names = new SoftHashMap();

    /**
     * Instantiate a PersonDirNameFinder backed by the given
     * IPersonAttributeDao.
     * @param pa DAO to back this PersonDirNameFinder
     */
    PersonDirNameFinder (IPersonAttributeDao pa) {
        this.paDao = pa;
    }


    public String getName (String key) {
        String name = (String) this.names.get(key);
        
        if (name == null && key !=null) {
            // cached name not found, get name from underlying DAO.
            name = primGetName(key);
            // cache the name
            this.names.put(key, name);
        }
        return  name;
    }


    public java.util.Map getNames (java.lang.String[] keys) {
        Map selectedNames = new HashMap();
        for (int i = 0; i < keys.length; i++) {
            String name = getName(keys[i]);
            selectedNames.put(keys[i], name);
        }
        return  selectedNames;
    }


    public Class getType () {
        return  org.jasig.portal.security.IPerson.class;
    }

    /**
     * Actually lookup a user name using the underlying IPersonAttributeDao.
     * @param key - entity key which in this case is a unique identifier for a user
     * @return the display name for the identified user
     */
    private String primGetName (String key) {
        String name = key;
        Map userInfo = this.paDao.getUserAttributes(name);
        if (userInfo != null)
        {
            Object displayName = userInfo.get("displayName");
            String displayNameStr = "";
            if (displayName != null)
            {
                if (displayName instanceof java.util.List)
                {
                    List displayNameList = (List) displayName;
                    if (! displayNameList.isEmpty() )
                        { displayNameStr = (String)displayNameList.get(0); } 
                }
                else displayNameStr = (String)displayName;
        
                if (! displayNameStr.trim().equals("")) 
                    { name = displayNameStr; }
            }
        }
        return  name;
    }

    /**
     * Get a static singleton instance of this class backed by PersonDirectory.
     * @return singleton PersonDirNameFinder backed by PersonDirectory
     * @deprecated as of uP 2.5 instead use PersonDirNameFinderFactory
     */
    public static IEntityNameFinder singleton () {
        return new PersonDirNameFinderFactory().newFinder();
    }

    /**
     * Returns a String that represents the value of this object.
     * @return a string representation of the receiver
     */
    public String toString () {
        return  "PersonDirNameFinder backed by " + this.paDao;
    }
}



