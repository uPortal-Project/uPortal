<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="/">

  <xsl:if test="login-status/failure">
    <span class="uportal-channel-error" align="center">The user name/password combination you entered is not recognized.  Please try again!</span>
  </xsl:if>
  
  <div align="center">
  <form action="authentication.jsp" method="post">
    <input type="hidden" name="action" value="login"/>
    <table border="0">
      <tr>
        <th class="uportal-channel-table-header">User name:</th>
        <td><input type="text" name="userName" value="demo" size="15"/></td>
      </tr>
      <tr>
        <th class="uportal-channel-table-header">Password:</th>
        <td><input type="password" name="password" value="demo" size="15"/></td>
      </tr>
    </table>
    <input type="submit" value="Submit"/>
  </form>
  </div>
</xsl:template>

</xsl:stylesheet>
