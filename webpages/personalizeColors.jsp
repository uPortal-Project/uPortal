<%--
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
--%>

<%-- Debra Rundle (C) copyright June 2000 --%>
<%@ page import="java.text.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ page import = "java.sql.*" %>
<%@ page import="org.jasig.portal.Logger" %>
<%@ page errorPage="error.jsp" %>
<%@ include file="checkinit.jsp" %>
<%@ include file="checkGuest.jsp" %>

<jsp:useBean id="layoutBean" class="org.jasig.portal.LayoutBean" type="org.jasig.portal.ILayoutBean" scope="session" />
<jsp:useBean id="rdbmService" class="org.jasig.portal.RdbmServices" scope="session" />

<%
String sAction = request.getParameter ("action");

if (sAction != null) {
  if (sAction.equals ("saveColors")) layoutBean.setColors (request, response, out);
  response.sendRedirect ("layout.jsp");
}
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <title>Personalize Colors</title>

    <link rel=stylesheet href="stylesheets/portal.css" TYPE="text/css">
    <meta http-equiv="expires" content="Tue, 20 Aug 1996 14:25:27 GMT">
    <%
      String fComponent = (String)request.getParameter("Component");
      if (fComponent == null) fComponent = "bgColor";

      String fColorScheme = (String)request.getParameter("ColorScheme");
      if (fColorScheme == null) fColorScheme = "University";

      String fColorPalette = (String)request.getParameter("ColorPalette");
      if (fColorPalette == null) fColorPalette = "browsersafe";

      String omyBGColor = layoutBean.getBackgroundColor (request, response, out);
      String fBGColor = (String)request.getParameter("bgColor");
      String myBGColor = layoutBean.getBackgroundColor (request, response, out);
      myBGColor = myBGColor.trim();
      if ((myBGColor.substring(0,1)).equals("#")) myBGColor = myBGColor.substring(1);
      if (fBGColor == null) fBGColor = myBGColor;

      String fTextColor = (String)request.getParameter("TextColor");
      String myTextColor = layoutBean.getForegroundColor (request, response, out);
      if (myTextColor == null) myTextColor = fTextColor;
      myTextColor = myTextColor.trim();
      if ((myTextColor.substring(0,1)).equals("#")) myTextColor = myTextColor.substring(1);
      if (fTextColor == null) fTextColor = myTextColor;

      String fActiveTabColor = (String)request.getParameter("ActiveTabColor");
      String myActiveTabColor = layoutBean.getActiveTabColor (request, response, out);
      myActiveTabColor = myActiveTabColor.trim();
      if ((myActiveTabColor.substring(0,1)).equals("#")) myActiveTabColor = myActiveTabColor.substring(1);
      if (fActiveTabColor == null) fActiveTabColor = myActiveTabColor;

      String fTabColor = (String)request.getParameter("TabColor");
      String myTabColor = layoutBean.getTabColor (request, response, out);
      myTabColor = myTabColor.trim();
      if ((myTabColor.substring(0,1)).equals("#")) myTabColor = myTabColor.substring(1);
      if (fTabColor == null) fTabColor = myTabColor;

      String fChannelColor = (String)request.getParameter("ChannelColor");
      String myChannelColor = layoutBean.getChannelHeadingColor (request, response, out);
      myChannelColor = myChannelColor.trim();
      if ((myChannelColor.substring(0,1)).equals("#")) myChannelColor = myChannelColor.substring(1);
      if (fChannelColor == null) fChannelColor = myChannelColor;
    %>
    <style>
      .MiniPortalTitleText
      {
        text-decoration:none;
        color: <%=fTextColor%>;
        font-weight:bold;
        font-family:arial,helvetica,times,courier;
        font-size:14pt
      }

      .MiniPortalText
      {
        text-decoration:none;
        font-weight:plain;
        font-family:arial,helvetica,times,courier;
        font-size:12pt
      }
    </style>

    <script language="Javascript">
      <!--
      function viewNewColor(whatColor) {
        whatIndex = document.ComponentsForm.PortalComponent.options.selectedIndex

        whatComponent = document.ComponentsForm.PortalComponent.options[whatIndex].value

        maxComponents = document.ComponentsForm.PortalComponent.options.length

        showColor = "personalizeColors.jsp?Component=" + whatComponent + "&ColorScheme=<%=fColorScheme%>&ColorPalette=<%=fColorPalette%>"

        showColor = showColor + "&" + whatComponent + "=" + whatColor

        if (whatComponent != "bgColor") showColor = showColor + "&bgColor=<%=fBGColor%>"

        if (whatComponent != "TextColor") showColor = showColor + "&TextColor=<%=fTextColor%>"

        if (whatComponent != "ActiveTabColor") showColor = showColor + "&ActiveTabColor=<%=fActiveTabColor%>"

        if (whatComponent != "TabColor") showColor = showColor + "&TabColor=<%=fTabColor%>"

        if (whatComponent != "ChannelColor") showColor = showColor + "&ChannelColor=<%=fChannelColor%>"

        window.location = showColor

        return true
      }
      -->
    </script>
  </head>

  <body bgcolor=<%=myBGColor%> link=<%=myActiveTabColor%> vlink=<%=myTabColor%> alink=<%=myChannelColor%>>
    <%-- Header --%>
    <% session.setAttribute ("headerTitle", "Personalize Colors"); %>
    <%@ include file="header.jsp" %>

    <%-- begin one column formatting table for mini-portal --%>
    <table border="0">
      <tr>
        <td valign="top">
          <table border="0">
            <tr>
              <td>View your color changes on the mini portal below:</td>
            </tr>
            <tr>
              <td>
                <table border="1" bgColor="<%=fBGColor%>">
                  <tr>
                    <td>
                      <table border="0" cellspacing="0" cellpadding="0">
                        <tr>
                          <td bgcolor="<%=fActiveTabColor%>"><span class="MiniPortalTitleText"><center>Active Tab</center></span></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="<%=fTabColor%>"><span class="MiniPortalTitleText"><center>Tab 2</center></span></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="<%=fTabColor%>"><span class="MiniPortalTitleText"><center>Tab 3</center></span></td>
                        </tr>
                        <tr>
                          <td colspan="5" bgcolor="<%=fActiveTabColor%>"><img src="images/dot_clear.gif" vspace=2></td>
                        </tr>
                        <tr>
                          <td colspan="5"><img src="images/dot_clear.gif" vspace="6"/></td>
                        </tr>
                        <tr>
                          <td bgcolor="<%=fChannelColor%>"><span class="MiniPortalTitleText">Plain Text</span></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="<%=fChannelColor%>"><span class="MiniPortalTitleText">All Capitals</span></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="<%=fChannelColor%>"><span class="MiniPortalTitleText">Numbers</span></td>
                        </tr>
                        <tr>
                          <td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="6"/></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="6"/></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="6"/></td>
                        </tr>
                        <tr>
                          <td bgcolor="ffffff"><span class="MiniPortalText">a b c d e f g h i j k l m n o p q r s t u v w x y z </span><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="ffffff"><span class="MiniPortalText">A B C D E F G H I J K L M N O P Q R S T U V W X Y Z </span></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="ffffff"><span class="MiniPortalText">1 2 3 4 5 6 7 8 9 0 ! @  $ % ^ & * ( ) </span></td>
                        </tr>
                        <tr>
                          <td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="6"/></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="6"/></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="6"/></td>
                        </tr>
                        <tr>
                          <td bgcolor="ffffff"><span class="MiniPortalText">a b c d e f g h i j k l m n o p q r s t u v w x y z </span></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="ffffff"><span class="MiniPortalText">A B C D E F G H I J K L M N O P Q R S T U V W X Y Z </span></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="ffffff"><span class="MiniPortalText">1 2 3 4 5 6 7 8 9 0 ! @  $ % ^ & * ( ) </span></td>
                        </tr>
                        <tr>
                          <td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="6"/></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="6"/></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="6"/></td>
                        </tr>
                        <tr>
                          <td bgcolor="ffffff"><A HREF="personalizeColors.jsp"><FONT COLOR="<%=fActiveTabColor%>"><B>Note your Link color is assigned to Active Tab color</B></FONT></A></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="ffffff"><A HREF="personalizeColors.jsp"><FONT COLOR="<%=fTabColor%>"><B>Your visited link color is assigned to the non-active tab color</B></FONT></A></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="ffffff"><img src="images/dot_clear.gif" hspace="2"/></td>
                        </tr>
                        <tr>
                          <td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="6"/></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="6"/></td>
                          <td><img src="images/dot_clear.gif" hspace="2"/></td>
                          <td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="6"/></td>
                        </tr>
                        <tr>
                          <td colspan="5"><img src="images/dot_clear.gif" vspace="6"/></td>
                        </tr>
                        <tr>
                          <td colspan="5"><center><span class="MiniPortalText">Save your new colors or return to your portal:</span></center></td>
                        </tr>
                        <tr>
                          <td colspan="5"><img src="images/dot_clear.gif" vspace="6"/></td>
                        </tr>
                        <tr>
                          <td colspan="5">
                            <center>
                              <form name=SaveReturn action="personalizeColors.jsp" method=post>
                                <input type=hidden name="bgColor" value="<%=fBGColor%>">
                                <input type=hidden name="fgColor" value="<%=fTextColor%>">
                                <input type=hidden name="tabColor" value="<%=fTabColor%>">
                                <input type=hidden name="activeTabColor" value="<%=fActiveTabColor%>">
                                <input type=hidden name="channelHeadingColor" value="<%=fChannelColor%>">
                                <input type=hidden name=action value="saveColors">
                                <input type=submit name=submit value="Save Colors">
                              </form>
                            </center>
                          </td>
                        </tr>
                      </table>
                    </td>
                  </tr>
                </table>
              </td>
              </tr>
              </table>
              <%-- end one column formatting table - mini-portal --%>

              <%-- begin two column formatting table --%>
              <table>
              <tr>
              <td valign="top">
              <table border=1>
              <tr>
              <td>
              <table border="1">
              <tr>
              <td>Change by scheme:</td>
              </tr>
              <tr>
              <td><img src="images/dot_clear.gif" vspace="6"/></td>
              </tr>
              <tr>
              <td>
              <form name="colorGroup">
              <select name="selColorGroup" onchange="top.location=(this.options[selectedIndex].value)">
              <%--
              For testing without DB
              <option selected value="personalizeColors.jsp?ColorScheme=University">University</option>
              --%>
              <%
              Connection connection = null;
              String SchemeDrop = "";
              String color_scheme_name = null;

              try {
              connection = rdbmService.getConnection();
              Statement statement = connection.createStatement();
              try
              {
              String SQLString = "SELECT DISTINCT COLOR_SCHEME_NAME FROM PORTAL_COLORS";
              Logger.log (Logger.DEBUG, SQLString);
              ResultSet rs = statement.executeQuery(SQLString);
              while(rs.next()) {
              color_scheme_name = rs.getString(1);
              SchemeDrop = SchemeDrop + "<option ";
              if (fColorScheme.equals(color_scheme_name)) SchemeDrop = SchemeDrop + "selected ";
              SchemeDrop = SchemeDrop + "value=\"personalizeColors.jsp?ColorScheme=" + color_scheme_name + "\">" + color_scheme_name + "</option>";
              }
              }
              catch (SQLException e)
              {
              // Logger.log (Logger.ERROR, e + " SQL: " + SQLString);
              }
              finally
              {
              statement.close();
              }
              }
              catch (Exception e)
              {
              // Logger.log (Logger.ERROR, e);
              }
              finally
              {
              rdbmService.releaseConnection(connection);
              }
              %>
              <%=SchemeDrop%>
              </select>
              </form>
              </td>
              </tr>
              <tr>
              <td>
              <table border="1" bordercolor="<%=myTextColor%>" bordercolordark="<%=myTextColor%>" bordercolorlight="<%=myTextColor%>">
              <tr>
                <td>Apply</td>
                <td><img src="images/dot_clear.gif">BG</td>
                <td><img src="images/dot_clear.gif">TX</td>
                <td><img src="images/dot_clear.gif">AT</td>
                <td><img src="images/dot_clear.gif">OT</td>
                <td><img src="images/dot_clear.gif">CH</td>
              </tr>
              <form name="ColorScheme">
                <tr>
                  <td><center><input type="radio" name="colors" onclick="javascript:window.location='personalizeColors.jsp?ColorScheme=University&bgColor=<%=myBGColor%>&TextColor=<%=myTextColor%>&ActiveTabColor=<%=myActiveTabColor%>&TabColor=<%=myTabColor%>&ChannelColor=<%=myChannelColor%>';">Revert to your colors</center></td>
                  <td bgcolor="<%=myBGColor%>"><img src="images/dot_clear.gif" vspace="12"/></td>
                  <td bgcolor="<%=myTextColor%>"><img src="images/dot_clear.gif" vspace="12"/></td>
                  <td bgcolor="<%=myActiveTabColor%>"><img src="images/dot_clear.gif" vspace="12"/></td>
                  <td bgcolor="<%=myTabColor%>"><img src="images/dot_clear.gif" vspace="12"/></td>
                  <td bgcolor="<%=myChannelColor%>"><img src="images/dot_clear.gif" vspace="12"/></td>
                </tr>
              <%--
              For testing without DB
              <tr><td><input type="radio" name="colors" onclick="javascript:window.location='personalizeColors.jsp?ColorScheme=University&bgColor=ffffff&TextColor=000000&ActiveTabColor=ff9900&TabColor=cc6633&ChannelColor=ffcc33';"></td><td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="000000"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="ff9900"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="cc6633"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="ffcc33"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="ff9900"><img src="images/dot_clear.gif" vspace="12"/></td></tr><tr><td><input type="radio" name="colors" onclick="javascript:window.location='personalizeColors.jsp?ColorScheme=University&bgColor=ffffff&TextColor=000000&ActiveTabColor=83a368&TabColor=99ccff&ChannelColor=009900';"></td><td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="000000"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="83a368"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="99ccff"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="009900"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="83a368"><img src="images/dot_clear.gif" vspace="12"/></td></tr><tr><td><input type="radio" name="colors" onclick="javascript:window.location='personalizeColors.jsp?ColorScheme=University&bgColor=ffffff&TextColor=000000&ActiveTabColor=009900&TabColor=336699&ChannelColor=83a368';"></td><td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="000000"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="009900"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="336699"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="83a368"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="009900"><img src="images/dot_clear.gif" vspace="12"/></td></tr><tr><td><input type="radio" name="colors" onclick="javascript:window.location='personalizeColors.jsp?ColorScheme=University&bgColor=eeeeee&TextColor=000000&ActiveTabColor=83a368&TabColor=336699&ChannelColor=83a368';"></td><td bgcolor="eeeeee"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="000000"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="83a368"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="336699"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="83a368"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="83a368"><img src="images/dot_clear.gif" vspace="12"/></td></tr><tr><td><input type="radio" name="colors" onclick="javascript:window.location='personalizeColors.jsp?ColorScheme=University&bgColor=cc6633&TextColor=000000&ActiveTabColor=ff9900&TabColor=ffffff&ChannelColor=ffcc33';"></td><td bgcolor="cc6633"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="000000"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="ff9900"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="ffcc33"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="ff9900"><img src="images/dot_clear.gif" vspace="12"/></td></tr><tr><td><input type="radio" name="colors" onclick="javascript:window.location='personalizeColors.jsp?ColorScheme=University&bgColor=0044AA&TextColor=ffffff&ActiveTabColor=ffDD00&TabColor=ff6600&ChannelColor=ffDD00';"></td><td bgcolor="0044AA"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="ffDD00"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="ff6600"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="ffDD00"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="ffDD00"><img src="images/dot_clear.gif" vspace="12"/></td></tr><tr><td><input type="radio" name="colors" onclick="javascript:window.location='personalizeColors.jsp?ColorScheme=University&bgColor=000000&TextColor=ffffff&ActiveTabColor=ff9900&TabColor=cc6633&ChannelColor=ffcc33';"></td><td bgcolor="000000"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="ffffff"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="ff9900"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="cc6633"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="ffcc33"><img src="images/dot_clear.gif" vspace="12"/></td><td bgcolor="ff9900"><img src="images/dot_clear.gif" vspace="12"/></td></tr>
              --%>
              <%
              connection = null;
              String ColorSquares = "";
              String SeqID = null;
              String bgColor = null;
              String TextColor = null;
              String Link = null;
              String VLink = null;
              String ALink = null;

              try {
              connection = rdbmService.getConnection();
              Statement statement = connection.createStatement();
              String SQLString = "SELECT SEQ_ID, bgcolor, TEXT, LINK, VLINK, ALINK FROM PORTAL_COLORS WHERE COLOR_SCHEME_name='" + fColorScheme + "' ORDER BY bgcolor DESC, TEXT DESC, LINK DESC, VLINK DESC";
              Logger.log (Logger.DEBUG, SQLString);
              ResultSet rs = statement.executeQuery(SQLString);
              while(rs.next()) {
              SeqID     = rs.getString(1);
              bgColor   = rs.getString(2);
              TextColor = rs.getString(3);
              Link      = rs.getString(4);
              VLink     = rs.getString(5);
              ALink     = rs.getString(6);
              ColorSquares = ColorSquares + "<tr><td><input type=\"radio\" name=\"colors\" onClick=\"javascript:window.location='personalizeColors.jsp?ColorScheme=" + fColorScheme + "&bgColor=" + bgColor + "&TextColor=" + TextColor + "&ActiveTabColor=" + Link + "&TabColor=" + VLink + "&ChannelColor=" + ALink + "';\">" + "</td><td bgcolor=\"" + bgColor + "\"><img src=\"images/dot_clear.gif\" vspace=\"12\"/></td><td bgcolor=\"" + TextColor + "\"><img src=\"images/dot_clear.gif\" vspace=\"12\"/></td><td bgcolor=\"" + Link + "\"><img src=\"images/dot_clear.gif\" vspace=\"12\"/></td><td bgcolor=\"" + VLink + "\"><img src=\"images/dot_clear.gif\" vspace=\"12\"/></td><td bgcolor=\"" + ALink + "\"><img src=\"images/dot_clear.gif\" vspace=\"12\"/></td></tr>";
              }
              statement.close();
              rdbmService.releaseConnection(connection);
              }
              catch (Exception e) {
              // Logger.log (Logger.ERROR, e);
              }

              %>
              <%=ColorSquares%>
              </form>
              </table>
              </td>
              </tr>
              </table>
              </td>
              </tr>
              </table>
              </td>
              <td valign="top"><img src="images/dot_clear.gif" hspace="10"/></td>
              <td valign="top">
              <table border="1">
              <tr>
              <td>
              <table border="0">
              <tr>
              <td>Change Individual Colors:</td>
              </tr>
              <tr>
              <td><img src="images/dot_clear.gif" vspace="6"/></td>
              </tr>
              <tr>
              <td>
              <form name="ComponentsForm">
              <select name="PortalComponent">
              <%
                StringWriter IndivDrop = new StringWriter();

                IndivDrop.write("<option ");
                if(fComponent.equals("bgColor"))
                {
                  IndivDrop.write("selected ");
                }
                IndivDrop.write("value=\"bgColor\">Background</option>\n");

                IndivDrop.write("<option ");
                if (fComponent.equals("TextColor"))
                {
                  IndivDrop.write("selected ");
                }
                IndivDrop.write("value=\"TextColor\">Title Text</option>\n");

                IndivDrop.write("<option ");
                if (fComponent.equals("ActiveTabColor"))
                {
                  IndivDrop.write("selected ");
                }
                IndivDrop.write("value=\"ActiveTabColor\">Active Tab</option>\n");

                IndivDrop.write("<option ");
                if (fComponent.equals("TabColor"))
                {
                  IndivDrop.write("selected ");
                }
                IndivDrop.write("value=\"TabColor\">Other Tabs</option>\n");

                IndivDrop.write("<option ");
                if (fComponent.equals("ChannelColor"))
                {
                  IndivDrop.write("selected ");
                }
                IndivDrop.write("value=\"ChannelColor\">Channel Heading</option>\n");
              %>
              <%= IndivDrop.toString() %>
              </select>
              </form>
              </td>
              </tr>
              <tr>
              <td>Click on a color below to change:</td>
              </tr>
              <tr>
              <td><img src="images/dot_clear.gif" vspace="6"/></td>
              </tr>
              <tr>
              <td halign=LEFT>
              <img src="images/<%=fColorPalette%>.gif" usemap="#ColorsMap" ismap border="0"/>
              <map name="ColorsMap">
              <area shape="rect" coords="145,373,155,383" alt="Cyan" href="javascript:viewNewColor('00ffff')">
              <area shape="rect" coords="145,313,155,323" alt="Blue" href="javascript:viewNewColor('0000ff')">
              <area shape="rect" coords="145,253,155,263" alt="Magenta" href="javascript:viewNewColor('ff00ff')">
              <area shape="rect" coords="145,193,155,203" alt="Red" href="javascript:viewNewColor('ff0000')">
              <area shape="rect" coords="145,133,155,143" alt="Yellow" href="javascript:viewNewColor('ffff00')">
              <area shape="rect" coords="145,73,155,83" alt="Green" href="javascript:viewNewColor('00ff00')">
              <area shape="rect" coords="145,13,155,23" alt="Cyan" href="javascript:viewNewColor('00ffff')">
              <area shape="rect" coords="24,12,36,24" href="javascript:viewNewColor('ccffff')">
              <area shape="rect" coords="36,12,48,24" href="javascript:viewNewColor('99ffff')">
              <area shape="rect" coords="48,12,60,24" href="javascript:viewNewColor('66ffff')">
              <area shape="rect" coords="60,12,72,24" href="javascript:viewNewColor('33ffff')">
              <area shape="rect" coords="72,12,84,24" href="javascript:viewNewColor('00ffff')">
              <area shape="rect" coords="84,12,96,36" href="javascript:viewNewColor('00cccc')">
              <area shape="rect" coords="96,12,108,48" href="javascript:viewNewColor('009999')">
              <area shape="rect" coords="108,12,120,60" href="javascript:viewNewColor('006666')">
              <area shape="rect" coords="120,12,132,72" href="javascript:viewNewColor('003333')">
              <area shape="rect" coords="24,24,36,132" href="javascript:viewNewColor('ccffcc')">
              <area shape="rect" coords="36,24,48,36" href="javascript:viewNewColor('99ffcc')">
              <area shape="rect" coords="48,24,60,36" href="javascript:viewNewColor('66ffcc')">
              <area shape="rect" coords="60,24,72,36" href="javascript:viewNewColor('33ffcc')">
              <area shape="rect" coords="72,24,84,36" href="javascript:viewNewColor('00ffcc')">
              <area shape="rect" coords="36,36,48,120" href="javascript:viewNewColor('99ff99')">
              <area shape="rect" coords="48,36,60,48" href="javascript:viewNewColor('66ff99')">
              <area shape="rect" coords="60,36,72,48" href="javascript:viewNewColor('33ff99')">
              <area shape="rect" coords="72,36,84,48" href="javascript:viewNewColor('00ff99')">
              <area shape="rect" coords="84,36,96,48" href="javascript:viewNewColor('00cc99')">
              <area shape="rect" coords="48,48,60,108" href="javascript:viewNewColor('66ff66')">
              <area shape="rect" coords="60,48,72,60" href="javascript:viewNewColor('33ff66')">
              <area shape="rect" coords="72,48,84,60" href="javascript:viewNewColor('00ff66')">
              <area shape="rect" coords="84,48,96,60" href="javascript:viewNewColor('00cc66')">
              <area shape="rect" coords="96,48,108,60" href="javascript:viewNewColor('009966')">
              <area shape="rect" coords="60,60,72,96" href="javascript:viewNewColor('33ff33')">
              <area shape="rect" coords="72,60,84,72" href="javascript:viewNewColor('00ff33')">
              <area shape="rect" coords="84,60,96,72" href="javascript:viewNewColor('00cc33')">
              <area shape="rect" coords="96,60,108,72" href="javascript:viewNewColor('009933')">
              <area shape="rect" coords="108,60,120,72" href="javascript:viewNewColor('006633')">
              <area shape="rect" coords="72,72,84,84" href="javascript:viewNewColor('00ff00')">
              <area shape="rect" coords="84,72,96,84" href="javascript:viewNewColor('009900')">
              <area shape="rect" coords="96,72,108,84" href="javascript:viewNewColor('00cc00')">
              <area shape="rect" coords="108,72,120,84" href="javascript:viewNewColor('009900')">
              <area shape="rect" coords="120,72,132,84" href="javascript:viewNewColor('003300')">
              <area shape="rect" coords="72,84,84,96" href="javascript:viewNewColor('33ff00')">
              <area shape="rect" coords="84,84,96,96" href="javascript:viewNewColor('339900')">
              <area shape="rect" coords="96,84,108,96" href="javascript:viewNewColor('33cc00')">
              <area shape="rect" coords="108,84,120,96" href="javascript:viewNewColor('339900')">
              <area shape="rect" coords="120,84,132,192" href="javascript:viewNewColor('333300')">
              <area shape="rect" coords="60,96,72,108" href="javascript:viewNewColor('66ff33')">
              <area shape="rect" coords="72,96,84,108" href="javascript:viewNewColor('66ff00')">
              <area shape="rect" coords="84,96,96,108" href="javascript:viewNewColor('66cc00')">
              <area shape="rect" coords="96,96,108,108" href="javascript:viewNewColor('669900')">
              <area shape="rect" coords="108,96,120,180" href="javascript:viewNewColor('666600')">
              <area shape="rect" coords="48,108,60,120" href="javascript:viewNewColor('99ff66')">
              <area shape="rect" coords="60,108,72,120" href="javascript:viewNewColor('99ff33')">
              <area shape="rect" coords="72,108,84,120" href="javascript:viewNewColor('99ff00')">
              <area shape="rect" coords="84,108,96,120" href="javascript:viewNewColor('99cc00')">
              <area shape="rect" coords="96,108,108,168" href="javascript:viewNewColor('999900')">
              <area shape="rect" coords="36,120,48,132" href="javascript:viewNewColor('ccff99')">
              <area shape="rect" coords="48,120,60,132" href="javascript:viewNewColor('ccff66')">
              <area shape="rect" coords="60,120,72,132" href="javascript:viewNewColor('ccff33')">
              <area shape="rect" coords="72,120,84,132" href="javascript:viewNewColor('ccff00')">
              <area shape="rect" coords="84,120,96,156" href="javascript:viewNewColor('cccc00')">
              <area shape="rect" coords="24,132,36,144" href="javascript:viewNewColor('ffffcc')">
              <area shape="rect" coords="36,132,48,144" href="javascript:viewNewColor('ffff99')">
              <area shape="rect" coords="48,132,60,144" href="javascript:viewNewColor('ffff66')">
              <area shape="rect" coords="60,132,72,144" href="javascript:viewNewColor('ffff33')">
              <area shape="rect" coords="72,132,84,144" href="javascript:viewNewColor('ffff00')">
              <area shape="rect" coords="24,144,36,252" href="javascript:viewNewColor('ffcccc')">
              <area shape="rect" coords="36,144,48,156" href="javascript:viewNewColor('ffcc99')">
              <area shape="rect" coords="48,144,60,156" href="javascript:viewNewColor('ffcc66')">
              <area shape="rect" coords="60,144,72,156" href="javascript:viewNewColor('ffcc33')">
              <area shape="rect" coords="72,144,84,156" href="javascript:viewNewColor('ffcc00')">
              <area shape="rect" coords="36,156,48,240" href="javascript:viewNewColor('ff9999')">
              <area shape="rect" coords="48,156,60,168" href="javascript:viewNewColor('ff9966')">
              <area shape="rect" coords="60,156,72,168" href="javascript:viewNewColor('ff9933')">
              <area shape="rect" coords="72,156,84,168" href="javascript:viewNewColor('ff9900')">
              <area shape="rect" coords="84,156,96,168" href="javascript:viewNewColor('cc9900')">
              <area shape="rect" coords="48,168,60,228" href="javascript:viewNewColor('ff6666')">
              <area shape="rect" coords="60,168,72,180" href="javascript:viewNewColor('33ff6633')">
              <area shape="rect" coords="72,168,84,180" href="javascript:viewNewColor('ff6600')">
              <area shape="rect" coords="84,168,96,180" href="javascript:viewNewColor('cc6600)">
              <area shape="rect" coords="96,168,108,180" href="javascript:viewNewColor('996600">
              <area shape="rect" coords="60,180,72,216" href="javascript:viewNewColor('ff3333')">
              <area shape="rect" coords="72,180,84,192" href="javascript:viewNewColor('ff3300')">
              <area shape="rect" coords="84,180,96,192" href="javascript:viewNewColor('cc3300')">
              <area shape="rect" coords="96,180,108,192" href="javascript:viewNewColor('993300')">
              <area shape="rect" coords="108,180,120,192" href="javascript:viewNewColor('663300')">
              <area shape="rect" coords="72,192,84,204" href="javascript:viewNewColor('ff0000')">
              <area shape="rect" coords="84,192,96,204" href="javascript:viewNewColor('cc0000')">
              <area shape="rect" coords="96,192,108,204" href="javascript:viewNewColor('990000')">
              <area shape="rect" coords="108,192,120,204" href="javascript:viewNewColor('660000')">
              <area shape="rect" coords="120,192,132,204" href="javascript:viewNewColor('330000')">
              <area shape="rect" coords="72,204,84,216" href="javascript:viewNewColor('ff0033')">
              <area shape="rect" coords="84,204,96,216" href="javascript:viewNewColor('cc0033')">
              <area shape="rect" coords="96,204,108,216" href="javascript:viewNewColor('990033')">
              <area shape="rect" coords="108,204,120,216" href="javascript:viewNewColor('660033')">
              <area shape="rect" coords="120,204,132,336" href="javascript:viewNewColor('330033')">
              <area shape="rect" coords="60,216,72,228" href="javascript:viewNewColor('ff3333')">
              <area shape="rect" coords="72,216,84,228" href="javascript:viewNewColor('ff0066')">
              <area shape="rect" coords="84,216,96,228" href="javascript:viewNewColor('cc0066')">
              <area shape="rect" coords="96,216,108,228" href="javascript:viewNewColor('990066')">
              <area shape="rect" coords="108,216,132,312" href="javascript:viewNewColor('660066')">
              <area shape="rect" coords="48,228,60,240" href="javascript:viewNewColor('ff6699')">
              <area shape="rect" coords="60,228,72,240" href="javascript:viewNewColor('ff3399')">
              <area shape="rect" coords="72,228,84,240" href="javascript:viewNewColor('ff0099')">
              <area shape="rect" coords="84,228,96,240" href="javascript:viewNewColor('cc0099')">
              <area shape="rect" coords="96,228,108,288" href="javascript:viewNewColor('990099')">
              <area shape="rect" coords="36,240,48,252" href="javascript:viewNewColor('ff99cc')">
              <area shape="rect" coords="48,240,60,252" href="javascript:viewNewColor('ff66cc')">
              <area shape="rect" coords="60,240,72,252" href="javascript:viewNewColor('ff33cc')">
              <area shape="rect" coords="72,240,84,252" href="javascript:viewNewColor('ff00cc')">
              <area shape="rect" coords="84,240,96,276" href="javascript:viewNewColor('cc00cc')">
              <area shape="rect" coords="24,252,36,264" href="javascript:viewNewColor('ffccff')">
              <area shape="rect" coords="36,252,48,264" href="javascript:viewNewColor('ff99ff')">
              <area shape="rect" coords="48,252,60,264" href="javascript:viewNewColor('ff66ff')">
              <area shape="rect" coords="60,252,72,264" href="javascript:viewNewColor('ff33ff')">
              <area shape="rect" coords="72,252,84,264" href="javascript:viewNewColor('ff00ff')">
              <area shape="rect" coords="24,264,36,372" href="javascript:viewNewColor('ccccff')">
              <area shape="rect" coords="36,264,48,276" href="javascript:viewNewColor('cc99ff')">
              <area shape="rect" coords="48,264,60,276" href="javascript:viewNewColor('cc66ff')">
              <area shape="rect" coords="60,264,72,276" href="javascript:viewNewColor('cc33ff')">
              <area shape="rect" coords="72,264,84,276" href="javascript:viewNewColor('cc00ff')">
              <area shape="rect" coords="36,276,48,360" href="javascript:viewNewColor('9999ff')">
              <area shape="rect" coords="48,276,60,288" href="javascript:viewNewColor('9966ff')">
              <area shape="rect" coords="60,276,72,288" href="javascript:viewNewColor('9933ff')">
              <area shape="rect" coords="72,276,84,288" href="javascript:viewNewColor('9900ff')">
              <area shape="rect" coords="84,276,96,360" href="javascript:viewNewColor('9900cc')">
              <area shape="rect" coords="48,288,60,348" href="javascript:viewNewColor('6666ff')">
              <area shape="rect" coords="60,288,72,300" href="javascript:viewNewColor('6633ff')">
              <area shape="rect" coords="72,288,84,300" href="javascript:viewNewColor('6600ff')">
              <area shape="rect" coords="84,288,96,300" href="javascript:viewNewColor('6600cc')">
              <area shape="rect" coords="96,288,108,348" href="javascript:viewNewColor('660099')">
              <area shape="rect" coords="60,300,72,336" href="javascript:viewNewColor('3333ff')">
              <area shape="rect" coords="72,300,84,312" href="javascript:viewNewColor('3300ff')">
              <area shape="rect" coords="84,300,96,312" href="javascript:viewNewColor('3300cc')">
              <area shape="rect" coords="96,300,108,312" href="javascript:viewNewColor('330099')">
              <area shape="rect" coords="108,300,120,312" href="javascript:viewNewColor('330066')">
              <area shape="rect" coords="72,312,84,324" href="javascript:viewNewColor('0000ff')">
              <area shape="rect" coords="84,312,96,324" href="javascript:viewNewColor('000099')">
              <area shape="rect" coords="96,312,108,324" href="javascript:viewNewColor('0000cc')">
              <area shape="rect" coords="108,312,120,324" href="javascript:viewNewColor('000099')">
              <area shape="rect" coords="120,312,132,324" href="javascript:viewNewColor('000033')">
              <area shape="rect" coords="72,324,84,336" href="javascript:viewNewColor('0066ff')">
              <area shape="rect" coords="84,324,96,336" href="javascript:viewNewColor('0033cc')">
              <area shape="rect" coords="96,324,108,336" href="javascript:viewNewColor('003399')">
              <area shape="rect" coords="108,324,120,336" href="javascript:viewNewColor('003366')">
              <area shape="rect" coords="120,324,132,372" href="javascript:viewNewColor('003333')">
              <area shape="rect" coords="60,336,72,348" href="javascript:viewNewColor('3366ff')">
              <area shape="rect" coords="72,336,84,348" href="javascript:viewNewColor('0066ff')">
              <area shape="rect" coords="84,336,96,348" href="javascript:viewNewColor('0066cc')">
              <area shape="rect" coords="96,336,108,348" href="javascript:viewNewColor('006699')">
              <area shape="rect" coords="108,336,120,372" href="javascript:viewNewColor('006666')">
              <area shape="rect" coords="48,348,60,360" href="javascript:viewNewColor('6699ff')">
              <area shape="rect" coords="60,348,72,360" href="javascript:viewNewColor('3399ff')">
              <area shape="rect" coords="72,348,84,360" href="javascript:viewNewColor('0099ff')">
              <area shape="rect" coords="84,348,96,360" href="javascript:viewNewColor('0099cc')">
              <area shape="rect" coords="96,348,108,372" href="javascript:viewNewColor('009999')">
              <area shape="rect" coords="36,360,48,372" href="javascript:viewNewColor('99ccff')">
              <area shape="rect" coords="48,360,60,372" href="javascript:viewNewColor('66ccff')">
              <area shape="rect" coords="60,360,72,372" href="javascript:viewNewColor('33ccff')">
              <area shape="rect" coords="72,360,84,372" href="javascript:viewNewColor('00ccff')">
              <area shape="rect" coords="84,360,96,372" href="javascript:viewNewColor('00cccc')">
              <area shape="rect" coords="24,372,36,384" href="javascript:viewNewColor('ccffff')">
              <area shape="rect" coords="36,372,48,384" href="javascript:viewNewColor('99ffff')">
              <area shape="rect" coords="48,372,60,384" href="javascript:viewNewColor('66ffff')">
              <area shape="rect" coords="60,372,72,384" href="javascript:viewNewColor('33ffff')">
              <area shape="rect" coords="72,372,84,384" href="javascript:viewNewColor('00ffff')">
              <area shape="rect" coords="84,372,96,384" href="javascript:viewNewColor('00cccc')">
              <area shape="rect" coords="96,372,108,384" href="javascript:viewNewColor('009999')">
              <area shape="rect" coords="108,372,120,384" href="javascript:viewNewColor('006666')">
              <area shape="rect" coords="120,372,132,384" href="javascript:viewNewColor('003333')">
              <area shape="rect" coords="12,384,36,396" href="javascript:viewNewColor('ffffff')">
              <area shape="rect" coords="36,384,60,396" href="javascript:viewNewColor('cccccc')">
              <area shape="rect" coords="60,384,84,396" href="javascript:viewNewColor('999999')">
              <area shape="rect" coords="84,384,108,396" href="javascript:viewNewColor('666666')">
              <area shape="rect" coords="108,384,132,396" href="javascript:viewNewColor('333333')">
              <area shape="rect" coords="12,12,24,396" alt="White" href="javascript:viewNewColor('ffffff')">
              <area shape="rect" coords="0,0,190,408" alt="Black" href="javascript:viewNewColor('000000')">
              </map>
              </td>
              </tr>
              </table>
              </td>
              </tr>
              </table>

              <%-- table formating in rows for View and individual select colors --%>
              </td>
            </tr>
          </table>
          <%-- end two column formatting table --%>
        </td>
      </tr>
    </table>

    <%-- Footer --%>
    <%@ include file="footer.jsp" %>

  </body>
</html>
