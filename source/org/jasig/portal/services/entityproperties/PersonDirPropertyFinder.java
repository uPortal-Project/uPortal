/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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
 */


package  org.jasig.portal.services.entityproperties;

import java.util.Hashtable;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.services.LogService;
import org.jasig.portal.services.PersonDirectory;
import org.jasig.portal.utils.SmartCache;


/**
 * A finder implementation to provide IPerson properties derived from the
 * PersonDirectory
 *
 * @author Alex Vigdor av317@columbia.edu
 * @version $Revision$
 */
public class PersonDirPropertyFinder
        implements IEntityPropertyFinder {
    private Class person = org.jasig.portal.security.IPerson.class;
    private PersonDirectory pd;
    private SmartCache cache;

    public PersonDirPropertyFinder() {
        pd = new PersonDirectory();
        cache = new SmartCache(120);
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
            }
        }
        return  r;
    }

    protected Hashtable getPropertiesHash(EntityIdentifier entityID) {
        Hashtable ht;
        if ((ht = (Hashtable)cache.get(entityID.getKey())) == null) {
            ht = new Hashtable(0);
            try {
                ht = pd.getUserDirectoryInformation(entityID.getKey());
            } catch (Exception e) {
                LogService.log(LogService.ERROR, e);
            }
            cache.put(entityID.getKey(), ht);
        }
        return  ht;
    }

}



