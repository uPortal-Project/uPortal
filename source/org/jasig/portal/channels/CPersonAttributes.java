/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IMultithreadedMimeResponse;
import org.jasig.portal.PortalException;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.PersonDirectory;
import org.jasig.portal.services.persondir.IPersonAttributeDao;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;

/**
 * This channel demonstrates the method of obtaining and displaying
 * standard uPortal person attributes.
 *
 * Implements MultithreadedIMimeResponse in order to support the inline display of jpegPhotos
 * Note:  for proper operation, one should use an idempotent baseActionURL.
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @author Yuji Shinozaki, ys2n@virginia.edu
 * @version $Revision$ $Date$
 */
public class CPersonAttributes extends BaseMultithreadedChannel implements IMultithreadedMimeResponse {

  private static final String sslLocation = "CPersonAttributes/CPersonAttributes.ssl";

  public void renderXML (ContentHandler out, String uid) throws PortalException {
    ChannelState channelState = (ChannelState)channelStateMap.get(uid);
    ChannelStaticData staticData = channelState.getStaticData();
    ChannelRuntimeData runtimeData = channelState.getRuntimeData();
    IPerson person = staticData.getPerson();
    Document doc = DocumentFactory.getNewDocument();

    Element attributesE = doc.createElement("attributes");
    
    IPersonAttributeDao pa = PersonDirectory.getPersonAttributeDao();
    Set possibleAttrs = pa.getPossibleUserAttributeNames();
    
    if (possibleAttrs != null)
        possibleAttrs = new HashSet(possibleAttrs);
    else
        possibleAttrs = new HashSet();
    
    for (Enumeration attribs = person.getAttributeNames(); attribs.hasMoreElements(); ) {
      // Get the attribute name
      String attName = (String) attribs.nextElement();
      
      // Remove this attr from the list of possible attrs
      possibleAttrs.remove(attName);
      
      // Set the attribute
      Element attributeE = doc.createElement("attribute");

      Element nameE = doc.createElement("name");
      nameE.appendChild(doc.createTextNode(attName));
      attributeE.appendChild(nameE);

      // Get the IPerson attribute value for this eduPerson attribute name
      if (person.getAttributeValues(attName) != null) {
        Object[] values = person.getAttributeValues(attName);
        for (int i = 0; i < values.length; i++) {
           if (log.isTraceEnabled())
               log.trace("type of value["+i+"] is " + values[i].getClass().getName());
           String value = values[i].toString();
           Element valueE = doc.createElement("value");
           valueE.appendChild(doc.createTextNode(value));
           attributeE.appendChild(valueE);
        }
      }

      attributesE.appendChild(attributeE);
    }
    
    //Sort the set of possible attributes
    possibleAttrs = new TreeSet(possibleAttrs);
    
    //Add the unknown attributes to the element list.
    for (Iterator attribs = possibleAttrs.iterator(); attribs.hasNext(); ) {
        // Get the attribute name
        String attName = (String) attribs.next();
        
        // Set the attribute
        Element attributeE = doc.createElement("attribute");

        Element nameE = doc.createElement("name");
        nameE.appendChild(doc.createTextNode(attName));
        attributeE.appendChild(nameE);

        attributesE.appendChild(attributeE);
    }

    doc.appendChild(attributesE);

    XSLT xslt = XSLT.getTransformer(this, runtimeData.getLocales());
    xslt.setXML(doc);
    xslt.setStylesheetParameter("baseActionURL",runtimeData.getBaseActionURL());
    xslt.setStylesheetParameter("downloadWorkerURL",
                                 runtimeData.getBaseWorkerURL(UPFileSpec.FILE_DOWNLOAD_WORKER,true));
    xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.transform();
  }


  // IMimeResponse implementation -- ys2n@virginia.edu
    /**
     * Returns the MIME type of the content.
     */
    public java.lang.String getContentType (String uid) {
        // In the future we will need some sort of way of grokking the
        // mime-type of the byte-array and returning an appropriate mime-type
        // Right now there is no good way of doing that, and we will
        // assume that the only thing we will be delivering is a jpegPhoto.
        // attribute with a mimetype of image/jpeg
        // In the future however, we may need a way to deliver different
        // attributes as differenct mimetypes (e.g certs).
        //
        String mimetype;
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelRuntimeData runtimeData = channelState.getRuntimeData();
        // runtime parameter "attribute" determines which attribute to return when
        // called as an IMimeResponse.
        String attrName = runtimeData.getParameter("attribute");

        if ("jpegPhoto".equals(attrName)) {
            mimetype="image/jpeg";
        }
        else {
            // default -- an appropriate choice?
            mimetype="application/octet-stream";
        }
        return mimetype;
    }

    /**
     * Returns the MIME content in the form of an input stream.
     * Returns null if the code needs the OutputStream object
     */
    public java.io.InputStream getInputStream (String uid) throws IOException {
        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelStaticData staticData = channelState.getStaticData();
        ChannelRuntimeData runtimeData = channelState.getRuntimeData();

        String attrName = runtimeData.getParameter("attribute");
        IPerson person = staticData.getPerson();

        if ( attrName == null ) {
            attrName = "";
        }

        // get the image out of the IPerson as a byte array.
        // Note:  I am assuming here that the only thing that this
        // IMimeResponse will return is a jpegPhoto.  Some other
        // generalized mechanism will need to be inserted here to
        // support other mimetypes and IPerson attributes.
        byte[] imgBytes = (byte [])person.getAttribute(attrName);

        // need to create a ByteArrayInputStream()

        if ( imgBytes == null ) {
            imgBytes = new byte[0]; // let's avoid a null pointer
        }
        java.io.InputStream is = (java.io.InputStream) new java.io.ByteArrayInputStream(imgBytes);

        return is;
    }

    /**
     * Pass the OutputStream object to the download code if it needs special handling
     * (like outputting a Zip file).  Unimplemented.
     */
    public void downloadData (OutputStream out, String uid) throws IOException {
    }

    /**
     * Returns the name of the MIME file.
     */
    public java.lang.String getName (String uid) {
        // As noted above the only attribute we support right now is "image/jpeg" for
        // the jpegPhoto attribute.

        ChannelState channelState = (ChannelState)channelStateMap.get(uid);
        ChannelRuntimeData runtimeData = channelState.getRuntimeData();
        String payloadName;
        if ("jpegPhoto".equals(runtimeData.getParameter("attribute")))
            payloadName = "image.jpg";
        else
            payloadName = "unknown";
        return payloadName;
    }

    /**
     * Returns a list of header values that can be set in the HttpResponse.
     * Returns null if no headers need to be set.
     */
    public Map getHeaders (String uid) {
        return null;
    }

    /**
     * Let the channel know that there were problems with the download
     * @param e
     */
    public void reportDownloadError(Exception e) {
      log.error(e.getMessage(), e);
    }

}
