<?xml version="1.0" ?>
<!--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

-->

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
			<resources:output path="media/skins/test/uportal3/"/>
		</head>
	</xsl:template>

</xsl:stylesheet>