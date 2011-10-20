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

import java.io.IOException;

import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * <p>A utility class that demonstrates changing and locking md5 passwords in
 * the UP_PERSON_DIR table. The program accepts two optional flags <code>-c</code>
 * causes the user to be created if he/she doesn't exist. The <code>-l</code>
 * flag causes the specified user's account to be locked.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */
public final class Md5Passwd {
    public static void main(String[] args) throws IOException {
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        final IPasswordUpdateTool passwordUpdateTool = applicationContext.getBean(IPasswordUpdateTool.class);

        if (args.length == 1 && args[0].charAt(0) != '-') {
            passwordUpdateTool.updatePassword(args[0], false);
        }
        else if (args.length == 2 && args[0].equals("-c") && args[1].charAt(0) != '-') {
            passwordUpdateTool.updatePassword(args[1], true);
        }
        else {
            System.err.println("Usage \"Md5Passwd [-c| -l] <user>\"");
            return;
        }
    }
    
    private Md5Passwd() {}
}
