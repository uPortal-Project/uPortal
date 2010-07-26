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
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
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
                if (log.isDebugEnabled())
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
