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

package org.jasig.portal.security.provider;

import java.sql.Connection;
import java.sql.ResultSet;

import org.jasig.portal.RDBMServices;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
            RDBMServices.PreparedStatement pstmt = null;
            try {
                String query = "SELECT  ENCRPTD_PSWD, FIRST_NAME, LAST_NAME, EMAIL FROM UP_PERSON_DIR WHERE USER_NAME = ?";
                log.debug("RDBMUserLayoutStore::getUserAccountInformation(): " + query);
                pstmt = new RDBMServices.PreparedStatement(con, query);
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
