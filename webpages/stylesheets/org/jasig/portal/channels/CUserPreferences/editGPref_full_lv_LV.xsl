<?xml version='1.0'?>

<!--xsl:stylesheet xmlns:xsl='http://www.w3.org/XSL/Transform/1.0'-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="baseActionURL">index.jsp</xsl:param>
<xsl:param name="profileName">default profile</xsl:param>
<xsl:param name="locale">lv_LV</xsl:param>

<xsl:template match="gpref">
<html>
<body>

<!-- header table -->
<form action="{$baseActionURL}" method="post">
<xsl:attribute name="action"><xsl:value-of select="$baseActionURL"/></xsl:attribute>
<input type="hidden" name="action" value="submitEditChoice"/>
<table width="100%" border="0"><tr><td align="left">profila nosaukums: <xsl:value-of select="$profileName"/></td><td align="right">
<input type="submit" name="submit" value="manage"/>
<select name="userPreferencesAction">
<option selected="" value="layout">kan\u0101li un mapes</option>
<option value="gpref">glob\u0101lie iestat\u012Bjumi</option>
<option value="manageProfiles">profili</option>
</select>
</td>
</tr></table>
</form>
<!-- end of the header table -->

<p align="center">Stilu lapas parametru redi\u0123\u0113šana:</p>
<form method="post">
<xsl:attribute name="action"><xsl:value-of select="$baseActionURL"/></xsl:attribute>
<input type="hidden" name="action" value="submitEditValues"/>


<table border="1" cellpadding="5" cellspacing="0" align="center">
<tr><td><b>parameter</b></td><td><b>value</b></td><td><b>apraksts</b></td></tr>

<!-- eventually this should check if there are any parameters at all of this class before drawing a table row-->
<!-- process strukt\u016Bras stilu lapas parametri-->
<tr><td colspan="3" align="right">strukt\u016Bras stilu lapas parametri</td></tr>
<xsl:for-each select="structureparameters/parameter">
<xsl:call-template name="processParameter"/>
</xsl:for-each>

<!-- process t\u0113mas stila lapas parametri-->
<tr><td colspan="3" align="right">t\u0113mas stila lapas parametri</td></tr>
<xsl:for-each select="themeparameters/parameter">
<xsl:call-template name="processParameter"/>
</xsl:for-each>
</table>

<p align="center">
<input type="submit" name="submit" value="Saglab\u0101t"/>
<input type="submit" name="submit" value="Atcelt"/>
</p>
</form>
</body>
</html>
</xsl:template>

<xsl:template name="processParameter">
<tr><td><xsl:value-of select="name"/></td>
<td><input type="text" name="{name}" value="{value}"/></td>
<td><xsl:value-of select="description"/></td>
</tr>
</xsl:template>

</xsl:stylesheet>
