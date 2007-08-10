<?xml version='1.0'?>

<!--xsl:stylesheet xmlns:xsl='http://www.w3.org/XSL/Transform/1.0'-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="baseActionURL">index.jsp</xsl:param>
<xsl:param name="locale">de_DE</xsl:param>

<xsl:template match="profile">
<html>
<body>

<p align="center">Was möchten Sie editieren ??</p>
<form method="post">
<xsl:attribute name="action"><xsl:value-of select="$baseActionURL"/></xsl:attribute>
<input type="hidden" name="action" value="submitEditChoice"/>
<p align="center">
<select name="manageTarget">
<option selected="" value="layout">Benutzerlayout verwalten</option>
<option value="gpref">Globale Präferenzen verwalten</option>
<option value="prof">Profile</option>
</select>
<br/>
<input type="submit" name="submit" value="Proceed"/>
</p>




</form>
</body>
</html>
</xsl:template>
</xsl:stylesheet>
