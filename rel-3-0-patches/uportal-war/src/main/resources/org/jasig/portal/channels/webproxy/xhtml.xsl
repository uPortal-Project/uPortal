<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:html="http://www.w3.org/1999/xhtml"
                version="1.0">

<xsl:param name="locale">en_US</xsl:param>

<xsl:output method="html"/>
<xsl:preserve-space elements="script/comment() script/text()"/>

   <xsl:template match="html|html:html">
      <xsl:copy-of select="/html/head/base" />
      <xsl:apply-templates select="body"/>
   </xsl:template>

   <xsl:template match="body">
        <!--handles script code in head-->
        <xsl:if test="/html/head/script">
          <xsl:apply-templates select="/html/head/script"/>
        </xsl:if>
        <xsl:apply-templates/>
   </xsl:template>

   <!--outputs script code in an HTML comment--> 
   <xsl:template match="script/comment()">
     <xsl:value-of select="."/>
   </xsl:template>

   <!--handles all other html tags-->
   <xsl:template match="@*|*" priority="-5">
      <xsl:copy>
        <xsl:apply-templates select="@*|*|text()|comment()"/>
      </xsl:copy>
   </xsl:template>

   <!--handles all text nodes-->
   <xsl:template match="text()"  priority="-5">
      <xsl:value-of select="."/>
   </xsl:template>

   <!--outputs all comments-->
   <xsl:template match="comment()" priority="-5">
     <xsl:comment>
       <xsl:value-of select="."/>
     </xsl:comment>
   </xsl:template>
 
</xsl:stylesheet>
