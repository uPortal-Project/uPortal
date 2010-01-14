<?xml version="1.0" encoding="utf-8" ?> 
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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="no" /> 

  <xsl:param name="locale">en_US</xsl:param>
  <xsl:param name="mode"/>
  <xsl:param name="User-Pwd-Only-Mode"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param> 

  <xsl:template match="*">

  <table boder="1" width="70%" cellspacing="0" cellpadding="0" align="center" class="uportal-channel-text">
   <tr>
    <td align="center">
	   <xsl:if test="$User-Pwd-Only-Mode='yes' or $User-Pwd-Only-Mode='no'">
	     Since uPortal version 2.5.
	   </xsl:if>
	
	   <xsl:if test="$User-Pwd-Only-Mode='not-active'">
	     About for Administrators.
	   </xsl:if>

	</td>
   </tr>
   
   <tr>
    <td>
	 <xsl:text> </xsl:text>
	</td>
   </tr>
   
   <tr>
    <td align="center">
	 <a href='{$baseActionURL}'>done</a>
	</td>
   </tr>
  </table> 
	 
  </xsl:template>
  
</xsl:stylesheet>
