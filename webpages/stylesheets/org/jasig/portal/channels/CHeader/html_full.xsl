<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="baseActionURL">default</xsl:param>
<xsl:param name="guest">false</xsl:param>

<xsl:template match="header">
  <table border="0">
    <tr class="uportal-background-med">
      <td nowrap="nowrap" class="uportal-channel-text">
        Welcome <xsl:value-of select="full-name"/>!<br/>
        <xsl:choose>
        <xsl:when test="$guest='false'">
          You are currently logged in.
        </xsl:when>
        <xsl:otherwise>
          Please log in.
        </xsl:otherwise>        
        </xsl:choose>
      </td>
    </tr>
    <tr class="uportal-background-light">
      <td nowrap="nowrap" class="uportal-channel-text">
        <xsl:value-of select="timestamp-long"/>
      </td>
    </tr>
    <xsl:if test="$guest='false'">
      <tr><td nowrap="nowrap" class="uportal-channel-text"><a href="logout.jsp">Logout</a></td></tr>
    </xsl:if>
  </table>
</xsl:template>


</xsl:stylesheet>
