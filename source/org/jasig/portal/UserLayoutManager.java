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
  public UserLayoutManager (HttpServletRequest req, String sUserName)
  {
    String sLayoutDtd = "userLayout.dtd";
    String sPathToLayoutDtd = null;
    boolean bPropsLoaded = false;

    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;
    String str_uLayoutXML = null;
    uLayoutXML = null;

    try
    {
      if (!bPropsLoaded)
      {
        File layoutPropsFile = new File (getPortalBaseDir () + "properties" + File.separator + "layout.properties");
        Properties layoutProps = new Properties ();
        layoutProps.load (new FileInputStream (layoutPropsFile));
        sPathToLayoutDtd = layoutProps.getProperty ("pathToUserLayoutDTD");
        bPropsLoaded = true;
      }

      // read uLayoutXML

      HttpSession session = req.getSession (false);
      uLayoutXML = (Document) session.getAttribute ("userLayoutXML");

      if (uLayoutXML == null) 
      {
        if (sUserName == null)
          sUserName = "guest";

        con = rdbmService.getConnection ();
        Statement stmt = con.createStatement ();

        String sQuery = "SELECT USER_LAYOUT_XML FROM PORTAL_USERS WHERE USER_NAME='" + sUserName + "'";
        Logger.log (Logger.DEBUG, sQuery);

        ResultSet rs = stmt.executeQuery (sQuery);

        if (rs.next ()) 
        {
          str_uLayoutXML = rs.getString ("USER_LAYOUT_XML");

          // Tack on the full path to user_layout.dtd
          //peterk: should be done on the SAX level or not at all
          int iInsertBefore = str_uLayoutXML.indexOf (sLayoutDtd);
          
          if (iInsertBefore != -1) 
            str_uLayoutXML = str_uLayoutXML.substring (0, iInsertBefore) + sPathToLayoutDtd + str_uLayoutXML.substring (iInsertBefore);

          // read in the layout DOM
          // note that we really do need to have a DOM structure here in order to introduce
          // persistent changes on the level of userLayout.
          //org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser();
          org.apache.xerces.parsers.DOMParser parser = new org.apache.xerces.parsers.DOMParser ();
          
          // set parser features
          parser.setFeature ("http://apache.org/xml/features/validation/dynamic", true);
          
          parser.parse (new org.xml.sax.InputSource (new StringReader (str_uLayoutXML)));
          uLayoutXML = parser.getDocument ();
          session.setAttribute ("userLayoutXML", uLayoutXML);
        }
        stmt.close ();
      }
    }
    catch (Exception e) 
    {
      Logger.log (Logger.ERROR, e);
      Logger.log (Logger.ERROR, str_uLayoutXML);
    }
    finally 
    {
      rdbmService.releaseConnection (con);
    }
  }

  public Node getNode (String elementID) 
  {
    return uLayoutXML.getElementById (elementID);
  }
  
  public Node getRoot () 
  {
    return uLayoutXML;
  }

  public void minimizeChannel (String str_ID) 
  {
    //Element channel = uLayoutXML.getElementById (str_ID);
    Element channel = ((org.apache.xerces.dom.DocumentImpl)uLayoutXML).getElementById (str_ID);
    
    if (channel != null) 
    {
      if (channel.getAttribute ("minimized").equals ("false"))
        channel.setAttribute ("minimized", "true");
      else 
        channel.setAttribute ("minimized", "false");
    } 
    else 
      Logger.log (Logger.ERROR,"UserLayoutManager::minimizeChannel() : unable to find a channel with ID=" + str_ID);
  }

  public void removeChannel (String str_ID) 
  {
    Element channel = uLayoutXML.getElementById (str_ID);
    
    if (channel!=null) 
    {
      Node parent=channel.getParentNode ();
      
      if (parent!=null) 
        parent.removeChild (channel);
      else 
        Logger.log (Logger.ERROR,"UserLayoutManager::removeChannel() : attempt to remove a root node !");
    } 
    else Logger.log (Logger.ERROR,"UserLayoutManager::removeChannel() : unable to find a channel with ID="+str_ID);
  }
}
