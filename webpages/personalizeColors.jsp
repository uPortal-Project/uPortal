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

<html>
<head>
<title>Personalize Colors</title>
<META HTTP-EQUIV="expires" CONTENT="Tue, 20 Aug 1996 14:25:27 GMT">
<% 

String fComponent = (String)request.getParameter("Component");
if (fComponent == null) fComponent = "BGColor";

String fColorScheme = (String)request.getParameter("ColorScheme");
if (fColorScheme == null) fColorScheme = "University";


String fColorPalette = (String)request.getParameter("ColorPalette");
if (fColorPalette == null) fColorPalette = "browsersafe";

String omyBGColor = layoutBean.getBackgroundColor (request, response, out);
String fBGColor = (String)request.getParameter("BGColor");
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


<STYLE>


BODY { color: <%=myTextColor%>;

	 background: <%=myBGColor%>}

	 A:link    { color: <%=myActiveTabColor%>}

       A:visited { color: <%=myTabColor%>}

       A:active  { color: <%=myChannelColor%>}

       .MiniPortalTitleText { text-decoration:none; color: <%=fTextColor%>;

                              font-weight:bold; font-family:arial,helvetica,times,courier;

                              font-size:14pt}

       .MiniPortalText      { text-decoration:none; color: <%=fTextColor%>;

                              font-weight:plain; font-family:arial,helvetica,times,courier;

                              font-size:12pt}

       .PortalTitleText     { text-decoration:none; color: <%=fTextColor%>;

                              font-weight:bold; font-family:arial,helvetica,times,courier;

                              font-size:14pt}

       .PortalText          { text-decoration:none; color: <%=fTextColor%>;

                              font-weight:plain; font-family:arial,helvetica,times,courier;

                              font-size:12pt}
</STYLE>
<SCRIPT LANGUAGE="Javascript">

<!--

function viewNewColor(whatColor) {

  whatIndex = document.ComponentsForm.PortalComponent.options.selectedIndex

  whatComponent = document.ComponentsForm.PortalComponent.options[whatIndex].value

  maxComponents = document.ComponentsForm.PortalComponent.options.length

  showColor = "personalizeColors.jsp?Component=" + whatComponent + "&ColorScheme=<%=fColorScheme%>&ColorPalette=<%=fColorPalette%>"

  showColor = showColor + "&" + whatComponent + "=" + whatColor

  if (whatComponent != "BGColor") showColor = showColor + "&BGColor=<%=fBGColor%>"

  if (whatComponent != "TextColor") showColor = showColor + "&TextColor=<%=fTextColor%>"

  if (whatComponent != "ActiveTabColor") showColor = showColor + "&ActiveTabColor=<%=fActiveTabColor%>"

  if (whatComponent != "TabColor") showColor = showColor + "&TabColor=<%=fTabColor%>"

  if (whatComponent != "ChannelColor") showColor = showColor + "&ChannelColor=<%=fChannelColor%>"

  window.location = showColor

  return true

}

-->

</SCRIPT>
</head>
<BODY BGCOLOR=<%=myBGColor%> TEXT=<%=myTextColor%> LINK=<%=myActiveTabColor%> VLINK=<%=myTabColor%> ALINK=<%=myChannelColor%>>
<%-- <% layoutBean.writeBodyStyle (request, response, out); %> --%>

<%-- Header --%>
<% session.setAttribute ("headerTitle", "Personalize Colors"); %>
<%@ include file="header.jsp" %>

<%-- begin one column formatting table for mini-portal --%>
<TABLE><TR><TD VALIGN=TOP>



<TABLE>

<TR><TD>

View your color changes on the mini portal below:</TD><TR>

<TR><TD>

<TABLE BORDER=1 BGCOLOR="<%=fBGColor%>">

<TR><TD>

<TABLE BORDER=0 cellspacing=0 cellpadding=0>

<TR>

  <TD BGCOLOR="<%=fActiveTabColor%>"><SPAN CLASS="MiniPortalTitleText"><CENTER>Active Tab</CENTER></SPAN></TD>

  <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>

  <TD BGCOLOR="<%=fTabColor%>"><SPAN CLASS="MiniPortalTitleText"><CENTER>Tab 2</CENTER></SPAN></TD>

  <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>

  <TD BGCOLOR="<%=fTabColor%>"><SPAN CLASS="MiniPortalTitleText"><CENTER>Tab 3</CENTER></SPAN></TD>

</TR>

<TR><TD COLSPAN=5 BGCOLOR="<%=fActiveTabColor%>"><IMG SRC="images/dot_clear.gif" VSPACE=2></TD></TR>



<TR><TD COLSPAN=5><IMG SRC="images/dot_clear.gif" VSPACE=6></TD></TR>

<TR><TD BGCOLOR="<%=fChannelColor%>"><SPAN CLASS="MiniPortalTitleText">Plain Text</SPAN></TD>

    <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>

    <TD BGCOLOR="<%=fChannelColor%>"><SPAN CLASS="MiniPortalTitleText">All Capitals</SPAN></TD>

    <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>

    <TD BGCOLOR="<%=fChannelColor%>"><SPAN CLASS="MiniPortalTitleText">Numbers</SPAN></TD>

</TR>

<TR>
   <TD BGCOLOR=FFFFFF><IMG SRC="images/dot_clear.gif" VSPACE=6></TD>
   <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>
   <TD BGCOLOR=FFFFFF><IMG SRC="images/dot_clear.gif" VSPACE=6></TD>
   <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>
   <TD BGCOLOR=FFFFFF><IMG SRC="images/dot_clear.gif" VSPACE=6></TD>
</TR>

<TR><TD BGCOLOR=FFFFFF><SPAN CLASS="MiniPortalText">a b c d e f g h i j k l m n o p q r s t u v w x y z<IMG SRC="images/dot_clear.gif" HSPACE=2></TD>

    <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>

    <TD BGCOLOR=FFFFFF><SPAN CLASS="MiniPortalText">A B C D E F G H I J K L M N O P Q R S T U V W X Y Z</TD>

    <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>

    <TD BGCOLOR=FFFFFF><SPAN CLASS="MiniPortalText">1 2 3 4 5 6 7 8 9 0 ! @  $ % ^ & * ( )</TD>

</TR>

<TR>
   <TD BGCOLOR=FFFFFF><IMG SRC="images/dot_clear.gif" VSPACE=6></TD>
   <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>
   <TD BGCOLOR=FFFFFF><IMG SRC="images/dot_clear.gif" VSPACE=6></TD>
   <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>
   <TD BGCOLOR=FFFFFF><IMG SRC="images/dot_clear.gif" VSPACE=6></TD>
</TR>

<TR><TD BGCOLOR=FFFFFF><SPAN CLASS="MiniPortalText">a b c d e f g h i j k l m n o p q r s t u v w x y z </SPAN></TD>

    <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>

    <TD BGCOLOR=FFFFFF><SPAN CLASS="MiniPortalText">A B C D E F G H I J K L M N O P Q R S T U V W X Y Z </SPAN></TD>

    <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>

    <TD BGCOLOR=FFFFFF><SPAN CLASS="MiniPortalText">1 2 3 4 5 6 7 8 9 0 ! @  $ % ^ & * ( ) </SPAN></TD>

</TR>

<TR>
   <TD BGCOLOR=FFFFFF><IMG SRC="images/dot_clear.gif" VSPACE=6></TD>
   <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>
   <TD BGCOLOR=FFFFFF><IMG SRC="images/dot_clear.gif" VSPACE=6></TD>
   <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>
   <TD BGCOLOR=FFFFFF><IMG SRC="images/dot_clear.gif" VSPACE=6></TD>
</TR>

<TR><TD BGCOLOR=FFFFFF><A HREF="personalizeColors.jsp"><FONT COLOR="<%=fActiveTabColor%>"><B>Note your Link color is assigned to Active Tab color</B></FONT></A></TD>

    <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>

    <TD BGCOLOR=FFFFFF><A HREF="personalizeColors.jsp"><FONT COLOR="<%=fTabColor%>"><B>Your visited link color is assigned to the non-active tab color</B></FONT></A></TD>

    <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>

    <TD BGCOLOR=FFFFFF><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>

</TR>

<TR>
   <TD BGCOLOR=FFFFFF><IMG SRC="images/dot_clear.gif" VSPACE=6></TD>
   <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>
   <TD BGCOLOR=FFFFFF><IMG SRC="images/dot_clear.gif" VSPACE=6></TD>
   <TD><IMG SRC="images/dot_clear.gif" HSPACE=2></TD>
   <TD BGCOLOR=FFFFFF><IMG SRC="images/dot_clear.gif" VSPACE=6></TD>
</TR>

<TR><TD COLSPAN=5><IMG SRC="images/dot_clear.gif" VSPACE=6></TD></TR>

<TR><TD COLSPAN=5><CENTER><SPAN CLASS="MiniPortalText">Save your new colors or return to your portal:</SPAN></CENTER></TD></TR>

<TR><TD COLSPAN=5><IMG SRC="images/dot_clear.gif" VSPACE=6></TD></TR>

<TR><TD COLSPAN=5><CENTER>

<form name=SaveReturn action="personalizeColors.jsp" method=post>

<input type=hidden name="bgColor" value="<%=fBGColor%>">

<input type=hidden name="fgColor" value="<%=fTextColor%>">

<input type=hidden name="tabColor" value="<%=fTabColor%>">

<input type=hidden name="activeTabColor" value="<%=fActiveTabColor%>">

<input type=hidden name="channelHeadingColor" value="<%=fChannelColor%>">

<input type=hidden name=action value="saveColors">

<input type=submit name=submit value="Save Colors">
</form></CENTER>

</TD></TR>

</TABLE>

</TD></TR>

</TABLE>

</TD></TR></TABLE><%-- end one column formatting table - mini-portal --%>


<%-- begin two column formatting table --%>

<TABLE><TR><TD VALIGN=TOP>

<TABLE BORDER=1><TR><TD>
<TABLE BORDER=0>
<TR><TD>Change by scheme:</TD></TR>
<TR><TD><IMG SRC="images/dot_clear.gif" VSPACE=6></TD></TR>
<TR><TD>
<FORM NAME="colorGroup">
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
</FORM>

</TD></TR>
<TR><TD>
<TABLE BORDER=1 BORDERCOLOR="<%=myTextColor%>" BORDERCOLORDARK="<%=myTextColor%>" BORDERCOLORLIGHT="<%=myTextColor%>">

<TR>

<TD>Apply</TD><TD><IMG SRC="images/dot_clear.gif">BG</TD><TD><IMG SRC="images/dot_clear.gif">TX</TD><TD><IMG SRC="images/dot_clear.gif">AT</TD><TD><IMG SRC="images/dot_clear.gif">OT</TD><TD><IMG SRC="images/dot_clear.gif">CH</TD>

</TR>

<FORM NAME=ColorScheme>

<TR><TD><CENTER><INPUT TYPE=RADIO NAME=COLORS onclick="javascript:window.location='personalizeColors.jsp?ColorScheme=University&BGColor=<%=myBGColor%>&TextColor=<%=myTextColor%>&ActiveTabColor=<%=myActiveTabColor%>&TabColor=<%=myTabColor%>&ChannelColor=<%=myChannelColor%>';">Revert to your colors</CENTER></TD><TD BGCOLOR="<%=myBGColor%>"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="<%=myTextColor%>"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="<%=myActiveTabColor%>"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="<%=myTabColor%>"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="<%=myChannelColor%>"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD></TR>
<%--
For testing without DB
<TR><TD><INPUT TYPE=RADIO NAME=COLORS onClick="javascript:window.location='personalizeColors.jsp?ColorScheme=University&BGColor=FFFFFF&TextColor=000000&ActiveTabColor=FF9900&TabColor=CC6633&ChannelColor=FFCC33';"></TD><TD BGCOLOR="FFFFFF"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="000000"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="FF9900"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="CC6633"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="FFCC33"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="FF9900"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD></TR><TR><TD><INPUT TYPE=RADIO NAME=COLORS onClick="javascript:window.location='personalizeColors.jsp?ColorScheme=University&BGColor=FFFFFF&TextColor=000000&ActiveTabColor=83A368&TabColor=99CCFF&ChannelColor=009900';"></TD><TD BGCOLOR="FFFFFF"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="000000"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="83A368"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="99CCFF"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="009900"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="83A368"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD></TR><TR><TD><INPUT TYPE=RADIO NAME=COLORS onClick="javascript:window.location='personalizeColors.jsp?ColorScheme=University&BGColor=FFFFFF&TextColor=000000&ActiveTabColor=009900&TabColor=336699&ChannelColor=83A368';"></TD><TD BGCOLOR="FFFFFF"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="000000"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="009900"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="336699"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="83A368"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="009900"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD></TR><TR><TD><INPUT TYPE=RADIO NAME=COLORS onClick="javascript:window.location='personalizeColors.jsp?ColorScheme=University&BGColor=EEEEEE&TextColor=000000&ActiveTabColor=83A368&TabColor=336699&ChannelColor=83A368';"></TD><TD BGCOLOR="EEEEEE"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="000000"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="83A368"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="336699"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="83A368"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="83A368"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD></TR><TR><TD><INPUT TYPE=RADIO NAME=COLORS onClick="javascript:window.location='personalizeColors.jsp?ColorScheme=University&BGColor=CC6633&TextColor=000000&ActiveTabColor=FF9900&TabColor=FFFFFF&ChannelColor=FFCC33';"></TD><TD BGCOLOR="CC6633"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="000000"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="FF9900"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="FFFFFF"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="FFCC33"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="FF9900"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD></TR><TR><TD><INPUT TYPE=RADIO NAME=COLORS onClick="javascript:window.location='personalizeColors.jsp?ColorScheme=University&BGColor=0044AA&TextColor=FFFFFF&ActiveTabColor=FFDD00&TabColor=FF6600&ChannelColor=FFDD00';"></TD><TD BGCOLOR="0044AA"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="FFFFFF"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="FFDD00"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="FF6600"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="FFDD00"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="FFDD00"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD></TR><TR><TD><INPUT TYPE=RADIO NAME=COLORS onClick="javascript:window.location='personalizeColors.jsp?ColorScheme=University&BGColor=000000&TextColor=FFFFFF&ActiveTabColor=FF9900&TabColor=CC6633&ChannelColor=FFCC33';"></TD><TD BGCOLOR="000000"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="FFFFFF"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="FF9900"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="CC6633"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="FFCC33"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD><TD BGCOLOR="FF9900"><IMG SRC="images/dot_clear.gif" VSPACE=12></TD></TR>
--%>
<%
  connection = null;
  String ColorSquares = "";
  String SeqID = null;
  String BGColor = null;
  String TextColor = null;
  String Link = null;
  String VLink = null;
  String ALink = null;

   try {
       connection = rdbmService.getConnection();
       Statement statement = connection.createStatement();
       String SQLString = "SELECT SEQ_ID, BGCOLOR, TEXT, LINK, VLINK, ALINK FROM PORTAL_COLORS WHERE COLOR_SCHEME_NAME='" + fColorScheme + "' ORDER BY BGCOLOR DESC, TEXT DESC, LINK DESC, VLINK DESC";
       Logger.log (Logger.DEBUG, SQLString);
       ResultSet rs = statement.executeQuery(SQLString);
       while(rs.next()) {
          SeqID     = rs.getString(1);
          BGColor   = rs.getString(2);
          TextColor = rs.getString(3);
          Link      = rs.getString(4);
          VLink     = rs.getString(5);
          ALink     = rs.getString(6);
          ColorSquares = ColorSquares + "<TR><TD><INPUT TYPE=RADIO NAME=COLORS onClick=\"javascript:window.location='personalizeColors.jsp?ColorScheme=" + fColorScheme + "&BGColor=" + BGColor + "&TextColor=" + TextColor + "&ActiveTabColor=" + Link + "&TabColor=" + VLink + "&ChannelColor=" + ALink + "';\">" + "</TD><TD BGCOLOR=\"" + BGColor + "\"><IMG SRC=\"images/dot_clear.gif\" VSPACE=12></TD><TD BGCOLOR=\"" + TextColor + "\"><IMG SRC=\"images/dot_clear.gif\" VSPACE=12></TD><TD BGCOLOR=\"" + Link + "\"><IMG SRC=\"images/dot_clear.gif\" VSPACE=12></TD><TD BGCOLOR=\"" + VLink + "\"><IMG SRC=\"images/dot_clear.gif\" VSPACE=12></TD><TD BGCOLOR=\"" + ALink + "\"><IMG SRC=\"images/dot_clear.gif\" VSPACE=12></TD></TR>";
       }
       statement.close();
       rdbmService.releaseConnection(connection);
   }
   catch (Exception e) {
      // Logger.log (Logger.ERROR, e);
   }

%>
<%=ColorSquares%>


</FORM>

</TABLE>

</TD></TR></TABLE>
</TD></TR></TABLE>



</TD><TD VALIGN=TOP>

<IMG SRC="images/dot_clear.gif" HSPACE=10>

</TD><TD VALIGN=TOP>

<TABLE BORDER=1><TR><TD>
<TABLE BORDER=0>
<TR><TD>Change Individual Colors:</TD></TR>
<TR><TD><IMG SRC="images/dot_clear.gif" VSPACE=6></TD></TR>
<TR><TD>
  <FORM NAME="ComponentsForm">
        <select name="PortalComponent">
<%
String IndivDrop="<option ";
if (fComponent.equals("BGColor")) IndivDrop = IndivDrop + "selected ";
IndivDrop = IndivDrop + "value=\"BGColor\">Background</option><option ";
if (fComponent.equals("TextColor")) IndivDrop = IndivDrop + "selected ";
IndivDrop = IndivDrop + "value=\"TextColor\">Text</option><option ";
if (fComponent.equals("ActiveTabColor")) IndivDrop = IndivDrop + "selected ";
IndivDrop = IndivDrop + "value=\"ActiveTabColor\">Active Tab</option><option ";
if (fComponent.equals("TabColor")) IndivDrop = IndivDrop + "selected ";
IndivDrop = IndivDrop + "value=\"TabColor\">Other Tabs</option><option ";
if (fComponent.equals("ChannelColor")) IndivDrop = IndivDrop + "selected ";
IndivDrop = IndivDrop + "value=\"ChannelColor\">Channel Heading</option>";
%>
<%=IndivDrop%>
        </select>

  </FORM>
</TD></TR>
<TR><TD>Click on a color below to change:</TD></TR>
<TR><TD><IMG SRC="images/dot_clear.gif" VSPACE=6></TD></TR>
<TR><TD HALIGN=LEFT>
<IMG SRC="images/<%=fColorPalette%>.gif" usemap="#ColorsMap" ismap BORDER=0>

     <map name="ColorsMap">

     <area shape="rect" coords="145,373,155,383" alt="Cyan" href="javascript:viewNewColor('00FFFF')">

     <area shape="rect" coords="145,313,155,323" alt="Blue" href="javascript:viewNewColor('0000FF')">

     <area shape="rect" coords="145,253,155,263" alt="Magenta" href="javascript:viewNewColor('FF00FF')">

     <area shape="rect" coords="145,193,155,203" alt="Red" href="javascript:viewNewColor('FF0000')">

     <area shape="rect" coords="145,133,155,143" alt="Yellow" href="javascript:viewNewColor('FFFF00')">

     <area shape="rect" coords="145,73,155,83" alt="Green" href="javascript:viewNewColor('00FF00')">

     <area shape="rect" coords="145,13,155,23" alt="Cyan" href="javascript:viewNewColor('00FFFF')">



     <area shape="rect" coords="24,12,36,24" href="javascript:viewNewColor('CCFFFF')">

     <area shape="rect" coords="36,12,48,24" href="javascript:viewNewColor('99FFFF')">

     <area shape="rect" coords="48,12,60,24" href="javascript:viewNewColor('66FFFF')">

     <area shape="rect" coords="60,12,72,24" href="javascript:viewNewColor('33FFFF')">

     <area shape="rect" coords="72,12,84,24" href="javascript:viewNewColor('00FFFF')">

     <area shape="rect" coords="84,12,96,36" href="javascript:viewNewColor('00CCCC')">

     <area shape="rect" coords="96,12,108,48" href="javascript:viewNewColor('009999')">

     <area shape="rect" coords="108,12,120,60" href="javascript:viewNewColor('006666')">

     <area shape="rect" coords="120,12,132,72" href="javascript:viewNewColor('003333')">



     <area shape="rect" coords="24,24,36,132" href="javascript:viewNewColor('CCFFCC')">

     <area shape="rect" coords="36,24,48,36" href="javascript:viewNewColor('99FFCC')">

     <area shape="rect" coords="48,24,60,36" href="javascript:viewNewColor('66FFCC')">

     <area shape="rect" coords="60,24,72,36" href="javascript:viewNewColor('33FFCC')">

     <area shape="rect" coords="72,24,84,36" href="javascript:viewNewColor('00FFCC')">



     <area shape="rect" coords="36,36,48,120" href="javascript:viewNewColor('99FF99')">

     <area shape="rect" coords="48,36,60,48" href="javascript:viewNewColor('66FF99')">

     <area shape="rect" coords="60,36,72,48" href="javascript:viewNewColor('33FF99')">

     <area shape="rect" coords="72,36,84,48" href="javascript:viewNewColor('00FF99')">

     <area shape="rect" coords="84,36,96,48" href="javascript:viewNewColor('00CC99')">



     <area shape="rect" coords="48,48,60,108" href="javascript:viewNewColor('66FF66')">

     <area shape="rect" coords="60,48,72,60" href="javascript:viewNewColor('33FF66')">

     <area shape="rect" coords="72,48,84,60" href="javascript:viewNewColor('00FF66')">

     <area shape="rect" coords="84,48,96,60" href="javascript:viewNewColor('00CC66')">

     <area shape="rect" coords="96,48,108,60" href="javascript:viewNewColor('009966')">



     <area shape="rect" coords="60,60,72,96" href="javascript:viewNewColor('33FF33')">

     <area shape="rect" coords="72,60,84,72" href="javascript:viewNewColor('00FF33')">

     <area shape="rect" coords="84,60,96,72" href="javascript:viewNewColor('00CC33')">

     <area shape="rect" coords="96,60,108,72" href="javascript:viewNewColor('009933')">

     <area shape="rect" coords="108,60,120,72" href="javascript:viewNewColor('006633')">



     <area shape="rect" coords="72,72,84,84" href="javascript:viewNewColor('00FF00')">

     <area shape="rect" coords="84,72,96,84" href="javascript:viewNewColor('009900')">

     <area shape="rect" coords="96,72,108,84" href="javascript:viewNewColor('00CC00')">

     <area shape="rect" coords="108,72,120,84" href="javascript:viewNewColor('009900')">

     <area shape="rect" coords="120,72,132,84" href="javascript:viewNewColor('003300')">



     <area shape="rect" coords="72,84,84,96" href="javascript:viewNewColor('33FF00')">

     <area shape="rect" coords="84,84,96,96" href="javascript:viewNewColor('339900')">

     <area shape="rect" coords="96,84,108,96" href="javascript:viewNewColor('33CC00')">

     <area shape="rect" coords="108,84,120,96" href="javascript:viewNewColor('339900')">

     <area shape="rect" coords="120,84,132,192" href="javascript:viewNewColor('333300')">



     <area shape="rect" coords="60,96,72,108" href="javascript:viewNewColor('66FF33')">

     <area shape="rect" coords="72,96,84,108" href="javascript:viewNewColor('66FF00')">

     <area shape="rect" coords="84,96,96,108" href="javascript:viewNewColor('66CC00')">

     <area shape="rect" coords="96,96,108,108" href="javascript:viewNewColor('669900')">

     <area shape="rect" coords="108,96,120,180" href="javascript:viewNewColor('666600')">



     <area shape="rect" coords="48,108,60,120" href="javascript:viewNewColor('99FF66')">

     <area shape="rect" coords="60,108,72,120" href="javascript:viewNewColor('99FF33')">

     <area shape="rect" coords="72,108,84,120" href="javascript:viewNewColor('99FF00')">

     <area shape="rect" coords="84,108,96,120" href="javascript:viewNewColor('99CC00')">

     <area shape="rect" coords="96,108,108,168" href="javascript:viewNewColor('999900')">



     <area shape="rect" coords="36,120,48,132" href="javascript:viewNewColor('CCFF99')">

     <area shape="rect" coords="48,120,60,132" href="javascript:viewNewColor('CCFF66')">

     <area shape="rect" coords="60,120,72,132" href="javascript:viewNewColor('CCFF33')">

     <area shape="rect" coords="72,120,84,132" href="javascript:viewNewColor('CCFF00')">

     <area shape="rect" coords="84,120,96,156" href="javascript:viewNewColor('CCCC00')">



     <area shape="rect" coords="24,132,36,144" href="javascript:viewNewColor('FFFFCC')">

     <area shape="rect" coords="36,132,48,144" href="javascript:viewNewColor('FFFF99')">

     <area shape="rect" coords="48,132,60,144" href="javascript:viewNewColor('FFFF66')">

     <area shape="rect" coords="60,132,72,144" href="javascript:viewNewColor('FFFF33')">

     <area shape="rect" coords="72,132,84,144" href="javascript:viewNewColor('FFFF00')">



     <area shape="rect" coords="24,144,36,252" href="javascript:viewNewColor('FFCCCC')">

     <area shape="rect" coords="36,144,48,156" href="javascript:viewNewColor('FFCC99')">

     <area shape="rect" coords="48,144,60,156" href="javascript:viewNewColor('FFCC66')">

     <area shape="rect" coords="60,144,72,156" href="javascript:viewNewColor('FFCC33')">

     <area shape="rect" coords="72,144,84,156" href="javascript:viewNewColor('FFCC00')">



     <area shape="rect" coords="36,156,48,240" href="javascript:viewNewColor('FF9999')">

     <area shape="rect" coords="48,156,60,168" href="javascript:viewNewColor('FF9966')">

     <area shape="rect" coords="60,156,72,168" href="javascript:viewNewColor('FF9933')">

     <area shape="rect" coords="72,156,84,168" href="javascript:viewNewColor('FF9900')">

     <area shape="rect" coords="84,156,96,168" href="javascript:viewNewColor('CC9900')">



     <area shape="rect" coords="48,168,60,228" href="javascript:viewNewColor('FF6666')">

     <area shape="rect" coords="60,168,72,180" href="javascript:viewNewColor('33FF6633')">

     <area shape="rect" coords="72,168,84,180" href="javascript:viewNewColor('FF6600')">

     <area shape="rect" coords="84,168,96,180" href="javascript:viewNewColor('CC6600)">

     <area shape="rect" coords="96,168,108,180" href="javascript:viewNewColor('996600">



     <area shape="rect" coords="60,180,72,216" href="javascript:viewNewColor('FF3333')">

     <area shape="rect" coords="72,180,84,192" href="javascript:viewNewColor('FF3300')">

     <area shape="rect" coords="84,180,96,192" href="javascript:viewNewColor('CC3300')">

     <area shape="rect" coords="96,180,108,192" href="javascript:viewNewColor('993300')">

     <area shape="rect" coords="108,180,120,192" href="javascript:viewNewColor('663300')">



     <area shape="rect" coords="72,192,84,204" href="javascript:viewNewColor('FF0000')">

     <area shape="rect" coords="84,192,96,204" href="javascript:viewNewColor('CC0000')">

     <area shape="rect" coords="96,192,108,204" href="javascript:viewNewColor('990000')">

     <area shape="rect" coords="108,192,120,204" href="javascript:viewNewColor('660000')">

     <area shape="rect" coords="120,192,132,204" href="javascript:viewNewColor('330000')">



     <area shape="rect" coords="72,204,84,216" href="javascript:viewNewColor('FF0033')">

     <area shape="rect" coords="84,204,96,216" href="javascript:viewNewColor('CC0033')">

     <area shape="rect" coords="96,204,108,216" href="javascript:viewNewColor('990033')">

     <area shape="rect" coords="108,204,120,216" href="javascript:viewNewColor('660033')">

     <area shape="rect" coords="120,204,132,336" href="javascript:viewNewColor('330033')">



     <area shape="rect" coords="60,216,72,228" href="javascript:viewNewColor('FF3333')">

     <area shape="rect" coords="72,216,84,228" href="javascript:viewNewColor('FF0066')">

     <area shape="rect" coords="84,216,96,228" href="javascript:viewNewColor('CC0066')">

     <area shape="rect" coords="96,216,108,228" href="javascript:viewNewColor('990066')">

     <area shape="rect" coords="108,216,132,312" href="javascript:viewNewColor('660066')">



     <area shape="rect" coords="48,228,60,240" href="javascript:viewNewColor('FF6699')">

     <area shape="rect" coords="60,228,72,240" href="javascript:viewNewColor('FF3399')">

     <area shape="rect" coords="72,228,84,240" href="javascript:viewNewColor('FF0099')">

     <area shape="rect" coords="84,228,96,240" href="javascript:viewNewColor('CC0099')">

     <area shape="rect" coords="96,228,108,288" href="javascript:viewNewColor('990099')">



     <area shape="rect" coords="36,240,48,252" href="javascript:viewNewColor('FF99CC')">

     <area shape="rect" coords="48,240,60,252" href="javascript:viewNewColor('FF66CC')">

     <area shape="rect" coords="60,240,72,252" href="javascript:viewNewColor('FF33CC')">

     <area shape="rect" coords="72,240,84,252" href="javascript:viewNewColor('FF00CC')">

     <area shape="rect" coords="84,240,96,276" href="javascript:viewNewColor('CC00CC')">



     <area shape="rect" coords="24,252,36,264" href="javascript:viewNewColor('FFCCFF')">

     <area shape="rect" coords="36,252,48,264" href="javascript:viewNewColor('FF99FF')">

     <area shape="rect" coords="48,252,60,264" href="javascript:viewNewColor('FF66FF')">

     <area shape="rect" coords="60,252,72,264" href="javascript:viewNewColor('FF33FF')">

     <area shape="rect" coords="72,252,84,264" href="javascript:viewNewColor('FF00FF')">



     <area shape="rect" coords="24,264,36,372" href="javascript:viewNewColor('CCCCFF')">

     <area shape="rect" coords="36,264,48,276" href="javascript:viewNewColor('CC99FF')">

     <area shape="rect" coords="48,264,60,276" href="javascript:viewNewColor('CC66FF')">

     <area shape="rect" coords="60,264,72,276" href="javascript:viewNewColor('CC33FF')">

     <area shape="rect" coords="72,264,84,276" href="javascript:viewNewColor('CC00FF')">



     <area shape="rect" coords="36,276,48,360" href="javascript:viewNewColor('9999FF')">

     <area shape="rect" coords="48,276,60,288" href="javascript:viewNewColor('9966FF')">

     <area shape="rect" coords="60,276,72,288" href="javascript:viewNewColor('9933FF')">

     <area shape="rect" coords="72,276,84,288" href="javascript:viewNewColor('9900FF')">

     <area shape="rect" coords="84,276,96,360" href="javascript:viewNewColor('9900CC')">



     <area shape="rect" coords="48,288,60,348" href="javascript:viewNewColor('6666FF')">

     <area shape="rect" coords="60,288,72,300" href="javascript:viewNewColor('6633FF')">

     <area shape="rect" coords="72,288,84,300" href="javascript:viewNewColor('6600FF')">

     <area shape="rect" coords="84,288,96,300" href="javascript:viewNewColor('6600CC')">

     <area shape="rect" coords="96,288,108,348" href="javascript:viewNewColor('660099')">



     <area shape="rect" coords="60,300,72,336" href="javascript:viewNewColor('3333FF')">

     <area shape="rect" coords="72,300,84,312" href="javascript:viewNewColor('3300FF')">

     <area shape="rect" coords="84,300,96,312" href="javascript:viewNewColor('3300CC')">

     <area shape="rect" coords="96,300,108,312" href="javascript:viewNewColor('330099')">

     <area shape="rect" coords="108,300,120,312" href="javascript:viewNewColor('330066')">



     <area shape="rect" coords="72,312,84,324" href="javascript:viewNewColor('0000FF')">

     <area shape="rect" coords="84,312,96,324" href="javascript:viewNewColor('000099')">

     <area shape="rect" coords="96,312,108,324" href="javascript:viewNewColor('0000CC')">

     <area shape="rect" coords="108,312,120,324" href="javascript:viewNewColor('000099')">

     <area shape="rect" coords="120,312,132,324" href="javascript:viewNewColor('000033')">



     <area shape="rect" coords="72,324,84,336" href="javascript:viewNewColor('0066FF')">

     <area shape="rect" coords="84,324,96,336" href="javascript:viewNewColor('0033CC')">

     <area shape="rect" coords="96,324,108,336" href="javascript:viewNewColor('003399')">

     <area shape="rect" coords="108,324,120,336" href="javascript:viewNewColor('003366')">

     <area shape="rect" coords="120,324,132,372" href="javascript:viewNewColor('003333')">



     <area shape="rect" coords="60,336,72,348" href="javascript:viewNewColor('3366FF')">

     <area shape="rect" coords="72,336,84,348" href="javascript:viewNewColor('0066FF')">

     <area shape="rect" coords="84,336,96,348" href="javascript:viewNewColor('0066CC')">

     <area shape="rect" coords="96,336,108,348" href="javascript:viewNewColor('006699')">

     <area shape="rect" coords="108,336,120,372" href="javascript:viewNewColor('006666')">



     <area shape="rect" coords="48,348,60,360" href="javascript:viewNewColor('6699FF')">

     <area shape="rect" coords="60,348,72,360" href="javascript:viewNewColor('3399FF')">

     <area shape="rect" coords="72,348,84,360" href="javascript:viewNewColor('0099FF')">

     <area shape="rect" coords="84,348,96,360" href="javascript:viewNewColor('0099CC')">

     <area shape="rect" coords="96,348,108,372" href="javascript:viewNewColor('009999')">



     <area shape="rect" coords="36,360,48,372" href="javascript:viewNewColor('99CCFF')">

     <area shape="rect" coords="48,360,60,372" href="javascript:viewNewColor('66CCFF')">

     <area shape="rect" coords="60,360,72,372" href="javascript:viewNewColor('33CCFF')">

     <area shape="rect" coords="72,360,84,372" href="javascript:viewNewColor('00CCFF')">

     <area shape="rect" coords="84,360,96,372" href="javascript:viewNewColor('00CCCC')">



     <area shape="rect" coords="24,372,36,384" href="javascript:viewNewColor('CCFFFF')">

     <area shape="rect" coords="36,372,48,384" href="javascript:viewNewColor('99FFFF')">

     <area shape="rect" coords="48,372,60,384" href="javascript:viewNewColor('66FFFF')">

     <area shape="rect" coords="60,372,72,384" href="javascript:viewNewColor('33FFFF')">

     <area shape="rect" coords="72,372,84,384" href="javascript:viewNewColor('00FFFF')">

     <area shape="rect" coords="84,372,96,384" href="javascript:viewNewColor('00CCCC')">

     <area shape="rect" coords="96,372,108,384" href="javascript:viewNewColor('009999')">

     <area shape="rect" coords="108,372,120,384" href="javascript:viewNewColor('006666')">

     <area shape="rect" coords="120,372,132,384" href="javascript:viewNewColor('003333')">



     <area shape="rect" coords="12,384,36,396" href="javascript:viewNewColor('FFFFFF')">

     <area shape="rect" coords="36,384,60,396" href="javascript:viewNewColor('CCCCCC')">

     <area shape="rect" coords="60,384,84,396" href="javascript:viewNewColor('999999')">

     <area shape="rect" coords="84,384,108,396" href="javascript:viewNewColor('666666')">

     <area shape="rect" coords="108,384,132,396" href="javascript:viewNewColor('333333')">



     <area shape="rect" coords="12,12,24,396" alt="White" href="javascript:viewNewColor('FFFFFF')">

     <area shape="rect" coords="0,0,190,408" alt="Black" href="javascript:viewNewColor('000000')">

     </map>

</TD></TR>
</TABLE>

</TD></TR>
</TABLE>



<%-- Table formating in rows for View and individual select colors --%>

</TD></TR>

</TABLE>



<%-- end two column formatting table --%>

</TD></TR>

</TABLE>



<%-- Footer --%>
<%@ include file="footer.jsp" %>

</body>
</html>
