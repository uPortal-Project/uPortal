package org.jasig.portal;

import java.util.*;
import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import javax.servlet.http.HttpSession;
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
 * <p>The Logger.properties file determines what the log level is.  If that file cannot
 * be found then it defaults to iLogLevelSetting = DEBUG
 * @author Ken Weiner
 * @author Steven Toth
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
  public static final String LOGGER_LEVEL_PROPERTY_NAME = "org.jasig.portal.Logger.loglevel";
  private static int iLogLevelSetting = DEBUG;
  private static final String sIllArgExMessage = "Log level must be NONE, SEVERE, ERROR, WARN, INFO, or DEBUG";
  private static final String sLogRelativePath = "logs/portal.log";
  private static int iMaxLogFiles = 5;
  private static boolean bInitialized = false;
  protected static final int LoggerTimeout = 600000; //Time until the log closes the log file.
  protected static final int LoggerFlushTimeout = 10000;  //Every x mills, the log will be flushed to disk.

  /**
   * Sets the current log level.  Use one of the static integer members
   * of this class ranging from  Logger.DEBUG to Logger.NONE.
   * The log level setting will determine the severity threshold of all log
   * messages.  The more lenient the log level, the more log messages will be logged.
   * @param a log level
   * @throws IllegalArgumentException if the log level is not one of the acceptable log levels
   */
  static
  {
    initialize ();
  }

  public static void loadDefaultLogLevel ()
  {
    String loglevel = System.getProperty (LOGGER_LEVEL_PROPERTY_NAME);
    if ( loglevel != null )
    {
      loglevel = loglevel.trim ().toLowerCase ();
      if (loglevel.startsWith ("none"))
      iLogLevelSetting = 0;
      else
      if (loglevel.startsWith ("severe"))
      iLogLevelSetting = 1;
      else
      if (loglevel.startsWith ("error"))
      iLogLevelSetting = 2;
      else
      if (loglevel.startsWith ("warn"))
      iLogLevelSetting = 3;
      else
      if (loglevel.startsWith ("info"))
      iLogLevelSetting = 4;
      else
      if (loglevel.startsWith ("debug"))
      iLogLevelSetting = 5;
    }
    else
    {
      File loggerFile = new File (  getPortalBaseDir () + File.separator +"properties" + File.separator+"Logger.properties" );
      if (loggerFile.exists ())
      {
        try
        {
          Properties props = System.getProperties ();
          props.load (new FileInputStream (loggerFile));
          System.setProperties (props);
          loadDefaultLogLevel ();
        }
        catch (Exception e)
        {
          e.printStackTrace ();
        }
      }
    }
  }
  public static void setLogLevel (int iLogLevel)
  {
    if (!bInitialized)
    initialize ();

    if (iLogLevel <= DEBUG)
    iLogLevelSetting = iLogLevel;
    else
    throw new IllegalArgumentException (sIllArgExMessage);
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
  public static void setMaxBackupLogFiles (int iMaxBackupLogFiles)
  {
    if (!bInitialized)
    initialize ();

    iMaxLogFiles = iMaxBackupLogFiles;
  }

  /**
   * Gets the amount of backup log files to create
   * @return the amount of backup log files to create
   */
  public static int getMaxBackupLogFiles ()
  {
    return iMaxLogFiles;
  }

  /**
   * Increments the old logs and creates a new log with the
   * current time and log level written at the top
   */
  public static void initialize ()
  {
    try
    {
      String sPortalBaseDir = getPortalBaseDir ();

      if (sPortalBaseDir != null && new File (sPortalBaseDir).exists ())
      {
        // Advance logs
        advanceLogs ();

        loadDefaultLogLevel ();

        // Print a header to the log containing the current time and log level
        String sDate = getLogTimeStamp ();
        PrintWriter out = new PrintWriter (new BufferedWriter (new FileWriter (sPortalBaseDir + sLogRelativePath, true)));
        out.println ("Portal log service initiating on " + sDate + "  Log level: " + getLogLevel (iLogLevelSetting));
        out.println ();
        out.close ();

        // Insures that initialization is only done once
        bInitialized = true;
      }
    }
    catch (Exception e)
    {
      System.err.println ("Problem writing to log.");
      e.printStackTrace ();
    }

  }

  private static InheritableThreadLocal sessionReference = new InheritableThreadLocal ();

  public static void setSession(HttpSession session)
  {
    sessionReference.set(session);
  }




  public static synchronized void log (int iLogLevel, HttpSession session, String sMessage)
  {
    //log ( iLogLevel,"Session ID:" +session.getId () + " reports: " + sMessage) ;
    sessionReference.set(session);
    log(iLogLevel,sMessage);
  }

  public static synchronized void log (int iLogLevel, HttpSession session, Throwable ex)
  {
    //log (iLogLevel, "Session " + session.getId () + "throws following exception");
    sessionReference.set(session);
    log (iLogLevel, ex);
  }

  /**
   * Writes a message to the log
   * @param the log level
   * @param the message to write
   */
  protected static HashMap mappings = new HashMap ();
  protected static Long mapmax = new Long(0);

  protected static synchronized void printSession (PrintWriter out, String sId) throws IOException
  {
    Long obj;
    Object val;
    if (( (val=mappings.get (sId))!= null) && val instanceof Long)
    {
      obj = (Long)val;
    }
    else
    {
      mapmax = new Long ( mapmax.longValue() + 1 );
      mappings.put(sId,mapmax);
      obj = mapmax;
    }
    out.print( "Session ID:");
     out.print( obj );
    out.print(" - ");
  }
  public static synchronized void log (int iLogLevel, String sMessage)
  {

    if (iLogLevel <= iLogLevelSetting)
    {
      if (!bInitialized)
      initialize ();
      String sDate = getLogTimeStamp ();
      String sPortalBaseDir = getPortalBaseDir ();

      try
      {

        if (sPortalBaseDir != null && new File (sPortalBaseDir).exists ())
        {
          PrintWriter out = new PrintWriter (new BufferedWriter (new FileWriter (sPortalBaseDir + sLogRelativePath, true)));
          out.print (getLogLevel (iLogLevel) + " - " + sDate + " - " );
          if ( sessionReference.get () != null )
          {
            printSession (out, ((HttpSession)(sessionReference.get () )).getId ());
          }
          else
          {
           out.print("No Session - ");
          }
          out.println (sMessage);
          out.close ();
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
  }

  /**
   * Writes an exception's stack trace to the log
   * @param the log level
   * @param an exception
   * @throws IllegalArgumentException if the log level is not one of the acceptable log levels
   */
  public static synchronized void log (int iLogLevel, Throwable ex)
  {

    if (iLogLevel <= iLogLevelSetting)
    {
      if (!bInitialized)
      initialize ();

      String sDate = getLogTimeStamp ();
      String sPortalBaseDir = getPortalBaseDir ();

      try
      {
        if (sPortalBaseDir != null && new File (sPortalBaseDir).exists ())
        {
          PrintWriter out = new PrintWriter (new BufferedWriter (new FileWriter (sPortalBaseDir + sLogRelativePath, true)));
          out.print (getLogLevel (iLogLevel) + " - " + sDate + " - " );
          if ( sessionReference.get () != null )
          {
            printSession (out, ((HttpSession)(sessionReference.get () )).getId ());
          }
          else
          {
           out.print("No Session - ");
          }
          out.println ( "Stack trace:");
          out.println ();
          ex.printStackTrace (out);
          out.println ();
          out.close ();
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
  }
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
    java.util.Date currentTime = new java.util.Date ();
    String sDate = formatter.format (currentTime);
    return sDate;
  }

  /**
   * This method advances the log files, throwing away the oldest
   */
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
    sLogBase = sLogFile.substring (0, sLogFile.indexOf (sLogExt));

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
}