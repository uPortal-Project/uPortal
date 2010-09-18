<?xml version="1.0" encoding="utf-8"?>
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

<!-- ========================================================================= -->
<!-- ========== README ======================================================= -->
<!-- ========================================================================= -->
<!--
 | Date: 08/14/2008
 | Author: Matt Polizzotti
 | Company: Unicon,Inc.
 | uPortal Version: uP3.0.0 and uP3.0.1
 |
 | This file determines the focused page layout and presentation of the mobile portal.
 | The file is imported by the base stylesheet muniversality.xsl.
 | Parameters and templates from other XSL files may be referenced; refer to muniversality.xsl for the list of parameters and imported XSL files.
 | For more information on XSL, refer to [http://www.w3.org/Style/XSL/].
-->

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:dlm="http://www.uportal.org/layout/dlm"
    xmlns:upAuth="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanAuthorizationHelper"
    xmlns:upGroup="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanGroupMembershipHelper"
    xmlns:upMsg="http://xml.apache.org/xalan/java/org.jasig.portal.security.xslt.XalanMessageHelper"
    exclude-result-prefixes="upAuth upGroup upMsg" 
    version="1.0">


<!-- ========================================================================= -->
<!-- ========== TEMPLATE: FOCUSED LAYOUT ===================================== -->
<!-- ========================================================================= -->
<!-- 
| RED
| The layout template (focused mode) defines the structure of the portal when content is focused. In 
| other words, when a user clicks a 'channel or portlet link' and the contents of that channel or 
| portlet are focused or rendered in the mobile device. Template contents can be any valid XSL or XHTML.
--> 
<xsl:template match="layout" mode="focused">
   <div class="fl-view fl-transition-slide">
        <xsl:call-template name="mobile.header.focused" />
        <xsl:call-template name="mobile.navigation.focused" />
        <xsl:call-template name="mobile.channel.content.focused" />
        <xsl:call-template name="mobile.footer" />
        <xsl:call-template name="mobile.navigation.script"/>
    </div>
</xsl:template>
<!-- ========================================================================= -->


</xsl:stylesheet>