<!-- Stylesheet used to create the uPortal (2.x) database schema xml file 
     for Torque  (input file: tables.xml -->

<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                version="1.0">

<xsl:output method="xml" 
            indent="yes" 
	    doctype-system="http://jakarta.apache.org/turbine/dtd/database.dtd" />

<xsl:template match="tables">
  <database name="portal">
    <xsl:apply-templates select="table"/>
  </database>
</xsl:template>

<xsl:template match="table">
  <table name="{name}" skipSql="false" description="{desc}">
    <xsl:apply-templates select="columns"/>
  </table>
</xsl:template>

<xsl:template match="columns">
  <xsl:for-each select="column">
  <column>
      <xsl:attribute name="name">
        <xsl:value-of select="name"/>
      </xsl:attribute>
      <xsl:attribute name="type">
        <xsl:value-of select="type"/>
      </xsl:attribute>
      <xsl:if test="param">
        <xsl:attribute name="size">
	  <xsl:value-of select="param"/>
	</xsl:attribute>
      </xsl:if>
      <xsl:if test="../not-null='{name}'">
        <xsl:attribute name="required">true</xsl:attribute>
      </xsl:if>
      <xsl:if test="key='PK'">
        <xsl:attribute name="primaryKey">true</xsl:attribute>
      </xsl:if>
      <xsl:attribute name="description">
        <xsl:value-of select="desc"/>
      </xsl:attribute>
  </column>
  <!--foreign keys??-->
  </xsl:for-each>
</xsl:template>

</xsl:stylesheet>
