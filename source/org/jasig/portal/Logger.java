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

import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;

import java.util.Date;
import java.util.Properties;

import java.text.SimpleDateFormat;

import org.apache.log4j.Category;
import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;

import org.apache.xerces.parsers.DOMParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

import org.xml.sax.InputSource;

import org.jasig.portal.PortalFileAppender;

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
 * @author Ken Weiner
 * @version $Revision$
 */
public class Logger extends GenericPortalBean
{
  // Log levels
  public static final int NONE   = 0;
  public static final int SEVERE = 1;
  public static final int ERROR  = 2;
  public static final int WARN   = 3;
  public static final int INFO   = 4;
  public static final int DEBUG  = 5;

  private static int iLogLevelSetting = DEBUG;

  private static String sLogLevelSetting = "DEBUG";

  private static final String sIllArgExMessage = "Log level must be NONE, SEVERE, ERROR, WARN, INFO, or DEBUG";
  private static final String fs = File.separator;
  private static final String sLogRelativePath = "logs" + fs + "portal.log";

  private static boolean bInitialized = false;

  private static Category     m_catFramework = null;

  private static PortalFileAppender m_logFile = null;

  private static int m_maxBackupIndex = 10;   // Maximum number of log files (Default is 10)
  private static int m_maxFileSize = 500000;  // Maximum size of each log file (Default is 500k)

  /**
   * Sets the current log level.  Use one of the static integer members
   * of this class ranging from  Logger.DEBUG to Logger.NONE.
   * The log level setting will determine the severity threshold of all log
   * messages.  The more lenient the log level, the more log messages will be logged.
   * @param a log level
   * @throws IllegalArgumentException if the log level is not one of the acceptable log levels
   */
  public static void setLogLevel (int iLogLevel)
  {
    if (!bInitialized)
    {
      initialize ();
    }

    if (iLogLevel <= DEBUG)
    {
      iLogLevelSetting = iLogLevel;
    }
    else
    {
      throw new IllegalArgumentException (sIllArgExMessage);
    }
  }

  /**
   * Gets the current log level setting
   * @return the current log level setting
   */
  public static int getLogLevel ()
  {
    return iLogLevelSetting;
  }

  /**
   * Sets the amount of backup log files to create
   * @param the amount of backup log files to create
   */
  /*
  public static void setMaxBackupLogFiles (int iMaxBackupLogFiles)
  {
    if (!bInitialized)
    {
      initialize ();
    }

    iMaxLogFiles = iMaxBackupLogFiles;
  }
  */

  /**
   * Gets the amount of backup log files to create
   * @return the amount of backup log files to create
   */
  /*
  public static int getMaxBackupLogFiles ()
  {
    return iMaxLogFiles;
  }
  */

  /**
   * Increments the old logs and creates a new log with the
   * current time and log level written at the top
   */
  public static void initialize ()
  {
    String sPortalBaseDir = getPortalBaseDir();
    File propertiesFile = null;

    try
    {
      // Load the portal properties file
      propertiesFile = new File(sPortalBaseDir + "/properties/portal.properties");

      if(propertiesFile != null)
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
        // Advance logs
        //advanceLogs();

        //BufferedWriter fileWriter = new BufferedWriter(new FileWriter(sPortalBaseDir + sLogRelativePath), m_writeBufferSize);
        
        m_logFile = new PortalFileAppender(new PatternLayout("%-5p %-23d{ISO8601} %m%n"), sPortalBaseDir + sLogRelativePath);

        // Make sure to roll the logs to start fresh
        m_logFile.rollOver();

        m_logFile.setMaxBackupIndex(m_maxBackupIndex);
        m_logFile.setMaxFileSize(m_maxFileSize);

        m_catFramework = Category.getRoot();
        m_catFramework.addAppender(m_logFile);

        m_catFramework.setPriority(Priority.toPriority(sLogLevelSetting));

        // Print a header to the log containing the current time and log level
        //String sDate = getLogTimeStamp ();
        //PrintWriter out = new PrintWriter (new BufferedWriter (new FileWriter (sPortalBaseDir + sLogRelativePath, true)));
        //out.println("Portal log service initiating on " + sDate + "  Log level: " + getLogLevel (iLogLevelSetting));
        //out.println();
        //out.close();

        // Insures that initialization is only done once
        bInitialized = true;
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

  public static void log (int iLogLevel, String sMessage)
  {
    try
    {
      if (!bInitialized)
      {
        initialize();
      }

      switch(iLogLevel)
      {
        case NONE:
          return;
        case SEVERE:
          m_catFramework.fatal(sMessage);
          return;
        case ERROR:
          m_catFramework.error(sMessage);
          return;
        case WARN:
          m_catFramework.warn(sMessage);
          return;
        case INFO:
          m_catFramework.info(sMessage);
          return;
        case DEBUG:
          m_catFramework.debug(sMessage);
          return;
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

  public static void log (int iLogLevel, Throwable ex)
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
          return;
        case SEVERE:
          m_catFramework.fatal(stackTrace.toString());
          return;
        case ERROR:
          m_catFramework.error(stackTrace.toString());
          return;
        case WARN:
          m_catFramework.warn(stackTrace.toString());
          return;
        case INFO:
          m_catFramework.info(stackTrace.toString());
          return;
        case DEBUG:
          m_catFramework.debug(stackTrace.toString());
          return;
        default:
          throw new IllegalArgumentException ();
      }
    }
    catch(Exception e)
    {
      System.err.println ("Problem writing to log.");
      e.printStackTrace();
    }
    catch(Error er)
    {
      System.err.println ("Problem writing to log.");
      er.printStackTrace();
    }
  }

  /**
   * Writes a message to the log
   * @param the log level
   * @param the message to write
   */
  /*
  public static void log (int iLogLevel, String sMessage)
  {
    try
    {
      if (!bInitialized)
        initialize ();

      if (iLogLevel <= iLogLevelSetting)
      {
        String sDate = getLogTimeStamp ();
        String sPortalBaseDir = getPortalBaseDir ();

        if (sPortalBaseDir != null && new File (sPortalBaseDir).exists ())
        {
          PrintWriter out = new PrintWriter (new BufferedWriter (new FileWriter (sPortalBaseDir + sLogRelativePath, true)));
          out.println (getLogLevel (iLogLevel) + " - " + sDate + " - " + sMessage);
          out.close ();
        }
      }
    }
    catch (IllegalArgumentException iae)
    {
      throw new IllegalArgumentException (sIllArgExMessage);
    }
    catch (Exception e)
    {
      System.err.println ("Problem writing to log.");
      e.printStackTrace ();
    }
  }
  */

  /**
   * Writes an exception's stack trace to the log
   * @param the log level
   * @param an exception
   * @throws IllegalArgumentException if the log level is not one of the acceptable log levels
   */
  /*
  public static void log (int iLogLevel, Throwable ex)
  {
    try
    {
      if (!bInitialized)
        initialize ();

      if (iLogLevel <= iLogLevelSetting)
      {
        String sDate = getLogTimeStamp ();
        String sPortalBaseDir = getPortalBaseDir ();

        if (sPortalBaseDir != null && new File (sPortalBaseDir).exists ())
        {
          PrintWriter out = new PrintWriter (new BufferedWriter (new FileWriter (sPortalBaseDir + sLogRelativePath, true)));
          out.println (getLogLevel (iLogLevel) + " - " + sDate + " - " + "Stack trace:");
          out.println ();
          ex.printStackTrace (out);
          out.println ();
          out.close ();
        }
      }
    }
    catch (IllegalArgumentException iae)
    {
      throw new IllegalArgumentException (sIllArgExMessage);
    }
    catch (Exception e)
    {
      System.err.println ("Problem writing to log.");
      e.printStackTrace ();
    }
  }
  */

  /**
   * Translates integer version of log level into
   * string version for printing to the log
   * @param the log level
   * @return a string representation of the log level
   * @throws IllegalArgumentException if the log level is not one of the acceptable log levels
   */
  private static String getLogLevel (int iLogLevel)
  {
    String sLogLevel = null;

    switch (iLogLevel)
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
        throw new IllegalArgumentException ();
    }

    return sLogLevel;
  }

  /**
   * Gets a timestamp string suitable for displaying in the logs
   * @return a timestamp for a log entry
   */
  private static String getLogTimeStamp ()
  {
    // Format the current date/time.
    SimpleDateFormat formatter = new SimpleDateFormat ("EEE, yyyy/MM/dd 'at' HH:mm:ss:SSS z");
    java.util.Date currentTime = new java.util.Date();
    String sDate = formatter.format (currentTime);
    return sDate;
  }

  /**
   * This method advances the log files, throwing away the oldest
   */
  /*
  private static void advanceLogs ()
  {
    String sLogFile, sLogBase, sOldPath, sNewPath;
    String sLogExt = ".log";
    String sDelimiter = "-";
    File oldFile, newFile, logsDir;
    java.text.DecimalFormat df = new java.text.DecimalFormat ("00");

    // Create logs directory if it doesn't already exist
    logsDir = new File (getPortalBaseDir () + "logs");

    if (!logsDir.exists ())
      logsDir.mkdir ();

    // Get full path to current log file
    sLogFile = getPortalBaseDir () + sLogRelativePath;

    // Get portion before ".log"
    sLogBase = sLogFile.substring(0, sLogFile.indexOf (sLogExt));

    // Delete oldest log file
    new File (sLogBase + sDelimiter + df.format (iMaxLogFiles) + sLogExt).delete ();

    // Rename log files, incrementing the number of each one
    for (int i = iMaxLogFiles - 1; i > 0; i--)
    {
      sNewPath = sLogBase + sDelimiter + df.format (i + 1) + sLogExt;
      sOldPath = sLogBase + sDelimiter + df.format (i) + sLogExt;
      newFile = new File (sNewPath);
      oldFile = new File (sOldPath);
      oldFile.renameTo (newFile);
    }

    // Rename the previous log file
    sNewPath = sLogBase + sDelimiter + df.format (1) + sLogExt;
    newFile = new File (sNewPath);
    oldFile = new File (sLogFile);
    oldFile.renameTo (newFile);
  }
  */
}
