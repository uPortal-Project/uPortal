<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:portal="class:org.jasig.portal.StylesheetUtils" 
                version="1.0" exclude-result-prefixes="portal">

<xsl:output method="html"/>
<xsl:preserve-space elements="script/comment() script/text()"/>
 
   <xsl:param name="baseActionURL">default</xsl:param>
   <xsl:param name="cw_passThrough">default</xsl:param>
   <xsl:param name="cw_xml">default</xsl:param>

   <xsl:template match="html">
      <xsl:apply-templates select="body"/>
   </xsl:template>

   <xsl:template match="body">
        <!--handles script code in head-->
        <xsl:if test="//head/script">
          <xsl:element name="script">
            <xsl:copy-of select="//head/script/@*"/>
            <xsl:if test="//head/script/comment()">
              <xsl:value-of select="//head/script/comment()"/>
            </xsl:if>
            <xsl:if test="//head/script/text()">
              <xsl:value-of select="//head/script/text()"/>
            </xsl:if>
          </xsl:element>
        </xsl:if>
        <xsl:apply-templates/>
   </xsl:template>

   <xsl:template match="form">
      <xsl:param name="action-uri">
        <xsl:choose>
           <!--handles relative URIs for action attributes-->
           <xsl:when test="not(contains(@action, ':')) or not( contains(@action, ':') and not(contains(substring-before(@action, ':'), '/')) )">       
              <xsl:choose>
                <xsl:when test="//base">
                  <xsl:call-template name="get-absolute-uri">
                    <xsl:with-param name="uri" select="//base/@href"/>
                    <xsl:with-param name="file" select="@action"/>
                  </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="get-absolute-uri">
                    <xsl:with-param name="uri" select="$cw_xml"/>
                    <xsl:with-param name="file" select="@action"/>
                  </xsl:call-template>
                </xsl:otherwise>
              </xsl:choose>
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
           <xsl:value-of select="$baseActionURL"/>
	 </xsl:attribute>
         <xsl:apply-templates/>
       </xsl:when>
       <xsl:when test="$cw_passThrough='application' and ( string-length(normalize-space(@action))=0 or $cw_xml=@action or $cw_xml=$action-uri )">
         <xsl:copy-of select="attribute::*[not(name()='action')]" />
         <xsl:attribute name="action">
           <xsl:value-of select="$baseActionURL"/>
         </xsl:attribute>
         <xsl:apply-templates/>
       </xsl:when>
       <xsl:when test="$cw_passThrough='all'">
         <xsl:copy-of select="attribute::*[not(name()='action')]" />
         <xsl:attribute name="action">
           <xsl:value-of select="$baseActionURL"/>
         </xsl:attribute>
         <xsl:if test="not(string-length(normalize-space(@action))=0 or $cw_xml=@action or $cw_xml=$action-uri)">
           <input type="hidden" name="cw_xml" value="{$action-uri}"/>
         </xsl:if>
         <xsl:apply-templates/>
       </xsl:when>
       <xsl:otherwise>
         <xsl:choose>
          <xsl:when test="not(contains(@action, ':')) or not( contains(@action, ':') and not(contains(substring-before(@action, ':'), '/')) )">
           <xsl:copy-of select="attribute::*[not(name()='action')]"/>
           <xsl:attribute name="action">
             <xsl:value-of select="$action-uri"/>
           </xsl:attribute>
           <xsl:apply-templates/>
          </xsl:when>
          <xsl:otherwise>
           <xsl:apply-templates select="@*|*|text()|comment()"/>
          </xsl:otherwise>
         </xsl:choose>
       </xsl:otherwise>
      </xsl:choose>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="input">
      <xsl:copy>
      <xsl:choose>
        <xsl:when test="@src">
          <xsl:param name="src-uri">
            <xsl:choose>
              <xsl:when test="not(contains(@src, ':')) or not( contains(@src, ':') and not(contains(substring-before(@src, ':'), '/')) )">
                 <!--handles relative URIs for src attributes-->
                 <xsl:choose>
                   <xsl:when test="//base">
                     <xsl:call-template name="get-absolute-uri">
                       <xsl:with-param name="uri" select="//base/@href"/>
                       <xsl:with-param name="file" select="@src"/>
                     </xsl:call-template>
                   </xsl:when>
                   <xsl:otherwise>
                     <xsl:call-template name="get-absolute-uri">
                       <xsl:with-param name="uri" select="$cw_xml"/>
                       <xsl:with-param name="file" select="@src"/>
                     </xsl:call-template>
                   </xsl:otherwise>
                 </xsl:choose>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="@src"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:param>
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

   <xsl:template match="a">
      <xsl:copy>
      <xsl:param name="href-uri">
        <xsl:choose>
           <xsl:when test="not(contains(@href, ':')) or not( contains(@href, ':') and not(contains(substring-before(@href, ':'), '/')) )">
              <!--handles relative URIs for href attributes-->
              <xsl:choose>
                <xsl:when test="//base">
                  <xsl:call-template name="get-absolute-uri">
                    <xsl:with-param name="uri" select="//base/@href"/>
                    <xsl:with-param name="file" select="@href"/>
                  </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="get-absolute-uri">
                    <xsl:with-param name="uri" select="$cw_xml"/>
                    <xsl:with-param name="file" select="@href"/>
                  </xsl:call-template>
                </xsl:otherwise>
              </xsl:choose> 
           </xsl:when>
           <xsl:otherwise>
             <xsl:value-of select="@href"/>
           </xsl:otherwise>
        </xsl:choose>
      </xsl:param> 
      <xsl:choose>
       <xsl:when test="$cw_passThrough='marked' and (contains(@href, '&amp;cw_inChannelLink=') or contains(@href, '?cw_inChannelLink=') )">
         <xsl:copy-of select="attribute::*[not(name()='href')]" />
         <xsl:attribute name="href">
           <xsl:value-of select="concat($baseActionURL, '?', substring-after(@href, '?'))"/>
         </xsl:attribute>
         <xsl:apply-templates/>
       </xsl:when>
       <xsl:when test="$cw_passThrough='application' and ( string-length(normalize-space(@href))=0 or $cw_xml=@href or $cw_xml=$href-uri )">
         <xsl:copy-of select="attribute::*[not(name()='href')]" />
         <xsl:attribute name="href">
           <xsl:value-of select="$baseActionURL"/>
         </xsl:attribute>
         <xsl:apply-templates/> 
       </xsl:when>
       <xsl:when test="$cw_passThrough='all'">
         <xsl:copy-of select="attribute::*[not(name()='href')]" />
         <xsl:attribute name="href">
           <xsl:choose>
             <xsl:when test="string-length(normalize-space(@href))=0 or $cw_xml=@href or $cw_xml=$href-uri">
               <xsl:value-of select="$baseActionURL"/>
             </xsl:when>
             <xsl:otherwise>
               <xsl:value-of select="concat($baseActionURL, '?cw_xml=', $href-uri)"/>
             </xsl:otherwise>
           </xsl:choose>
         </xsl:attribute>
         <xsl:apply-templates/>
       </xsl:when>
       <xsl:otherwise>
         <xsl:choose>
          <xsl:when test="not(contains(@href, ':')) or not( contains(@href, ':') and not(contains(substring-before(@href, ':'), '/')) )">
           <xsl:copy-of select="attribute::*[not(name()='href')]"/>
           <xsl:attribute name="href">
              <xsl:value-of select="$href-uri"/>
           </xsl:attribute>
           <xsl:apply-templates/>
          </xsl:when>
          <xsl:otherwise>
           <xsl:apply-templates select="@*|*|text()|comment()"/>
          </xsl:otherwise>
         </xsl:choose>
       </xsl:otherwise>
      </xsl:choose> 
      </xsl:copy>
   </xsl:template>
 
   <!--handles relative URIs for src attributes-->
   <xsl:template match="img">
      <xsl:copy>
      <xsl:choose>
         <xsl:when test="not(contains(@src, ':')) or not( contains(@src, ':') and not(contains(substring-before(@src, ':'), '/')) )">
           <xsl:copy-of select="attribute::*[not(name()='src')]"/>
           <xsl:attribute name="src">
              <xsl:choose>
                <xsl:when test="//base">
                  <xsl:call-template name="get-absolute-uri">
                    <xsl:with-param name="uri" select="//base/@href"/>
                    <xsl:with-param name="file" select="@src"/>
                  </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="get-absolute-uri">
                    <xsl:with-param name="uri" select="$cw_xml"/>
                    <xsl:with-param name="file" select="@src"/>
                  </xsl:call-template>
                </xsl:otherwise>
              </xsl:choose>
           </xsl:attribute>
           <xsl:apply-templates/>
         </xsl:when>
         <xsl:otherwise>
           <xsl:apply-templates select="@*|*|text()|comment()"/> 
         </xsl:otherwise>
      </xsl:choose>
      </xsl:copy>
   </xsl:template>
 
   <!--returns the directory part of the URI concatenated with the file-->
   <xsl:template name="get-absolute-uri">
      <xsl:param name="uri"/>
      <xsl:param name="file"/>
      <xsl:param name="base"/>
      <xsl:choose>
         <!--handles relative uri's beginning with /-->
         <xsl:when test="starts-with($file, '/')">
            <xsl:call-template name="get-absolute-uri-slash">
               <xsl:with-param name="file" select="$file"/>
               <xsl:with-param name="uri" select="$uri"/>
            </xsl:call-template>
         </xsl:when>
         <!--handles all other relative uri's-->
         <xsl:otherwise>
           <xsl:choose>
             <xsl:when test="contains($uri, '/') and not(starts-with($file, '/'))">
                <xsl:call-template name="get-absolute-uri">
                   <xsl:with-param name="base"
                         select="concat($base, substring-before($uri, '/'), '/')"/>
                   <xsl:with-param name="uri" select="substring-after($uri, '/')"/>
                   <xsl:with-param name="file" select="$file"/>
                </xsl:call-template>
             </xsl:when>
             <xsl:otherwise>
                <xsl:value-of select="portal:getAbsURI(concat($base, $file))"/>
             </xsl:otherwise>
           </xsl:choose>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--returns the correct URI when given a relative URI beginning with a '/'-->
   <xsl:template name="get-absolute-uri-slash">
       <xsl:param name="file"/>
       <xsl:param name="uri"/>
       <xsl:value-of select="concat(substring-before($uri, '://'), '://', 
                    substring-before(substring-after($uri, '://'), '/'), $file)"/>
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
