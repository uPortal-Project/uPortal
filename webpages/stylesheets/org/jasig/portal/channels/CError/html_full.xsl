<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="baseActionURL">default</xsl:param>
<xsl:param name="allowRefresh">true</xsl:param>
<xsl:param name="allowReinstantiation">true</xsl:param>
<xsl:param name="showStackTrace">false</xsl:param>


<xsl:template match="error">
<html>
<h2>Error Report</h2>
<xsl:apply-templates select="channel"/>
<!-- analyze error codes, give user-friendly reports (i.e. hexadecimal code value :) -->


<table cellpadding="1"><tr><td><b>uPortal message</b></td><td><xsl:value-of select="message"/></td></tr>
<tr><td><b>Error type</b></td><td>
<xsl:choose>
<xsl:when test="@code='4'">channel timed out (code 4)</xsl:when>
<xsl:when test="@code='1'">channel failed to render (code 1)</xsl:when>
<xsl:when test="@code='2'">channel failed to initialize (code 2)</xsl:when>
<xsl:when test="@code='3'">channel failed to accept runtime data (code 3)</xsl:when>
<xsl:when test="@code='0'">general error (code 0)</xsl:when>
<xsl:when test="@code='5'">channel failed to accept PCS (code 5)</xsl:when>
<xsl:when test="@code='-1'">uPortal error (code -1)</xsl:when>
</xsl:choose>
</td> </tr>
<xsl:apply-templates select="exception"/>
</table>

<form action="{$baseActionURL}" method="post">       
<input type="hidden" name="action" value="channelFate"/>
<xsl:if test="$allowRefresh='true'">
 <input type="submit" name="channel_fate" value="retry"/>
</xsl:if>
<xsl:if test="$allowReinstantiation='true'">
 <input type="submit" name="channel_fate" value="restart channel"/>
</xsl:if>
<xsl:if test="exception">
<xsl:choose>
<xsl:when test="$showStackTrace='true'">
<input type="submit" name="toggle_stack_trace" value="hide stack trace"/>
</xsl:when>
<xsl:otherwise>
<input type="submit" name="toggle_stack_trace" value="show stack trace"/>
</xsl:otherwise>
</xsl:choose>
</xsl:if>
</form>
 </html>
</xsl:template>


<xsl:template match="channel">
<b>An error has occurred in a channel named "<xsl:value-of select="name"/>" (channelID=<xsl:value-of select="id"/>).</b>
</xsl:template>

<xsl:template match="exception">
<tr><td><b>Problem type</b></td><td>
<xsl:choose>
<xsl:when test="@code='-1'">runtime exception (code -1)</xsl:when>
<xsl:when test="@code='0'">general rendering problem (code 0)</xsl:when>
<xsl:when test="@code='1'">internal timeout (code 1)</xsl:when>
<xsl:when test="@code='2'">authorization problem (code 2)</xsl:when>
<xsl:when test="@code='3'">missing resource (code 3)</xsl:when>
</xsl:choose>
</td></tr>
<xsl:if test="@code='1'"><tr><td>Timeout limit</td><td><xsl:value-of select="timeout/@value"/></td></tr></xsl:if>
<xsl:if test="@code='3'"><tr><td>Resource description</td><td><xsl:value-of select="resource/description"/></td></tr>
<tr><td>Resource URI</td><td><xsl:value-of select="resource/uri"/></td></tr></xsl:if>
<tr><td><b>Error message</b></td><td><xsl:value-of select="message"/></td></tr>
<xsl:if test="$showStackTrace='true'"><tr><td><b>Stack trace</b></td><td><xsl:value-of select="stack"/></td></tr></xsl:if>
</xsl:template>

</xsl:stylesheet>
