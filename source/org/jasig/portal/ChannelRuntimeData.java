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


package  org.jasig.portal;

import  javax.servlet.http.*;
import  java.util.Hashtable;
import  java.util.Map;
import  java.io.File;
import  java.util.Enumeration;
import  javax.servlet.ServletOutputStream;
import  java.io.IOException;

/**
 * A set of runtime data acessable by a channel.
 * Includes the following data
 * <ul>
 *  <li>Base channel action URL</li>
 *  <li>A hashtable of parameters passed to the current channel</li>
 *
 * @author Peter Kharchenko
 * @version $Revision$
 */
public class ChannelRuntimeData extends Hashtable implements Cloneable {
    private String baseActionURL;
    private static final String fs = File.separator;
    private BrowserInfo binfo;

  /**
   * default empty constructor
   */
  public ChannelRuntimeData () {
    super();
    // set the default values for the parameters here
    baseActionURL = null;
  }

  /**
   * Create a new instance of ourself
   * Used by the CError channel
   */
  public Object clone () {
    ChannelRuntimeData crd = new ChannelRuntimeData();
    crd.baseActionURL = baseActionURL;
    crd.binfo = binfo;
    crd.putAll(this);
    return  crd;
  }

    //set methods
  public void setBaseActionURL (String baURL) {
    baseActionURL = baURL;
  }

  public void setBrowserInfo (BrowserInfo bi) {
    this.binfo = bi;
  }

  public BrowserInfo getBrowserInfo () {
    return  binfo;
  }

  public void setParameters (Map params) {
    // copy a Map
    this.putAll(params);
  }

  public synchronized String[] setParameterValues (String pName, String[] values) {
    return  (String[])super.put(pName, values);
  }

  public synchronized void setParameter (String key, String value) {
    String[] valueArray = new String[1];
    valueArray[0] = value;
    super.put(key, valueArray);
  }

  public synchronized com.oreilly.servlet.multipart.Part[] setParameterValues (String pName, com.oreilly.servlet.multipart.Part[] values) {
    return  (com.oreilly.servlet.multipart.Part[])super.put(pName, values);
  }

  public synchronized void setParameter (String key, com.oreilly.servlet.multipart.Part value) {
    com.oreilly.servlet.multipart.Part[] valueArray = new com.oreilly.servlet.multipart.Part[1];
    valueArray[0] = value;
    super.put(key, valueArray);
  }

  // the get methods ...
  public String getBaseActionURL () {
    return  baseActionURL;
  }

  public synchronized String getParameter (String key) {
    String[] value_array = this.getParameterValues(key);
    if ((value_array != null) && (value_array.length > 0))
      return  value_array[0];
    else
      return  null;
  }

  public synchronized Object getObjectParameter (String key) {
    Object[] value_array = this.getParameterValues(key);
    if ((value_array != null) && (value_array.length > 0))
      return  value_array[0];
    else
      return  null;
  }

  public String[] getParameterValues (String parameter) {
    Object[] pars = (Object[])super.get(parameter);
    if (pars instanceof String[]) {
      return  (String[])pars;
    }
    else {
      return  null;
    }
  }

  public Object[] getObjectParameterValues (String parameter) {
    return  (Object[])super.get(parameter);
  }

  /**
   * Return the names of all the runtimeData parameters
   */
  public Enumeration getParameterNames () {
    return  (Enumeration)super.keys();
  }

}



