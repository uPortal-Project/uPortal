<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="/">

  <xsl:if test="login-status/failure">
    <!-- please replace <font> tag with a <span> tag and error text css class !!! -->
    <font color="red">Invalid user name or password.  Please try again!</font>
  </xsl:if>
  
  <div align="center" class="PortalChannelText">
  <form action="authentication.jsp" method="post">
    <input type="hidden" name="action" value="login"/>
    <table border="0">
      <tr>
        <td>User name:</td>
        <td><input type="text" name="userName" value="demo" size="15"/></td>
      </tr>
      <tr>
        <td>Password:</td>
        <td><input type="password" name="password" value="demo" size="15"/></td>
      </tr>
    </table>
    <input type="submit" value="Submit"/>
  </form>
  </div>
</xsl:template>

</xsl:stylesheet>
