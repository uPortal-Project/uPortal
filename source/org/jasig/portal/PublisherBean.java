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
      String [] param = new String[5];
      param = (String[])enum.nextElement();
      out.println(param[4]+ "<br>");
      out.println("<table width=\"40%\" border=\"0\">");
      out.println("<tr>");
      out.println("<td width=\"19%\">"+param[0]+":</td>");
      out.println("<td width=\"81%\">");
      out.println("<input type=\"text\" name=\""+param[1]+"\" size=\""+param[2]+"\" maxlength=\""+param[3]+"\" >");
      out.println("</td>");
      out.println("</tr>");
      out.println("</table>");
      out.println("<br>");
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
   */
  public boolean registerChannel ()
  {
    RdbmServices rdbmService = new RdbmServices ();
    Connection con = null;

    boolean status = false;

    try
    {
      con = rdbmService.getConnection ();
      Statement stmt = con.createStatement();

      StringWriter sw = new StringWriter ();
      //debug("chan: "+chan);
      //chanXml = chan;
      //debug("chanXml: "+chanXml);
      //debug("sChanName: "+ sChanName);
      chanXml.saveDocument(sw);
      String sChanXml = sw.toString();

      String sInsert = "INSERT INTO PORTAL_CHANNELS (TITLE, PUB_EMAIL, CHANNEL_XML) VALUES ('" + sChanName + "','"+  sPubEmail +"','" + sChanXml + "')";
      int iInserted = stmt.executeUpdate (sInsert);
      if (iInserted == 1) status = true;
      Logger.log (Logger.DEBUG, "Saving channel xml for " + sChanName + ". Inserted " + iInserted + " rows.");
      stmt.close ();
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