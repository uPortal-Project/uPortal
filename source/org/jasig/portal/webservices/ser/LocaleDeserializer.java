/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
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
import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.encoding.DeserializationContext;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.DeserializerImpl;
import org.apache.axis.encoding.DeserializerTarget;
import org.apache.axis.message.SOAPHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * The LocaleSerializer deserializes a Locale.  
 * Much of the work is done in the base class. 
 * A BeanDeserializer couldn't be used for a Locale because
 * the Locale object doesn't have a no-argument constructor.                                              
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 * @deprecated As of uPortal 2.2, Remote Channel is replaced by WSRP
 */
public class LocaleDeserializer extends DeserializerImpl {

  // Storage for Locale fields.
  private Map localeInfo = new HashMap(3);
  
  // Hints...
  private static final Object LANGUAGE_HINT = new Object();
  private static final Object COUNTRY_HINT = new Object();
  private static final Object VARIANT_HINT = new Object();
  
  /** 
   * Checks that element is a locale element and then returns a LocaleHandler to 
   * handle the locale's children
   * @param namespace the namespace
   * @param localName the local name
   * @param prefix the prefix
   * @param attributes the attributes
   * @param context the deserialization context
   * @throws SAXException
   */    
  public SOAPHandler onStartChild(String namespace, String localName, String prefix, Attributes attributes, DeserializationContext context) throws SAXException {
    if (!localName.equals("locale")) {
      throw new SAXException("Expecting <locale> element. Got <" + localName + "> instead.");
    }
    return new LocaleHandler(this);
  }
  
  /** 
   * Callback from our deserializers.  The hint serves as a key to
   * the localeInfo map to locate a particular locale value.
   * @param value the value
   * @param hint the hint
   * @throws SAXException
   */    
  public void setValue(Object value, Object hint) throws SAXException {
    // Stuff the locale values into the localeInfo object
    if (hint != null) {
      localeInfo.put(hint, value);
    }
    
    // Construct the locale only after all the values are known
    // We have to wait until at least the locale name and value are known before
    // constructing the locale since the Locale object doesn't have a no-argument
    // constructor.
    if (localeInfo.size() == 3) {
      // Gather locale values stored in localeInfo
      String lLanguage = (String)localeInfo.get(LANGUAGE_HINT);
      String lCountry = (String)localeInfo.get(COUNTRY_HINT);
      String lVariant = (String)localeInfo.get(VARIANT_HINT);
      
      // Make the locale object
      Locale locale = null;
      if (lLanguage != null && lCountry != null && lVariant != null) {
        locale = new Locale(lLanguage, lCountry, lVariant);
      } else if (lLanguage != null && lCountry != null) {
        locale = new Locale(lLanguage, lCountry);
      } else if (lLanguage != null) {
        // This constructor was introduced in JDK 1.4.
        // Uncomment when we require JDK 1.4 for uPortal
        //locale = new Locale(lLanguage);
      }
      
      // Set the value of this deserializer to the locale      
      this.value = locale;
    }
  }
  
  /**
   * A deserializer for a locale element.  Handles getting the key and
   * value objects from their own deserializers, and then putting
   * the values into the localeInfo Map.
   */
  protected class LocaleHandler extends DeserializerImpl {
    LocaleDeserializer cd = null;

    /** 
     * LocaleHandler Constructor.
     * @param cd the locale deserializer
     */     
    public LocaleHandler(LocaleDeserializer cd) {
      this.cd = cd;
    }
    
    /** 
     * Callback from our deserializers.  The hint indicates
     * which locale field should be set.
     * @param val the value
     * @param hint the hint
     * @throws SAXException
     */
    public void setValue(Object val, Object hint) throws SAXException {
      cd.setValue(val, hint);
    }
    
    /** 
     * Called for each child element of the locale element.
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
      if (localName.equals("language")) {
        dt = new DeserializerTarget(this, LANGUAGE_HINT);
      } else if (localName.equals("country")) {
        dt = new DeserializerTarget(this, COUNTRY_HINT);
      } else if (localName.equals("variant")) {
        dt = new DeserializerTarget(this, VARIANT_HINT);  
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

