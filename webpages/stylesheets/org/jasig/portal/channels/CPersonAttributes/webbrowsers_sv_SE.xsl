<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:param name="baseActionURL">baseActionURL</xsl:param>
  <xsl:param name="downloadWorkerURL">downloadWorker</xsl:param>
  <xsl:param name="locale">sv_SE</xsl:param>

  <xsl:template match="/">
    <p class="uportal-text">
      uPortal-kanaler har tillgång till användarens attribut
      genom <span class="uportal-channel-code">org.jasig.portal.security.IPerson</span>-objektet.
      Attributnamn definieras i 
      <a href="http://www.educause.edu/eduperson/">eduPerson objektklass</a> version 1.0
    </p>
    <p>
      De som implementerar uPortal knyter dessa standard attributnamn till
      lokala attributnamn i sina person-kataloger eller -databaser. Knytningarna finns i
      filen <span class="uportal-channel-code">properties/PersonDirs.xml</span>.
    </p>
    <xsl:apply-templates select="attributes"/>
  </xsl:template>

  <xsl:template match="attributes">
    <table border="0" cellpadding="2" cellspacing="3">
      <tr class="uportal-background-med">
        <th>Attr.namn</th>
        <th>Attr.värde</th>
      </tr>
      <tr class="uportal-background-light">
        <td colspan="2">Tillgängliga attribut</td>
      </tr>
      <xsl:apply-templates select="attribute" mode="defined"/>
      <tr class="uportal-background-light">
        <td colspan="2">Ej tillgängliga attribut</td>
      </tr>
      <xsl:apply-templates select="attribute" mode="undefined"/>
    </table>
  </xsl:template>

  <xsl:template match="attribute" mode="defined">
    <xsl:if test="value">
      <tr>
        <td><xsl:value-of select="name"/></td>
        <xsl:choose>
        <xsl:when test="name='jpegPhoto'">
            <td><img src="{$downloadWorkerURL}?attribute={name}" /></td>
        </xsl:when>
        <xsl:otherwise>
            <td><xsl:value-of select="value"/></td>
        </xsl:otherwise>
        </xsl:choose>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="attribute" mode="undefined">
    <xsl:if test="not(value)">
      <tr>
        <td><xsl:value-of select="name"/></td>
        <td>[Ej tillgänglig]</td>
      </tr>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet> 
