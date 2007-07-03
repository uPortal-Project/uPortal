<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">

    <servlet-mapping>
           <servlet-name><xsl:value-of select="//*[name() = 'portlet-name']"/></servlet-name>
           <url-pattern>/<xsl:value-of select="//*[name() = 'portlet-name']"/>/*</url-pattern>
    </servlet-mapping>

  </xsl:template>

</xsl:transform>