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
 */
package org.jasig.portal;

import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileInputStream;

import java.util.Properties;

import org.apache.log4j.Category;
import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;

import org.jasig.portal.services.PortalFileAppender;

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
 */

public class Logger extends GenericPortalBean
{
  // Log levels
  public static final int NONE = 0;
  public static final int SEVERE = 1;
  public static final int ERROR = 2;
  public static final int WARN = 3;
  public static final int INFO = 4;
  public static final int DEBUG = 5;

  private static int iLogLevelSetting = DEBUG;

  private static String sLogLevelSetting = "DEBUG";

  private static final String sIllArgExMessage = "Log level must be NONE, SEVERE, ERROR, WARN, INFO, or DEBUG";

  private static final String fs = File.separator;

  private static String sLogRelativePath = "logs";

  private static boolean bInitialized = false;

  private static Category m_catFramework = null;

  private static PortalFileAppender m_logFile = null;

  private static int m_maxBackupIndex = 10; // Maximum number of log files (Default is 10)

  private static int m_maxFileSize = 500000; // Maximum size of each log file (Default is 500k)

  /**
   * Sets the current log level.  Use one of the static integer members
   * of this class ranging from  Logger.DEBUG to Logger.NONE.
   * The log level setting will determine the severity threshold of all log
   * messages.  The more lenient the log level, the more log messages will be logged.
   *
   * @param a log level
   * @throws IllegalArgumentException if the log level is not one of the acceptable log levels
   */
  public static void setLogLevel(int iLogLevel)
  {
    if(!bInitialized)
    {
      initialize();
    }
    if(iLogLevel <= DEBUG)
    {
      iLogLevelSetting = iLogLevel;
    }
    else
    {
      throw new IllegalArgumentException(sIllArgExMessage);
    }
  }

  /**
   * Gets the current log level setting
   * @return the current log level setting
   */
  public static int getLogLevel()
  {
    return iLogLevelSetting;
  }

  /**
   * Increments the old logs and creates a new log with the
   * current time and log level written at the top
   */
  public static void initialize()
  {
    String sPortalBaseDir = getPortalBaseDir();
    File propertiesFile = null;

    try
    {
      // Load the portal properties file
      propertiesFile = new File(sPortalBaseDir + "/properties/portal.properties");

      if(propertiesFile != null && propertiesFile.exists())
      {
        Properties portalProperties = new Properties();
        portalProperties.load(new FileInputStream(propertiesFile));

        if(portalProperties.getProperty("logger.level") != null)
        {
          sLogLevelSetting = portalProperties.getProperty("logger.level");
        }
        else
        {
          System.out.println("Defaulting to " + sLogLevelSetting + " for portal log level.");
        }
        if(portalProperties.getProperty("logger.maxLogFiles") != null)
        {
          m_maxBackupIndex = Integer.parseInt(portalProperties.getProperty("logger.maxLogFiles"));
        }
        else
        {
          System.out.println("Defaulting to " + m_maxBackupIndex + " for maximum number of portal log files.");
        }
        if(portalProperties.getProperty("logger.maxFileSize") != null)
        {
          m_maxFileSize = Integer.parseInt(portalProperties.getProperty("logger.maxFileSize"));
        }
        else
        {
          System.out.println("Defaulting to " + m_maxFileSize + " for maximum log file size.");
        }
      }
      else
      {
        System.out.println("The file portal.properties could not be loaded, using default properties.");
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    try
    {
      if(sPortalBaseDir != null && new File(sPortalBaseDir).exists())
      {
        PatternLayout patternLayout = new PatternLayout("%-5p %-23d{ISO8601} %m%n");

        // Make sure the relative path ends with a seperator
        if(!sLogRelativePath.endsWith(File.separator))
        {
          sLogRelativePath = sLogRelativePath + File.separator;
        }

        // Make sure the portal base directory path ends with a seperator
        if(!sPortalBaseDir.endsWith(File.separator))
        {
          sPortalBaseDir = sPortalBaseDir + File.separator;
        }

        // Create the log file directory path
        String logFileDirectoryPath = sPortalBaseDir + sLogRelativePath;

        File logFileDirectory = new File(logFileDirectoryPath);

        // Make sure the log file directory exists
        if(!logFileDirectory.exists())
        {
          if(!logFileDirectory.mkdir() || !logFileDirectory.exists() || !logFileDirectory.isDirectory())
          {
            System.out.println("Could not create log file directory!");
          }
        }

        // Create the file appender
        m_logFile = new PortalFileAppender(patternLayout, logFileDirectoryPath + "portal.log");

        // Make sure to roll the logs to start fresh
        m_logFile.rollOver();
        m_logFile.setMaxBackupIndex(m_maxBackupIndex);
        m_logFile.setMaxFileSize(m_maxFileSize);
        m_catFramework = Category.getRoot();
        m_catFramework.addAppender(m_logFile);
        m_catFramework.setPriority(Priority.toPriority(sLogLevelSetting));

        // Insures that initialization is only done once
        bInitialized = true;
      }
      else
      {
        System.out.println("Logger.initialize(): PortalBaseDir is not set or does not exist!");
      }
    }
    catch(Exception e)
    {
      System.err.println("Problem writing to log.");
      e.printStackTrace();
    }
    catch(Error er)
    {
      System.err.println("Problem writing to log.");
      er.printStackTrace();
    }
  }

  public static void log(int iLogLevel, String sMessage)
  {
    try
    {
      if(!bInitialized)
      {
        initialize();
      }

      switch(iLogLevel)
      {
        case NONE:
          return ;

        case SEVERE:
          m_catFramework.fatal(sMessage);
          return ;

        case ERROR:
          m_catFramework.error(sMessage);
          return ;

        case WARN:
          m_catFramework.warn(sMessage);
          return ;

        case INFO:
          m_catFramework.info(sMessage);
          return ;

        case DEBUG:
          m_catFramework.debug(sMessage);
          return ;

        default:
          throw new IllegalArgumentException();
      }
    }
    catch(Exception e)
    {
      System.err.println("Problem writing to log.");
      e.printStackTrace();
    }
    catch(Error er)
    {
      System.err.println("Problem writing to log.");
      er.printStackTrace();
    }
  }

  public static void log(int iLogLevel, Throwable ex)
  {
    try
    {
      if(!bInitialized)
      {
        initialize();
      }

      StringWriter stackTrace = new StringWriter();
      ex.printStackTrace(new PrintWriter(stackTrace));

      switch(iLogLevel)
      {
        case NONE:
          return ;

        case SEVERE:
          m_catFramework.fatal(stackTrace.toString());
          return ;

        case ERROR:
          m_catFramework.error(stackTrace.toString());
          return ;

        case WARN:
          m_catFramework.warn(stackTrace.toString());
          return ;

        case INFO:
          m_catFramework.info(stackTrace.toString());
          return ;

        case DEBUG:
          m_catFramework.debug(stackTrace.toString());
          return ;

        default:
          throw new IllegalArgumentException();
      }
    }
    catch(Exception e)
    {
      System.err.println("Problem writing to log.");
      e.printStackTrace();
    }
    catch(Error er)
    {
      System.err.println("Problem writing to log.");
      er.printStackTrace();
    }
  }

  /**
   * Translates integer version of log level into
   * string version for printing to the log
   * @param the log level
   * @return a string representation of the log level
   * @throws IllegalArgumentException if the log level is not one of the acceptable log levels
   */
  private static String getLogLevel(int iLogLevel)
  {
    String sLogLevel = null;
    switch(iLogLevel)
    {
      case NONE:
        sLogLevel = "NONE  ";
        break;

      case SEVERE:
        sLogLevel = "SEVERE";
        break;

      case ERROR:
        sLogLevel = "ERROR ";
        break;

      case WARN:
        sLogLevel = "WARN  ";
        break;

      case INFO:
        sLogLevel = "INFO  ";
        break;

      case DEBUG:
        sLogLevel = "DEBUG ";
        break;

      default:
        throw new IllegalArgumentException();
    }
    return sLogLevel;
  }
}
