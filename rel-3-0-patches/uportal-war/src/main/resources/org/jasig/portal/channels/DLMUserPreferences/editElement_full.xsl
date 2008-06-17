<?xml version="1.0" encoding="UTF-8"?>
<!-- $Header$ --><!--xsl:stylesheet xmlns:xsl='http://www.w3.org/XSL/Transform/1.0'--><xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:param name="baseActionURL">index.jsp</xsl:param>
<xsl:param name="locale">en_US</xsl:param>

<xsl:template match="editelement">
<html>
<body>

<p align="center"><xsl:value-of select="$EDITING"/><xsl:value-of select="type"/> "<xsl:value-of><xsl:attribute name="select"><xsl:value-of select="$NAME"/></xsl:attribute></xsl:value-of>":</p>
<form method="post">
<xsl:attribute name="action"><xsl:value-of select="$baseActionURL"/></xsl:attribute>
<input name="action" type="hidden" value="submitEditValues"/>


<table align="center" border="1" cellpadding="5" cellspacing="0">
<tr><td><b><xsl:value-of select="$VARIABLE"/></b></td><td><b>value</b></td><td><b><xsl:value-of select="$DESCRIPTION"/></b></td></tr>
<tr><td align="right" colspan="3"><xsl:value-of select="$INTRINSIC"/><xsl:value-of select="type"/><xsl:value-of select="$ATTRIBUTES"/></td></tr>
<tr><td><xsl:value-of select="type"/><xsl:value-of select="$NAME"/></td><td><input type="text" value="{name}"><xsl:attribute><xsl:value-of select="$NAME"/><xsl:attribute name="name"><xsl:value-of select="$NAME"/></xsl:attribute></xsl:attribute></input></td><td><xsl:value-of select="type"/><xsl:value-of select="$NAME"/></td></tr>

<!-- eventually this should check if there are any attributes at all of this class before drawing a table row-->
<!-- process structure stylesheet attributes-->
<tr><td align="right" colspan="3"><xsl:value-of select="$STRUCTURE_STYLESHEET_ATTRIBUTES"/></td></tr>
<xsl:for-each select="structureattributes/attribute">
<xsl:call-template name="processAttribute"/>
</xsl:for-each>

<!-- process theme stylesheet attributes-->
<tr><td align="right" colspan="3"><xsl:value-of select="$THEME_STYLESHEET_ATTRIBUTES"/></td></tr>
<xsl:for-each select="themeattributes/attribute">
<xsl:call-template name="processAttribute"/>
</xsl:for-each>
</table>

<p align="center">
<input name="submit" type="submit"><xsl:attribute name="value"><xsl:value-of select="$SAVE"/></xsl:attribute></input>
<input name="submit" type="submit"><xsl:attribute name="value"><xsl:value-of select="$CANCEL"/></xsl:attribute></input>
</p>

</form>
</body>
</html>
</xsl:template>

<xsl:template name="processAttribute">
<tr><td><xsl:value-of><xsl:attribute name="select"><xsl:value-of select="$NAME"/></xsl:attribute></xsl:value-of></td>
<td><input name="{name}" type="text" value="{value}"/></td>
<td><xsl:value-of><xsl:attribute name="select"><xsl:value-of select="$DESCRIPTION"/></xsl:attribute></xsl:value-of></td>
</tr>
</xsl:template>

</xsl:stylesheet>