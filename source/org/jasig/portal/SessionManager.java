/**
 *  $Author$ $Date$ $Id: SessionManager.java,v 1.8
 *  2001/05/30 00:54:55 zshaw Exp $ $Name$ $Revision$ Copyright (c) 2000
 *  The JA-SIG Collaborative. All rights reserved. Redistribution and use in
 *  source and binary forms, with or without modification, are permitted
 *  provided that the following conditions are met: 1. Redistributions of source
 *  code must retain the above copyright notice, this list of conditions and the
 *  following disclaimer. 2. Redistributions in binary form must reproduce the
 *  above copyright notice, this list of conditions and the following disclaimer
 *  in the documentation and/or other materials provided with the distribution.
 *  3. Redistributions of any form whatsoever must retain the following
 *  acknowledgment: "This product includes software developed by the JA-SIG
 *  Collaborative (http://www.jasig.org/)." THIS SOFTWARE IS PROVIDED BY THE
 *  JA-SIG COLLABORATIVE "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *  JA-SIG COLLABORATIVE OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. Modified by: Vikrant
 *  Joshi (April 4, 2001)
 */

package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;

/**
 *  The SessionManager is responsible for handling all the aspects of a user's
 *  session including (but not limited to): Allowing or denying logins;
 *  eliminating stale or orphaned sessions; detecting memory starvation and
 *  trying to recover; maintaining statistics on sessions; It is used like a
 *  static singleton, where you simply call one of the methods directly on the
 *  class. It is auto configured, and uses the properties/session.properties
 *  file to configure itself. If that file is missing, it defaults to the safest
 *  set of options (and tells you it is missing). All log messages it reports
 *  begin with the word "SessionManager:" so you can track what it is doing.
 *
 *@author     $Author$ (with additional changes by Vikrant Joshi).
 *@created    June 20, 2001
 */
public class SessionManager {

    private static Hashtable sessionTypes = new Hashtable();
    // this tracks the sessionType/userName to sessionID mapping
    private static Hashtable sessionToUser = new Hashtable();
    // this tracks the active sessionIDs in the system
    private static Hashtable sessionWrapTable = new Hashtable();
    // this maps the active sessionIDs to the corresponding SessionWrappers.
    private static boolean initialized = false;
    private static Properties sessionProps = new Properties();
    private static boolean starvation_denies_login = true;
    // default to not allowing logins when memory low
    private static int starvation_limit = 1;



    /**
     *  Used to get different configuration properties that control how the
     *  SessionManager works. As a security precaution, you must re-edit the
     *  session.properties config file and then re-start the web server to get
     *  the SessionManager to reload.
     *
     *@param  configName  Description of Parameter
     *@return             The Configuration value
     *@since
     *@author             $Author$
     */
    public static String getConfiguration(String configName) {
        init();

        return sessionProps.getProperty(configName);
    }


    /**
     *  Lists the available properties that the SessionManager has configured.
     *
     *@return    The ConfigurationNames value
     *@since
     *@author    $Author$
     */
    public static Enumeration getConfigurationNames() {
        init();

        return sessionProps.propertyNames();
    }


    /**
     *  This method lists the names of the sessionTypes that are currently being
     *  tracked. You use this method to gather statistics on the different
     *  sessions managed by the SessionManager.
     *
     *@return    The SessionTypes value
     *@since
     *@author    $Author$
     */
    public static Enumeration getSessionTypes() {
        init();

        return sessionTypes.keys();
    }


    /**
     *  Returns the current number of active sessions for the given type. If a
     *  sessionType is given that does not exist, then it returns a -1.
     *
     *@param  sessionType  Description of Parameter
     *@return              The SessionCount value
     *@since
     */
    public static long getSessionCount(String sessionType) {
        init();

        Hashtable userToSession = (Hashtable) sessionTypes.get(sessionType);
        if (userToSession == null) {
            return -1;
        }
        // bad session type, return negative

        return userToSession.size();
    }


    /**
     *  Returns the list of all the currently active users that have the given
     *  session type. It returns null if the given sessionType doesn't exist.
     *
     *@param  sessionType  Description of Parameter
     *@return              The UserNames value
     *@since
     *@author              $Author$
     */
    public static Enumeration getUserNames(String sessionType) {
        init();

        Hashtable userToSession = (Hashtable) sessionTypes.get(sessionType);

        if (userToSession == null) {
            return null;
        }
        // bad sesion, return null

        return userToSession.keys();
    }


    /**
     *  Get the list of session IDs that are currently active.
     *
     *@param  sessionType  Description of Parameter
     *@return              The SessionIDs value
     *@since
     *@author              $Author$
     */
    public static Enumeration getSessionIDs(String sessionType) {
        init();

        Hashtable userToSession = (Hashtable) sessionTypes.get(sessionType);
        if (userToSession == null) {
            return null;
            // bad sesion, return null
        }

        return userToSession.elements();
    }


    /**
     *  Gets the Creation time of a session
     *
     *@param  sessionId  Description of Parameter
     *@return            The CreationTime value
     *@since
     *@author            $Author$
     */
    public static Date getCreationTime(String sessionId) {
        init();

        SessionWrapper sessInfo = (SessionWrapper) sessionWrapTable.get(sessionId);
        if (sessInfo == null) {
            return null;
        }
        // this session is not in our table
        return sessInfo.creationTime;
    }


    // we default to 1MB

    /**
     *  This method does configuration based on the session.properties file. If
     *  the properties file isn't there, it will default to a safe
     *  configuration. This method can be safely called many times, and it will
     *  only initialize on the first one. We use an init method (and call at the
     *  beginning of all methods in SessionManager) because the base directory
     *  is only set in the GenericPortalBean during runtime. This means we can't
     *  use a static initialize block since that will get run before the
     *  GenericPortalBean knows the base directory. Also, we can't rely on the
     *  system calling init since there are many different access points to the
     *  system.
     *
     *@since
     *@author    $Author$
     */
    private static void init() {

        File sessionPropsFile = new File(GenericPortalBean.getPortalBaseDir() + "properties" + File.separator + "session.properties");

        if (!initialized) {
            try {

                sessionProps.load(new FileInputStream(sessionPropsFile));

                // setup our starvation_denis_login state
                if ("no".equals(sessionProps.getProperty("session.login.starvation_denies_login"))) {
                    // don't block users if memory runs out
                    Logger.log(Logger.INFO, "SessionManager: starvation of memory will ALLOW (NOT DENY) users access.");
                    starvation_denies_login = false;
                }
                else {
                    Logger.log(Logger.INFO, "SessionManager: starvation of memory will DENY users access.");
                    starvation_denies_login = true;
                }

                String starvationLimit = sessionProps.getProperty("session.login.starvation_limit");

                if (starvationLimit != null) {
                    try {
                        starvation_limit = Integer.parseInt(starvationLimit);
                        Logger.log(Logger.INFO, "SessionManager:  starvation limit set to " + starvationLimit);
                    }
                    catch (NumberFormatException e) {
                        Logger.log(Logger.ERROR, "SessionManager: could not parse the session.login.starvation_limit: '" + starvationLimit + "'");
                    }
                }

                // if an exception is thrown, this won't get set, and the file will be checked for again.
                // TODO:  not sure if this is desireable or not
                initialized = true;
            }
            catch (IOException e) {
                // just default to a safe state if nothing works
                Logger.log(Logger.ERROR, "SessionManager: could not open " + sessionPropsFile + ". DEFAULTING TO SAFE STATE AND CONTINUING.");
            }
        }

    }


    /**
     *  Tells the SessionManager that the specified userName is logged in and
     *  using the session with sessionID. You also need to specify the
     *  sessionType to prevent different parts of the application from stepping
     *  on eachother.
     *
     *@param  userName     Description of Parameter
     *@param  session      Description of Parameter
     *@param  sessionType  Description of Parameter
     *@since
     *@author              $Author$
     */
    public static void login(String userName, HttpSession session, String sessionType) {
        init();

        // these are used to print the requisite log messages, but outside the
        // synchronized block so that it runs as fast as possible
        SessionWrapper sessInfo = null;
        boolean invalidatedPreviousLogin = false;
        boolean allowingMultipleLogins = false;

        synchronized (sessionWrapTable) {
            Hashtable userToSession = (Hashtable) sessionTypes.get(sessionType);

            // register this new sessionType if it doesn't exist already
            if (userToSession == null) {
                userToSession = new Hashtable();
                sessionTypes.put(sessionType, userToSession);
            }

            // userToSession should now have the list of users currently active, find them and remove them
            String deadSessId = (String) userToSession.get(userName);

            if (deadSessId != null) {
                SessionWrapper deadSessWrap = (SessionWrapper) sessionWrapTable.get(deadSessId);

                // looks like this user is already logged in, kill their old session
                // but only if configured to NOT allow_multiple
                if ("no".equals(sessionProps.getProperty("session.login.allow_multiple"))) {
                    deadSessWrap.session.invalidate();
                    invalidatedPreviousLogin = true;
                }
                else {
                    // this is used to print out a warning message to the sysadmin
                    allowingMultipleLogins = true;
                }

                // clear them from our list
                sessionToUser.remove(deadSessWrap.Id);
                sessionWrapTable.remove(deadSessWrap.Id);
                userToSession.remove(userName);
            }

            sessInfo = new SessionWrapper(session);

            // now put this user in our list so we can track them

            sessionToUser.put(sessInfo.Id, userName);
            userToSession.put(userName, sessInfo.Id);
            sessionWrapTable.put(sessInfo.Id, sessInfo);
        }

        if (invalidatedPreviousLogin) {
            Logger.log(Logger.INFO, "SessionManager: Invalidating previous session for user : " + userName + ".");
        }

        if (allowingMultipleLogins) {
            Logger.log(Logger.WARN, "SessionManager: Allowing multiple logins of users.  THIS IS A MEMORY LEAK!");
        }

        if (sessInfo != null) {
            Logger.log(Logger.INFO, "SessionManager: logging in user " + userName + " with session ID " + sessInfo.Id);
        }

    }


    /**
     *  Tells the SessionManager that the specified userName has logged out and
     *  should be cleaned from the list of active sessions. You must specify the
     *  sessionType when removing a user this way since it is possible for more
     *  than one session to have the same user name.
     *
     *@param  userName     Description of Parameter
     *@param  sessionType  Description of Parameter
     *@since
     *@author              $Author$
     */
    public static void logout(String userName, String sessionType) {
        init();

        SessionWrapper sessInfo = null;

        synchronized (sessionWrapTable) {
            Hashtable userToSession = (Hashtable) sessionTypes.get(sessionType);

            // check to see if that sessionType exists already
            if (userToSession == null) {
                return;
            }
            // bad session type, do nothing

            String sessId = (String) userToSession.get(userName);
            if (sessId != null) {
                sessInfo = (SessionWrapper) sessionWrapTable.get(sessId);
                if (sessInfo == null || sessInfo.Id == null) {
                    return;
                }
                // bad session Id, or bad sessInfo object, do nothing

                // remove them from our list, and trust someone else to get rid of the session
                userToSession.remove(userName);
                sessionToUser.remove(sessInfo.Id);
                sessionWrapTable.remove(sessInfo.Id);
            }
        }

        if (sessInfo != null) {
            Logger.log(Logger.INFO, "SessionManager:  logged out user " + userName + " with session ID " + sessInfo.Id);
        }
    }


    /**
     *  Tells the SessionManager that the specified sessionID is being/has been
     *  invalidated. You must also give a session type so that the
     *  SessionManager can find the userName associated with this session and
     *  get rid of it.
     *
     *@param  session      Description of Parameter
     *@param  sessionType  Description of Parameter
     *@since
     *@author              $Author$
     */
    public static void logout(HttpSession session, String sessionType) {
        init();

        String sessionID = null;
        String userName = null;

        synchronized (sessionWrapTable) {
            sessionID = session.getId();

            // get the user name for this sessionID
            userName = (String) sessionToUser.get(sessionID);

            // check to make sure that this userName/sessionID really exists
            if (userName == null) {
                return;
            }
            // bad user name, do nothing

            Hashtable userToSession = (Hashtable) sessionTypes.get(sessionType);

            if (userToSession == null) {
                return;
            }
            // bad sessionType, do nothing

            // remove them from our list
            sessionToUser.remove(sessionID);
            sessionWrapTable.remove(sessionID);
            userToSession.remove(userName);

        }

        if (sessionID != null) {
            Logger.log(Logger.INFO, "SessionManager:  logged out user " + userName + " with session ID " + sessionID);
        }
    }


    /**
     *  This is used by other systems to figure out if they should allow more
     *  users onto the system. It is currently configured by the
     *  session.properties file, but it might be possible later to allow an
     *  admin control panel to set/unset this (wow, that would be a bit
     *  unsecure).
     *
     *@return    Description of the Returned Value
     *@since
     *@author    $Author$
     */
    public static boolean allowLogins() {

        if (starvation_denies_login) {
            // we ARE denying logins when memory gets too low
            long starvationDelta = memoryStarvationDelta();

            if (starvationDelta < 0) {
                // Captain! We're out 'o memory!
                // try to recover by forcing the GC to go
                Runtime.getRuntime().gc();

                starvationDelta = memoryStarvationDelta();

                if (starvationDelta < 0) {
                    // still out of memory, return false
                    Logger.log(Logger.ERROR, "SessionManager: memory starvation reached at " +
                            Runtime.getRuntime().freeMemory() + "memory free");
                    return false;
                }
                else {
                    // yeah! we got more memory.  Thanks GC!
                    return true;
                }
            }
            else {
                // good to go, let them login
                return true;
            }

        }
        else {
            return true;
        }
    }


    /**
     *  Simple method which does the actual test of memory. It doesn't try to do
     *  any recovery and is only called by the allowLogins method.
     *
     *@return    Description of the Returned Value
     *@since
     *@author    $Author$
     */
    protected static long memoryStarvationDelta() {

        long freeMem = Runtime.getRuntime().freeMemory();
        // get just the MB
        freeMem = freeMem / 1024;

        return (freeMem - (starvation_limit * 1024));
    }

}

