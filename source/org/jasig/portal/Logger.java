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


package  org.jasig.portal;

import  org.jasig.portal.services.LogService;
import  org.apache.log4j.Priority;


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
 * the static member log level, which can be changed by calling setLogLevel().
 *
 * @author Ken Weiner (IBS)
 * @author Bernie Durfee (IBS)
 * @version $Revision$
 * @deprecated Use org.jasig.portal.LogService instead
 */
public class Logger extends GenericPortalBean {
  /**
   *  Description of the Field
   */
  public final static Priority NONE = Priority.DEBUG;
  /**
   *  Description of the Field
   */
  public final static Priority SEVERE = Priority.FATAL;
  /**
   *  Description of the Field
   */
  public final static Priority ERROR = Priority.ERROR;
  /**
   *  Description of the Field
   */
  public final static Priority WARN = Priority.WARN;
  /**
   *  Description of the Field
   */
  public final static Priority INFO = Priority.INFO;
  /**
   *  Description of the Field
   */
  public final static Priority DEBUG = Priority.DEBUG;

  /**
   * put your documentation comment here
   * @param iLogLevel
   * @param sMessage
   * @deprecated Use org.jasig.portal.LogService instead
   */
  public static void log (Priority logLevel, String sMessage) {
    LogService.instance().log(logLevel, sMessage);
  }

  /**
   * put your documentation comment here
   * @param iLogLevel
   * @param ex
   * @deprecated Use org.jasig.portal.LogService instead
   */
  public static void log (Priority logLevel, Throwable ex) {
    LogService.instance().log(logLevel, ex);
  }

  /**
   * put your documentation comment here
   * @param logLevel
   * @param sMessage
   * @param ex
   */
  public static void log (Priority logLevel, String sMessage, Throwable ex) {
    LogService.instance().log(logLevel, sMessage, ex);
  }

  /**
   *  Generic logging method that logs to a default of INFO. These should be
   *  eliminated eventually.
   *
   *@param  sMessage  Description of Parameter
   */
  public final static void log (String sMessage) {
    LogService.instance().log(sMessage);
  }
}



