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
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES(INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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