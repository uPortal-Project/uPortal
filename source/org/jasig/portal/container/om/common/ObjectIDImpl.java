/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.common;

import java.io.Serializable;

import org.apache.pluto.om.common.ObjectID;

/**
 * Wraps around the internal Object IDs. By holding both
 * the string and the integer version of an Object ID this class
 * helps speed up the internal processing.  Code copied from Apache Pluto.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ObjectIDImpl implements ObjectID, Serializable {

    private String stringOID = null;
    private int intOID;

    private ObjectIDImpl(int oid) {
        stringOID = String.valueOf(oid);
        intOID = oid;
    }

    private ObjectIDImpl(int oid, String stringOID) {
        this.stringOID = stringOID;
        intOID = oid;
    }


    // Addtional methods

    public boolean equals(Object object) {
        boolean result = false;

        if (object instanceof ObjectID)
            result = (intOID == ((ObjectIDImpl)object).intOID);
        else if (object instanceof String)
            result = stringOID.equals(object);
        else if (object instanceof Integer)
            result = (intOID == ((Integer)object).intValue());
        return result;
    }

    public int hashCode() {
        return intOID;
    }

    public String toString() {
        return stringOID;
    }

    public int intValue() {
        return intOID;
    }

    static public ObjectID createFromString(String idStr) {
        char[] id = idStr.toCharArray();
        int _id = 1;
        for (int i = 0; i < id.length; i++) {
            if ((i % 2) == 0)
                _id *= id[i];
            else
                _id ^= id[i];
            _id = Math.abs(_id);
        }
        return new ObjectIDImpl(_id, idStr);
    }
}
