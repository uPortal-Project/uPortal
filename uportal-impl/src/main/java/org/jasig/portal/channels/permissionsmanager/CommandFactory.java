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

package  org.jasig.portal.channels.permissionsmanager;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * CommadFactory provides IPermissionCommand objects to CPermissionsManager
 *
 * @author Alex Vigdor
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
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



