<?xml version='1.0' encoding='utf-8' ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output indent="yes"/>
  <xsl:param name="locale">de_DE</xsl:param>

  <xsl:template match="/">
    <xsl:apply-templates select="snooper"/>
  </xsl:template>
  
  <xsl:template match="snooper">
    <table width="100%" cellspacing="0" cellpadding="2" border="0">
      <tr>
        <td colspan="2" class="uportal-background-med">
  	      <span class="uportal-channel-table-caption">HTTP Antragsinfo</span>
        </td>
      </tr>
      <xsl:apply-templates select="request-info"/>
      <tr>
        <td colspan="2" class="uportal-background-med">
  	      <span class="uportal-channel-table-caption">HTTP Überschrift-Info</span>
        </td>
      </tr>
      <xsl:apply-templates select="request-info/headers"/>   
      <tr>
        <td colspan="2" class="uportal-background-med">
  	      <span class="uportal-channel-table-caption">Channel Runtime Data Info</span>
        </td>
      </tr>
      <xsl:apply-templates select="channel-runtime-data"/>   
    </table>
  </xsl:template>

  <xsl:template match="request-info">
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Antrags-Protokoll: </p></td>
      <td class="uportal-channel-table-row-odd"><xsl:value-of select="request-protocol"/></td>
    </tr>  
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Antrags-Methode: </p></td>
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
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Antrag URI: </p></td>
      <td class="uportal-channel-table-row-even"><xsl:value-of select="request-uri"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Kontext Pfad: </p></td>
      <td class="uportal-channel-table-row-even"><xsl:value-of select="context-path"/></td>
    </tr>        
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Servlet Pfad: </p></td>
      <td class="uportal-channel-table-row-even"><xsl:value-of select="servlet-path"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Query String: </p></td>
      <td class="uportal-channel-table-row-odd"><xsl:value-of select="query-string"/></td>
    </tr>    
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Pfad Info: </p></td>
      <td class="uportal-channel-table-row-odd"><xsl:value-of select="path-info"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Übersetzer Pfad: </p></td>
      <td class="uportal-channel-table-row-even"><xsl:value-of select="path-translated"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Inhaltslänge: </p></td>
      <td class="uportal-channel-table-row-even"><xsl:value-of select="content-length"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Typ des Inhalts: </p></td>
      <td class="uportal-channel-table-row-odd"><xsl:value-of select="content-type"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Remotebenutzer: </p></td>
      <td class="uportal-channel-table-row-even"><xsl:value-of select="remote-user"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Remoteadresse: </p></td>
      <td class="uportal-channel-table-row-odd"><xsl:value-of select="remote-address"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Remote Host: </p></td>
      <td class="uportal-channel-table-row-even"><xsl:value-of select="remote-host"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Authorizationsentwurf: </p></td>
      <td class="uportal-channel-table-row-odd"><xsl:value-of select="authorization-scheme"/></td>
    </tr>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Lokal: </p></td>
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

  <xsl:template match="channel-runtime-data">
    <xsl:apply-templates select="locales"/>
  </xsl:template>
  
  <xsl:template match="locales">
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light"><p class="uportal-channel-table-row-even">Lokals: </p></td>
      <td class="uportal-channel-table-row-odd">
        <table border="0" cellspacing="1" cellpadding="1">
          <tr>
            <td class="uportal-channel-table-row-odd"><u>Name</u></td>
            <td class="uportal-channel-table-row-odd"><u>Code</u></td>
            <td class="uportal-channel-table-row-odd"><u>Language</u></td>
            <td class="uportal-channel-table-row-odd"><u>Country</u></td>
            <td class="uportal-channel-table-row-odd"><u>Variant</u></td>
          </tr>
          <xsl:apply-templates select="locale"/>   
        </table>
      </td>
    </tr>  
  </xsl:template>  
  
  <xsl:template match="locale">
    <tr>
      <td class="uportal-channel-table-row-odd" nowrap="nowrap"><xsl:value-of select="@displayName"/></td>
      <td class="uportal-channel-table-row-odd" nowrap="nowrap"><xsl:value-of select="@code"/></td>
      <td class="uportal-channel-table-row-odd" nowrap="nowrap"><xsl:value-of select="language/@displayName"/> (<xsl:value-of select="language/@iso2"/>, <xsl:value-of select="language/@iso3"/>)</td>
      <td class="uportal-channel-table-row-odd" nowrap="nowrap"><xsl:value-of select="country/@displayName"/> (<xsl:value-of select="country/@iso2"/>, <xsl:value-of select="country/@iso3"/>)</td>
      <td class="uportal-channel-table-row-odd" nowrap="nowrap"><xsl:value-of select="variant/@displayName"/> (<xsl:value-of select="variant/@code"/>)</td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
