/**
 *  Copyright (c) 2000 The JA-SIG Collaborative. All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  1. Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer. 2. Redistributions in
 *  binary form must reproduce the above copyright notice, this list of
 *  conditions and the following disclaimer in the documentation and/or other
 *  materials provided with the distribution. 3. Redistributions of any form
 *  whatsoever must retain the following acknowledgment: "This product includes
 *  software developed by the JA-SIG Collaborative (http://www.jasig.org/)."
 *  THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 *  EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR ITS CONTRIBUTORS
 *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Priority;


/**
 * As of uPortal 2.4, use Apache Commons Logging directly instead of using this
 * LogService.  This LogService is retained here for backwards compatibility,
 * and presumably will disappear in a future release.
 * 
 *  The LogService is a service offered by the uPortal framework whereby messages
 * can be logged.  Each uPortal deployment can customize exactly where how and
 * how much they want logged.  As of uPortal 2.4, this is accomplished by this class
 * delegating all logging to Apache Commons Logging.  The expected typical (and
 * default) local logging configuration is to use the Logger.properties file to configure
 * Log4J as the underlying logging implementation, to a file on disk.  However, the
 * options are endless under Log4J: you can configure it to log as XML, to log to a tree
 * of files depending upon where the logging message is coming from and at what 
 * logging level, to send log messages over the network to a Chainsaw instance listening to
 * your uPortal...  Furthermore, you don't even have to use Log4J: Commons Logging supports
 * JDK1.4 logging as well as the ability for you to plug in a custom logging implementation if
 * you really want to.
 * 
 * @author     Ken Weiner, Bernie Durfee, Vikrant Joshi, Zed A. Shaw, andrew.petro@yale.edu
 * @version    $Revision$ $Date$
 * @deprecated As of uPortal 2.4, please use Apache Commons Logging directly
 */
public final class LogService {
  // Log levels, create fake ones if they don't match the Log4J standard ones

  public final static Priority NONE = Priority.DEBUG;

  public final static Priority SEVERE = Priority.FATAL;

  public final static Priority ERROR = Priority.ERROR;

  public final static Priority WARN = Priority.WARN;

  public final static Priority INFO = Priority.INFO;

  public final static Priority DEBUG = Priority.DEBUG;
  private static final Log log = LogFactory.getLog("org.jasig.portal");
  private static final LogService m_instance = new LogService();


  static{
      initialize();
  }
  
  protected LogService () {
    initialize();
  }

  public final static LogService instance () {
    return  (m_instance);
  }

  /**
   * Used to configure Log4J with the Logger.properties file.
   * Now does nothing, as the build.xml now copies the Logger.properties file
   * to the well known name (log4j.properties) and location (base of the classpath
   * by virtue of being in the base of the WEB-INF/classes/ directory) expected
   * by Log4J.
   *  @deprecated no longer does anything
   */
  private final static void initialize () {
    /*
     * No longer does anything.
     * The build.xml compile task copies the Logger.properties file
     * to the well known name and location Log4J expects.
     */
  }


  public final static void log (Priority pLogLevel, String sMessage) {
      initialize();
      if (pLogLevel == null){
          log.fatal(sMessage);
      } else if (pLogLevel.equals(SEVERE)){
          log.fatal(sMessage);
      } else if (pLogLevel.equals(ERROR)){
          log.error(sMessage);
      } else if (pLogLevel.equals(WARN)){
          log.warn(sMessage);
      } else if (pLogLevel.equals(INFO)){
          log.info(sMessage);
      } else if (pLogLevel.equals(DEBUG)){
          log.debug(sMessage);
      } else if (pLogLevel.equals(NONE)){
          log.debug(sMessage);
      } else {
          // should never get here
          log.fatal(sMessage);
      }
  }


  /**
   * Log the given throwable at the given priority.
   * @param pLogLevel - logging level
   * @param ex - throwable to be logged
   */
  public final static void log (Priority pLogLevel, Throwable ex) {
      initialize();
      if (pLogLevel == null){
          log.fatal(ex, ex);
      } else if (pLogLevel.equals(SEVERE)){
          log.fatal(ex, ex);
      } else if (pLogLevel.equals(ERROR)){
          log.error(ex, ex);
      } else if (pLogLevel.equals(WARN)){
          log.warn(ex, ex);
      } else if (pLogLevel.equals(INFO)){
          log.info(ex, ex);
      } else if (pLogLevel.equals(DEBUG)){
          log.debug(ex, ex);
      } else if (pLogLevel.equals(NONE)){
          log.debug(ex, ex);
      } else {
          // should never get here
          log.fatal("Unrecognized logging level " + pLogLevel, ex);
      }
  }


  public final static void log (Priority pLogLevel, String sMessage, Throwable ex) {
      initialize();
      if (pLogLevel == null){
          log.fatal(sMessage, ex);
      }else if (pLogLevel.equals(SEVERE)){
          log.fatal(sMessage, ex);
      } else if (pLogLevel.equals(ERROR)){
          log.error(sMessage, ex);
      } else if (pLogLevel.equals(WARN)){
          log.warn(sMessage, ex);
      } else if (pLogLevel.equals(INFO)){
          log.info(sMessage, ex);
      } else if (pLogLevel.equals(DEBUG)){
          log.debug(sMessage, ex);
      } else if (pLogLevel.equals(NONE)){
          log.debug(sMessage, ex);
      } else {
          // Should never get here.
          log.fatal(sMessage, ex);
      }
  }

  public final static void log (String sMessage) {
      initialize();
    log.info(sMessage);
  }
  
}



