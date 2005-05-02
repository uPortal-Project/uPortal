/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

/**
 * Wrapper class to make com.oreilly.servlet.multipart.FilePart object
 * available as a DataSource.
 *
 * We have to buffer the data stream since the MimePart class will try to
 * read the stream several times (??) and we can't rewind the HttpRequest stream.
 * <p>
 * <b>Note</b>: The clients of this class must explictly call <code>dispose()</code> 
 * method to release temp files associated with this object.
 * </p>
 * 
 * @author George Lindholm, ITServices, UBC
 * @version $Revision$
*/
package org.jasig.portal;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import javax.activation.DataSource;
import com.oreilly.servlet.multipart.FilePart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MultipartDataSource implements DataSource {
    
    private static final Log log = LogFactory.getLog(MultipartDataSource.class);
    
  java.io.File tempfile;
  ByteArrayOutputStream buff = null;
  String contentType = null;
  String filename = null;
  String errorMessage = null;
  boolean isAvailable = false;

  public MultipartDataSource(FilePart filePart) throws IOException {
    contentType = filePart.getContentType();
    filename = filePart.getFileName();
    try{
        tempfile = java.io.File.createTempFile("uPdata",null);
        tempfile.deleteOnExit();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(tempfile));
        filePart.writeTo(out);
        out.close();
    }
    catch(IOException ioe){
        log.error("MultipartDataSource unable to create temp file", ioe);
        if(tempfile!=null){
            try{
                tempfile.delete();
            }
            catch(Exception e){}
            tempfile = null;
        }
        buff = new ByteArrayOutputStream();
        filePart.writeTo(buff);
    }
    this.isAvailable = true;
  }
  
  public MultipartDataSource (String fileName, String errorMessage){
    this.filename = fileName;
    this.errorMessage = errorMessage;
    this.isAvailable = false;
  }
  
  public boolean isAvailable () {
    return this.isAvailable;
  }

    /**
     * Releases tempfile associated with this object any memory they consume
     * will be returned to the OS.
     * @since uPortal 2.5.  Prior to uPortal 2.5, tempfile deletion was a side effect
     * of the finalizer.
     */
    public void dispose() {
        buff = null;
        if (tempfile != null) {
            boolean success = tempfile.delete();
            if (! success) {
                log.error("Unable to delete temp file [" + tempfile.getPath() + "]");
            }
            
            tempfile = null;
        }
    }

  public InputStream getInputStream() throws IOException {
    if (!isAvailable())
      throw new IOException (this.getErrorMessage());
      
    if(tempfile!=null){
        return new BufferedInputStream(new FileInputStream(tempfile));
    }
    else{
        return new ByteArrayInputStream(buff.toByteArray());
    }
  }

  public OutputStream getOutputStream() throws IOException {
    throw new IOException("getOutputStream() not implemented");
  }

  public String getContentType() {
    return contentType;
  }

  public String getName() {
    return filename;
  }
  
  public String getErrorMessage(){
    return errorMessage;
  }

  public void setFileTypeMap(javax.activation.FileTypeMap p0) throws Exception {
    throw new Exception("setFileTypeMap() not implemented");
  }
}
