<%-- forward to layout if this is the guest account
  -- include this in pages which guest should not access
  --%>
<% String checkGuestUserName = (String) session.getAttribute ("userName"); %>
<% if ( checkGuestUserName == null || checkGuestUserName == "guest" ) { %>
  <jsp:forward page="layout.jsp"/>
<% } %>
