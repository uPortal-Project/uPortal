<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:portal="class:org.jasig.portal.utils.StylesheetUtils" 
                version="1.0" exclude-result-prefixes="portal">

<xsl:output method="html"/>
<xsl:preserve-space elements="script/comment() script/text()"/>
 
   <xsl:param name="baseActionURL">default</xsl:param>
   <xsl:param name="cw_passThrough">default</xsl:param>
   <xsl:param name="cw_xml">default</xsl:param>
   <xsl:param name="base">
     <xsl:choose>
       <xsl:when test="/html/head/base/@href">
	 <xsl:value-of select="/html/head/base/@href"/>
       </xsl:when>
       <xsl:otherwise>
         <xsl:value-of select="$cw_xml"/>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:param>

   <xsl:template match="html">
      <xsl:apply-templates select="body"/>
   </xsl:template>

   <xsl:template match="body">
        <!--handles script code in head-->
        <xsl:if test="/html/head/script">
          <xsl:element name="script">
	    <xsl:copy-of select="/html/head/script/@*[not(name()='src')]"/>
            <xsl:choose>
	      <xsl:when test="not(contains(/html/head/script/@src, ':')) or not( contains(/html/head/script/@src, ':') and not(contains(substring-before(/html/head/script/@src, ':'), '/')) )">
                <xsl:attribute name="src">
		  <xsl:value-of select="portal:getAbsURI(string($base), string(/html/head/script/@src))"/>
                </xsl:attribute>
              </xsl:when>
              <xsl:otherwise>
	        <xsl:copy-of select="/html/head/script/@src"/>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="/html/head/script/comment()">
              <xsl:value-of select="/html/head/script/comment()"/>
            </xsl:if>
            <xsl:if test="/html/head/script/text()">
              <xsl:value-of select="/html/head/script/text()"/>
            </xsl:if>
          </xsl:element>
        </xsl:if>
        <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="form">
      <xsl:param name="action-uri">
        <xsl:choose>
           <!--handles relative URIs for action attributes-->
           <xsl:when test="not(string-length(normalize-space(@action))=0) and (not(contains(@action, ':')) or not( contains(@action, ':') and not(contains(substring-before(@action, ':'), '/')) ))">       
             <xsl:value-of select="portal:getAbsURI(string($base), string(@action))"/>
           </xsl:when>  
           <xsl:otherwise>
             <xsl:value-of select="@action"/>
           </xsl:otherwise>
        </xsl:choose>
      </xsl:param>
      <xsl:copy>
      <xsl:choose>
       <xsl:when test="$cw_passThrough='marked' and input/@name='cw_inChannelLink'">
         <xsl:copy-of select="attribute::*[not(name()='action')]" />
         <xsl:attribute name="action">
           <xsl:choose>
             <xsl:when test="contains($action-uri, '?')">
               <xsl:value-of select="concat($baseActionURL, '?', substring-after($action-uri, '?'))"/>
             </xsl:when>
             <xsl:otherwise>
               <xsl:value-of select="$baseActionURL"/>
             </xsl:otherwise>
           </xsl:choose>
         </xsl:attribute>
         <xsl:if test="not(string-length(normalize-space(@action))=0 or $action-uri=$cw_xml or $cw_xml=substring-before($action-uri, '?'))">
            <input type="hidden" name="cw_xml" value="{$action-uri}"/>
         </xsl:if>
         <xsl:apply-templates/>
       </xsl:when>
       <xsl:when test="$cw_passThrough='application' and ( string-length(normalize-space(@action))=0 or $cw_xml=$action-uri or $cw_xml=substring-before($action-uri, '?'))">
         <xsl:copy-of select="attribute::*[not(name()='action')]" />
         <xsl:attribute name="action">
           <xsl:choose>
             <xsl:when test="contains($action-uri, '?')">
               <xsl:value-of select="concat($baseActionURL, '?', substring-after($action-uri, '?'))"/>
             </xsl:when>
             <xsl:otherwise>
               <xsl:value-of select="$baseActionURL"/>
             </xsl:otherwise>
           </xsl:choose>
         </xsl:attribute>
         <xsl:apply-templates/>
       </xsl:when>
       <xsl:when test="$cw_passThrough='all'">
         <xsl:copy-of select="attribute::*[not(name()='action')]" />
         <xsl:attribute name="action">
           <xsl:choose>
             <xsl:when test="contains($action-uri, '?')">
               <xsl:value-of select="concat($baseActionURL, '?', substring-after($action-uri, '?'))"/>
             </xsl:when>
             <xsl:otherwise>
               <xsl:value-of select="$baseActionURL"/>
             </xsl:otherwise>
           </xsl:choose>
         </xsl:attribute>
         <xsl:if test="not(string-length(normalize-space(@action))=0 or $cw_xml=$action-uri or $cw_xml=substring-before($action-uri, '?'))">
           <input type="hidden" name="cw_xml" value="{$action-uri}"/>
         </xsl:if>
         <xsl:apply-templates/>
       </xsl:when>
       <xsl:otherwise>
         <xsl:copy-of select="attribute::*[not(name()='action')]"/>
         <xsl:attribute name="action">
           <xsl:value-of select="$action-uri"/>
         </xsl:attribute>
         <xsl:apply-templates/>
       </xsl:otherwise>
      </xsl:choose>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="input">
      <xsl:param name="src-uri">
       <xsl:choose>
       <xsl:when test="@src">
         <xsl:choose>
            <!--handles relative URIs for src attributes-->
            <xsl:when test="not(contains(@src, ':')) or not( contains(@src, ':') and not(contains(substring-before(@src, ':'), '/')) )">
              <xsl:value-of select="portal:getAbsURI(string($base), string(@src))"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="@src"/>
            </xsl:otherwise>
         </xsl:choose>
       </xsl:when>
       </xsl:choose>
      </xsl:param>

      <xsl:copy>
      <xsl:choose>
        <xsl:when test="@src">
          <!-- moved to top <xsl:param name="src-uri">... -->
            
          <xsl:copy-of select="attribute::*[not(name()='src')]" />
          <xsl:attribute name="src">
            <xsl:value-of select="$src-uri"/>
          </xsl:attribute>
          <xsl:apply-templates/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="@*|*|text()|comment()"/>
        </xsl:otherwise> 
      </xsl:choose>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="a|area">
      <xsl:param name="href-uri">
        <xsl:choose>
           <!--handles relative URIs for href attributes-->
           <xsl:when test="not(string-length(normalize-space(@href))=0) and (not(contains(@href, ':')) or not( contains(@href, ':') and not(contains(substring-before(@href, ':'), '/')) ))">
             <xsl:value-of select="portal:getAbsURI(string($base), string(@href))"/>
           </xsl:when>
           <xsl:otherwise>
             <xsl:value-of select="@href"/>
           </xsl:otherwise>
        </xsl:choose>
      </xsl:param> 
      <xsl:copy>
      <xsl:choose>
       <xsl:when test="$cw_passThrough='marked' and (contains(@href, '&amp;cw_inChannelLink=') or contains(@href, '?cw_inChannelLink=') )">
         <xsl:copy-of select="attribute::*[not(name()='href')]" />
         <xsl:attribute name="href">
           <xsl:choose>
             <xsl:when test="string-length(normalize-space($href-uri))=0 or $href-uri=$cw_xml or $cw_xml=substring-before($href-uri, '?')">
               <xsl:value-of select="concat($baseActionURL, '?', substring-after(@href, '?'))"/>
             </xsl:when>
             <xsl:otherwise>
                <xsl:value-of select="concat($baseActionURL, '?', substring-after(@href, '?'), '&amp;cw_xml=', substring-before($href-uri, '?'))"/>
             </xsl:otherwise>
           </xsl:choose>
         </xsl:attribute>
         <xsl:apply-templates/>
       </xsl:when>
       <xsl:when test="$cw_passThrough='application' and ( string-length(normalize-space(@href))=0 or $cw_xml=$href-uri or $cw_xml=substring-before($href-uri, '?'))">
         <xsl:copy-of select="attribute::*[not(name()='href')]" />
         <xsl:attribute name="href">
           <xsl:choose>
             <xsl:when test="contains($href-uri, '?')">
               <xsl:value-of select="concat($baseActionURL, '?', substring-after($href-uri, '?'))"/>
             </xsl:when>
             <xsl:otherwise>
               <xsl:value-of select="$baseActionURL"/>
             </xsl:otherwise>
           </xsl:choose>
         </xsl:attribute>
         <xsl:apply-templates/> 
       </xsl:when>
       <xsl:when test="$cw_passThrough='all'">
         <xsl:copy-of select="attribute::*[not(name()='href')]" />
         <xsl:attribute name="href">
           <xsl:choose>
             <xsl:when test="string-length(normalize-space(@href))=0 or $cw_xml=$href-uri or $cw_xml=substring-before($href-uri, '?')">
               <xsl:choose>
                 <xsl:when test="contains($href-uri, '?')">
                   <xsl:value-of select="concat($baseActionURL, '?', substring-after($href-uri, '?'))"/>
                 </xsl:when>
                 <xsl:otherwise>
                   <xsl:value-of select="$baseActionURL"/>
                 </xsl:otherwise>
               </xsl:choose>
             </xsl:when>
             <xsl:otherwise>
               <xsl:value-of select="concat($baseActionURL, '?cw_xml=', $href-uri)"/>
             </xsl:otherwise>
           </xsl:choose>
         </xsl:attribute>
         <xsl:apply-templates/>
       </xsl:when>
       <xsl:otherwise>
         <xsl:copy-of select="attribute::*[not(name()='href')]"/>
         <xsl:attribute name="href">
            <xsl:value-of select="$href-uri"/>
         </xsl:attribute>
         <xsl:apply-templates/>
       </xsl:otherwise>
      </xsl:choose> 
      </xsl:copy>
   </xsl:template>
 
   <!--handles relative URIs for src attributes-->
   <xsl:template match="img|script">
      <xsl:copy>
      <xsl:choose>
         <xsl:when test="not(contains(@src, ':')) or not( contains(@src, ':') and not(contains(substring-before(@src, ':'), '/')) )">
           <xsl:copy-of select="attribute::*[not(name()='src')]"/>
           <xsl:attribute name="src">
             <xsl:value-of select="portal:getAbsURI(string($base), string(@src))"/>
           </xsl:attribute>
           <xsl:apply-templates/>
         </xsl:when>
         <xsl:otherwise>
           <xsl:apply-templates select="@*|*|text()|comment()"/> 
         </xsl:otherwise>
      </xsl:choose>
      </xsl:copy>
   </xsl:template>

   <!--outputs script code (for the case when script in source is an html comment)--> 
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
