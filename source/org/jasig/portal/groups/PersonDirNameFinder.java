/**
 * Copyright (c) 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *
 */


package  org.jasig.portal.groups;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.jasig.portal.services.PersonDirectory;
import org.jasig.portal.utils.SoftHashMap;


/**
 * PersonDirectory implementation of <code>IEntityNameFinder</code> for <code>IPersons</code>.
 * @author Alex Vigdor
 * @version $Revision$
 */
public class PersonDirNameFinder
        implements IEntityNameFinder {
    // Singleton instance:
    private static IEntityNameFinder singleton;
    private static PersonDirectory pd;
    // Our cache of entity names:
    private SoftHashMap names;

    /**
     * ReferenceIPersonNameFinder constructor comment.
     */
    private PersonDirNameFinder () throws SQLException
    {
        super();
        pd = PersonDirectory.instance();
        names = new SoftHashMap();
    }

    /**
     * Given the key, returns the entity's name.
     * @param key java.lang.String
     */
    public String getName (String key) throws Exception {
        if (primGetNames().get(key) == null) {
            primGetNames().put(key, primGetName(key));
        }
        return  (String)primGetNames().get(key);
    }

    /**
     * Given an array of keys, returns the names of the entities.  If a key
     * is not found, its name will be null.
     * @param keys java.lang.String[]
     */
    public java.util.Map getNames (java.lang.String[] keys) throws Exception {
        Map selectedNames = new HashMap();
        for (int i = 0; i < keys.length; i++) {
            String name = getName(keys[i]);
            selectedNames.put(keys[i], name);
        }
        return  selectedNames;
    }

    /**
     * Returns the entity type for this <code>IEntityFinder</code>.
     * @return java.lang.Class
     */
    public Class getType () {
        return  org.jasig.portal.security.IPerson.class;
    }

    /**
     * put your documentation comment here
     * @param key
     * @return
     * @exception java.sql.SQLException
     */
    private String primGetName (String key) throws java.sql.SQLException {
        String name = key;
        Hashtable userInfo = pd.getUserDirectoryInformation(name);
        String displayName = (String)userInfo.get("displayName");
        if ((displayName != null)&& !(displayName.trim().equals(""))) {
            name = displayName;
        }
  
        return  name;
    }

    /**
     * @return java.util.Map
     */
    private Map primGetNames () {
        return  names;
    }

    /**
     * @return IEntityNameFinder
     */
    public static synchronized IEntityNameFinder singleton () throws SQLException {
        if (singleton == null) {
            singleton = new PersonDirNameFinder();
        }
        return  singleton;
    }

    /**
     * Returns a String that represents the value of this object.
     * @return a string representation of the receiver
     */
    public String toString () {
        return  "IEntityNameFinder for " + getType().getName();
    }
}



