/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.webservices.ser;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.xml.namespace.QName;

import org.apache.axis.encoding.DeserializationContext;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.DeserializerImpl;
import org.apache.axis.encoding.DeserializerTarget;
import org.apache.axis.message.SOAPHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * The CookieSerializer deserializes a Cookie.  
 * Much of the work is done in the base class. 
 * A BeanDeserializer couldn't be used for a Cookie because
 * the Cookie object doesn't have a no-argument constructor.                                              
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CookieDeserializer extends DeserializerImpl {

  // Storage for cookie fields.
  private Map cookieInfo = new HashMap(8);
  
  // Hints...
  private static final Object NAME_HINT = new Object();
  private static final Object VALUE_HINT = new Object();
  private static final Object COMMENT_HINT = new Object();
  private static final Object DOMAIN_HINT = new Object();
  private static final Object MAXAGE_HINT = new Object();
  private static final Object PATH_HINT = new Object();
  private static final Object SECURE_HINT = new Object();
  private static final Object VERSION_HINT = new Object();
  
  /** 
   * Checks that element is a cookie element and then returns a CookieHandler to 
   * handle the cookie's children
   * @param namespace the namespace
   * @param localName the local name
   * @param prefix the prefix
   * @param attributes the attributes
   * @param context the deserialization context
   * @throws SAXException
   */    
  public SOAPHandler onStartChild(String namespace, String localName, String prefix, Attributes attributes, DeserializationContext context) throws SAXException {
    if (!localName.equals("cookie")) {
      throw new SAXException("Expecting <cookie> element. Got <" + localName + "> instead.");
    }
    return new CookieHandler(this);
  }
  
  /** 
   * Callback from our deserializers.  The hint serves as a key to
   * the cookieInfo map to locate a particular cookie value.
   * @param val the value
   * @param hint the hint
   * @throws SAXException
   */    
  public void setValue(Object value, Object hint) throws SAXException {
    // Stuff the cookie values into the cookieInfo object
    if (hint != null) {
      cookieInfo.put(hint, value);
    }
    
    // Construct the cookie only after all the values are known
    // We have to wait until at least the cookie name and value are known before
    // constructing the cookie since the Cookie object doesn't have a no-argument
    // constructor.
    if (cookieInfo.size() == 8) {
      // Gather cookie values stored in cookieInfo
      String  cName    = (String)cookieInfo.get(NAME_HINT);
      String  cValue   = (String)cookieInfo.get(VALUE_HINT);
      String  cComment = (String)cookieInfo.get(COMMENT_HINT);
      String  cDomain  = (String)cookieInfo.get(DOMAIN_HINT);
      int     cMaxAge  = ((Integer)cookieInfo.get(MAXAGE_HINT)).intValue();
      String  cPath    = (String)cookieInfo.get(PATH_HINT);
      boolean cSecure  = ((Boolean)cookieInfo.get(SECURE_HINT)).booleanValue();
      int     cVersion = ((Integer)cookieInfo.get(VERSION_HINT)).intValue();
      
      // Make the cookie object and set values avoiding null Strings
      Cookie cookie = new Cookie(cName != null ? cName : "", cValue != null ? cValue : "");      
      if (cComment != null)
        cookie.setComment(cComment);
      if (cDomain != null)
        cookie.setDomain(cDomain);
      cookie.setMaxAge(cMaxAge);
      if (cPath != null)
        cookie.setPath(cPath);
      cookie.setSecure(cSecure);
      cookie.setVersion(cVersion);
      
      // Set the value of this deserializer to the cookie
      this.value = cookie;
    }
  }
  
  /**
   * A deserializer for a cookie element.  Handles getting the key and
   * value objects from their own deserializers, and then putting
   * the values into the cookieInfo Map.
   */
  protected class CookieHandler extends DeserializerImpl {
    CookieDeserializer cd = null;

    /** 
     * CookieHandler Constructor.
     * @param cd the cookie deserializer
     */     
    public CookieHandler(CookieDeserializer cd) {
      this.cd = cd;
    }
    
    /** 
     * Callback from our deserializers.  The hint indicates
     * which cookie field should be set.
     * @param val the value
     * @param hint the hint
     * @throws SAXException
     */
    public void setValue(Object val, Object hint) throws SAXException {
      cd.setValue(val, hint);
    }
    
    /** 
     * Called for each child element of the cookie element.
     * @param namespace the namespace
     * @param localName the local name
     * @param prefix the prefix
     * @param attributes the attributes
     * @param context the deserialization context
     * @throws SAXException
     */    
    public SOAPHandler onStartChild(String namespace, String localName, String prefix, Attributes attributes, DeserializationContext context) throws SAXException {
      // Get a deserializer for element's type
      QName typeQName = context.getTypeFromAttributes(namespace, localName, attributes);
      Deserializer dser = context.getDeserializerForType(typeQName);

      // If no deserializer, use the base DeserializerImpl.
      if (dser == null)
        dser = new DeserializerImpl();

      // When the child value is ready, we want our set method to be invoked.
      // To do this register a DeserializeTarget on the new Deserializer.
      DeserializerTarget dt = null;
      if (localName.equals("name")) {
        dt = new DeserializerTarget(this, NAME_HINT);
      } else if (localName.equals("value")) {
        dt = new DeserializerTarget(this, VALUE_HINT);
      } else if (localName.equals("comment")) {
        dt = new DeserializerTarget(this, COMMENT_HINT);  
      } else if (localName.equals("domain")) {
        dt = new DeserializerTarget(this, DOMAIN_HINT);   
      } else if (localName.equals("maxAge")) {
        dt = new DeserializerTarget(this, MAXAGE_HINT);
      } else if (localName.equals("path")) {
        dt = new DeserializerTarget(this, PATH_HINT);       
      } else if (localName.equals("secure")) {
        dt = new DeserializerTarget(this, SECURE_HINT);          
      } else if (localName.equals("version")) {
        dt = new DeserializerTarget(this, VERSION_HINT);                   
      } else {
        // Do nothing
      }

      if (dt != null) {
        dser.registerValueTarget(dt);
      }
      
      return (SOAPHandler)dser;
    }
  }
}

