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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javax.servlet.jsp.JspWriter;

import java.io.File;
import java.io.StringWriter;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Arrays;
import java.util.Enumeration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import com.objectspace.xml.IXml;
import com.objectspace.xml.Xml;

import org.jasig.portal.layout.IChannel;
import org.jasig.portal.layout.IParameter;
import org.jasig.portal.layout.Factory;

import org.jasig.portal.ParameterField;
import org.jasig.portal.GenericPortalBean;

import org.jasig.portal.security.IRole;
import org.jasig.portal.security.IPerson;

/**
 * Provides methods associated with publishing a channel.
 * This includes naming and defining characteristics.
 * Channel may then be subject for approval and added to a registry.
 * @author John Laker
 * @version $Revision$
 */
public class PublisherBean extends GenericPortalBean
{
  private org.jasig.portal.layout.IChannel m_chan;
  private IXml   m_chanXml;
  private String m_sPubEmail;
  private String m_sChanName;
  private String m_sChanType;

  private Vector m_vAllowedRoles;
  private Vector m_vDeniedRoles;

  private Vector m_vChanCats;

  private Hashtable m_hChanParamFields;

  public PublisherBean()
  {
  }

  public void reset()
  {
    m_chan              = null;
    m_chanXml           = null;
    m_sPubEmail         = null;
    m_sChanName         = null;
    m_sChanType         = null;
    m_vAllowedRoles     = null;
    m_vDeniedRoles      = null;
    m_vChanCats         = null;
    m_hChanParamFields  = null;
  }

  /**
   * Update the class variables if they come in from the request
   */
  public void setChannelData(HttpServletRequest request)
  {
    if(request == null)
    {
      return;
    }

    if(request.getParameter("pubEmail") != null)
    {
      m_sPubEmail = request.getParameter("pubEmail");
    }

    if(request.getParameter("chanName") != null)
    {
      m_sChanName = request.getParameter("chanName");
    }

    if(request.getParameter("chanType") != null)
    {
      m_sChanType = request.getParameter("chanType");
    }

    if(request.getParameterValues("chanCats") != null)
    {
      m_vChanCats = new Vector(Arrays.asList(request.getParameterValues("chanCats")));
    }

    if(request.getParameter("numChannelParameters") != null)
    {
      setChanParamFields(request);
    }

    return;
  }

  /**
   * Method creates an instance of the channel class
   * and then writes the fields needed to set the channel parameters
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void writeParamFields(HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try
    {
      if(m_sChanType == null || m_sChanType.equals(""))
      {
        return;
      }

      String channelClassName = null;

      // If the channel name isn't fully qualified
      //  then assume it is in org.jasig.portal.channels
      //  For backwards compatability
      if(m_sChanType.indexOf(".") == -1)
      {
        channelClassName = "org.jasig.portal.channels." + m_sChanType;
      }
      else
      {
        channelClassName = m_sChanType;
      }

      // Create the channel object
      Object channelObject = Class.forName(channelClassName).newInstance();

      org.jasig.portal.IChannel ch = null;

      // If necessary, wrap an IXMLChannel to be compatible with 1.0's IChannel
      if(channelObject instanceof org.jasig.portal.IChannel)
      {
        ch = (org.jasig.portal.IChannel)channelObject;
      }
      else
      if(channelObject instanceof org.jasig.portal.IXMLChannel)
      {
        ch = new XMLChannelWrapper((org.jasig.portal.IXMLChannel) channelObject);
      }

      Vector vChanParameters = ch.getParameters();

      out.print("<tr><td><input type=\"hidden\" name=\"numChannelParameters\" value=\"" + vChanParameters.size() + "\"></input></td></tr>");

      for(int i = 0; i < vChanParameters.size(); i++)
      {
        ParameterField param = new ParameterField();

        param = (ParameterField)vChanParameters.elementAt(i);

        out.print("<tr><td><input type=\"hidden\" name=\"chanParameterName." + i + "\" value=\"" + param.getName() + "\"></input></td></tr>");
        out.print("<tr>\n");
        out.print("  <td colspan=\"5\">" + param.getDesc() + "</td>\n");
        out.print("</tr>\n");
        out.print("<tr>\n");
        out.print("  <td width=\"10%\">" + param.getLabel() + ":</td>\n");
        out.print("  <td width=\"90%\">");
        out.print("    <input type=\"text\" name=\"chanParameterValue." + i + "\" ");
        out.print("size=\"" + param.getLength() + "\" ");
        out.print("maxlength=\"" + param.getMaxLength() + "\" ></input>");
        out.print("  </td>\n");
        out.print("</tr>\n");
        out.print("<tr><td>&nbsp;</tr></td>");
      }

      if(m_sChanType.indexOf("CApplet") != -1)
      {
        out.print("<tr><td><input type=\"hidden\" name=\"numAppletParameters\" value=\"5\"></input></td></tr>");

        for(int i = 0; i < 5; i++)
        {
          out.print("<tr>\n");
          out.print("  <td width=\"10%\">Name " + i + ":</td>\n");
          out.print("  <td width=\"20%\"><input type=\"text\" name=\"appletParameterName" + i + "\" size=\"10\" maxlength=\"20\" ></input></td>\n");
          out.print("  <td width=\"10%\">Value "+i+":</td>\n");
          out.print("  <td width=\"60%\"><input type=\"text\" name=\"appletParameterValue" + i + "\" size=\"50\" maxlength=\"70\" ></input></td>\n");
          out.print("</tr>\n");
        }
      }
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
  }

  public void setChanParamFields(HttpServletRequest request)
  {
    try
    {
      m_hChanParamFields = new Hashtable();

      // Find and store all channel parameters
      if(request.getParameter("numChannelParameters") != null)
      {
        int iNumChannelParameters = Integer.parseInt(request.getParameter("numChannelParameters"));

        for(int i = 0; i < iNumChannelParameters; i++)
        {
          // Get a name value pair from the request
          String channelParameterName  = request.getParameter("chanParameterName." + i);
          String channelParameterValue = request.getParameter("chanParameterValue." + i);

          // Add the name/value pair to the hashtable of channel parameters
          m_hChanParamFields.put(channelParameterName, channelParameterValue);
        }
      }

      // Find and store all applet parameters
      if(request.getParameter("numAppletParameters") != null)
      {
        int iNumAppletParameters = Integer.parseInt(request.getParameter("numAppletParameters"));

        for(int i = 0; i < iNumAppletParameters; i++)
        {
          // Get a name value pair from the request
          String appletParameterName  = request.getParameter("appletParameterName."  + i);
          String appletParameterValue = request.getParameter("appletParameterValue." + i);

          // Add the name/value pair to the hashtable of channel parameters
          m_hChanParamFields.put("APPLET." + appletParameterName, appletParameterValue);
        }
      }
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
  }
  /**
   * Creates a new channel object
   * @param the servlet request object
   */
  public void createChannel(HttpServletRequest req)
  {
    try
    {
      // Create a new XML document for the channel
      m_chanXml = Xml.newDocument("org.jasig.portal.layout", new File("layout.dtd"), "channel");

      // Get the DXML representation of the channel
      m_chan = (org.jasig.portal.layout.IChannel)m_chanXml.getRoot();

      // If the channel name isn't fully qualified
      //  then assume it is in org.jasig.portal.channels
      //  For backwards compatability
      if(m_sChanType.indexOf(".") == -1)
      {
        m_chan.setClassAttribute("org.jasig.portal.channels." + m_sChanType);
      }
      else
      {
        m_chan.setClassAttribute(m_sChanType);
      }

      // Set all of the parameters for the channel
      for(Enumeration e = m_hChanParamFields.keys(); e.hasMoreElements();)
      {
        String key = (String)e.nextElement();

        IParameter parameter = Factory.newParameter();

        parameter.setNameAttribute(key);
        parameter.setValueAttribute((String)m_hChanParamFields.get(key));
        m_chan.addParameter(parameter);
      }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }

  /**
   * displays a preview of the channel
   * for the user to see before subscribing
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void previewChannel(HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try
    {
      LayoutBean layoutbean = new LayoutBean();

      createChannel(req);

      org.jasig.portal.IChannel ch = layoutbean.getChannelInstance(m_chan);

      m_sChanName = ch.getName();

      out.println ("<table border=0 cellpadding=1 cellspacing=4 width=100%>");
      out.println ("  <tr>");
      out.println ("    <td bgcolor=cccccc>");

      // Channel heading
      out.println ("      <table border=0 cellpadding=0 cellspacing=0 width=100% bgcolor=#83a3b8>");
      out.println ("        <tr>");
      out.println ("          <td>");
      out.println ("            <font face=arial color=#000000><b>&nbsp;" + m_sChanName + "</b></font>");
      out.println ("          </td>");
      out.println ("          <td nowrap valign=\"middle\" align=right>");
      out.println ("            &nbsp;");

      // Channel control buttons
      if (ch.isMinimizable ())
      out.println ("<img border=0 width=\"18\" height=\"15\" src=\"images/minimize.gif\" alt=\"Minimize\">");

      if (ch.isDetachable ())
      out.println ("<img border=0 width=\"18\" height=\"15\" src=\"images/detach.gif\" alt=\"Detach\">");

      if (ch.isRemovable ())
      out.println ("<img border=0 width=\"18\" height=\"15\" src=\"images/remove.gif\" alt=\"Remove\">");

      if (ch.isEditable ())
      out.println ("<img border=0 width=\"28\" height=\"15\" src=\"images/edit.gif\" alt=\"Edit\">");

      if (ch.hasHelp ())
      out.println ("<img border=0 width=\"18\" height=\"15\" src=\"images/help.gif\" alt=\"Help\">");

      out.println ("            &nbsp;");
      out.println ("          </td>");
      out.println ("        </tr>");
      out.println ("      </table>");

      // Channel body
      out.println ("      <table border=0 cellpadding=0 cellspacing=0 width=100%>");
      out.println ("        <tr>");
      out.println ("          <td bgcolor=#ffffff>");

      out.println ("            <table border=0 cellpadding=3 cellspacing=0 width=100% bgcolor=#ffffff>");
      out.println ("              <tr>");
      out.println ("                <td valign=top>");

      // Render channel contents
      ch.render(req, res, out);

      out.println ("                </td>");
      out.println ("              </tr>");
      out.println ("            </table>");

      out.println ("          </td>");
      out.println ("        </tr>");
      out.println ("      </table>");

      out.println ("    </td>");
      out.println ("  </tr>");
      out.println ("</table>");
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
  }

  /**
   * Saves channel to database
   * @param the servlet request object
   */
  public boolean registerChannel(HttpServletRequest req)
  {
    // Get the session
    HttpSession session = req.getSession(false);
    if(session == null)
    {
      Logger.log(Logger.ERROR, "PublisherBean.registerChannel(): Cannot publish channel without session");
      return(false);
    }

    // Get the person object
    IPerson person = (IPerson)session.getAttribute("Person");
    if(person == null)
    {
      Logger.log(Logger.ERROR, "PublisherBean.registerChannel(): Cannot publish channel without Person object in session");
      return(false);
    }

    // Make sure the user is authorized to publish
    AuthorizationBean authorizationBean = new AuthorizationBean();
    if(!authorizationBean.canUserPublish(person))
    {
      Logger.log(Logger.ERROR, "PublisherBean.registerChannel(): User " + person.getID() + " tried to publish without authorization");
      return(false);
    }

    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;
    ResultSet rs   = null;
    boolean status = false;
    Statement stmt = null;

    try
    {
      con = rdbmService.getConnection ();
      stmt = con.createStatement();

      String sQuery = "SELECT MAX(CHAN_ID)+1 FROM PORTAL_CHANNELS";
      rs = stmt.executeQuery(sQuery);
      rs.next();
      int nextID = rs.getInt(1);
      debug("nextID: "+nextID);

      // Make sure all parameters are escaped properly before storing
      IParameter chanParameters [] = m_chan.getParameters();
      for(int i = 0; i < chanParameters.length; i++)
      {
        if(chanParameters[i] != null)
        {
          chanParameters[i].setNameAttribute(UtilitiesBean.escapeString(chanParameters[i].getNameAttribute()));
          chanParameters[i].setValueAttribute(UtilitiesBean.escapeString(chanParameters[i].getValueAttribute()));
        }
      }

      StringWriter sw = new StringWriter ();
      m_chanXml.saveDocument(sw);
      String sChanXml = sw.toString();
      int trim = sChanXml.indexOf("<channel");
      sChanXml = sChanXml.substring(trim);

      // Insert the channel data
      String sInsert  = "INSERT INTO PORTAL_CHANNELS (CHAN_ID, TITLE, PUB_EMAIL, APPROVED, CHANNEL_XML, USER_NAME) ";
             sInsert += "VALUES (" + nextID + ",'" + m_sChanName + "','" +  m_sPubEmail + "',0,'" + sChanXml + "','" + person.getID() + "')";

      int iInserted = stmt.executeUpdate (sInsert);

      Logger.log (Logger.DEBUG, sInsert);

      if(iInserted == 1)
      {
        status = true;
      }

      if (status)
      {
        status = storeChanCats(req,  nextID, con);

        if (status)
        {
          status = storeChanRoles(req, nextID, con);
        }
        else
        {
          // Something went wrong while saving the channel categories.  Remove the orphan channel
          String sDelete = "DELETE FROM PORTAL_CHANNELS WHERE CHAN_ID=" + nextID;
          int iDeleted = stmt.executeUpdate(sDelete);
        }
      }
      return status;
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
      return status;
    }
    finally
    {
      try
      {
        if (stmt != null)
          stmt.close ();
      }
      catch (SQLException ex)
      {
        Logger.log(Logger.ERROR, ex);
      }

      rdbmService.releaseConnection(con);
    }
  }

  /**
   * Relates channel to classifications
   * @param the servlet request object
   */
  public boolean storeChanCats(HttpServletRequest req, int id, Connection con)
  {
    RdbmServices rdbmService = new RdbmServices ();
    boolean status = false;

    try
    {
      Statement stmt = con.createStatement();

      for(int i = 0; i < m_vChanCats.size(); i++)
      {
        String sInsert = "INSERT INTO PORTAL_CHAN_CLASS (CLASS_ID, CHAN_ID) VALUES (" + m_vChanCats.elementAt(i) + "," + id + ")";
        Logger.log(Logger.DEBUG, sInsert);
        int iInserted = stmt.executeUpdate(sInsert);

        if(iInserted == 1)
        {
          status = true;
        }
      }

      stmt.close();

      return status;
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
      return false;
    }
    finally
    {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * Relates channel to roles
   * @param the servlet request object
   */
  public boolean storeChanRoles(HttpServletRequest req, int id, Connection con)
  {
    try
    {
      AuthorizationBean authorizationBean = new AuthorizationBean();

      // Check for an unrestricted channel
      if(m_vAllowedRoles == null)
      {
        return true;
      }

      // Have the authorization bean store the channel roles
      int rolesSet = authorizationBean.setChannelRoles(id, m_vAllowedRoles);

      // Make sure all of the roles have been stored
      if(rolesSet == m_vAllowedRoles.size())
      {
        return(true);
      }
      else
      {
        return(false);
      }
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
      return(false);
    }
  }

  /**
   * Writes a list of publishable channel types
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void writeChannelTypes(HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;
    ResultSet rs   = null;
    Statement stmt = null;

    try
    {
      con  = rdbmService.getConnection ();
      stmt = con.createStatement();

      String sQuery = "SELECT TYPE, NAME, DESCR FROM PORTAL_CHAN_TYPES";
      Logger.log (Logger.DEBUG, sQuery);

        rs = stmt.executeQuery (sQuery);

        while(rs.next())
        {
          out.println("<tr valign=\"top\">\n");
          out.println("  <td width=\"26%\" height=\"37\">");
          out.println("<input type=\"radio\" name=\"chanType\" value=\""+rs.getString("TYPE")+"\">"+rs.getString("NAME")+"</td>\n");
          out.println("  <td width=\"3%\" height=\"37\">&nbsp;</td>\n");
          out.println("  <td width=\"71%\" height=\"37\"><font size=\"2\">"+rs.getString("DESCR")+"</font></td>\n");
          out.println("</tr>\n");
          out.println("<tr>\n");
          out.println("  <td width=\"26%\" height=\"2\">&nbsp;</td>\n");
          out.println("  <td width=\"3%\" height=\"2\">&nbsp;</td>\n");
          out.println("  <td width=\"71%\" height=\"2\">&nbsp;</td>\n");
          out.println("</tr>\n");
        }
        stmt.close();
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
    finally
    {
      rdbmService.releaseConnection(con);
    }
  }

  /**
   * Writes a list of channel classifications for publishing.
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void writeChanCats (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;
    ResultSet rs = null;
    Statement stmt = null;

    try
    {
      con = rdbmService.getConnection ();
      stmt = con.createStatement();

      String sQuery = "SELECT CLASS_ID, NAME FROM PORTAL_CLASS";
      Logger.log (Logger.DEBUG, sQuery);

      rs = stmt.executeQuery(sQuery);

      while(rs.next())
      {
        out.println("<input name=\"chanCats\" type=\"checkbox\" value=\""+rs.getString("CLASS_ID")+"\">"+rs.getString("NAME")+"</input><br>\n");
      }

      stmt.close();
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
    finally
    {
      rdbmService.releaseConnection(con);
    }
  }

  public void updateRoles(HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try
    {
      AuthorizationBean authorizationBean = new AuthorizationBean();

      Vector vRoles = authorizationBean.getAllRoles();

      // Check to see which button was pressed
      String sAction = req.getParameter("userAction");

      // Initialize roles vectors
      if(m_vAllowedRoles == null || m_vDeniedRoles == null)
      {
        resetRoles();
      }

      if(sAction != null && sAction.equals("allow"))
      {
        // Get the list of selected roles from the denied roles list
        String [] aDeniedRoles = req.getParameterValues("deniedRoles");

        // Convert the list to a vector for easier handling
        Vector aNewAllowedRoles = new Vector(Arrays.asList(aDeniedRoles));

        // Remove the helper tag if the user selected it
        aNewAllowedRoles.remove("-- At least one role must be allowed --");

        // Add the roles to the existing list of allowed roles
        m_vAllowedRoles.addAll(aNewAllowedRoles);

        // Remove the roles from the denied list
        m_vDeniedRoles.removeAll(aNewAllowedRoles);
      }
      else
      if(sAction != null && sAction.equals("deny"))
      {
        // Get the list of selected roles from the allowed roles list
        String [] aAllowedRoles = req.getParameterValues("allowedRoles");

        // Convert the list to a vector for easier handling
        Vector aNewDeniedRoles = new Vector(Arrays.asList(aAllowedRoles));

        // Remove the helper tag if the user selected it
        aNewDeniedRoles.remove("-- All roles can subscribe to channel --");

        // Add the roles to the existing list of denied roles
        m_vDeniedRoles.addAll(aNewDeniedRoles);

        // Remove the roles from the allowed list
        m_vAllowedRoles.removeAll(aNewDeniedRoles);
      }

      // Validate the roles lists
      if(vRoles.size() != m_vAllowedRoles.size() + m_vDeniedRoles.size())
      {
        resetRoles();
      }
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }

    return;
  }

  private void resetRoles()
  {
    try
    {
      AuthorizationBean authorizationBean = new AuthorizationBean();

      m_vAllowedRoles = new Vector();
      m_vDeniedRoles  = new Vector();

      Vector vRoles = authorizationBean.getAllRoles();

      for(int i = 0; i < vRoles.size(); i++)
      {
        // Add the string representation of each role to the list
        m_vDeniedRoles.add(((IRole)vRoles.elementAt(i)).getRoleTitle());
      }
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
  }

  /**
   * Writes a list of channel classifications for publishing.
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void writeAllowedRoles(HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try
    {
      // List the roles or the helper tag if no roles are in the list
      if(m_vAllowedRoles.size() > 0)
      {
        for(int i = 0; i < m_vAllowedRoles.size(); i++)
        {
          out.println("<option>" + m_vAllowedRoles.elementAt(i) + "</option>");
        }
      }
      else
      {
        out.println("<option>-- At least one role must be allowed --</option>");
      }
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
  }

  /**
   * Writes a list of channel classifications for publishing.
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void writeDeniedRoles(HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    try
    {
      // List the roles or the helper tag if no roles are in the list
      if(m_vDeniedRoles.size() > 0)
      {
        for(int i = 0; i < m_vDeniedRoles.size(); i++)
        {
          out.println("<option>" + m_vDeniedRoles.elementAt(i) + "</option>");
        }
      }
      else
      {
        out.println("<option>-- All roles can subscribe to channel --</option>");
      }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }

  public boolean checkAllowedRoles()
  {
    try
    {
      // The allowed roles list must have at least one value in it
      if(m_vAllowedRoles != null && m_vAllowedRoles.size() > 0)
      {
        return(true);
      }
      else
      {
        return(false);
      }
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
      return(false);
    }
  }

  /**
   * Allows admin to approve channel for subscription
   * @param the servlet request object
   */
  public void approveChannel(HttpServletRequest req)
  {
    RdbmServices rdbmService = new RdbmServices();
    Connection con = null;
    String sChanId = req.getParameter("CHAN_ID");

    try
    {
      con = rdbmService.getConnection ();
      Statement stmt = con.createStatement();

      String sUpdate = "UPDATE PORTAL_CHANNELS SET APPROVED = 1 WHERE CHAN_ID = " + sChanId;

      int iUpdated = stmt.executeUpdate(sUpdate);

      Logger.log(Logger.DEBUG, "Updating channel xml for " + sChanId + ". Updated " + iUpdated + " rows.");

      stmt.close();
    }
    catch (Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
    finally
    {
      rdbmService.releaseConnection(con);
    }
  }

  public String getClassName(String sClassID)
  {
    if(sClassID == null)
    {
      return(null);
    }

    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;

    try
    {
      con = rdbmService.getConnection();
      Statement stmt = con.createStatement();

      String sQuery = "SELECT NAME FROM PORTAL_CLASS WHERE CLASS_ID = " + sClassID;

      stmt.executeQuery(sQuery);

      ResultSet rs = stmt.getResultSet();

      String sClassName = null;

      if(rs.next())
      {
        sClassName = rs.getString("NAME");
      }

      stmt.close();

      return(sClassName);
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
      return(null);
    }
    finally
    {
      rdbmService.releaseConnection (con);
    }
  }

  public Vector getChanCats()
  {
    return m_vChanCats;
  }

  public void setChanCats(Vector vChanCats)
  {
    m_vChanCats = vChanCats;
  }

  public String getPubEmail()
  {
    return m_sPubEmail;
  }

  public void setPubEmail(String sPubEmail)
  {
    m_sPubEmail = sPubEmail;
  }

  public String getChanType()
  {
    return m_sChanType;
  }

  public void setChanType(String sChanType)
  {
    m_sChanType = sChanType;
  }

  public String getChanName()
  {
    return m_sChanName;
  }

  public void setChanName(String sChanName)
  {
    m_sChanName = sChanName;
  }

  public void writeChanCatsList(JspWriter out)
  {
    try
    {
      if(m_vChanCats != null)
      {
        for(int i = 0; i < m_vChanCats.size(); i++)
        {
          out.print(getClassName((String)m_vChanCats.elementAt(i)));

          if(i < m_vChanCats.size() - 1)
          {
            out.print(", ");
          }
        }
      }
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
  }

  public void writeAllowedRolesList(JspWriter out)
  {
    try
    {
      if(m_vAllowedRoles != null)
      {
        for(int i = 0; i < m_vAllowedRoles.size(); i++)
        {
          out.print(m_vAllowedRoles.elementAt(i));

          if(i < m_vAllowedRoles.size() - 1)
          {
            out.print(", ");
          }
        }
      }
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
  }

  public void writeChanParamFieldsList(JspWriter out)
  {
    try
    {
      if(m_hChanParamFields != null)
      {
        for(Enumeration e = m_hChanParamFields.keys(); e.hasMoreElements();)
        {
          String key = (String)e.nextElement();

          out.println("<tr>");
          out.println("  <td>" + key + "</td>");
          out.println("  <td>" + (String)m_hChanParamFields.get(key) + "</td>");
          out.println("</tr>");
        }
      }
    }
    catch(Exception e)
    {
      Logger.log(Logger.ERROR, e);
    }
  }
}

