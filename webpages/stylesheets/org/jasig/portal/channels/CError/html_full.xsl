<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="baseActionURL">default</xsl:param>
<xsl:param name="allowRefresh">true</xsl:param>
<xsl:param name="allowReinstantiation">true</xsl:param>
<xsl:param name="showStackTrace">false</xsl:param>


<xsl:template match="error">

<xsl:apply-templates select="channel"/>

<table align="center" border="0" cellspacing="2" cellpadding="3" width="90%">
  <caption class="uportal-channel-table-caption">Error Report</caption>
  <xsl:if test="message">
  <tr>
    <th class="uportal-channel-table-header">Message</th>
    <td><xsl:value-of select="message"/></td>
  </tr>
  </xsl:if>
  <tr>
    <th class="uportal-channel-table-header">Error type</th>
    <td>
      <xsl:choose>
        <xsl:when test="@code='4'">Channel timed out (code 4)</xsl:when>
        <xsl:when test="@code='1'">Channel failed to render (code 1)</xsl:when>
        <xsl:when test="@code='2'">Channel failed to initialize (code 2)</xsl:when>
        <xsl:when test="@code='3'">Channel failed to accept runtime data (code 3)</xsl:when>
        <xsl:when test="@code='0'">General error (code 0)</xsl:when>
        <xsl:when test="@code='5'">Channel failed to accept PCS (code 5)</xsl:when>
        <xsl:when test="@code='-1'">uPortal error (code -1)</xsl:when>
      </xsl:choose>
    </td>
  </tr>
  <xsl:apply-templates select="exception"/>
</table>

<div align="center">
<form action="{$baseActionURL}" method="post">       
  <input type="hidden" name="action" value="channelFate"/>
  <xsl:if test="$allowRefresh='true'">
  <input type="submit" name="channel_fate" value="Retry"/>
  </xsl:if>
  <xsl:if test="$allowReinstantiation='true'">
  <input type="submit" name="channel_fate" value="Restart channel"/>
  </xsl:if>
  <xsl:if test="exception">
  <xsl:choose>
    <xsl:when test="$showStackTrace='true'">
      <input type="submit" name="toggle_stack_trace" value="Hide stack trace"/>
    </xsl:when>
    <xsl:otherwise>
      <input type="submit" name="toggle_stack_trace" value="Show stack trace"/>
    </xsl:otherwise>
  </xsl:choose>
  </xsl:if>
</form>
</div>

</xsl:template>


<xsl:template match="channel">
  <table align="center" width="100%"><tr><td bgcolor="#eeeeee" class="uportal-channel-warning">
    <xsl:value-of select="name"/> [ID=<xsl:value-of select="id"/>]<br/>
    is currently experiencing problems.
  </td></tr></table>
</xsl:template>

<xsl:template match="exception">
  <tr>
    <th class="uportal-channel-table-header">Problem type</th>
    <td>
      <xsl:choose>
        <xsl:when test="@code='-1'">runtime exception (code -1)</xsl:when>
        <xsl:when test="@code='0'">general rendering problem (code 0)</xsl:when>
        <xsl:when test="@code='1'">internal timeout (code 1)</xsl:when>
        <xsl:when test="@code='2'">authorization problem (code 2)</xsl:when>
        <xsl:when test="@code='3'">missing resource (code 3)</xsl:when>
      </xsl:choose>
    </td></tr>
  <xsl:if test="@code='1'">
  <tr>
    <th class="uportal-channel-table-header">Timeout limit</th>
    <td><xsl:value-of select="timeout/@value"/></td>
  </tr>
  </xsl:if>
  <xsl:if test="@code='3'">
  <tr>
    <th class="uportal-channel-table-header">Resource description</th>
    <td><xsl:value-of select="resource/description"/>&#160;</td>
  </tr>
  <tr>
    <th class="uportal-channel-table-header">Resource URI</th>
    <td><xsl:value-of select="resource/uri"/></td>
  </tr>
  </xsl:if>
  <tr>
    <th class="uportal-channel-table-header">Error message</th>
    <td><xsl:value-of select="message"/></td>
  </tr>
  <xsl:if test="$showStackTrace='true'">
  <tr>
    <th class="uportal-channel-table-header"><b>Stack trace</b></th>
    <td><xsl:value-of select="stack"/></td>
  </tr>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>
