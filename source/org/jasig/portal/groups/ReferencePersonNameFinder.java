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
 */
package org.jasig.portal.groups;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jasig.portal.RDBMServices;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Reference implementation of <code>IEntityNameFinder</code> for <code>IPersons</code>.
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class ReferencePersonNameFinder implements IEntityNameFinder
{
    private static final Log log = LogFactory.getLog(ReferencePersonNameFinder.class);
    
    // Singleton instance:
    private static IEntityNameFinder singleton;

    // SQL Strings:
    private static String USER_TABLE = "UP_USER";
    private static String USER_ID_COLUMN = "USER_ID";
    private static String USER_NAME_COLUMN = "USER_NAME";

    private static String DIRECTORY_TABLE = "UP_PERSON_DIR";
    private static String DIRECTORY_FIRST_NAME_COLUMN = "FIRST_NAME";
    private static String DIRECTORY_LAST_NAME_COLUMN = "LAST_NAME";
    private static String DIRECTORY_USER_NAME_COLUMN = "USER_NAME";

    // Our cache of entity names:
    private Map names;
/**
 * ReferenceIPersonNameFinder constructor comment.
 */
private ReferencePersonNameFinder() throws SQLException
{
    super();
    initialize();
}
/**
 * Get names by user ID from UP_PERSON_DIR.
 * @exception java.sql.SQLException
 */
private Map getDirectoryNames() throws java.sql.SQLException
{
    Connection conn = null;
    Statement stmnt = null;
    Map directoryNames = new HashMap();
    try
    {
        conn = RDBMServices.getConnection();
        try
        {
            stmnt = conn.createStatement();

            String sql = getSelectDirectoryNamesSql();
            log.debug( "ReferencePersonNameFinder.getDirectoryNames(): " + sql);

                ResultSet rs = stmnt.executeQuery(sql);
            while (rs.next())
            {
                    String key = "" + rs.getString(DIRECTORY_USER_NAME_COLUMN);
                String fname = rs.getString(DIRECTORY_FIRST_NAME_COLUMN);
                String lname = rs.getString(DIRECTORY_LAST_NAME_COLUMN);
                if ( fname == null ) { fname = ""; }
                if ( lname == null ) { lname = ""; }
                String fullname = fname + " " + lname;
                directoryNames.put(key, fullname);
            }
        }
        finally
            { stmnt.close(); }
    }
    catch (SQLException sqle)
    {
        log.error( sqle);
        throw sqle;
    }

    finally
    {
        RDBMServices.releaseConnection(conn);
    }
    return directoryNames;
}
/**
 * Given the key, returns the entity's name.
 * @param key java.lang.String
 */
public String getName(String key) throws Exception {
    return (String) primGetNames().get(key);
}
/**
 * Given an array of keys, returns the names of the entities.  If a key
 * is not found, its name will be null.
 * @param keys java.lang.String[]
 */
public java.util.Map getNames(java.lang.String[] keys) throws Exception
{
     Map selectedNames = new HashMap();
     for ( int i=0; i<keys.length; i++ )
     {
         String name = (String) primGetNames().get(keys[i]);
         selectedNames.put(keys[i], name);
     }
     return selectedNames;
}
/**
 * @return java.lang.String
 */
private String getSelectDirectoryNamesSql()
{
    return "SELECT " + DIRECTORY_USER_NAME_COLUMN + ", " + DIRECTORY_FIRST_NAME_COLUMN + ", " +
           DIRECTORY_LAST_NAME_COLUMN + " FROM " + DIRECTORY_TABLE;
}
/**
 * @return java.lang.String
 */
private String getSelectUserNamesSql()
{
    return "SELECT " + USER_NAME_COLUMN + " FROM " + USER_TABLE;
}
/**
 * Returns the entity type for this <code>IEntityFinder</code>.
 * @return java.lang.Class
 */
public Class getType() {
    return org.jasig.portal.security.IPerson.class;
}
/**
 * Get names by user ID from UP_USER.
 * @exception java.sql.SQLException
 */
private Map getUserNames() throws java.sql.SQLException
{
    Connection conn = null;
    Statement stmnt = null;
    Map userNames = new HashMap();
    try
    {
        conn = RDBMServices.getConnection();
        try
        {
            stmnt = conn.createStatement();

            String sql = getSelectUserNamesSql();
            log.debug( "ReferencePersonNameFinder.getUserNames(): " + sql);

            ResultSet rs = stmnt.executeQuery(sql);
            while (rs.next())
            {
                String key = rs.getString(USER_NAME_COLUMN);
                userNames.put(key, key);
            }

        }
        finally
            { stmnt.close(); }
    }
    catch (SQLException sqle)
    {
        log.error( sqle);
        throw sqle;
    }

    finally
    {
        RDBMServices.releaseConnection(conn);
    }
    return userNames;
}
/**
 * Loads the names cache.  This may seem like a dumb way to do it (making two
 * passes and merging the results) but it does avoid the outer join horror.
 * In any event, local implementations may often use a directory service
 * like LDAP.
 * @exception java.sql.SQLException
 */
private void initialize() throws java.sql.SQLException
{
    Map userNames = getUserNames();            // user name by user id
    Map directoryNames = getDirectoryNames();  // full name by user name

    Iterator i = userNames.entrySet().iterator();
    while ( i.hasNext() )
    {
        Map.Entry entry = (Map.Entry) i.next();
        String directoryName = (String) directoryNames.get((String) entry.getValue());
        if ( directoryName != null )
           { entry.setValue(directoryName); }
    }
    names = userNames;
}
/**
 * @return java.util.Map
 */
private Map primGetNames()
{
    return names;
}
/**
 * @return IEntityNameFinder
 */
public static synchronized IEntityNameFinder singleton() throws SQLException
{
    if ( singleton == null )
        { singleton = new ReferencePersonNameFinder(); }
    return singleton;
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
    return "IEntityNameFinder for " + getType().getName() ;
}
}
