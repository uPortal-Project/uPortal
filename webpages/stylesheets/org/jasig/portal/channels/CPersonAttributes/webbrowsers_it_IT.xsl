<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:param name="baseActionURL">baseActionURL</xsl:param>
  <xsl:param name="downloadWorkerURL">downloadWorker</xsl:param>
  <xsl:param name="locale">it_IT</xsl:param>

  <xsl:template match="/">
    <p class="uportal-text">
      I canali di uPortal hanno accesso agli attributi dell'utente
      attraverso l'oggetto <span class="uportal-channel-code">org.jasig.portal.security.IPerson</span>.
      I nomi degli attributi sono definiti nella 
      <a href="http://www.educause.edu/eduperson/">eduPerson object class</a> versione 1.0.
    </p>
    <p>
      i realizzatori di uPortal hanno associato i nomi standard degli attributi ai
      nomi locali nella directory locale (LDAP) o sul database degli utenti. Le associazioni sono contenute
      nel file <span class="uportal-channel-code">properties/PersonDirs.xml</span>.
    </p>
    <xsl:apply-templates select="attributes"/>
  </xsl:template>

  <xsl:template match="attributes">
    <table border="0" cellpadding="2" cellspacing="3">
      <tr class="uportal-background-med">
        <th>Nome Att.</th>
        <th>Valore Att.</th>
      </tr>
      <tr class="uportal-background-light">
        <td colspan="2">Attributi disponibili:</td>
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
