/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

/*
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



