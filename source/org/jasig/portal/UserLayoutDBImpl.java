package org.jasig.portal;

import java.sql.*;

/**
 * Reference implementation of IUserLayoutDB
 * This implementation simply stores serialized XML string
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class UserLayoutDBImpl implements IUserLayoutDB {

    private static String DEFAULT_MEDIA="netscape";

    public String getUserLayout(String userName,String media) {
	RdbmServices rdbmService = new RdbmServices ();
	Connection con = null;
	String str_uLayoutXML = null;
	
	try {
	    con = rdbmService.getConnection ();
	    Statement stmt = con.createStatement ();
	    
	    // for now, the media parameter gets ignored. Need to restructure PORTAL_USERS table to sepearate layouts, so they can be media-specific
	    String sQuery = "SELECT USER_LAYOUT_XML FROM PORTAL_USERS WHERE USER_NAME='" + userName + "'";
	    Logger.log (Logger.DEBUG, sQuery);

	    ResultSet rs = stmt.executeQuery (sQuery);
	    
	    if (rs.next ()) 
		{
		    str_uLayoutXML = rs.getString ("USER_LAYOUT_XML");
		} 
	    stmt.close();
	} catch (Exception e) {
	    Logger.log(Logger.ERROR,e);
	} finally {
	    rdbmService.releaseConnection (con);
	}

	return str_uLayoutXML;
    }


    public void setUserLayout(String userName,String media,String layoutXML) {

    }
}


