/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.i18n;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.security.IPerson;

/**
 * Database implementation of locale storage interface.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class RDBMLocaleStore implements ILocaleStore {

    private static final Log log = LogFactory.getLog(RDBMLocaleStore.class);

    public Locale[] getUserLocales(IPerson person) throws Exception {
        List localeList = new ArrayList();
        Connection con = RDBMServices.getConnection();
        try {
            String query = "SELECT * FROM UP_USER_LOCALE WHERE USER_ID=? ORDER BY PRIORITY";
            PreparedStatement pstmt = con.prepareStatement(query);
            try {
                pstmt.clearParameters();
                pstmt.setInt(1, person.getID());
                log.debug(query);
                ResultSet rs = pstmt.executeQuery();
                try {
                    while (rs.next()) {
                       String localeString = rs.getString("LOCALE");
                       Locale locale = LocaleManager.parseLocale(localeString);
                       localeList.add(locale);
                    }
                } finally {
                    rs.close();
                }
            } finally {
                pstmt.close();
            }
        } finally {
            RDBMServices.releaseConnection(con);
        }
        return (Locale[])localeList.toArray(new Locale[0]);
    }

    public void updateUserLocales(IPerson person, Locale[] locales) throws SQLException {
        Connection con = RDBMServices.getConnection();
        try {
            // Delete the existing list of locales
            String delete = "DELETE FROM UP_USER_LOCALE WHERE USER_ID=?";
            PreparedStatement pstmt = con.prepareStatement(delete);
            try {
                pstmt.clearParameters();
                pstmt.setInt(1, person.getID());
                log.debug(delete);
                pstmt.executeUpdate();

            } finally {
                pstmt.close();
            }
            // Insert the new list of locales
            String insert = "INSERT INTO UP_USER_LOCALE VALUES (?, ?, ?)";
            pstmt = con.prepareStatement(insert);
            try {
                for (int i = 0; i < locales.length; i++) {
                    pstmt.clearParameters();
                    pstmt.setInt(1, person.getID());
                    pstmt.setString(2, locales[i].toString());
                    pstmt.setInt(3, i);
                    log.debug(insert);
                    pstmt.executeUpdate();
                }

            } finally {
                pstmt.close();
            }
        } finally {
            RDBMServices.releaseConnection(con);
        }
    }
}
