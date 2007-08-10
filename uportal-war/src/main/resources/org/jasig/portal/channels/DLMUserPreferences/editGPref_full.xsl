<?xml version="1.0" encoding="UTF-8"?>
<!-- $Header$ --><!--xsl:stylesheet xmlns:xsl='http://www.w3.org/XSL/Transform/1.0'--><xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:param name="baseActionURL">index.jsp</xsl:param>
<xsl:param name="profileName">default profile</xsl:param>
<xsl:param name="locale">en_US</xsl:param>

<xsl:template match="gpref">
<html>
<body>

<!-- header table -->
<form action="{$baseActionURL}" method="post">
<xsl:attribute name="action"><xsl:value-of select="$baseActionURL"/></xsl:attribute>
<input name="action" type="hidden" value="submitEditChoice"/>
<table border="0" width="100%"><tr><td align="left"><xsl:value-of select="$PROFILE_NAME"/><xsl:value-of select="$profileName"/></td><td align="right">
<input name="submit" type="submit"><xsl:attribute name="value"><xsl:value-of select="$MANAGE"/></xsl:attribute></input>
<select name="userPreferencesAction">
<option selected=""><xsl:value-of select="$CHANNELS_AND_FOLDERS"/><xsl:attribute name="value"><xsl:value-of select="$LAYOUT"/></xsl:attribute></option>
<option value="gpref"><xsl:value-of select="$GLOBAL_PREFERENCES"/></option>
<option value="manageProfiles"><xsl:value-of select="$PROFILES"/></option>
</select>
</td>
</tr></table>
</form>
<!-- end of the header table -->

<p align="center"><xsl:value-of select="$EDITING_STYLESHEET_PARAMETERS"/></p>
<form method="post">
<xsl:attribute name="action"><xsl:value-of select="$baseActionURL"/></xsl:attribute>
<input name="action" type="hidden" value="submitEditValues"/>


<table align="center" border="1" cellpadding="5" cellspacing="0">
<tr><td><b><xsl:value-of select="$PARAMETER"/></b></td><td><b>value</b></td><td><b><xsl:value-of select="$DESCRIPTION"/></b></td></tr>

<!-- eventually this should check if there are any parameters at all of this class before drawing a table row-->
<!-- process structure stylesheet parameters-->
<tr><td align="right" colspan="3"><xsl:value-of select="$STRUCTURE_STYLESHEET_PARAMETERS"/></td></tr>
<xsl:for-each select="structureparameters/parameter">
<xsl:call-template name="processParameter"/>
</xsl:for-each>

<!-- process theme stylesheet parameters-->
<tr><td align="right" colspan="3"><xsl:value-of select="$THEME_STYLESHEET_PARAMETERS"/></td></tr>
<xsl:for-each select="themeparameters/parameter">
<xsl:call-template name="processParameter"/>
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

<xsl:template name="processParameter">
<tr><td><xsl:value-of select="name"/></td>
<td><input name="{name}" type="text"><xsl:attribute name="value"><xsl:value-of select="$VALUE"/></xsl:attribute></input></td>
<td><xsl:value-of><xsl:attribute name="select"><xsl:value-of select="$DESCRIPTION"/></xsl:attribute></xsl:value-of></td>
</tr>
</xsl:template>

</xsl:stylesheet>