/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.rdbm;

import java.sql.SQLException;


/**
 * The join query interface defines methods for storing and retrieving
 * queries fragments that will be used for performing joins in SQL.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision $
 */
public interface IJoinQueryString {
    /**
     * Gets a query by the specified key.
     * 
     * @param key The key to find the query for.
     * @return The query for the key.
     * @throws SQLException Will be thrown if no query exists for the key
     */
    public String getQuery(String key) throws SQLException;
    
    /**
     * Adds a query for the specified key, overwriting and query that was
     * already associated with the key.
     * 
     * @param key The key to store the query under.
     * @param value The query to store under the key.
     * @throws SQLException
     */
    public void addQuery(String key, String value) throws SQLException;
}
