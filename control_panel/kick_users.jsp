<!DOCTYPE html PUBLIC "-//W3C//DTD html 4.0 transitional//EN">
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.*" %>


<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

    <title>Kick Users: <%=request.getServerName() %>:<%=request.getServerPort()%></title>
  </head>

  <body bgcolor="#FFFFFF">
<% 
    // don't show anything if there are no users to invalidate
    Enumeration sessionIds = SessionManager.getSessionTypes();
    int sessionCount = 0;
    while(sessionIds.hasMoreElements())
        sessionCount += SessionManager.getSessionCount((String)sessionIds.nextElement());
    
    if(sessionCount == 0) {
    
%>
 
        <p>There are no users to kick off.  Wait until someone logs in.
        <p><a href="index.jsp">Click here to continue.</a></p>

<% } else { %>

    <p>&nbsp;</p>

    <table width="100%">
      <tr>
        <td>Control Panel: Kick Users</td>

        <td><a href="index.jsp">[Stats]</a>&nbsp; <a
        href="kick_guests.jsp">[Kick Guests]</a> </td>
      </tr>
    </table>
    <br>
     

    <p>&nbsp;</p>

    <form method="GET" action="kick_users_continue.jsp">
      <table border="0">
        <tr>
          <td bgcolor="#999999"><b>Indicate the criteria used to for determining which
          users to kick:</b></td>

          <td align="left">
          </td>
        </tr>

        <tr>
          <td>
          Creation time of user (<>): <input name="creationTime" size="3">
          </td>

        </tr>
        <tr>
          <td>
          Last accessed time (<>): <input name="lastAccessedTime" size="3">
          </td>
        </tr>
        
        <tr>
            <td>
            Inactivity time set to (<>) seconds: <input name="inactiveTime" size="7">
            </td>
        </tr>
        <tr>
        
          <td>
          Sessions of type:
          <select name="sessionType">
<%
    // list out all the different session types currently available
    Enumeration sessionTypes = SessionManager.getSessionTypes();
    while(sessionTypes.hasMoreElements()) {
        out.println("<option>" + (String)sessionTypes.nextElement() + "</option>");
    }
%>
            
            </select>
          </td>

        </tr>
        <tr>
          <td bgcolor="#999999">
          <b>Choose the method used to invalidate the users:</b>
          </td>
        </tr>

        <tr>
        <td>
        <input type="radio" name="kickType" value="invalidate"> Invalidate sessions immediately (DANGEROUS!)<br>
        <input type="radio" name="kickType" value="reset" selected> Reset inactive interval to (sec): 
        <input size="3" name="resetTime">
        </td>
        </tr>
        
        <tr>
          <td><input type="submit" value="Kick Users"></td>

          <td>
          </td>
        </tr>

        <tr>
          <td>
          </td>

          <td>
          </td>
        </tr>
      </table>
    </form>
<% } %>

  </body>
</html>


