<?xml version='1.0'?>

<!--xsl:stylesheet xmlns:xsl='http://www.w3.org/XSL/Transform/1.0'-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="baseActionURL">index.jsp</xsl:param>
<xsl:param name="locale">sv_SE</xsl:param>

<xsl:template match="profile">
<html>
<body>

<p align="center">Vad vill du ändra ??</p>
<form method="post">
<xsl:attribute name="action"><xsl:value-of select="$baseActionURL"/></xsl:attribute>
<input type="hidden" name="action" value="submitEditChoice"/>
<p align="center">
<select name="manageTarget">
<option selected="" value="layout">Hantera layout</option>
<option value="gpref">Hantera globala inställningar</option>
<option value="prof">profiler</option>
</select>
<br/>
<input type="submit" name="submit" value="Proceed"/>
</p>




</form>
</body>
</html>
</xsl:template>
</xsl:stylesheet>
