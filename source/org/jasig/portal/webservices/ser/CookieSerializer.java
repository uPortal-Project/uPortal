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

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.xml.namespace.QName;

import org.apache.axis.Constants;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.wsdl.fromJava.Types;
import org.xml.sax.Attributes;

/**
 * Serializer for Cookies.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CookieSerializer implements Serializer {

  /**
   * Serializes a Cookie.
   * @param name the name
   * @param attributes the attributes
   * @param value the value to serialize
   * @param context the serialization context
   */
  public void serialize(QName name, Attributes attributes, Object value, SerializationContext context) throws IOException {
     // Make sure this object is actually a javax.servlet.http.Cookie
    if (!(value instanceof Cookie)) {
      throw new IOException("Can't serialize a " + value.getClass().getName() + " with a CookieSerializer.");
    }
    Cookie cookie = (Cookie)value;
 
    context.startElement(name, attributes);
    context.startElement(new QName("", "cookie"), null);
    context.serialize(new QName("", "name"), null, cookie.getName());
    context.serialize(new QName("", "value"), null, cookie.getValue()); 
    context.serialize(new QName("", "comment"), null, cookie.getComment()); 
    context.serialize(new QName("", "domain"), null, cookie.getDomain()); 
    context.serialize(new QName("", "maxAge"), null, new Integer(cookie.getMaxAge())); 
    context.serialize(new QName("", "path"), null, cookie.getPath()); 
    context.serialize(new QName("", "secure"), null, new Boolean(cookie.getSecure())); 
    context.serialize(new QName("", "version"), null, new Integer(cookie.getVersion()));     
    context.endElement();
    context.endElement();
  }
  
  /**
   * Returns the mechanism type.
   * @return mechanismType the mechanism type
   */  
  public String getMechanismType() { 
    return Constants.AXIS_SAX; 
  }
  
  /**
   * Return XML schema for the specified type, suitable for insertion into
   * the types element of a WSDL document.
   * @param types the Java2WSDL Types object which holds the context for the WSDL being generated.
   * @return <code>true</code> if we wrote a schema, <code>false</code> if we didn't.
   * @see org.apache.axis.wsdl.fromJava.Types
   */
  public boolean writeSchema(Types types) throws Exception {
    return false;
  }
}

