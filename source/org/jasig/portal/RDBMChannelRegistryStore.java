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

import  org.jasig.portal.utils.DTDResolver;
import  org.jasig.portal.services.LogService;
import  java.io.*;
import  org.apache.xml.serialize.*;
import  org.apache.xerces.dom.*;
import  org.w3c.dom.*;


/**
 * Reference implementation of IChannelRegistry.
 * Reads in an XML string
 * @author  John Laker, jlaker@udel.edu
 * @version $Revision$
 */
public class RDBMChannelRegistryStore implements IChannelRegistryStore {
  private DocumentImpl chanDoc = null;
  private Document types = null;
  String sRegDtd = "channelRegistry.dtd";

  /** Returns an XML document which describes the channel registry.
   * Right now this is being stored as a string in a field but could be also implemented to get from multiple tables.
   * @return a string of XML
   * @throws java.lang.Exception
   */
  public Document getChannelRegistryXML () throws Exception {
    return RdbmServices.getUserLayoutStoreImpl().getChannelRegistryXML();
  }

  /**
   * Returns an XML document which describes the channel types.
   * @return channelTypes, the list of publishable channel types
   * @throws java.lang.Exception
   */
  public Document getChannelTypesXML () throws Exception {
    return RdbmServices.getUserLayoutStoreImpl().getChannelTypesXML();
  }

  /** Returns a string of XML which describes the channel categories.
   * @param role role of the current user
   * @return Document
   */
  public Document getCategoryXML (String role) {
    Document catsDoc = null;
    try {
      catsDoc = new org.apache.xerces.dom.DocumentImpl();
      Element root = catsDoc.createElement("channelCats");
      GenericPortalBean.getUserLayoutStore().getCategoryXML(catsDoc, root, role);
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
    }
    return  catsDoc;
  }

  /** A method for adding a channel to the channel registry.
   * This would be called by a publish channel.
   * @param id the identifier for the channel
   * @param publisherId the identifier for the user who is publishing this channel
   * @param chanXML XML that describes the channel
   * @param catID an array of category IDs
   * @throws java.lang.Exception
   */
  public void addChannel (int id, int publisherId, Document chanXML, String catID[]) throws Exception {
    GenericPortalBean.getUserLayoutStore().addChannel(id, publisherId, chanXML, catID);
  }

  /** A method for adding a channel to the channel registry.
   * This would be called by a publish channel.
   * @param id the identifier for the channel
   * @param publisherId the identifier for the user who is publishing this channel
   * @param chanXML XML that describes the channel
   */
  public void addChannel (int id, int publisherId, Document chanXML) {
    try {
      GenericPortalBean.getUserLayoutStore().addChannel(id, publisherId, chanXML);
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
    }
  }

  /** A method for approving a channel so that users are allowed to subscribe to it.
   *  This would be called by the publish channel or the administrator channel
   *  @param chanId Channel to approve
   *  @param approvedId Account approving the channel
   *  @param approveDate When should the channel appear
   */
  public void approveChannel(int chanId, int approverId, java.sql.Timestamp approveDate) throws Exception {
    GenericPortalBean.getUserLayoutStore().approveChannel(chanId, approverId, approveDate);
  }

  /** A method for getting the next available channel ID.
   * This would be called by a publish channel.
   */
  public int getNextId () throws PortalException {
    int nextID;
    try {
      nextID = GenericPortalBean.getUserLayoutStore().getIncrementIntegerId("UP_CHANNEL");
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
      throw  new GeneralRenderingException("Unable to allocate new channel ID");
    }
    return  nextID;
  }

  /**
   * A method for removing a channel from the registry.
   * This could be used by an admin channel to unpublish a channel.
   * @param chanID a channel ID
   * @throws java.lang.Exception
   */
  public void removeChannel (String chanID) throws Exception {
    try {
      GenericPortalBean.getUserLayoutStore().removeChannel(chanID);
    } catch (Exception e) {
      LogService.instance().log(LogService.ERROR, e);
      throw  new GeneralRenderingException("Unable to remove channel " + chanID);
    }
  }

  /**
   * A method for persiting the channel registry to a file or database.
   * @param registryXML an XML description of the channel registry
   */
  public void setRegistryXML (String registryXML) {}

  /**
   * put your documentation comment here
   * @param chanDoc
   * @return
   */
  public String serializeDOM (Document chanDoc) {
    StringWriter stringOut = null;
    try {
      OutputFormat format = new OutputFormat(chanDoc);          //Serialize DOM
      stringOut = new StringWriter();           //Writer will be a String
      XMLSerializer serial = new XMLSerializer(stringOut, format);
      serial.asDOMSerializer();                 // As a DOM Serializer
      serial.serialize(chanDoc.getDocumentElement());
    } catch (java.io.IOException ioe) {
      LogService.instance().log(LogService.ERROR, ioe);
    }
    return  stringOut.toString();
    //LogService.instance().log(LogService.DEBUG, "STRXML = " + stringOut.toString());
  }
}



