<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:param name="baseActionURL">baseActionURL</xsl:param>
  <xsl:param name="downloadWorkerURL">downloadWorker</xsl:param>
  <xsl:param name="locale">de_DE</xsl:param>

  <xsl:template match="/">
    <p class="uportal-text">
      uPortal-Channels haben Zugang zu den Benutzerattributen
      über den <span class="uportal-channel-code">org.jasig.portal.security.IPerson</span> Objekt.
      Attributnamen sind definiert in der 
      <a href="http://www.educause.edu/eduperson/">eduPerson Objekt-Klasse</a> version 1.0.
    </p>
    <p>
      uPortal implementors müssen die Standardattributnamen abbilden nach ihren
      lokalen Namen in ihrem Personverzeichnis oder Datenbank.  Abbildungen sind enthalten in
      der <span class="uportal-channel-code">properties/PersonDirs.xml</span> Datei.
    </p>
    <xsl:apply-templates select="attributes"/>
  </xsl:template>

  <xsl:template match="attributes">
    <table border="0" cellpadding="2" cellspacing="3">
      <tr class="uportal-background-med">
        <th>Att. Name</th>
        <th>Att. Wert</th>
      </tr>
      <tr class="uportal-background-light">
        <td colspan="2">Vorhandene Attribute:</td>
      </tr>
      <xsl:apply-templates select="attribute"/>
    </table>
  </xsl:template>

  <xsl:template match="attribute">
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

</xsl:stylesheet> 
