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

import javax.xml.namespace.QName;

import org.apache.axis.encoding.DeserializationContext;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.DeserializerImpl;
import org.apache.axis.encoding.DeserializerTarget;
import org.apache.axis.message.SOAPHandler;
import org.jasig.portal.BrowserInfo;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.UPFileSpec;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * The ChannelRuntimeDataDeserializer deserializes a ChannelRuntimeData object.  
 * Much of the work is done in the base class. 
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class ChannelRuntimeDataDeserializer extends DeserializerImpl {

  // Storage for ChannelRuntimeData fields.
  private Map channelRuntimeDataInfo = new HashMap(7);
  
  // Hints...
  private static final Object REQUEST_PARAMS_HINT = new Object();
  private static final Object KEYWORDS_HINT = new Object();
  private static final Object RENDERING_AS_ROOT_HINT = new Object();
  private static final Object BROWSER_INFO_HINT = new Object();
  private static final Object UP_FILE_SPEC_HINT = new Object();
  private static final Object BASE_ACTION_URL_HINT = new Object();
  private static final Object HTTP_REQUEST_METHOD_HINT = new Object();
  
  /** 
   * Checks that element is a channel-runtime-data element 
   * and then returns a ChannelRuntimeDataHandler to handle the 
   * channel-runtime-data's children
   * @param namespace the namespace
   * @param localName the local name
   * @param prefix the prefix
   * @param attributes the attributes
   * @param context the deserialization context
   * @throws SAXException
   */    
  public SOAPHandler onStartChild(String namespace, String localName, String prefix, Attributes attributes, DeserializationContext context) throws SAXException {
    if (!localName.equals(ChannelRuntimeDataSerializer.CHANNEL_RUNTIME_DATA_ELEMENT_NAME)) {
      throw new SAXException("Expecting <channel-runtime-data> element. Got <" + localName + "> instead.");
    }
    return new ChannelRuntimeDataHandler(this);
  }
  
  /** 
   * Callback from our deserializers.  The hint serves as a key to the 
   * channelRuntimeDataInfo map to locate a particular ChannelRuntimeData value.
   * @param val the value
   * @param hint the hint
   * @throws SAXException
   */    
  public void setValue(Object value, Object hint) throws SAXException {
    // Stuff the values into the channelRuntimeDataInfo object
    if (hint != null) {
      channelRuntimeDataInfo.put(hint, value);
    }
    
    // Construct the ChannelRuntimeData object only after all the values are known.
    if (channelRuntimeDataInfo.size() == 7) {
      // Gather ChannelRuntimeData values stored in channelRuntimeDataInfo
      Map params = (Map)channelRuntimeDataInfo.get(REQUEST_PARAMS_HINT);
      String keywords = (String)channelRuntimeDataInfo.get(KEYWORDS_HINT);
      boolean isRenderingAsRoot = ((Boolean)channelRuntimeDataInfo.get(RENDERING_AS_ROOT_HINT)).booleanValue();
      BrowserInfo browserInfo = (BrowserInfo)channelRuntimeDataInfo.get(BROWSER_INFO_HINT);
      UPFileSpec upfs = (UPFileSpec)channelRuntimeDataInfo.get(UP_FILE_SPEC_HINT);
      String baseActionURL = (String)channelRuntimeDataInfo.get(BASE_ACTION_URL_HINT);
      String httpRequestMethod = (String)channelRuntimeDataInfo.get(HTTP_REQUEST_METHOD_HINT);
      
      // Make the ChannelRuntimeData object and set values
      ChannelRuntimeData runtimeData = new ChannelRuntimeData();      
      runtimeData.setParametersSingleValued(params);
      runtimeData.setKeywords(keywords);
      runtimeData.setRenderingAsRoot(isRenderingAsRoot);
      runtimeData.setBrowserInfo(browserInfo);
      runtimeData.setUPFile(upfs);
      runtimeData.setBaseActionURL(baseActionURL);
      runtimeData.setHttpRequestMethod(httpRequestMethod);
      
      // Set the value of this deserializer to the ChannelRuntimeData
      this.value = runtimeData;
    }
  }
  
  /**
   * A deserializer for a ChannelRuntimeData element.  Handles getting the key and
   * value objects from their own deserializers, and then putting
   * the values into the channelRuntimeDataInfo Map.
   */
  protected class ChannelRuntimeDataHandler extends DeserializerImpl {
    ChannelRuntimeDataDeserializer crdd = null;

    /** 
     * ChannelRuntimeDataHandler Constructor.
     * @param crdd the ChannelRuntimeData deserializer
     */     
    public ChannelRuntimeDataHandler(ChannelRuntimeDataDeserializer crdd) {
      this.crdd = crdd;
    }
    
    /** 
     * Callback from our deserializers.  The hint indicates
     * which ChannelRuntimeData field should be set.
     * @param val the value
     * @param hint the hint
     * @throws SAXException
     */
    public void setValue(Object val, Object hint) throws SAXException {
      crdd.setValue(val, hint);
    }
    
    /** 
     * Called for each child element of the channel-runtime-data element.
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
      if (localName.equals(ChannelRuntimeDataSerializer.REQUEST_PARAMS_ELEMENT_NAME)) {
        dt = new DeserializerTarget(this, REQUEST_PARAMS_HINT);
      } else if (localName.equals(ChannelRuntimeDataSerializer.KEYWORDS_ELEMENT_NAME)) {
        dt = new DeserializerTarget(this, KEYWORDS_HINT);
      } else if (localName.equals(ChannelRuntimeDataSerializer.RENDERING_AS_ROOT_ELEMENT_NAME)) {
        dt = new DeserializerTarget(this, RENDERING_AS_ROOT_HINT);
      } else if (localName.equals(ChannelRuntimeDataSerializer.BROWSER_INFO_ELEMENT_NAME)) {
        dt = new DeserializerTarget(this, BROWSER_INFO_HINT);  
      } else if (localName.equals(ChannelRuntimeDataSerializer.UP_FILE_SPEC_ELEMENT_NAME)) {
        dt = new DeserializerTarget(this, UP_FILE_SPEC_HINT);   
      } else if (localName.equals(ChannelRuntimeDataSerializer.BASE_ACTION_URL_ELEMENT_NAME)) {
        dt = new DeserializerTarget(this, BASE_ACTION_URL_HINT);
      } else if (localName.equals(ChannelRuntimeDataSerializer.HTTP_REQUEST_METHOD_ELEMENT_NAME)) {
        dt = new DeserializerTarget(this, HTTP_REQUEST_METHOD_HINT);       
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

