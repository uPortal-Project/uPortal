package org.jasig.portal;

import java.sql.*;
import java.io.*;
import java.util.*;
import java.text.*;

import org.apache.xalan.xpath.*;
import org.apache.xalan.xslt.*;
import org.apache.xml.serialize.*;
import org.w3c.dom.*;

/**
 * Reference implementation of IChannelRegistry.
 * Reads in an XML string
 * @author  John Laker
 * @version $Revision$
 */
public class ChannelRegistryImpl extends GenericPortalBean implements IChannelRegistry {

    private Document chanDoc;
    String sRegDtd = "channelRegistry.dtd";
    String sPathToRegDtd = null;
    
    /** Creates new ChannelRegistryImpl */
    public ChannelRegistryImpl() {
            boolean bPropsLoaded = false;

            String fs = System.getProperty ("file.separator");
            String propertiesDir = getPortalBaseDir () + "properties" + fs;
    
            try {
                if (!bPropsLoaded) {
                    File layoutPropsFile = new File (getPortalBaseDir () + "properties" + File.separator + "layout.properties");
	            Properties layoutProps = new Properties ();
	            layoutProps.load (new FileInputStream (layoutPropsFile));
	            sPathToRegDtd = layoutProps.getProperty ("pathToUserLayoutDTD");
	            bPropsLoaded = true;
	        }
            }
            catch (Exception e) { Logger.log(Logger.ERROR,e);}
    }

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
		// Tack on the full path to user_layout.dtd
		//peterk: should be done on the SAX level or not at all
		int iInsertBefore = regXML.indexOf (sRegDtd);
		
		if (iInsertBefore != -1) 
		    regXML = regXML.substring (0, iInsertBefore) + sPathToRegDtd + regXML.substring (iInsertBefore);
		
		// read in the layout DOM
		// note that we really do need to have a DOM structure here in order to introduce
		// persistent changes on the level of userLayout.
		//org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
		org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser ();
		
		// set parser features
		parser.setFeature ("http://apache.org/xml/features/validation/dynamic", true);
		
		parser.parse (new org.xml.sax.InputSource (new StringReader (regXML)));
		chanDoc = parser.getDocument ();
	    }
        }
        catch (Exception e) { Logger.log(Logger.ERROR,e);}
        
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
