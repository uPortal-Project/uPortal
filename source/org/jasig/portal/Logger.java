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
  // Log levels
  public static final int NONE = 0;
  public static final int SEVERE = 1;
  public static final int ERROR = 2;
  public static final int WARN = 3;
  public static final int INFO = 4;
  public static final int DEBUG = 5;

  /**
   * Sets the current log level.  Use one of the static integer members
   * of this class ranging from  Logger.DEBUG to Logger.NONE.
   * The log level setting will determine the severity threshold of all log
   * messages.  The more lenient the log level, the more log messages will be logged.
   *
   * @param a log level
   * @throws IllegalArgumentException if the log level is not one of the acceptable log levels
   * @deprecated Use org.jasig.portal.LogService instead
   */
  public static void setLogLevel (int iLogLevel) {
    LogService.instance().setLogLevel(iLogLevel);
  }

  /**
   * Gets the current log level setting
   * @return the current log level setting
   * @deprecated Use org.jasig.portal.LogService instead
   */
  public static int getLogLevel () {
    return  (LogService.instance().getLogLevel());
  }

  /**
   * put your documentation comment here
   * @param iLogLevel
   * @param sMessage
   * @param ex
   * @deprecated Use org.jasig.portal.LogService instead
   */
  public static void log (int iLogLevel, String sMessage, Throwable ex) {
    LogService.instance().log(iLogLevel, sMessage, ex);
  }

  /**
   * put your documentation comment here
   * @param iLogLevel
   * @param sMessage
   * @deprecated Use org.jasig.portal.LogService instead
   */
  public static void log (int iLogLevel, String sMessage) {
    LogService.instance().log(iLogLevel, sMessage);
  }

  /**
   * put your documentation comment here
   * @param iLogLevel
   * @param ex
   * @deprecated Use org.jasig.portal.LogService instead
   */
  public static void log (int iLogLevel, Throwable ex) {
    LogService.instance().log(iLogLevel, ex);
  }
}



