<!-- Header -->
<table border=0 cellpadding=0 cellspacing=1 width=100%>
  <tr>
    <td width=10%><img src="images/MyIBS.gif" width=100 height=50 border=0></td>
    <td width=20% align=left><font face=Arial size=2 color=blue><%= session.getAttribute ("headerTitle") != null ? session.getAttribute ("headerTitle") : "" %></font></td>
    <td width=70%>&nbsp;</td>
  </tr>
  <tr bgcolor="#000000">
    <td colspan=3><font face=Arial size=1 color=ffffff>&nbsp;Example Header</font></td>
  </tr>
</table>