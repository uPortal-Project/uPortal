/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.RDBMServices;

/**
 * A reference implementation of {@link IAccountStore}.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */
public class RDBMAccountStore implements IAccountStore {

    private static final Log log = LogFactory.getLog(RDBMAccountStore.class);

    public String[] getUserAccountInformation(String username) throws Exception {
        String[] acct = new String[] {
            null, null, null, null
        };
        Connection con = RDBMServices.getConnection();
        try {
            PreparedStatement pstmt = null;
            try {
                String query = "SELECT  ENCRPTD_PSWD, FIRST_NAME, LAST_NAME, EMAIL FROM UP_PERSON_DIR WHERE USER_NAME = ?";
                log.debug("RDBMUserLayoutStore::getUserAccountInformation(): " + query);
                pstmt = con.prepareStatement(query);
                pstmt.setString(1, username);
                ResultSet rset = pstmt.executeQuery();
                try {
                    if (rset.next()) {
                        acct[0] = rset.getString("ENCRPTD_PSWD");
                        acct[1] = rset.getString("FIRST_NAME");
                        acct[2] = rset.getString("LAST_NAME");
                    }
                } finally {
                    rset.close();
                }
            } finally {
                pstmt.close();
            }
        } finally {
            RDBMServices.releaseConnection(con);
        }
        return acct;
    }

}
