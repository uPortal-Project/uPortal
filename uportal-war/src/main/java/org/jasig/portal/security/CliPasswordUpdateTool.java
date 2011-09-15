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

package org.jasig.portal.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.persondir.ILocalAccountDao;
import org.jasig.portal.persondir.ILocalAccountPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>A utility class that demonstrates changing and locking md5 passwords in
 * the UP_PERSON_DIR table. The program accepts two optional flags <code>-c</code>
 * causes the user to be created if he/she doesn't exist. The <code>-l</code>
 * flag causes the specified user's account to be locked.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */
@Service
public class CliPasswordUpdateTool implements IPasswordUpdateTool {
    private ILocalAccountDao localAccountDao;
    private IPortalPasswordService portalPasswordService;

    @Autowired
    public void setLocalAccountDao(ILocalAccountDao localAccountDao) {
        this.localAccountDao = localAccountDao;
    }

    @Autowired
    public void setPortalPasswordService(IPortalPasswordService portalPasswordService) {
        this.portalPasswordService = portalPasswordService;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.security.IPasswordUpdateTool#updatePassword(java.lang.String, boolean)
     */
    @Override
    @Transactional
    public boolean updatePassword(String user, boolean create) throws IOException {
        // Make sure user is specified correctly
        if (StringUtils.isBlank(user)) {
            System.err.println("You did not specify a valid user name.  Please try again.");
            return false;
        }

        // attempt to get the account form the database
        ILocalAccountPerson account = this.localAccountDao.getPerson(user);
        if (account == null) {
            if (!create) {
                System.err.println("No such user: " + user);
                return false;
            }

            account = this.localAccountDao.createPerson(user);
        }

        System.out.print("Enter Password for " + user + ": ");
        System.out.flush(); // Needed for prompt to appear when running from Ant.
        final BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
        final String spass = d.readLine();

        // update the user's password
        final String encryptedPassword = this.portalPasswordService.encryptPassword(spass);
        account.setPassword(encryptedPassword);
        
        this.localAccountDao.updateAccount(account);

        System.out.println("Password Updated...");
        return true;
    }
}
