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

import  org.jasig.portal.concurrency.IBasicEntity;
import  org.jasig.portal.*;
import  org.jasig.portal.services.*;
import  java.util.*;
import  org.jasig.portal.utils.*;
import  java.sql.*;


/**
 * A finder implementation to provide IPerson properties derived from the 
 * PersonDirectory
 * 
 * @author Alex Vigdor av317@columbia.edu
 * @version $Revision$
 */
public class PersonDirPropertyFinder
        implements IEntityPropertyFinder {
    private static String USER_TABLE = "UP_USER";
    private static String USER_ID_COLUMN = "USER_ID";
    private static String USER_NAME_COLUMN = "USER_NAME";
    private Class person;
    private PersonDirectory pd;
    private SmartCache cache;
    private static String selectUser = "SELECT " + USER_NAME_COLUMN + " FROM "
            + USER_TABLE + " WHERE " + USER_ID_COLUMN + " = ?";

    public PersonDirPropertyFinder() {
        pd = new PersonDirectory();
        cache = new SmartCache(120);
        try {
            person = Class.forName("org.jasig.portal.security.IPerson");
        } catch (Exception e) {
            LogService.instance().log(LogService.ERROR, e);
        }
    }

    public String[] getPropertyNames(IBasicEntity entity) {
        String[] r = new String[0];
        if (entity.getEntityType().equals(person)) {
            r = (String[])getPropertiesHash(entity).keySet().toArray(r);
        }
        return  r;
    }

    public String getProperty(IBasicEntity entity, String name) {
        String r = null;
        if (entity.getEntityType().equals(person)) {
            r = (String)getPropertiesHash(entity).get(name);
        }
        return  r;
    }

    protected Hashtable getPropertiesHash(IBasicEntity entity) {
        Hashtable ht;
        if ((ht = (Hashtable)cache.get(entity.getEntityKey())) == null) {
            ht = new Hashtable(0);
            try {
                int key = Integer.parseInt(entity.getEntityKey());
                String uname = this.getUserName(key);
                ht = pd.getUserDirectoryInformation(uname);
            } catch (Exception e) {
                LogService.instance().log(LogService.ERROR, e);
            }
            cache.put(entity.getEntityKey(), ht);
        }
        return  ht;
    }

    protected String getUserName(int key) {
        Connection conn = null;
        RDBMServices.PreparedStatement ps = null;
        String name = null;
        try {
            conn = getConnection();
            ps = new RDBMServices.PreparedStatement(conn, selectUser);
            ps.clearParameters();
            ps.setInt(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                name = rs.getString(USER_NAME_COLUMN);
            }
            rs.close();
            ps.close();
        } catch (SQLException sqle) {
            LogService.instance().log(LogService.ERROR, sqle);
        } finally {
            releaseConnection(conn);
        }
        return  name;
    }

    protected Connection getConnection() {
        return  RDBMServices.getConnection();
    }

    protected void releaseConnection(Connection conn) {
        RDBMServices.releaseConnection(conn);
    }
}



