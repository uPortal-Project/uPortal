package org.jasig.portal;


/**
 * Reference implementation of IUserLayoutDB
 * This implementation simply stores serialized XML string
 * @author Peter Kharchenko
 * @version $Revision$
 */

import org.w3c.dom.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.jasig.portal.utils.DTDResolver;


public class UserLayoutDBImpl implements IUserLayoutDB {

    private static String DEFAULT_MEDIA="netscape";
    String sLayoutDtd = "userLayout.dtd";
    boolean bPropsLoaded = false;
    String sPathToLayoutDtd;

    public Document getUserLayout(int userId,int profileId) {
        RdbmServices rdbmService = new RdbmServices ();
        Connection con = null;
        String str_uLayoutXML = null;


        try {


            con = rdbmService.getConnection ();
            Statement stmt = con.createStatement ();

            // for now, the profileId parameter gets ignored. Need to restructure UP_USERS table to sepearate layouts, so they can be profile-specific
            String sQuery = "SELECT USER_LAYOUT_XML FROM UP_USERS WHERE ID='" + userId + "'";
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

        if(str_uLayoutXML==null) return null;
        else {
            try {
                DTDResolver userLayoutDtdResolver = new DTDResolver("userLayout.dtd");
                // read in the layout DOM
                // note that we really do need to have a DOM structure here in order to introduce
                // persistent changes on the level of userLayout.
                //org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
                org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser ();
                parser.setEntityResolver(userLayoutDtdResolver);
                // set parser features
                parser.setFeature ("http://apache.org/xml/features/validation/dynamic", true);

                parser.parse (new org.xml.sax.InputSource (new StringReader (str_uLayoutXML)));
                return parser.getDocument ();
            } catch (Exception e) {
                Logger.log(Logger.ERROR,"UserLayoutDBImpl::getUserLayout() : unable to parse the user layout XML."+e);
            }
        }
        return null;
    }


    public void setUserLayout(int userId,int profileId,Document layoutXML) {
        RdbmServices rdbmService = new RdbmServices ();
        Connection con = null;
        String sQuery = "";

        try {
            con = rdbmService.getConnection ();
            Statement stmt = con.createStatement ();

            StringWriter outString = new StringWriter ();
            org.apache.xml.serialize.OutputFormat format=new org.apache.xml.serialize.OutputFormat();
            format.setOmitXMLDeclaration(false);
            format.setIndenting(false);
            org.apache.xml.serialize.XMLSerializer xsl = new org.apache.xml.serialize.XMLSerializer (outString,format);
            xsl.serialize (layoutXML);
            String str_userLayoutXML=outString.toString();

            // for now, the profileName parameter gets ignored. Need to restructure UP_USERS table to sepearate layouts, so they can be profile-specific
            sQuery = "UPDATE UP_USERS SET USER_LAYOUT_XML='" + str_userLayoutXML + "' WHERE ID=" + userId;
            Logger.log (Logger.DEBUG, sQuery);
            ResultSet rs = stmt.executeQuery (sQuery);

        } catch (SQLException sqle) {
            Logger.log(Logger.ERROR, sQuery);
            Logger.log(Logger.ERROR,sqle);
        } catch (Exception e) {
            Logger.log(Logger.ERROR,e);
        } finally {
            rdbmService.releaseConnection (con);
        }

    }
}


