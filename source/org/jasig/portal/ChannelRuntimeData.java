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

import  javax.servlet.http.*;
import  java.util.Hashtable;
import  java.util.Map;
import  java.io.File;
import  java.util.Enumeration;
import  org.apache.xalan.xslt.XSLTInputSource;
import  javax.servlet.ServletOutputStream;
import  java.io.IOException;

/**
 * A set of runtime data acessable by a channel.
 * Includes the following data
 * <ul>
 *  <li>Base channel action URL</li>
 *  <li> HTTP request</li>
 *  <li>A hashtable of parameters passed to the current channel</li>
 *
 * @author Peter Kharchenko
 * @version $Revision$
 */
public class ChannelRuntimeData extends Hashtable
    implements Cloneable {
  private HttpServletRequest request;
  private HttpServletResponse response;
  private String baseActionURL;
  private static final String fs = File.separator;
  private BrowserInfo binfo;

  /**
   * put your documentation comment here
   */
  public ChannelRuntimeData () {
    super();
    // set the default values for the parameters here
    request = null;
    response = null;
    baseActionURL = null;
  }

  /**
   * Create a new instance of ourself
   * Used by the CError channel
   */
  public Object clone () {
    ChannelRuntimeData crd = new ChannelRuntimeData();
    crd.request = request;
    crd.response = response;
    crd.baseActionURL = baseActionURL;
    crd.binfo = binfo;
    crd.putAll(this);
    return  crd;
  }

  // the set methods ...
  public void setBaseActionURL (String baURL) {
    baseActionURL = baURL;
  }

  /**
   * put your documentation comment here
   * @param bi
   */
  public void setBrowserInfo (BrowserInfo bi) {
    this.binfo = bi;
  }

  /**
   * put your documentation comment here
   * @return
   */
  public BrowserInfo getBrowserInfo () {
    return  binfo;
  }

  /**
   * put your documentation comment here
   * @param req
   */
  public void setHttpRequest (HttpServletRequest req) {
    request = req;
  }

  /**
   * put your documentation comment here
   * @param res
   */
  public void setHttpResponse (HttpServletResponse res) {
    response = res;
  }

  /**
   * put your documentation comment here
   * @param params
   */
  public void setParameters (Map params) {
    // copy a Map
    this.putAll(params);
  }

  /**
   * put your documentation comment here
   * @param pName
   * @param values
   * @return
   */
  public synchronized String[] setParameterValues (String pName, String[] values) {
    return  (String[])super.put(pName, values);
  }

  /**
   * put your documentation comment here
   * @param key
   * @param value
   */
  public synchronized void setParameter (String key, String value) {
    String[] valueArray = new String[1];
    valueArray[0] = value;
    super.put(key, valueArray);
  }

  /**
   * put your documentation comment here
   * @param pName
   * @param values
   * @return
   */
  public synchronized com.oreilly.servlet.multipart.Part[] setParameterValues (String pName, com.oreilly.servlet.multipart.Part[] values) {
    return  (com.oreilly.servlet.multipart.Part[])super.put(pName, values);
  }

  /**
   * put your documentation comment here
   * @param key
   * @param value
   */
  public synchronized void setParameter (String key, com.oreilly.servlet.multipart.Part value) {
    com.oreilly.servlet.multipart.Part[] valueArray = new com.oreilly.servlet.multipart.Part[1];
    valueArray[0] = value;
    super.put(key, valueArray);
  }

  // the get methods ...
  public String getBaseActionURL () {
    return  baseActionURL;
  }

  /**
   * Return the HttpRequest object
   * @deprecated
   */
  public HttpServletRequest getHttpRequest () {
    return  request;
  }

  /**
   * Return the String corresponding to the key
   * @param object name
   * @return null if Object, String otherwise
   */
  public synchronized String getParameter (String key) {
    String[] value_array = this.getParameterValues(key);
    if ((value_array != null) && (value_array.length > 0))
      return  value_array[0];
    else
      return  null;
  }

  /**
   * Return the object corresponding to the key
   * @param object name
   * @return Object
   */
  public synchronized Object getObjectParameter (String key) {
    Object[] value_array = this.getParameterValues(key);
    if ((value_array != null) && (value_array.length > 0))
      return  value_array[0];
    else
      return  null;
  }

  /**
   * Return the String[] corresponding to the key
   * @param object name
   * @return null if Object[], otherwise String[]
   */
  public String[] getParameterValues (String parameter) {
    Object[] pars = (Object[])super.get(parameter);
    if (pars instanceof String[]) {
      return  (String[])pars;
    }
    else {
      return  null;
    }
  }

  /**
   * Return the Object[] corresponding to the key
   * @param object name
   * @return Object[]
   */
  public Object[] getObjectParameterValues (String parameter) {
    return  (Object[])super.get(parameter);
  }

  /**
   * Return the names of all the runtimeData parameters
   */
  public Enumeration getParameterNames () {
    return  (Enumeration)super.keys();
  }

  /**
   * Find a stylesheet for this connection
   * @parameter stylesheet title
   * @parameter Stylesheet object
   * @deprecated
   */
  public String getStylesheetURI (String title, StylesheetSet set) throws GeneralRenderingException {
    return  set.getStylesheetURI(title, request);
  }

  /**
   * Find a stylesheet for this connection
   * @parameter stylesheet title
   * @parameter Stylesheet object
   * @deprecated
   */
  public XSLTInputSource getStylesheet (String title, StylesheetSet set) {
    return  set.getStylesheet(title, request);
  }

  /**
   * Find a stylesheet for this connection
   * @parameter stylesheet title
   * @parameter Stylesheet object
   * @depricated
   */
  public XSLTInputSource getStylesheet (StylesheetSet set) throws GeneralRenderingException {
    return  set.getStylesheet(request);
  }

  /**
   * Return media type for this connection
   */
  public String getMedia () {
    MediaManager mm = new MediaManager();
    mm.setMediaProps(UtilitiesBean.getPortalBaseDir() + "properties" + fs + "media.properties");
    return  mm.getMedia(request);
  }

  // if you need to pass objects, use this
  public synchronized Object put (Object key, Object value) {
    return  super.put(key, value);
  }

  /**
   * put your documentation comment here
   * @param key
   * @return
   */
  public synchronized Object get (Object key) {
    return  super.get(key);
  }
}



