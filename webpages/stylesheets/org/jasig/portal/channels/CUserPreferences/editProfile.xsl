<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="baseActionURL">default</xsl:param>

<xsl:template match="profile">
<html>
<p align="center">Edit profile</p>


<form method="post" action="{$baseActionURL}">
<input type="hidden" name="action" value="setProfileNameAndDescription"/>
<table>
<tr>
<td>Profile Name</td>
<td><input type="text" name="name"><xsl:attribute name="value">
<xsl:value-of select="name"/></xsl:attribute></input></td>
<td></td></tr><tr>
<td>Description</td>
<td><input type="text" name="description"><xsl:attribute name="value"><xsl:value-of select="description"/></xsl:attribute></input></td>
<td>
<input type="submit" name="submit" value="Change"/>
</td>
</tr>
</table>   
</form>

<form method="post" action="{$baseActionURL}">
<input type="hidden" name="action" value="setMimeType"/>
<select name="mimeType">
<xsl:apply-templates select="mimetypes"/>
</select>
<input type="submit" name="submit" value="Change"/>
</form>


<form method="post" action="{$baseActionURL}">
<input type="hidden" name="action" value="setStructureStylesheet"/>
<select name="structureStylesheet">
<xsl:apply-templates select="structurestylesheets"/>
</select>
<input type="submit" name="submit" value="Change"/>
</form> 


<form method="post" action="{$baseActionURL}">
<input type="hidden" name="action" value="setThemeStylesheet"/>
<select name="themeStylesheet">
<xsl:apply-templates select="themestylesheets"/>
</select>
<input type="submit" name="submit" value="Change"/>
</form> 

<form method="post" action="{$baseActionURL}">
<input type="hidden" name="action" value="completeEdit"/>
<input type="submit" name="submit" value="Save"/>
<input type="submit" name="submit" value="Cancel"/>
</form>


</html>
</xsl:template>


<xsl:template match="current">
<option selected=""><xsl:attribute name="value"><xsl:value-of select="name"/></xsl:attribute><xsl:value-of select="name"/></option>
</xsl:template>

<xsl:template match="alternate">
<option><xsl:attribute name="value"><xsl:value-of select="name"/></xsl:attribute><xsl:value-of select="name"/></option>
</xsl:template>


</xsl:stylesheet>
