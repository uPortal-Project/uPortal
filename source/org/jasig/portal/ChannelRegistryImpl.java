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
import org.w3c.dom.*;

/**
 * Reference implementation of IChannelRegistry.
 * Reads in an XML string
 * @author  John Laker, jlaker@udel.edu
 * @version $Revision$
 */
public class ChannelRegistryImpl implements IChannelRegistry {

    private Document chanDoc;
    String sRegDtd = "channelRegistry.dtd";

    /**
     * Return a Document object based on the XML string returned
     * from the database.
     * @param catID a category ID
     * @param role role of the user
     * @return Document DOM object
     */
    public Document getRegistryDoc (String catID, String role) {

        String regXML = getRegistryXML(catID, role);

        try{
        if(regXML!=null) {
                DTDResolver chRegDtdResolver = new DTDResolver(sRegDtd);

                // read in the layout DOM
                org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser ();

                // set parser features
                parser.setFeature ("http://apache.org/xml/features/validation/dynamic", true);

                parser.setEntityResolver(chRegDtdResolver);
                parser.parse (new org.xml.sax.InputSource (new StringReader (regXML)));
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
    public String getRegistryXML(String catID, String role) {
        RdbmServices rdbmService = new RdbmServices ();
        Connection con = null;
        String chanXML = null;

        try {
            con = rdbmService.getConnection ();
            Statement stmt = con.createStatement ();

            String sQuery = "SELECT CHANNEL_REG FROM UP_CHANNELS WHERE ID=" + catID;
            Logger.log (Logger.DEBUG, sQuery);

            ResultSet rs = stmt.executeQuery (sQuery);

            if (rs.next ())
                {
                    chanXML = rs.getString ("CHANNEL_REG");
                }
            stmt.close();
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }

        return chanXML;
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

}
