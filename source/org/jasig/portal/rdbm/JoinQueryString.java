/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.rdbm;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

/**
 * Partial implemenation of {@link IJoinQueryString} which provides
 * the implemention of storing and retrieving join queries. It also
 * requires a test query be specified.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision $
 */
public abstract class JoinQueryString implements IJoinQueryString {

    final private Map queryStrings = new Hashtable();

    final private String testJoin;

    /**
     * Creates a new {@link JoinQueryString}.
     * 
     * @param testQuery The query to use to test if joins in the class are supported.
     */
    protected JoinQueryString(final String testQuery) {
        if (testQuery == null)
            throw new IllegalArgumentException("testQuery must not be null");
        
        testJoin = testQuery;
    }

    /**
     * Gets the query to use to test if joins in this class are supported.
     * 
     * @return The query to use to test if joins in this class are supported.
     */
    protected String getTestJoin() {
        return testJoin;
    }

    /**
     * @see org.jasig.portal.rdbm.IJoinQueryString#getQuery(java.lang.String)
     */
    public String getQuery(final String key) throws SQLException {
        final String query = (String)queryStrings.get(key);
        
        if (query == null) {
            throw new SQLException("Missing query");
        }
        
        return query;
    }

    /**
     * @see org.jasig.portal.rdbm.IJoinQueryString#addQuery(java.lang.String, java.lang.String)
     */
    public void addQuery(final String key, final String value) throws SQLException {
        queryStrings.put(key, value);
    }
}