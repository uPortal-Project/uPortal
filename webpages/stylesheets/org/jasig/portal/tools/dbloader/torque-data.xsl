<!-- Stylesheet used to create the uPortal (2.x) data xml file for Torque 
     (input file: data.xml) -->

<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:portal="org.jasig.uPortal.tools.dbloader.TorqueStylesheetUtils"
                version="1.0"
		exclude-result-prefixes="portal">

<xsl:output method="xml" 
            indent="yes" />

<xsl:template match="data">
  <dataset name="portal">
    <xsl:apply-templates />
  </dataset>
</xsl:template>

<xsl:template match="table">
  <xsl:for-each select="rows/row">
    <xsl:element name="{portal:getName(../../name)}">
      <xsl:for-each select="column">
        <xsl:attribute name="{portal:getName(name)}">
	  <xsl:value-of select="value" />
	</xsl:attribute>
      </xsl:for-each>
    </xsl:element>
  </xsl:for-each>
</xsl:template>

</xsl:stylesheet>
