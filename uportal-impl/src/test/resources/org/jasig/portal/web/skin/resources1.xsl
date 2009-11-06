<?xml version="1.0" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xalan" 
	xmlns:resources="http://www.jasig.org/uportal/XSL/web/skin"
	extension-element-prefixes="resources" 
    exclude-result-prefixes="xalan resources" 
	version="1.0">

    <xalan:component prefix="resources" elements="output">
        <xalan:script lang="javaclass" src="xalan://org.jasig.portal.web.skin.ResourcesXalanElements" />
    </xalan:component>
    
	<xsl:template match="head">
		<head>
			<resources:output/>
		</head>
	</xsl:template>

</xsl:stylesheet>