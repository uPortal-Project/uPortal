/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal;

import org.jasig.portal.utils.DTDResolver;
import java.sql.*;
import java.io.*;

import org.apache.xalan.xpath.*;
import org.apache.xalan.xslt.*;
import org.apache.xml.serialize.*;
import  org.apache.xerces.dom.*;
import org.w3c.dom.*;

/**
 * Reference implementation of IChannelRegistry.
 * Reads in an XML string
 * @author  John Laker, jlaker@udel.edu
 * @version $Revision$
 */
public class ChannelRegistryImpl implements IChannelRegistry {

    private Document chanDoc = null;
    private Document types = null;
    String sRegDtd = "channelRegistry.dtd";

    /**
     * Return a Document object based on the XML string returned
     * from the database.
     * @param catID a category ID
     * @param role role of the user
     * @return Document DOM object
     */
    public Document getRegistryDoc (String catID, String role) {

        Document regXML = getRegistryXML(catID, role);

        try{
        if(regXML!=null) {
                DTDResolver chRegDtdResolver = new DTDResolver(sRegDtd);

                // read in the layout DOM
                org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser ();

                // set parser features
                parser.setFeature ("http://apache.org/xml/features/validation/dynamic", true);

                parser.setEntityResolver(chRegDtdResolver);
                //parser.parse (new org.xml.sax.InputSource (new StringReader (regXML)));
                chanDoc = parser.getDocument ();
            }
        }
        catch (Exception e) {
          Logger.log(Logger.ERROR,e);
        }

        return chanDoc;
    }

/** Returns a string of XML which describes the channel registry.
 * Right now this is being stored as a string in a field but could be also implemented to get from multiple tables.
 * @param catID a category ID
 * @param role role of the current user
 * @return a string of XML
 */
    public Document getRegistryXML(String catID, String role) {
        System.out.println("Enterering ChannelRegistryImpl::getRegistryXML()");
        RdbmServices rdbmService = new RdbmServices ();
        Connection con = null;
        String chanXML = null;
        String catid = "";

        try {
            con = rdbmService.getConnection ();
            Statement stmt = con.createStatement ();

            String sQuery = "SELECT CL.CLASS_ID, CL.NAME, CH.CHANNEL_XML "+
                            "FROM UP_CLASS CL, UP_CHANNELS CH, UP_CHAN_CLASS CHCL " +
                            "WHERE CH.CHAN_ID=CHCL.CHAN_ID AND CHCL.CLASS_ID=CL.CLASS_ID";
            
            if(catID!=null) sQuery += " AND CL.CLASS_ID=" + catID;
            
            sQuery += " ORDER BY CL.NAME, CH.TITLE";
            
            Logger.log (Logger.DEBUG, sQuery);

            ResultSet rs = stmt.executeQuery (sQuery);

            chanDoc = new DocumentImpl();
            Element root = chanDoc.createElement("registry");
            Element cat = null;
            while (rs.next ())
                {
                    //String catid = "";
                    String catnm = rs.getString(2);
                    System.out.println("catnm: " + catnm);
                    String chxml = rs.getString(3);
                    System.out.println("chxml: " +  chxml);
                    Node chan = null;
                    
                    String s = rs.getString(1);
                    System.out.println("s: " + s);
                    if (!s.equals(catid)) {
                        if(catid.length() > 0) root.appendChild(cat);
                        catid = s;
                        cat = chanDoc.createElement("category");
                        cat.setAttribute("ID", "cat"+catid);
                        cat.setAttribute("name", catnm);
                    }
                    org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser ();
                    parser.parse (new org.xml.sax.InputSource (new StringReader (chxml)));
                    chan = parser.getDocument().getDocumentElement();
                    cat.appendChild(chanDoc.importNode(chan, false));
                }
            root.appendChild(cat);
            chanDoc.appendChild(root);
            stmt.close();
       /** 
        OutputFormat    format  = new OutputFormat( chanDoc );   //Serialize DOM
        StringWriter  stringOut = new StringWriter();        //Writer will be a String
        XMLSerializer    serial = new XMLSerializer( stringOut, format );
        serial.asDOMSerializer();                            // As a DOM Serializer

        serial.serialize( chanDoc.getDocumentElement() );

        System.out.println( "STRXML = " + stringOut.toString() );
        */
        //Logger.log(Logger.DEBUG, "STRXML = " + stringOut.toString());
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }
        return chanDoc;
    }

/** Returns a string of XML which describes the channel types.
 * Right now this is being stored as a string in a field but could be also implemented to get from multiple tables.
 * @param catID a category ID
 * @param role role of the current user
 * @return a string of XML
 */
    public Document getTypesXML(String role) {
        System.out.println("Enterering ChannelRegistryImpl::getTypesXML()");
        RdbmServices rdbmService = new RdbmServices ();
        Connection con = null;
        String chanXML = null;
        String catid = "";

        try {
            con = rdbmService.getConnection ();
            Statement stmt = con.createStatement ();

            String sQuery = "SELECT NAME, DEF_URI "+
                            "FROM UP_CHAN_TYPES";
            
            Logger.log (Logger.DEBUG, sQuery);

            ResultSet rs = stmt.executeQuery (sQuery);

            types = new DocumentImpl();
            Element root = types.createElement("channelTypes");
        
            while (rs.next ())
                {
                    //String catid = "";
                    String name = rs.getString(1);
                    System.out.println("name: " + name);
                    String uri = rs.getString(2);
                    System.out.println("uri: " +  uri);
                    Node chan = null;
                    
                    Element type = types.createElement("channelType");
                    Element elem = types.createElement("name");
                    elem.appendChild(types.createTextNode(name));
                    type.appendChild(elem);
                    elem = types.createElement("definition");
                    elem.appendChild(types.createTextNode(uri));
                    type.appendChild(elem);
                    root.appendChild(type);
                }
            types.appendChild(root);
            stmt.close();
       
        OutputFormat    format  = new OutputFormat( types );   //Serialize DOM
        StringWriter  stringOut = new StringWriter();        //Writer will be a String
        XMLSerializer    serial = new XMLSerializer( stringOut, format );
        serial.asDOMSerializer();                            // As a DOM Serializer

        serial.serialize( types.getDocumentElement() );

        System.out.println( "STRXML = " + stringOut.toString() );
        
        //Logger.log(Logger.DEBUG, "STRXML = " + stringOut.toString());
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }
        return types;
    }
    
/** A method for adding a channel to the channel registry.
 * This would be called by a publish channel.
 * @param catID an array of category IDs
 * @param chanXML XML that describes the channel
 * @param role an array of roles
 */
    public void addChannel(String catID[], String chanXML, String role[]) {
    }

/** A method for removing a channel from the registry.
 * This could be used by an admin channel to unpublish a channel from
 * certain categories, roles, or just remove it altogether.
 * @param catID an array of category IDs
 * @param chanID a channel ID
 * @param role an array of roles
 */
    public void removeChannel(String catID[],String chanID,String role[]) {
    }

/** A method for persiting the channel registry to a file or database.
 * @param registryXML an XML description of the channel registry
 */
    public void setRegistryXML(String registryXML) {
    }
    
    public Document getChannelTypesXML () {
        return null;
    }
    
    public Document getChannelCatsXML () {
        return null;
    }

}
