<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.*" %>

<%
    KickCriteria criteria = new KickCriteria(request);
    
    session.setAttribute("criteria", criteria);
    
    // do a test kick to see how many people would be affected
    Set sessionTypes = SessionManager.getSessionTypes();
    Iterator stI = sessionTypes.iterator();
    long kickCount = 0;
    
    // go through all session types
    while(stI.hasNext()) {
        String sessionType = (String)stI.next();
        
        Collection sessionIds = SessionManager.getSessionIDs(sessionType);
        Iterator i = sessionIds.iterator();
        
        // go through all session IDs and check to see if they match
        while(i.hasNext()) {
            String id =(String) i.next();
            HttpSession deadSession = SessionManager.getSession(id);
            if(criteria.matches(deadSession)) {
                kickCount++;
            }
        }
    }
%>

<html>
  <head>
    <title>Kick Users:  <%=request.getServerName() %>:<%=request.getServerPort()%></title>
  </head>

  
  <body bgcolor="#FFFFFF">
  <h3>Your session has: </h3>
    <ul>
    <li>Inactivity Timeout: <%= session.getMaxInactiveInterval() %>
    <li>Last Accessed Time: <%= new Date(session.getLastAccessedTime()) %>
    <li>Creation Time: <%= new Date(session.getCreationTime()) %>
    </ul>
    
    <h3>The criteria you selected was:</h3>

    <ul>

<% if (criteria.getCreationTimeCriteria() != KickCriteria.NOT_SET) { %>
    <li>Creation time <%= criteria.interpretCriteria(criteria.getCreationTimeCriteria(), true) %>  
    <%= criteria.getCreationTime() %> minutes ago.</li>
<% } %>

<% if (criteria.getLastAccessedTimeCriteria() != KickCriteria.NOT_SET) { %>  
      <li>Last accessed time <%= criteria.interpretCriteria(criteria.getLastAccessedTimeCriteria(), true) %>
      <%= criteria.getLastAccessedTime() %> minutes ago.</li>
<% } %>
 
<% if (criteria.getInactiveTimeCriteria() != KickCriteria.NOT_SET) { %>
      <li>Inactivity time is  <%= criteria.interpretCriteria(criteria.getInactiveTimeCriteria(), true) %>
      <%= criteria.getInactiveTime() %> seconds.</li>
<% } %>
 
 <li>Sessions of type <%= criteria.getSessionType() %>.</li>
 
   </ul>

   <p>And kicking users with a <%= KickCriteria.interpretKickType(criteria.getKickType()) %>
   will invalidate:
   
    <h3><%= kickCount %> users.</h3>

    <% if(criteria.getKickType() == KickCriteria.RESET_KICK) { %>
        <p>At the end of the inactivity timeout.</p>
    <% } else if(criteria.getKickType() == KickCriteria.INVALIDATE_KICK) { %>
        <p>Immediately after pressing the CONTINUE! button below.
    <% } %>

    <p style="font-size: 80%">NOTE: This number will fluctuate
    slightly if you continue since the action has not been
    committed, and the number of users affected could have
    changed since this estimate was obtained.</p>

    <form action="kick_users_completed.jsp">
      <input type="submit" name="button" value="CONTINUE!"> <input type=
      "submit" name="button" value="CANCEL">
    </form>
  </body>
</html>


