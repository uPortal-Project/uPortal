<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:param name="baseActionURL">baseActionURL</xsl:param>
  <xsl:param name="downloadWorkerURL">downloadWorker</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>

  <xsl:template match="/">
    <p class="uportal-text">
      uPortal channels have access to user attributes
      via the <span class="uportal-channel-code">org.jasig.portal.security.IPerson</span> object.
      Attribute names are defined in the 
      <a href="http://www.educause.edu/eduperson/">eduPerson object class</a> version 1.0.
    </p>
    <p>
      uPortal implementors are to map these standard attribute names to
      local names in their person directory or database.  Mappings are contained in
      the <span class="uportal-channel-code">properties/PersonDirs.xml</span> file.
    </p>
    <xsl:apply-templates select="attributes"/>
  </xsl:template>

  <xsl:template match="attributes">
    <table border="0" cellpadding="2" cellspacing="3">
      <tr class="uportal-background-med">
        <th>Att. Name</th>
        <th>Att. Value</th>
      </tr>
      <tr class="uportal-background-light">
        <td colspan="2">Available attributes:</td>
      </tr>
      <xsl:apply-templates select="attribute"/>
    </table>
  </xsl:template>

  <xsl:template match="attribute">
    <xsl:for-each select="value">
      <tr>
        <td>
          <xsl:if test="position() = 1">
            <xsl:value-of select="../name"/>
          </xsl:if>
        </td>
        <xsl:choose>
        <xsl:when test="../name='jpegPhoto'">
            <td><img src="{$downloadWorkerURL}?attribute={../name}" /></td>
        </xsl:when>
        <xsl:otherwise>
            <td><xsl:value-of select="."/></td>
        </xsl:otherwise>
        </xsl:choose>
      </tr>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet> 
