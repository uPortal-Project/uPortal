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

package  org.jasig.portal.utils;

import javax.xml.parsers.DocumentBuilder;

import org.jasig.portal.PropertiesManager;
import org.jasig.portal.services.LogService;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.io.IOException;

/**
 * Produces an empty Document implementation
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @version $Revision$
 */
public class DocumentFactory {
    protected static DocumentFactory _instance;
    protected static final LocalDocumentBuilder localDocBuilder = new LocalDocumentBuilder();
    protected static javax.xml.parsers.DocumentBuilderFactory dbFactory = null;


  protected static synchronized DocumentFactory instance(){
      if (_instance==null){
          _instance = new DocumentFactory();
      }
      return _instance;
  }

  protected DocumentFactory() {

    try{
      dbFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
      dbFactory.setNamespaceAware(true);
      dbFactory.setValidating(false);
    }
    catch (Exception e){
      LogService.log(LogService.ERROR,"DocumentFactory: unable to initialize DocumentBuilderFactory");
      LogService.log(LogService.ERROR,e);
    }
  }
  
  /**
   * Returns a new copy of a Document implementation. This will
   * return an <code>IPortalDocument</code> implementation.
   * @return an empty org.w3c.dom.Document implementation
   */
  public static Document getNewDocument() {
    IPortalDocument doc = null;
    try {
        String className = PropertiesManager.getProperty(
          "org.jasig.portal.utils.IPortalDocument.implementation");
        doc = (IPortalDocument)Class.forName(className).newInstance();
    } catch (Exception e) {
      LogService.log(LogService.ERROR, e);
      throw new RuntimeException(
        "org.jasig.portal.utils.DocumentFactory could not create new " +
        "IPortalDocument: " + e.getMessage());
    }
    return doc;
  }

  /**
   * Returns a new copy of a Document implementation.
   * @return an empty org.w3c.dom.Document implementation
   */
  static Document __getNewDocument() {
    Document doc = newDocumentBuilder().newDocument();
    return doc;
  }

     public static Document getDocumentFromStream(InputStream stream) throws IOException, SAXException {
      try{
        DocumentBuilder builder = newDocumentBuilder();
        Document doc = builder.parse(stream);
        return doc;
      }
      finally{
          try {
              stream.close();
          }
          catch(IOException e) {
          }
      }
  }

    public static javax.xml.parsers.DocumentBuilder newDocumentBuilder(){
      DocumentBuilder builder = (DocumentBuilder) localDocBuilder.get();
      return builder;
    }

    protected static class LocalDocumentBuilder extends ThreadLocal{
      protected Object initialValue(){
          Object r = null;
          try{
            r = dbFactory.newDocumentBuilder();
          }
          catch(Exception e){
              LogService.log(LogService.ERROR,e);
          }
          return r;
      }
  }
}
