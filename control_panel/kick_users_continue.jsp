<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.*" %>


<%
    // verify all the form variables are correct
    String creationTime = request.getParameter("creationTime");
    String lastAccessedTime = request.getParameter("lastAccessedTime");
    String inactiveTime = request.getParameter("inactiveTime");
    String sessionType = request.getParameter("sessionType");
    String kickType = request.getParameter("kickType");
    String resetTime = request.getParameter("resetTime");

    // pick a 200 second time as the default
    if(resetTime != null && resetTime.length() <= 0) {
        resetTime="200";
    }
    
    // a : indicates that this criteria was not specified    
    char creationTimeCriteria = ':';
    char lastAccessedTimeCriteria = ':';
    char inactiveTimeCriteria = ':';
    
    

    if(creationTime != null && creationTime.length() > 0)
        creationTimeCriteria=creationTime.charAt(0);
    
    if(lastAccessedTime != null && lastAccessedTime.length() > 0)
        lastAccessedTimeCriteria=lastAccessedTime.charAt(0);
    
    if(inactiveTime != null && inactiveTime.length() > 0)
        inactiveTimeCriteria=inactiveTime.charAt(0);
    
%>

<html>
  <head>
    <title>Kick Users:  <%=request.getServerName() %>:<%=request.getServerPort()%></title>
  </head>

  
  <body bgcolor="#FFFFFF">
    <p>The criteria you selected:</p>

    <ul>

<% if (creationTimeCriteria != ':') { %>
      <li>Creation time <%= creationTime %> minutes ago.</li>
<% } %>

<% if (lastAccessedTimeCriteria != ':') { %>  
      <li>Last accessed time <%= lastAccessedTime %> minutes ago.</li>
<% } %>
      
      <li>Sessions of type <%= sessionType %>.</li>
 
<% if (inactiveTimeCriteria != ':') { %>
      <li>Inactivity time is <%= inactiveTime %> seconds.</li>
<% } %>
 
   </ul>

<% if ("reset".equals(kickType)) { %>
    <p>And resetting inactivity timeout to <%=resetTime%> will remove:</p>
<% } else { %>
    <p>And invalidating user sessions will remove:</p>
<% } %>

    <h3>204 users</h3>

    <p>At the end of the inactivity timeout.</p>

    <p style="font-size: 80%">NOTE: This number will fluctuate
    slightly if you continue since the action has not been
    committed, and the number of users affected could have
    changed since this estimate was obtained.</p>

    <form action="kick_users_completed.jsp">
      <input type="submit" value="CONTINUE!"> <input type=
      "submit" value="CANCEL">
    </form>
  </body>
</html>


