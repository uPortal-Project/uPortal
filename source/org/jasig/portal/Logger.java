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
 */

package org.jasig.portal;

import java.io.*;

import org.apache.log4j.*;

/**
 * The Logger class is used to output messages to a log file.
 * The first call to a log method triggers an initialization which
 * renames old logs and creates a new log with a message stating the
 * current time and log level.  The maximum number of backup log files
 * is specified as a member variable and can be set by calling
 * setMaxBackupLogFiles ().  When calling a log method, it is necessary
 * to specify a log level which can be either NONE, SEVERE, ERROR, WARN,
 * INFO, or DEBUG (listed in order of decreasing severity).  Log messages
 * will only be logged if their log level is the same or more severe than
 * the static member log level, which can be changed by calling setLogLevel ().
 *
 * Zed's NOTES:  This has been completely re-written to use the Log4J system
 * entirely.  This means that you can now write wonderful config files which let
 * you pick your own log format and your own ouput methods.  It will also now
 * watch the config file for changes and reload them (thus, letting you make changes
 * without restarting the Portal).
 *
 * @author Ken Weiner, Bernie Durfee, Vikrant Joshi, Zed A. Shaw
 * @version $Revision$
 */

public class Logger extends GenericPortalBean {
    // Log levels, create fake ones if they don't match the Log4J standard ones
    public static final Priority NONE   = Priority.DEBUG;
    public static final Priority SEVERE = Priority.FATAL;
    public static final Priority ERROR  = Priority.ERROR;
    public static final Priority WARN   = Priority.WARN;
    public static final Priority INFO   = Priority.INFO;
    public static final Priority DEBUG  = Priority.DEBUG;

    private static final String fs = File.separator;
    private static final String sLogRelativePath = "logs" + fs + "portal.log";

    private static boolean bInitialized = false;

    private static Category     m_category = null;




    /**
     * Configures the Log4J system using the properties/Logger.properties file.
     * Read the Log4J docs on how to setup one of these files to do anything you want.
     * If this method isn't called before doing some logging, then Log4j will complain.
     */
    public static void initialize () {
        // don't bother if we are already initialized

        if(bInitialized)
            return;
        String sPortalBaseDir = getPortalBaseDir();

        try {

            PropertyConfigurator.configureAndWatch(sPortalBaseDir + "/properties/Logger.properties");

        } catch(Exception e) {
            e.printStackTrace();
        }



        try {
            m_category = Category.getRoot();

            bInitialized = true;

        } catch(Exception e) {
            System.err.println("Problem writing to log.");
            e.printStackTrace();
        }
        catch(Error er) {
            System.err.println("Problem writing to log.");
            er.printStackTrace();
        }
    }




    public static void log (Priority pLogLevel, String sMessage) {
        try {

            initialize();
            m_category.log(pLogLevel, sMessage);

        } catch(Exception e) {
            System.err.println("Problem writing to log.");
            e.printStackTrace();
        }
        catch(Error er) {
            System.err.println("Problem writing to log.");
            er.printStackTrace();
        }
    }



    public static void log (Priority pLogLevel, Throwable ex) {
        try {
            initialize();
            m_category.log(pLogLevel, "EXCEPTION: " + ex);
        } catch(Exception e) {
            System.err.println ("Problem writing to log.");
            e.printStackTrace();
        }
        catch(Error er) {
            System.err.println ("Problem writing to log.");
            er.printStackTrace();
        }
    }

    /** Generic logging method that logs to a default of
    INFO.  These should be eliminated eventually.
    */
    public static void log(String sMessage) {
        try {
            initialize();
            m_category.log(INFO, sMessage);
        } catch (Exception e) {
            System.err.println ("Problem writing to log.");
            e.printStackTrace();
        }
        catch(Error er) {
            System.err.println ("Problem writing to log.");
            er.printStackTrace();
        }
    }

}


