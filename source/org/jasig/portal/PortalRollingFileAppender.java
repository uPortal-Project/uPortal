/**
 * Copyright (c) 2001 The JA-SIG Collaborative.  All rights reserved.
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

import  java.beans.*;
import  java.io.IOException;
import  java.io.Writer;
import  java.io.FileWriter;
import  java.io.File;
import  java.io.OutputStream;
import  java.io.OutputStreamWriter;
import  org.apache.log4j.FileAppender;
import  org.apache.log4j.Layout;
import  org.apache.log4j.helpers.OptionConverter;
import  org.apache.log4j.helpers.QuietWriter;
import  org.apache.log4j.helpers.LogLog;
import  org.apache.log4j.helpers.CountingQuietWriter;
import  org.apache.log4j.spi.LoggingEvent;


/**
 * PortalRollingFileAppender extends FileAppender to backup the log files when 
 * they reach a certain size. 
 * @author Heinz Richter
 * @author Ceki G&uuml;lc&uuml;
 * @author Bernie Durfee 
 */
public class PortalRollingFileAppender extends FileAppender {
  /**
   * The default maximum file size is 10MB. 
   */
  protected long maxFileSize = 10*1024*1024;
  /**
   * There is one backup file by default.
   */
  protected int maxBackupIndex = 1;
  /**
   * Keep track of whether or not the logs have rolled
   */
  protected static boolean m_hasRolledOver = false;

  /**
   * The default constructor simply calls its {@link
   * FileAppender#FileAppender parents constructor}.
   */
  public PortalRollingFileAppender () {
    super();
  }

  /**
   * Instantiate a RollingFileAppender and open the file designated by
   * <code>filename</code>. The opened filename will become the ouput
   * destination for this appender.
   * 
   * <p>If the <code>append</code> parameter is true, the file will be
   * appended to. Otherwise, the file desginated by
   * <code>filename</code> will be truncated before being opened.
   */
  public PortalRollingFileAppender (Layout layout, String filename, boolean append) throws IOException
  {
    super(layout, filename, append);
  }

  /**
   * Instantiate a FileAppender and open the file designated by
   * <code>filename</code>. The opened filename will become the output
   * destination for this appender.
   * <p>The file will be appended to.
   */
  public PortalRollingFileAppender (Layout layout, String filename) throws IOException
  {
    super(layout, filename);
  }

  /**
   * Returns the value of the <b>MaxBackupIndex</b> option.
   */
  public int getMaxBackupIndex () {
    return  maxBackupIndex;
  }

  /**
   * Get the maximum size that the output file is allowed to reach
   * before being rolled over to backup files.
   * @since 1.1
   */
  public long getMaximumFileSize () {
    return  maxFileSize;
  }

  /**
   * Implements the usual roll over behaviour.
   * <p>If <code>MaxBackupIndex</code> is positive, then files
   * {<code>File.1</code>, ..., <code>File.MaxBackupIndex -1</code>}
   * are renamed to {<code>File.2</code>, ..., 
   * <code>File.MaxBackupIndex</code>}. Moreover, <code>File</code> is
   * renamed <code>File.1</code> and closed. A new <code>File</code> is
   * created to receive further log output.
   * <p>If <code>MaxBackupIndex</code> is equal to zero, then the
   * <code>File</code> is truncated with no backup files created.
   */
  public void rollOver () {
    // Open the current file
    File currentFile = new File(fileName);
    // Get just the name of the logfile
    String logFileName = currentFile.getName();
    // Get just the path to the log file
    String logFilePath = currentFile.getParentFile().getAbsolutePath() + File.separator;
    // Make sure the file is closed now
    closeFile();
    File target;
    File file;
    java.text.DecimalFormat df = new java.text.DecimalFormat();
    df.setMinimumIntegerDigits(2);
    LogLog.debug("rolling over count=" + ((CountingQuietWriter)qw).getCount());
    LogLog.debug("maxBackupIndex=" + maxBackupIndex);
    // If maxBackups <= 0, then there is no file renaming to be done.
    if (maxBackupIndex > 0) {
      // Delete the oldest file, to keep Windows happy.
      file = new File(logFilePath + df.format(maxBackupIndex) + '-' + logFileName);
      if (file.exists()) {
        file.delete();
      }
      // Map {(maxBackupIndex - 1), ..., 2, 1} to {maxBackupIndex, ..., 3, 2}
      for (int i = maxBackupIndex - 1; i >= 1; i--) {
        file = new File(logFilePath + df.format(i) + '-' + logFileName);
        if (file.exists()) {
          target = new File(logFilePath + df.format(i + 1) + '-' + logFileName);
          LogLog.debug("Renaming file " + file + " to " + target);
          file.renameTo(target);
        }
      }
      // Rename fileName to fileName.1
      target = new File(logFilePath + df.format(1) + '-' + logFileName);
      this.closeFile();         // keep windows happy. 
      file = new File(fileName);
      LogLog.debug("Renaming file " + file + " to " + target);
      file.renameTo(target);
    }
    try {
      // This will also close the file. This is OK since multiple
      // close operations are safe.
      this.setFile(fileName, false);
    } catch (IOException e) {
      LogLog.error("setFile(" + fileName + ", false) call failed.", e);
    }
  }

  /**
   * put your documentation comment here
   * @param fileName
   * @param append
   * @exception IOException
   */
  public synchronized void setFile (String fileName, boolean append) throws IOException {
    super.setFile(fileName, append);
    if (append) {
      File f = new File(fileName);
      ((CountingQuietWriter)qw).setCount(f.length());
    }
  }

  /**
   * Set the maximum number of backup files to keep around.
   * 
   * <p>The <b>MaxBackupIndex</b> option determines how many backup
   * files are kept before the oldest is erased. This option takes
   * a positive integer value. If set to zero, then there will be no
   * backup files and the log file will be truncated when it reaches
   * <code>MaxFileSize</code>.
   */
  public void setMaxBackupIndex (int maxBackups) {
    this.maxBackupIndex = maxBackups;
  }

  /**
   * Set the maximum size that the output file is allowed to reach
   * before being rolled over to backup files.
   * <p>This method is equivalent to {@link #setMaxFileSize} except
   * that it is required for differentiating the setter taking a
   * <code>long</code> argument from the setter taking a
   * <code>String</code> argument by the JavaBeans {@link
   * java.beans.Introspector Introspector}.
   * @see #setMaxFileSize(String)
   */
  public void setMaximumFileSize (long maxFileSize) {
    this.maxFileSize = maxFileSize;
  }

  /**
   * Set the maximum size that the output file is allowed to reach
   * before being rolled over to backup files.
   *
   * <p>In configuration files, the <b>MaxFileSize</b> option takes an
   * long integer in the range 0 - 2^63. You can specify the value
   * with the suffixes "KB", "MB" or "GB" so that the integer is
   * interpreted being expressed respectively in kilobytes, megabytes
   * or gigabytes. For example, the value "10KB" will be interpreted
   * as 10240.
   */
  public void setMaxFileSize (String value) {
    maxFileSize = OptionConverter.toFileSize(value, maxFileSize + 1);
  }

  /**
   * put your documentation comment here
   * @param writer
   */
  protected void setQWForFiles (Writer writer) {
    this.qw = new CountingQuietWriter(writer, errorHandler);
  }

  /**
   * This method differentiates RollingFileAppender from its super
   * class.  
   */
  protected void subAppend (LoggingEvent event) {
    // Make sure the logs have rolled at least once
    if (!m_hasRolledOver) {
      rollOver();
      m_hasRolledOver = true;
    }
    super.subAppend(event);
    if ((fileName != null) && ((CountingQuietWriter)qw).getCount() >= maxFileSize) {
      this.rollOver();
    }
  }
}



