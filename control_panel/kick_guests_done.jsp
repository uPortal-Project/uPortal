<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 3.2//EN">
<%@ page import="java.util.*" %>
<%@ page import="org.jasig.portal.*" %>

<html>
  <head>
    <title>Kick Guests: <%=request.getServerName() %>:<%=request.getServerPort()%></title>
  </head>
  <body bgcolor="#FFFFFF">
  
<%
    // this code is probably not going to work once Sun finally removes
    // the SessionContext from the API
       
    String requiredYes = (String)request.getParameter("requiredYes");
    if(!"YES".equals(requiredYes)) {
        // looks like this user can't read instructions
%>
    <p>You must type the word "YES" exactly perfect before you can delete all guest users.
    This done as a safety feature to protect from accidentally submitting the request.  When
    you type "YES" it must be in all capital letters and without the quotes.
    
 
    
    <p><a href="kick_guests.jsp">Click here to try again.</a>

<% } else { 
    	// delete all the guest users then
    	// get the user sessionIDs so we can NOT invalidate them
    	Set sessionTypes = SessionManager.getSessionTypes();
    	HashSet userIdSet = new HashSet();
    	userIdSet.add(sessionTypes);
    
    // go through all the sessionIds that the SessionContext has
    HttpSessionContext ctx = session.getSessionContext();
    Enumeration sessionIds = ctx.getIds();
    while(sessionIds.hasMoreElements()) {
        String currentId = (String)sessionIds.nextElement();
        if(! userIdSet.contains(currentId)) {
            // it's not a user, so zap 'em
            HttpSession curSession = ctx.getSession(currentId);
            curSession.invalidate();
        }
    }
%>
  

    <p>All guest users have now been invalidated. This will only
    affect a small percentage of users, but it will affect some.
    Be prepared for a few angry phone calls if there are a lot of
    guest users. <a href="index.jsp">[Continue]</a></p>

<% }%>
  
    
    
</body>
</html>




