<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.*" %>


<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 3.2//EN">
<html>
  <head>
    <title>Kick Users: <%=request.getServerName() %>:<%=request.getServerPort()%></title>
  </head>
  
  <body bgcolor="#FFFFFF">
  
  <% 
  KickCriteria criteria = (KickCriteria)session.getAttribute("criteria");
  
  if(!"CONTINUE!".equals(request.getParameter("button"))) { %>
  
  <p>Operation canceled.  <a href="index.jsp">[continue]</a>
  
  <% } else if(criteria == null) { %>
    <p>Problem processing request.  Please try again.
  
  <% } else { 
  // do the kick
    if(criteria.getKickType() == KickCriteria.RESET_KICK) {
        Logger.log(Logger.WARN, "Mass inactivity timeout reset to " + 
        criteria.getResetTime() + " being attempted by user with session ID " + 
        session.getId() + " from host " + request.getRemoteHost());
    } else {
        Logger.log(Logger.WARN, "Mass invalidation of users being attempted by user with session ID " +    
        session.getId() + " from host " + request.getRemoteHost());
    }
    
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
                if(criteria.getKickType() == KickCriteria.RESET_KICK) {
                    // reset them to the requested level
                    session.setMaxInactiveInterval((int)criteria.getResetTime());
                } else {
                    session.invalidate();
                }
            }
        }
    }

    if(criteria.getKickType() == criteria.RESET_KICK) {
        Logger.log(Logger.WARN, "Reset of " + kickCount + " users completed.");
    } else {
        Logger.log(Logger.WARN, "Invalidate of " + kickCount + " users completed.");
    }
  
  
  %>
    <p>A final total of:</p>

    <h3><%= kickCount %> users</h3>

    <p>Have had their inactivity timers set to <%= criteria.getResetTime() %> seconds and
    will be invalidated if they are inactive for longer than that
    time.</p>

    <p><a href="index.jsp">[Continue]</a></p>
    
 <% } %>
  </body>
</html>


