/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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
 * @author: Zed A Shaw
 * Modified by: Vikrant Joshi (April 4, 2001)
 *
 */

package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;


/**
 * This class acts as a common location for session management (implemented as a singleton).  It 
 * is used to prevent more than one login for each user to reduce the amount of memory used during
 * operation, and to prevent Denial Of Service (DOS) attacks.
 */
public class SessionManager {

    private static HttpSessionContext sessionContext = null;
    private static Hashtable sessionTypes = new Hashtable();     // this tracks the sessionType/userName to sessionID mapping
    private static Hashtable sessionToUser = new Hashtable();   // this tracks the active sessionIDs in the system
    private static Hashtable sessionWrapTable = new Hashtable();   // this maps the active sessionIDs to the corresponding SessionWrappers. The purpose of this table is purely for efficient search of SessionWrapper objects.


    /** Tells the SessionManager that the specified userName is logged in and using the session with sessionID.  
     * You also need to specify the sessionType to prevent different parts of the application from stepping on
     * eachother.
     */
    public static void login(String userName, HttpSession session, String sessionType) {
	synchronized (sessionWrapTable) {	
	    Hashtable userToSession = (Hashtable)sessionTypes.get(sessionType);

	    // register this new sessionType if it doesn't exist already
	    if(userToSession == null) {
	        userToSession = new Hashtable();
	        sessionTypes.put(sessionType, userToSession);
	    }

       	    // userToSession should now have the list of users currently active, find them and remove them
            String deadSessId = (String)userToSession.get(userName);

       	    if (deadSessId != null) {
	   	 SessionWrapper deadSessWrap = (SessionWrapper)sessionWrapTable.get(deadSessId);

	  	 // looks like this user is already logged in, kill their old session

	  	 deadSessWrap.session.invalidate();

	  	 // clear them from our list

	   	 sessionToUser.remove(deadSessWrap.Id);
       	  	 sessionWrapTable.remove(deadSessWrap.Id);
	   	 userToSession.remove(userName);
	   }

  	   SessionWrapper sessInfo = new SessionWrapper(session);	

	   // now put this user in our list so we can track them

	   sessionToUser.put(sessInfo.Id,userName);
	   userToSession.put(userName, sessInfo.Id);
	   sessionWrapTable.put(sessInfo.Id, sessInfo);
	}
    } 


    /** Tells the SessionManager that the specified userName has logged out and should be cleaned from
     * the list of active sessions.  You must specify the sessionType when removing a user this way
     * since it is possible for more than one session to have the same user name.
     */
    public static void logout(String userName,String sessionType)
    {
	synchronized (sessionWrapTable) {	
            Hashtable userToSession = (Hashtable)sessionTypes.get(sessionType);
	
            // check to see if that sessionType exists already
	    if(userToSession == null) 
	        return;  // bad session type, do nothing

	    String sessId = (String)userToSession.get(userName);
	    if (sessId != null) {
	        SessionWrapper sessInfo = (SessionWrapper)sessionWrapTable.get(sessId);
	        if(sessInfo == null || sessInfo.Id == null)
	    	    return;  // bad session Id, or bad sessInfo object, do nothing

	        // remove them from our list, and trust someone else to get rid of the session
	        userToSession.remove(userName);
	        sessionToUser.remove(sessInfo.Id);
	        sessionWrapTable.remove(sessInfo.Id);
	   }
	}
    }


    /** Tells the SessionManager that the specified sessionID is being/has been invalidated. 
     * You must also give a session type so that the SessionManager can find the userName
     * associated with this session and get rid of it.
     */
    public static void logout(HttpSession session, String sessionType) {
	synchronized (sessionWrapTable) {	
	    String sessionID = session.getId();

	    // get the user name for this sessionID
	    String userName = (String)sessionToUser.get(sessionID);
	
	    // check to make sure that this userName/sessionID really exists
	    if(userName == null)
	        return;  // bad user name, do nothing

	    Hashtable userToSession = (Hashtable)sessionTypes.get(sessionType);

	    if(userToSession == null)
	        return;  // bad sessionType, do nothing

	    // remove them from our list
	    sessionToUser.remove(sessionID);
	    sessionWrapTable.remove(sessionID);
	    userToSession.remove(userName);
	}
    }
	

    /** This method lists the names of the sessionTypes that are currently being tracked.
     * You use this method to gather statistics on the different sessions managed by the
     * SessionManager.
     */
    public static Enumeration getSessionTypes() {
	return sessionTypes.keys();
    }

    /** Returns the current number of active sessions for the given type.
     *  If a sessionType is given that does not exist, then it returns a -1.
     */
    public static long getSessionCount(String sessionType) {
	Hashtable userToSession = (Hashtable)sessionTypes.get(sessionType);
	if(userToSession == null) 
	    return -1;   // bad session type, return negative

	return userToSession.size();
    }

    /** Returns the list of all the currently active users that have the given session type. 
     *  It returns null if the given sessionType doesn't exist.
     */
    public static Enumeration getUserNames(String sessionType) {
	Hashtable userToSession = (Hashtable)sessionTypes.get(sessionType);

	if(userToSession == null)
	    return null;  // bad sesion, return null

	return userToSession.keys();
    }


    /** Get the list of session IDs that are currently active. */
    public static Enumeration getSessionIDs(String sessionType) {
	Hashtable userToSession = (Hashtable)sessionTypes.get(sessionType);
	if(userToSession == null) {
	    return null;  // bad sesion, return null
	}
	return userToSession.elements();
    }

    /** Gets the Creation time of a session */
    public static Date getCreationTime(String sessionId) {
           SessionWrapper sessInfo = (SessionWrapper)sessionWrapTable.get(sessionId);
	   if (sessInfo == null) return null;    // this session is not in our table
	   return sessInfo.creationTime;
    }

}
