package org.jasig.portal;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.net.*;
import com.objectspace.xml.*;
import org.jasig.portal.layout.*;
import org.jasig.portal.channels.*;
import org.jasig.portal.GenericPortalBean;

/**
 * Provides methods associated with publishing a channel.
 * This includes naming and defining characteristics.
 * Channel may then be subject for approval and added to a registry.
 * @author John Laker
 * @version $Revision$
 */
public class PublisherBean extends GenericPortalBean{

  org.jasig.portal.layout.IChannel chan;
  IXml chanXml;
  String sPubEmail;
  String sChanName;

  public PublisherBean() {
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
    Object channelObject = Class.forName("org.jasig.portal.channels."+req.getParameter("chan_type")).newInstance();
    org.jasig.portal.IChannel ch = (org.jasig.portal.IChannel) channelObject;
    Vector v = ch.getParameters();
    Enumeration enum = v.elements();
    while(enum.hasMoreElements()) {
      ParameterField param = new ParameterField();
      param = (ParameterField)enum.nextElement();
      out.print(param.writeField());
    }
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
  }

  /**
   * Creates a new channel object
   * @param the servlet request object
   */
  public void createChannel (HttpServletRequest req)
  {    
    try 
    {
      // Get a new channel and set its parameters
      chanXml = Xml.newDocument("org.jasig.portal.layout", new File("layout.dtd"),"channel");
      chan = (org.jasig.portal.layout.IChannel) chanXml.getRoot ();
      chan.setClassAttribute("org.jasig.portal.channels."+req.getParameter("chan_type"));
      Enumeration enum = req.getParameterNames();
      while (enum.hasMoreElements()) {
         String name = (String)enum.nextElement();
         IParameter param = Factory.newParameter();
         param.setNameAttribute(name);
         param.setValueAttribute(req.getParameter(name));
         chan.addParameter(param);
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
        try{
        sPubEmail = req.getParameter("pub_email");
        LayoutBean layoutbean = new LayoutBean();
        createChannel(req);
        org.jasig.portal.IChannel ch = layoutbean.getChannelInstance(chan);
        sChanName = ch.getName();

        out.println ("<table border=0 cellpadding=1 cellspacing=4 width=100%>");
        out.println ("  <tr>");
        out.println ("    <td bgcolor=cccccc>");

        // Channel heading
            
        out.println ("      <table border=0 cellpadding=0 cellspacing=0 width=100% bgcolor=#83a3b8>");
        out.println ("        <tr>");
        out.println ("          <td>");
        out.println ("            <font face=arial color=#000000><b>&nbsp;" + sChanName + "</b></font>");
        out.println ("          </td>");
        out.println ("          <td nowrap valign=center align=right>");
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
              ch.render (req, res, out);

              
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
        catch (Exception e){
              Logger.log (Logger.ERROR, e);
        }
  }

  /**
   * Saves channel to database
   * @param the servlet request object
   */
  public boolean registerChannel (HttpServletRequest req)
  {
    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;
    ResultSet rs = null;
    boolean status = false;

    try
    {
      con = rdbmService.getConnection ();
      Statement stmt = con.createStatement();

      String sQuery = "SELECT max(chan_id)+1  FROM PORTAL_CHANNELS";
      rs = stmt.executeQuery(sQuery);
      rs.next();
      int nextID = rs.getInt(1);
      debug("nextID: "+nextID);

      StringWriter sw = new StringWriter ();
      chanXml.saveDocument(sw);
      String sChanXml = sw.toString();
      int trim = sChanXml.indexOf("<channel");
      sChanXml = sChanXml.substring(trim);

      String sInsert = "INSERT INTO PORTAL_CHANNELS (CHAN_ID, TITLE, PUB_EMAIL, CHANNEL_XML) VALUES ("+nextID+",'" + sChanName + "','"+  sPubEmail +"','" + sChanXml + "')";
      int iInserted = stmt.executeUpdate (sInsert);
      if (iInserted == 1) status = true;
      Logger.log (Logger.DEBUG, "Saving channel xml for " + sChanName + ". Inserted " + iInserted + " rows.");
      stmt.close ();
      status = setChannelCats(req, nextID);
      return status;
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
      return status;
    }
    finally
    {
      rdbmService.releaseConnection (con);
    }
  }

  /**
   * Relates channel to classifications
   * @param the servlet request object
   */
  public boolean setChannelCats (HttpServletRequest req, int id)
  {
    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;
    boolean status = false;

    try
    {
      con = rdbmService.getConnection ();
      Statement stmt = con.createStatement();

      String[] cats = req.getParameterValues("class");
      for(int i=0; i<cats.length; i++){
        String sInsert = "INSERT INTO PORTAL_CHAN_CLASS (CLASS_ID, CHAN_ID) VALUES ("+cats[i]+","+ id + ")";
        Logger.log (Logger.DEBUG, sInsert);
        int iInserted = stmt.executeUpdate (sInsert);
        if (iInserted == 1) status = true;
       }
      stmt.close ();
      return status;
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
      return false;
    }
    finally
    {
      rdbmService.releaseConnection (con);
    }
  }

  /**
   * Writes a list of publishable channel types
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void writeChannelTypes (HttpServletRequest req, HttpServletResponse res, JspWriter out)
  {
    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;
    ResultSet rs = null;
    Statement stmt = null;

    try
    {
      con = rdbmService.getConnection ();
      stmt = con.createStatement();

      String sQuery = "SELECT TYPE, NAME, DESC FROM PORTAL_CHAN_TYPES";
      Logger.log (Logger.DEBUG, sQuery);

        rs = stmt.executeQuery (sQuery);

        while(rs.next()) {
        out.println("<tr valign=\"top\">");
        out.println("<td width=\"26%\" height=\"37\">");
        out.println("<input type=\"radio\" name=\"chan_type\" value=\""+rs.getString("TYPE")+"\">"+rs.getString("NAME")+"</td>");
        out.println("<td width=\"3%\" height=\"37\">&nbsp;</td>");
        out.println("<td width=\"71%\" height=\"37\"><font size=\"2\">"+rs.getString("DESC")+"</font></td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td width=\"26%\" height=\"2\">&nbsp;</td>");
        out.println("<td width=\"3%\" height=\"2\">&nbsp;</td>");
        out.println("<td width=\"71%\" height=\"2\">&nbsp;</td>");
        out.println("</tr>");
        }
        stmt.close();
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    finally
    {
      rdbmService.releaseConnection (con);
    }    
  }

  /**
   * Writes a list of channel classifications for publishing.
   * @param the servlet request object
   * @param the servlet response object
   * @param the JspWriter object
   */
  public void writeChannelCats (HttpServletRequest req, HttpServletResponse res, JspWriter out)
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

        rs = stmt.executeQuery (sQuery);

        while(rs.next()) {
        out.println("<input name=class type=checkbox value="+rs.getString("CLASS_ID")+">"+rs.getString("NAME")+"<br>");

        }
        stmt.close();
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    finally
    {
      rdbmService.releaseConnection (con);
    }    
  }

  /**
   * Allows admin to approve channel for subscription
   * @param the servlet request object
   */
  public void approveChannel (HttpServletRequest req)
  {
    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;
    String sChanId = req.getParameter("chan_id");
    
    try
    {
      con = rdbmService.getConnection ();
      Statement stmt = con.createStatement();

      String sUpdate = "UPDATE PORTAL_CHANNELS SET APPROVED = 1 WHERE CHAN_ID = "+ sChanId;
      int iUpdated = stmt.executeUpdate (sUpdate);
      Logger.log (Logger.DEBUG, "Updating channel xml for " + sChanId + ". Updated " + iUpdated + " rows.");
      stmt.close ();
    }
    catch (Exception e)
    {
      Logger.log (Logger.ERROR, e);
    }
    finally
    {
      rdbmService.releaseConnection (con);
    }    
  }

}