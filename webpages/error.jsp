<%@ page isErrorPage="true" %>
<html>
<head>
<title>Portal: An error has occured</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<table border="0" cellspacing="0" cellpadding="0" bgcolor="#FFFFFF" width="100%">
    <tr> 
      <td>&nbsp;&nbsp;</td> 
      <td width="100%">

<%if (exception != null){ %>
<b><font size="4" face="Arial, Helvetica, sans-serif">
This a general exception handling page for the portal application.<BR>
</font></b>
<UL>
<font size="2" face="Verdana, Arial, Helvetica, sans-serif">
<LI><%=exception.toString()%>
</font>
</UL>
<pre>
<%
  // Change true to false to disable the stack trace
  
  if (true)
  {
    java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream ();
    PrintWriter ps = new PrintWriter (bo);
    exception.printStackTrace(ps);
    ps.close ();
    out.print (bo.toString ());
  }
%>
</pre>
<%}%>

</td>
    </tr>
</table>
</body>
</html>