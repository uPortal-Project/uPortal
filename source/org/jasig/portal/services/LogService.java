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

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.PropertyConfigurator;
import org.jasig.portal.utils.ResourceLoader;


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
  private static boolean bInitialized = false;
  private static Logger m_logger = null;
  private static final LogService m_instance = new LogService();


  protected LogService () {
    initialize();
  }

  public final static LogService instance () {
    return  (m_instance);
  }

  /**
   *  Configures the Log4J system using the properties/Logger.properties file.
   *  Read the Log4J docs on how to setup one of these files to do anything
   *  you want. If this method isn't called before doing some logging, then
   *  Log4j will complain.
   */
  private final static void initialize () {
    // don't bother if we are already initialized
    if (bInitialized) {
      return;
    }
    try {
      String loggerPropsFileName = ResourceLoader.getResourceAsFileString(LogService.class, "/properties/Logger.properties");
      PropertyConfigurator.configureAndWatch(loggerPropsFileName);
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      // use a logger for the uPortal package to allow isolating logging
      m_logger = Logger.getLogger("org.jasig.portal");
      bInitialized = true;
    } catch (Exception e) {
      System.err.println("Problem writing to log.");
      e.printStackTrace();
    } catch (Error er) {
      System.err.println("Problem writing to log.");
      er.printStackTrace();
    }
  }


  public final static void log (Priority pLogLevel, String sMessage) {
    try {
      initialize();
      m_logger.log(pLogLevel, sMessage);
    } catch (Exception e) {
      System.err.println("Problem writing to log.");
      e.printStackTrace();
    } catch (Error er) {
      System.err.println("Problem writing to log.");
      er.printStackTrace();
    }
  }


  public final static void log (Priority pLogLevel, Throwable ex) {
    try {
      initialize();
      m_logger.log(pLogLevel, "EXCEPTION: " + ex, ex);
    } catch (Exception e) {
      System.err.println("Problem writing to log.");
      e.printStackTrace();
    } catch (Error er) {
      System.err.println("Problem writing to log.");
      er.printStackTrace();
    }
  }


  public final static void log (Priority pLogLevel, String sMessage, Throwable ex) {
    try {
      initialize();
      m_logger.log(pLogLevel, sMessage, ex);
    } catch (Exception e) {
      System.err.println("Problem writing to log.");
      e.printStackTrace();
    } catch (Error er) {
      System.err.println("Problem writing to log.");
      er.printStackTrace();
    }
  }

  public final static void log (String sMessage) {
    try {
      initialize();
      m_logger.log(INFO, sMessage);
    } catch (Exception e) {
      System.err.println("Problem writing to log.");
      e.printStackTrace();
    } catch (Error er) {
      System.err.println("Problem writing to log.");
      er.printStackTrace();
    }
  }
}



