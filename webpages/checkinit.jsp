<%-- If the portal base directory hasn't been set, forward back to
  -- index.jsp, which retrives it from the ServletContext.  The portal
  -- base directory is an application-wide parameter set in web.xml.
  --%>
<% if (org.jasig.portal.GenericPortalBean.getPortalBaseDir() == null) { %>
<jsp:forward page="index.jsp"/>
<% } %>


<%-- If the session has timed out or is otherwise lost, forward the user
  -- back to layout.jsp.
  --%>
<% if (session.isNew()) { %>
<jsp:forward page="layout.jsp"/>
<% } %>

