<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="baseActionURL">baseActionURL not set</xsl:param>
    
  <xsl:template match="/">
      <xsl:apply-templates select="layout"/>
  </xsl:template>
  
  <xsl:template match="layout">
      <xsl:apply-templates select="folder" mode="root"/>
  </xsl:template>
  
  
  <xsl:template match="folder" mode="root">
      <table align="center" border="0" cellpadding="0" cellspacing="15">
          <tr>
              <xsl:apply-templates select="folder[@type='regular' and @hidden='false']" mode="tab"/>
          </tr>
      </table>
  </xsl:template>
  
  <xsl:template match="folder" mode="tab">
      <td valign="top">
          <p class="uportal-label">Tab <xsl:value-of select="position()"/>: 
          <a href="{$baseActionURL}?uP_root=root&amp;uP_sparam=focusedTabID&amp;focusedTabID={@ID}"><xsl:value-of select="@name"/></a><br/></p>
          <xsl:apply-templates select="folder" mode="column"/>
      </td>
      <xsl:if test="position() &lt; last()">
          <td class="uportal-background-dark" width="1"><img width="1" src="media/org/jasig/portal/channels/CGenericXSLT/transparent.gif"/></td>
      </xsl:if>
  </xsl:template>
  
  <xsl:template match="folder" mode="column">
      <p class="uportal-label">&#160;&#160;&#160;&#160;&#160;Column <xsl:value-of select="position()"/></p>
      <ul>
        <xsl:apply-templates select="channel"/>
      </ul>
  </xsl:template>  
  
  <xsl:template match="channel">
      <li><a href="{$baseActionURL}?uP_root={@ID}"><xsl:value-of select="@name"/></a></li>
  </xsl:template>
    
</xsl:stylesheet>