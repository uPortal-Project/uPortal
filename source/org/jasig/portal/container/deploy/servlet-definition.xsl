<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">

    <servlet>
      <servlet-name><xsl:value-of select="//*[name() = 'portlet-name']"/></servlet-name>
      <display-name><xsl:value-of select="//*[name() = 'portlet-name']"/> Wrapper</display-name>
      <description>Automated generated Portlet Wrapper</description>
      <servlet-class>org.jasig.portal.container.PortletServlet</servlet-class>
      <init-param>
        <param-name>portlet-class</param-name>
        <param-value><xsl:value-of select="//*[name() = 'portlet-class']"/></param-value>
      </init-param>
      <init-param>
        <param-name>portlet-guid</param-name>
        <param-value>${req(APP_NAME)}.<xsl:value-of select="//*[name() = 'portlet-name']"/></param-value>
      </init-param>
    </servlet>

  </xsl:template>

</xsl:transform>