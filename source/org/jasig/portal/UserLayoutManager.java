package org.jasig.portal;

import java.sql.*;
import org.w3c.dom.*;

import org.apache.xalan.xpath.*;
import org.apache.xalan.xslt.*;
import org.apache.xml.serialize.*;
import org.w3c.dom.*;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.net.*;

public class UserLayoutManager extends GenericPortalBean
{

    private org.w3c.dom.Document uLayoutXML;

   
    /**
     * Constructor does the following
     *  1. Read layout.properties
     *  2. read userLayout from the database
     *  @param the servlet request object
     *  @param user name
     */
    public UserLayoutManager (HttpServletRequest req, String sUserName) {
	String sLayoutDtd = "userLayout.dtd";
	String sPathToLayoutDtd = null;
	boolean bPropsLoaded = false;

	RdbmServices rdbmService = new RdbmServices ();
	Connection con = null;
	String str_uLayoutXML=null;
	uLayoutXML=null;

	try {
	    if (!bPropsLoaded) {
		File layoutPropsFile = new File (getPortalBaseDir () + "properties"+System.getProperty("file.separator")+"layout.properties");
		Properties layoutProps = new Properties ();
		layoutProps.load (new FileInputStream (layoutPropsFile));
		sPathToLayoutDtd = layoutProps.getProperty ("pathToUserLayoutDTD");
		bPropsLoaded = true;
	    }



	    // read uLayoutXML

	    HttpSession session = req.getSession (false);
	    uLayoutXML = (Document) session.getAttribute ("userLayoutXML");
	    
	    if (uLayoutXML == null) {
		if (sUserName == null)
		    sUserName = "guest";
		
		con = rdbmService.getConnection ();
		Statement stmt = con.createStatement();
		
		String sQuery = "SELECT USER_LAYOUT_XML FROM PORTAL_USERS WHERE USER_NAME='" + sUserName + "'";
		Logger.log (Logger.DEBUG, sQuery);
		
		ResultSet rs = stmt.executeQuery (sQuery);
		
		if (rs.next ()) {
		    str_uLayoutXML = rs.getString ("USER_LAYOUT_XML");
		    
		    // Tack on the full path to user_layout.dtd
		    //peterk: should be done on the SAX level or not at all
		    
		    int iInsertBefore = str_uLayoutXML.indexOf (sLayoutDtd);
		    if(iInsertBefore!=-1) str_uLayoutXML = str_uLayoutXML.substring (0, iInsertBefore) + sPathToLayoutDtd + str_uLayoutXML.substring (iInsertBefore);
		    
		    // read in the layout DOM
		    // note that we really do need to have a DOM structure here in order to introduce
		    // persistent changes on the level of userLayout.
		    //org.apache.xerces.parsers.DOMParser parser=new org.apache.xerces.parsers.DOMParser();
		    org.apache.xerces.parsers.DOMParser parser=new org.apache.xerces.parsers.DOMParser();
		    // set parser features
		    parser.setFeature("http://apache.org/xml/features/validation/dynamic",true);
		    parser.parse(new org.xml.sax.InputSource(new StringReader(str_uLayoutXML)));
		    uLayoutXML=parser.getDocument();
		    session.setAttribute ("userLayoutXML", uLayoutXML);
		}
		stmt.close ();
	    }
	}
	catch (Exception e) {
	    Logger.log (Logger.ERROR, e);
	    Logger.log (Logger.ERROR,str_uLayoutXML);
	}
	finally {
	    rdbmService.releaseConnection (con);
	}
    }

    public Node getNode(String elementID) {
	return uLayoutXML.getElementById(elementID);
    }
    public Node getRoot() {
	return uLayoutXML;
    }

    public void minimizeChannel(String str_ID) {
	Element channel=uLayoutXML.getElementById(str_ID);
	if(channel!=null) {
	    if(channel.getAttribute("minimized").equals("false"))
		channel.setAttribute("minimized","true");
	    else channel.setAttribute("minimized","false");
	} else Logger.log(Logger.ERROR,"UserLayoutManager::minimizeChannel() : unable to find a channel with ID="+str_ID);
    }
    
    public void removeChannel(String str_ID) {
	Element channel=uLayoutXML.getElementById(str_ID);
	if(channel!=null) {
	    Node parent=channel.getParentNode();
	    if(parent!=null) {
		parent.removeChild(channel);
	    } else {
		Logger.log(Logger.ERROR,"UserLayoutManager::removeChannel() : attempt to remove a root node !");
	    }
	} else Logger.log(Logger.ERROR,"UserLayoutManager::removeChannel() : unable to find a channel with ID="+str_ID);
    }

}
