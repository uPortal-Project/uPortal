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

            String sQuery = "SELECT CHANNEL_REG FROM PORTAL_CHANNELS WHERE ID=" + catID;
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
