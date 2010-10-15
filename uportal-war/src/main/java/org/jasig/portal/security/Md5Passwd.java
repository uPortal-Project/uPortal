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
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.jasig.portal.persondir.ILocalAccountDao;
import org.jasig.portal.persondir.ILocalAccountPerson;
import org.jasig.portal.persondir.dao.jpa.LocalAccountPersonImpl;
import org.jasig.portal.spring.locator.LocalAccountDaoLocator;
import org.jasig.portal.spring.locator.PortalPasswordServiceLocator;

/**
 * <p>A utility class that demonstrates changing and locking md5 passwords in
 * the UP_PERSON_DIR table. The program accepts two optional flags <code>-c</code>
 * causes the user to be created if he/she doesn't exist. The <code>-l</code>
 * flag causes the specified user's account to be locked.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */
public class Md5Passwd {

  public Md5Passwd(String user, boolean create, boolean lock)
      throws IOException, NoSuchAlgorithmException, SQLException {

    // Make sure user is specified correctly
    if (user == null || user.trim().length() <= 0) {
      System.out.println("You did not specify a valid user name.  Please try again.");
      System.exit(0);
    }

    // attempt to get the account form the database
    ILocalAccountDao accountDao = LocalAccountDaoLocator.getLocalAccountDao();    
    ILocalAccountPerson account = accountDao.getPerson(user);
    
    if (account == null) {
        if (!create) {
            System.out.println("No such user: " + user);
            return;
        } else {
            account = new LocalAccountPersonImpl();
        }
    }

    String spass;
    if (!lock) {
        System.out.print("Enter Password for " + user + ": ");
        System.out.flush(); // Needed for prompt to appear when running from Ant.
        BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
        spass = d.readLine();
    } else {
        spass = "*LCK";
    }

    // update the user's password
    IPortalPasswordService passwordService = PortalPasswordServiceLocator.getPortalPasswordService();
    String encryptedPassword = passwordService.encryptPassword(spass);
    account.setPassword(encryptedPassword);
    accountDao.updateAccount(account);

    System.out.println("Password Updated...");
    return;
  }

  public static void main(String[] args)
      throws IOException, NoSuchAlgorithmException, SQLException {

    if (args.length == 1 && args[0].charAt(0) != '-')
      new Md5Passwd(args[0], false, false);
    else if (args.length == 2 && args[0].equals("-c") &&
        args[1].charAt(0) != '-')
      new Md5Passwd(args[1], true, false);
    else if (args.length == 2 && args[0].equals("-l") &&
        args[1].charAt(0) != '-')
      new Md5Passwd(args[1], false, true);
    else {
      System.err.println("Usage \"Md5Passwd [-c| -l] <user>\"");
      return;
    }
  }

}
