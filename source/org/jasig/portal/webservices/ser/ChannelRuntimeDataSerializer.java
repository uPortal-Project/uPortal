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

import javax.xml.namespace.QName;

import org.apache.axis.Constants;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.wsdl.fromJava.Types;
import org.jasig.portal.ChannelRuntimeData;
import org.xml.sax.Attributes;

/**
 * Serializer for ChannelRuntimeData.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class ChannelRuntimeDataSerializer implements Serializer {

  public static final String CHANNEL_RUNTIME_DATA_ELEMENT_NAME = "channel-runtime-data";
  public static final String REQUEST_PARAMS_ELEMENT_NAME = "request-parameters";
  public static final String KEYWORDS_ELEMENT_NAME = "keywords";
  public static final String RENDERING_AS_ROOT_ELEMENT_NAME = "rendering-as-root";
  public static final String BROWSER_INFO_ELEMENT_NAME = "browser-info";
  public static final String UP_FILE_SPEC_ELEMENT_NAME = "up-file-spec";
  public static final String BASE_ACTION_URL_ELEMENT_NAME = "base-action-url";
  public static final String HTTP_REQUEST_METHOD_ELEMENT_NAME = "http-request-method";
  
  /**
   * Serializes a ChannelRuntimeData object.
   * @param name the name
   * @param attributes the attributes
   * @param value the value to serialize
   * @param context the serialization context
   */
  public void serialize(QName name, Attributes attributes, Object value, SerializationContext context) throws IOException {
     // Make sure this object is actually an org.jasig.portal.ChannelRuntimeData
    if (!(value instanceof ChannelRuntimeData)) {
      throw new IOException("Can't serialize a " + value.getClass().getName() + " with a ChannelRuntimeDataSerializer.");
    }
    ChannelRuntimeData runtimeData = (ChannelRuntimeData)value;
 
    context.startElement(name, attributes);
    context.startElement(new QName("", CHANNEL_RUNTIME_DATA_ELEMENT_NAME), null);
    context.serialize(new QName("", REQUEST_PARAMS_ELEMENT_NAME), null, runtimeData.getParameters()); // a Map
    context.serialize(new QName("", KEYWORDS_ELEMENT_NAME), null, runtimeData.getKeywords()); // a String
    context.serialize(new QName("", RENDERING_AS_ROOT_ELEMENT_NAME), null, new Boolean(runtimeData.isRenderingAsRoot())); // a Boolean 
    context.serialize(new QName("", BROWSER_INFO_ELEMENT_NAME), null, runtimeData.getBrowserInfo()); // a BrowserInfo
    context.serialize(new QName("", UP_FILE_SPEC_ELEMENT_NAME), null, runtimeData.getUPFile()); // a UPFileSpec
    context.serialize(new QName("", BASE_ACTION_URL_ELEMENT_NAME), null, runtimeData.getBaseActionURL()); // a String
    context.serialize(new QName("", HTTP_REQUEST_METHOD_ELEMENT_NAME), null, runtimeData.getHttpRequestMethod());  // a String
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

