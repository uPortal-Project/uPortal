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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.channels.permissionsmanager;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * CommadFactory provides IPermissionCommand objects to CPermissionsManager
 *
 * @author Alex Vigdor
 * @version $Revision$
 */
public class CommandFactory {
    private static final Log log = LogFactory.getLog(CommandFactory.class);
    private static CommandFactory _instance = null;
    private static HashMap commands = new HashMap();

    /** Creates new CommandFactory */
    protected CommandFactory () {
        try {
            String commandBase = "org.jasig.portal.channels.permissionsmanager.commands.";
            commands.put("AssignPermissions", Class.forName(commandBase + "AssignPermissions").newInstance());
            commands.put("SelectActivities", Class.forName(commandBase + "SelectActivities").newInstance());
            commands.put("SelectOwners", Class.forName(commandBase + "SelectOwners").newInstance());
            commands.put("SelectTargets", Class.forName(commandBase + "SelectTargets").newInstance());
            commands.put("Cancel", Class.forName(commandBase + "Cancel").newInstance());
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    /**
     * put your documentation comment here
     * @return a <code>CommandFactory</code> singleton
     */
    protected static synchronized CommandFactory instance () {
        if (_instance == null) {
            _instance = new CommandFactory();
        }
        return  _instance;
    }

    /**
     * put your documentation comment here
     * @param key
     * @return the <code>IPermissionCommand</code> for the key
     */
    public static IPermissionCommand get (String key) {
        return  instance().getCommand(key);
    }

    /**
     * put your documentation comment here
     * @param key
     * @return the <code>IPermissionCommand</code> for the key
     */
    protected IPermissionCommand getCommand (String key) {
        return  (IPermissionCommand)commands.get(key);
    }
}



