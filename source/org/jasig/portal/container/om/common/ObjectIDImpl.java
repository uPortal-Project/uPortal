/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.container.om.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    // Internal methods.

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        intOID = stream.readInt();
        stringOID = String.valueOf(intOID);
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.write(intOID);
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
