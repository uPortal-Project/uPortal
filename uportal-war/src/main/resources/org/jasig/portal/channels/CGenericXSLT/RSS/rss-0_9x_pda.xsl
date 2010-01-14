<?xml version="1.0"?>
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
<xsl:output method="html"/>
<xsl:param name="locale">en_US</xsl:param>

 <xsl:template match="rss">
 <html><body>
 <xsl:apply-templates select="channel"/>
 </body></html>
 </xsl:template>


 <xsl:template match="channel">
  <table cellspacing="1" cellpadding="4" bgcolor="#FFFFFF" border="0" width="100%">
    <tr>

    <td valign="middle" align="left" bgcolor="#EEEEEE">
       <!--xsl:apply-templates select="image"/-->
      <font color="#000000" face="Arial,Helvetica"><b>
        <a>
        <xsl:attribute name="href">
         <xsl:value-of select="link"/>
        </xsl:attribute>
        <xsl:value-of select="title"/>
        </a></b>
      </font>
    </td></tr>

    <tr>
    <td>
    <!--center><xsl:value-of select="description"/></center-->
     <xsl:apply-templates select="item"/> 
    </td>
    </tr>
   </table>
 </xsl:template>

 <xsl:template match="item">
   <li><a>
    <xsl:attribute name="href">
     <xsl:value-of select="link"/>
    </xsl:attribute>
    <xsl:value-of select="title"/>
   </a>
 </li>
 </xsl:template>

<xsl:template match="image">
  <a>
  <xsl:attribute name="href">
   <xsl:value-of select="link"/>
  </xsl:attribute>
  <img hspace="10">
        <xsl:attribute name="src">
         <xsl:value-of select="url"/>
        </xsl:attribute>
        <xsl:attribute name="alt">
         <xsl:value-of select="description"/>
        </xsl:attribute>
        <xsl:attribute name="width">
         <xsl:value-of select="width"/>
        </xsl:attribute>
        <xsl:attribute name="height">
         <xsl:value-of select="height"/>
        </xsl:attribute>
   </img>
   </a>
 </xsl:template>

</xsl:stylesheet>