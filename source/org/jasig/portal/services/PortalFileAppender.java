
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
package org.jasig.portal.services;

import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.helpers.QuietWriter;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;

/**
 PortalFileAppender extends FileAppender to backup the log files when 
 they reach a certain size. 
 
 @author Apache
 @author Bernie Durfee
 */

public class PortalFileAppender
  extends FileAppender
{
  
  
  /**
   A string constant used in naming the option for setting the
   maximum size of the log file. Current value of this string constant is
   <b>MaxFileSize</b>.
   */
  static final public String MAX_FILE_SIZE_OPTION = "MaxFileSize";
  
  
  /**
   A string constant used in naming the option for setting the the
   number of backup files to retain. Current value of this string
   constant is <b>MaxBackupIndex</b>.  */
  static final public String MAX_BACKUP_INDEX_OPTION = "MaxBackupIndex";
  
  
  /**
   The default maximum file size is 500k
   */
  protected long maxFileSize = 500000;
  
  
  /**
   There are ten backup files by default.
   */
  protected int maxBackupIndex = 10;
  
  /**
   The default constructor simply calls its {@link
   FileAppender#FileAppender parents constructor}.
   */
  
  public PortalFileAppender() 
  {
    super();
  }
  
  /**
   Instantiate a PortalFileAppender and set the output destination to a
   new {@link OutputStreamWriter} initialized with <code>os</code>
   as its {@link OutputStream}.  
   
   @deprecated This constructor does not allow to roll files and
   will disappear in the near future.
   */
  
  public PortalFileAppender(Layout layout, OutputStream os) 
  {
    super(layout, os);
  }
  
  /**
   Instantiate a PortalFileAppender and set the output destination
   to <code>writer</code>.
   
   <p>The <code>writer</code> must have been opened by the user.  
   
   @deprecated This constructor does not allow to roll files and will
   disappear in the near future.  */
  
  public PortalFileAppender(Layout layout, Writer writer) 
  {
    super(layout, writer);
  }
  
  /**
   Instantiate a PortalFileAppender and open the file designated by
   <code>filename</code>. The opened filename will become the ouput
   destination for this appender.
   
   <p>If the <code>append</code> parameter is true, the file will be
   appended to. Otherwise, the file desginated by
   <code>filename</code> will be truncated before being opened.
   */
  
  public PortalFileAppender(Layout layout, String filename, boolean append) throws IOException 
  {
    super(layout, filename, append);
  }
  
  /**
   Instantiate a FileAppender and open the file designated by
   <code>filename</code>. The opened filename will become the output
   destination for this appender.
   
   <p>The file will be appended to.  */
  
  public PortalFileAppender(Layout layout, String filename) throws IOException 
  {
    super(layout, filename);
  }
  
  /**
   Retuns the option names for this component, namely {@link
   #MAX_FILE_SIZE_OPTION} and {@link #MAX_BACKUP_INDEX_OPTION} in
   addition to the options of {@link FileAppender#getOptionStrings
   FileAppender}.  */
  
  public String[] getOptionStrings()
  {
    return OptionConverter.concatanateArrays(super.getOptionStrings(), new String[]
    {
      MAX_FILE_SIZE_OPTION, MAX_BACKUP_INDEX_OPTION
    });
  }
  
  public synchronized void setFile(String fileName, boolean append) throws IOException
  {
    super.setFile(fileName, append);
    if(append)
    {
      File f = new File(fileName);
      ((CountingQuietWriter)qw).setCount(f.length());
    }
  }
  
  /**
   Implemetns the usual roll over behaviour.
   
   <p>If <code>MaxBackupIndex</code> is positive, then files
   {<code>File.1</code>, ..., <code>File.MaxBackupIndex -1</code>}
   are renamed to {<code>File.2</code>, ..., 
   <code>File.MaxBackupIndex</code>}. Moreover, <code>File</code> is
   renamed <code>File.1</code> and closed. A new <code>File</code> is
   created to receive further log output.
   
   <p>If <code>MaxBackupIndex</code> is equal to zero, then the
   <code>File</code> is truncated with no backup files created.
   
   */
  
  public synchronized void rollOver()
  {
    File target;
    File file;
    java.text.DecimalFormat df = new java.text.DecimalFormat("00");
    String sLogBase = fileName.substring(0, fileName.indexOf(".log"));
    
    // If maxBackups <= 0, then there is no file renaming to be done.
    if(maxBackupIndex > 0)
    {
      
      // Delete the oldest file, to keep Windows happy.
      file = new File(sLogBase + "-" + df.format(maxBackupIndex) + ".log");
      if(file.exists())
      {
        file.delete();
      }
      
      // Map {(maxBackupIndex - 1), ..., 2, 1} to {maxBackupIndex, ..., 3, 2}
      for(int i = maxBackupIndex - 1;i >= 1;i--)
      {
        file = new File(sLogBase + "-" + df.format(i) + ".log");
        if(file.exists())
        {
          target = new File(sLogBase + "-" + df.format(i + 1) + ".log");
          file.renameTo(target);
        }
      }
      
      // Rename fileName to fileName.1
      target = new File(sLogBase + "-01.log");
      
      // Keep windows happy
      this.closeWriterIfOurs();
      file = new File(fileName);
      file.renameTo(target);
    }
    try
    {
      
      // This will also close the file. This is OK since multiple
      
      // close operations are safe.
      this.setFile(fileName, false);
    }
    catch(IOException e)
    {
      System.err.println("setFile(" + sLogBase + ", false) call failed.");
      e.printStackTrace();
    }
  }
  
  /**
   Set the maximum number of backup files to keep around.
   
   */
  
  public void setMaxBackupIndex(int maxBackups)
  {
    this.maxBackupIndex = maxBackups;
  }
  
  /**
   Set the maximum size that the output file is allowed to reach
   before being rolled over.     
   */
  
  public void setMaxFileSize(long maxFileSize)
  {
    this.maxFileSize = maxFileSize;
  }
  
  /**
   Set PortalFileAppender specific options.
   
   In addition to {@link FileAppender#setOption FileAppender
   options} PortalFileAppender recognizes the options
   <b>MaxFileSize</b> and <b>MaxBackupIndex</b>.
   
   
   <p>The <b>MaxFileSize</b> determines the size of log file
   before it is rolled over to backup files. This option takes an
   long integer in the range 0 - 2^63. You can specify the value
   with the suffixes "KB", "MB" or "GB" so that the integer is
   interpreted being expressed respectively in kilobytes, megabytes
   or gigabytes. For example, the value "10KB" will be interpreted
   as 10240.
   
   <p>The <b>MaxBackupIndex</b> option determines how many backup
   files are kept before the oldest being erased. This option takes
   a positive integer value. If set to zero, then there will be no
   backup files and the log file will be truncated when it reaches
   <code>MaxFileSize</code>.
   
   */
  
  public void setOption(String key, String value)
  {
    super.setOption(key, value);
    if(key.equalsIgnoreCase(MAX_FILE_SIZE_OPTION))
    {
      maxFileSize = OptionConverter.toFileSize(value, maxFileSize + 1);
    }
    else
    {
      if(key.equalsIgnoreCase(MAX_BACKUP_INDEX_OPTION))
      {
        maxBackupIndex = OptionConverter.toInt(value, maxBackupIndex);
      }
    }
  }
  
  protected void setQWForFiles(Writer writer)
  {
    this.qw = new CountingQuietWriter(writer, errorHandler);
  }
  
  /**
   This method differentiates PortalFileAppender from its super
   class.  
   
   @since 0.9.0
   */
  
  protected void subAppend(LoggingEvent event)
  {
    super.subAppend(event);
    if((fileName != null) && ((CountingQuietWriter)qw).getCount() >= maxFileSize)
    {
      this.rollOver();
    }
  }
}
