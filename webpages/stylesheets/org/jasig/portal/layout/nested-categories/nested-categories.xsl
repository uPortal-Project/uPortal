<?xml version="1.0" encoding='utf-8'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="userLayoutRoot">root</xsl:param>

<!-- implement document fragment template here-->
<xsl:template match="layout_fragment">
  <layout_fragment>
    <content><xsl:apply-templates/></content>
  </layout_fragment>    
</xsl:template>

<xsl:template match="layout">
 <xsl:for-each select="folder">
  <layout>
    <header>
      <xsl:for-each select="folder[@type='header']">
	      <xsl:copy-of select=".//channel"/>
      </xsl:for-each>
    </header>

    <content>
      <xsl:choose>
        <xsl:when test="starts-with($userLayoutRoot, 'chan')">
          <xsl:apply-templates select="//channel[@ID=$userLayoutRoot]"/>
        </xsl:when>
        <xsl:when test="$userLayoutRoot='root'">
          <xsl:apply-templates select="folder[@type='regular']"/>
          <xsl:apply-templates select="channel"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="//folder[@ID=$userLayoutRoot]/folder[@type='regular']"/>
          <xsl:apply-templates select="//folder[@ID=$userLayoutRoot]/channel"/>
       </xsl:otherwise>    
      </xsl:choose>
      <!--xsl:apply-templates select="folder"/-->
    </content>

    <footer>
      <xsl:for-each select="folder[@type='footer']">
	      <xsl:copy-of select=".//channel"/>
      </xsl:for-each>
    </footer>
    
   </layout>    
 </xsl:for-each>
</xsl:template>


<xsl:template match="folder">
  <xsl:if test="not(@type='header')">
    <category ID="{@ID}" name="{@name}" priority="{@priority}"/>
  </xsl:if>
</xsl:template>

<xsl:template match="channel">
  <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="parameter">
  <xsl:copy-of select="."/>
</xsl:template>

</xsl:stylesheet>
