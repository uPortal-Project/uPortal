<!DOCTYPE html PUBLIC "-//w3c//dtd html 4.0 transitional//en">
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.*"%>

<%
    // This JSP is designed in the following manner:
    // All calculations are done at the top of the page in one block.  This lets
    // us move this block to another class more easily.  The calcuations are raw.
    // For example:  totalMemory is the exact memory returned not the adjusted memory.
    // This data is then modified later in the JSP page during the display portion.

    HttpSessionContext sessionContext = session.getSessionContext();
    
    int totalUserCount = 0;
    Set userTypes = SessionManager.getSessionTypes();
    Iterator itr = userTypes.iterator();
    while(itr.hasNext()) {
        totalUserCount += SessionManager.getSessionCount((String)itr.next());
    }
    
    int totalGuestCount = 0;
    Enumeration sessionIds = sessionContext.getIds();
    while(sessionIds.hasMoreElements()) {
        sessionIds.nextElement();  // do nothing with it since we are just counting
        totalGuestCount++;
    }
    // adjust the guest sessions according to what the user counts are
    totalGuestCount -= totalUserCount;
    // make sure that it ins't negative
    if(totalGuestCount < 0) totalGuestCount = 0;

    // memory calculations
    long freeMemory = Runtime.getRuntime().freeMemory();
    long totalMemory = Runtime.getRuntime().totalMemory();
    long usedMemory = totalMemory - freeMemory;
    long averageMemoryPerUser = usedMemory;
    if( (totalUserCount + totalGuestCount) != 0)
        averageMemoryPerUser = usedMemory / (totalUserCount + totalGuestCount);

    
        
%>

<html>
  <head>
    <title>Control Panel for <%=request.getServerName() %>:<%=request.getServerPort()%></title>
  </head>

  <body text="#000000" bgcolor="#FFFFFF" link="#0000EE" vlink=
  "#551A8B" alink="#FF0000">
    <h2>Host: <%=request.getServerName() %>:<%=request.getServerPort()%></h2> 

    <table border cols="2" width="100%">
      <tr>
        <td><strong style="font-size: 120%">Control
        Panel</strong></td>

        <td><a href="kick_users.jsp">[Kick Users]</a> &nbsp; <a
        href="kick_guests.jsp">[Kick Guests]</a> </td>
      </tr>
    </table>

    <table border cols="2">
      <tr>
        <td colspan="2" bgcolor="#CCCCCC">
          <div style="text-align: center">
            <strong>uPortal Stats</strong>
          </div>
        </td>
      </tr>

      <tr>
        <td>Active Users</td>

        <td><%= totalUserCount %> </td>
      </tr>

      <tr>
        <td>Active Guests</td>

        <td><%= totalGuestCount %></td>
      </tr>

      <tr>
        <td>Average Memory Used by each user</td>

        <td> <%= averageMemoryPerUser %></td>
      </tr>

      <tr>
        <td>Free Memory</td>

        <td><%= freeMemory %></td>
      </tr>

      <tr>
        <td>Used Memory</td>

        <td><%= usedMemory %></td>
      </tr>

      <tr>
        <td>Total Memory</td>

        <td><%= totalMemory %></td>
      </tr>
    </table>

    <table border cols="2">
      <tr>
        <td colspan="2" bgcolor="#CCCCCC">
          <div style="text-align: center">
            Session Manager Configuration
          </div>
        </td>
      </tr>

<%
    // dynamically determine the SessionManager's configuration
    Enumeration configNames = SessionManager.getConfigurationNames();
    while(configNames.hasMoreElements()) {
        out.println("<tr><td>");
        String configName = (String)configNames.nextElement();
        out.println(configName);
        out.println("</td><td>");
        out.println(SessionManager.getConfiguration(configName));
        out.println("</td></tr>");
    }
%>      
      
    </table>
  </body>
</html>


