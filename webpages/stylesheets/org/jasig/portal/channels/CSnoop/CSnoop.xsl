<?xml version='1.0' encoding='utf-8' ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output indent="yes"/>

  <xsl:template match="/">
    <table width="100%" cellspacing="0" cellpadding="2" border="0">
      <tr><td colspan="2" class="uportal-background-med">
  	<span class="uportal-channel-table-caption">Request Info</span></td></tr>
      <xsl:apply-templates select="request-info"/>
      <tr><td colspan="2" class="uportal-background-med">
  	<span class="uportal-channel-table-caption">HTTP Header Info</span></td></tr>
      <xsl:apply-templates select="request-info/headers"/>   
    </table>
  </xsl:template>

  <xsl:template match="request-info">
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Request Protocol: </p></td>
      <td class="uportal-channel-table-row-odd"><xsl:value-of select="request-protocol"/></td>
    </tr>  
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Request Method: </p></td>
      <td width="100%" class="uportal-channel-table-row-odd"><xsl:value-of select="request-method"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Server Name: </p></td>
      <td class="uportal-channel-table-row-even"><xsl:value-of select="server-name"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Server Port: </p></td>
      <td class="uportal-channel-table-row-odd"><xsl:value-of select="server-port"/></td>
    </tr>    
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Request URI: </p></td>
      <td class="uportal-channel-table-row-even"><xsl:value-of select="request-uri"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Context Path: </p></td>
      <td class="uportal-channel-table-row-even"><xsl:value-of select="context-path"/></td>
    </tr>        
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Servlet Path: </p></td>
      <td class="uportal-channel-table-row-even"><xsl:value-of select="servlet-path"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Query String: </p></td>
      <td class="uportal-channel-table-row-odd"><xsl:value-of select="query-string"/></td>
    </tr>    
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Path Info: </p></td>
      <td class="uportal-channel-table-row-odd"><xsl:value-of select="path-info"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Path Translated: </p></td>
      <td class="uportal-channel-table-row-even"><xsl:value-of select="path-translated"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Content Length: </p></td>
      <td class="uportal-channel-table-row-even"><xsl:value-of select="content-length"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Content Type: </p></td>
      <td class="uportal-channel-table-row-odd"><xsl:value-of select="content-type"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Remote User: </p></td>
      <td class="uportal-channel-table-row-even"><xsl:value-of select="remote-user"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Remote Address: </p></td>
      <td class="uportal-channel-table-row-odd"><xsl:value-of select="remote-address"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Remote Host: </p></td>
      <td class="uportal-channel-table-row-even"><xsl:value-of select="remote-host"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Authorization Scheme: </p></td>
      <td class="uportal-channel-table-row-odd"><xsl:value-of select="authorization-scheme"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Local: </p></td>
      <td class="uportal-channel-table-row-even"><xsl:value-of select="locale"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="headers">
    <xsl:apply-templates select="header"/>
  </xsl:template>

  <xsl:template match="header">
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
        <!-- The choose block here fixes a spelling error in the HTTP headers.
             'Referer' should be changed to 'Referrer' -->
        <p class="uportal-channel-table-row-even">
        <xsl:choose>
          <xsl:when test="@name = 'Referer'">
            Referrer: 
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@name"/>: 
          </xsl:otherwise>
        </xsl:choose>
        </p>
      </td>
      <td>
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 0">uportal-channel-table-row-even</xsl:when>
            <xsl:otherwise>uportal-channel-table-row-odd</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <xsl:value-of select="."/>
      </td>
    </tr>
  </xsl:template> 

</xsl:stylesheet>
