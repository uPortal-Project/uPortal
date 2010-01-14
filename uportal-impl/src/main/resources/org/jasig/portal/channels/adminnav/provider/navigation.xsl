<?xml version='1.0'?>
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

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html" indent="yes"/>
<xsl:param name="baseMediaURL">media/org/jasig/portal/channels/CChannelManager</xsl:param>

<xsl:template match="/">
<TABLE WIDTH="100%" BORDER="0" CELLSPACING="0" CELLPADDING="2">
   <TR><TD CLASS="uportal-channel-strong"><IMG SRC="{$baseMediaURL}/transparent.gif" width="7" height="10"/><xsl:value-of select="adminurls/heading"/></TD></TR>
   <TR><TD><IMG SRC="{$baseMediaURL}/transparent.gif" width="10" height="10"/></TD></TR>
<xsl:for-each select="//adminurl">
  <xsl:sort select="@desc"/>
   <TR><TD><SPAN CLASS="uportal-channel-text">
     <A HREF="{.}" onMouseover="window.status=''; return true;"><img alt="" src="{$baseMediaURL}/bullet.gif" width="9" height="9" hspace="7" border="0"/><xsl:value-of select="@desc"/></A>
   </SPAN></TD></TR>
</xsl:for-each>
  <TR><TD><IMG SRC="{$baseMediaURL}/transparent.gif" width="10" height="20"/></TD></TR>
</TABLE>
</xsl:template>

</xsl:stylesheet>