<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param>
  <xsl:variable name="baseMediaURL">media/org/jasig/portal/channels/error/CError/</xsl:variable>
  <xsl:param name="allowRefresh">true</xsl:param>
  <xsl:param name="allowReinstantiation">true</xsl:param>
  <xsl:param name="showStackTrace">true</xsl:param>

  <xsl:template match="error">

    <p><u>Error Report</u></p>

    <p align="left"><b>Channel ID:</b></p>
    <p align="right"><xsl:value-of select="channel/id"/></p>

    <p align="left"><b>Message:</b></p>
    <p align="right">
      <xsl:if test="not(message) or message = ''">Message not available</xsl:if>
      <xsl:value-of select="message"/>
    </p>

    <p align="left"><b>Error type:</b></p>
    <p align="right">
      <xsl:choose>
        <xsl:when test="@code='4'">(<xsl:value-of select="@code"/>) Channel timed out</xsl:when>
        <xsl:when test="@code='1'">(<xsl:value-of select="@code"/>) Channel failed to render</xsl:when>
        <xsl:when test="@code='2'">(<xsl:value-of select="@code"/>) Channel failed to initialize</xsl:when>
        <xsl:when test="@code='3'">(<xsl:value-of select="@code"/>) Channel failed to accept runtime data</xsl:when>
        <xsl:when test="@code='0'">(<xsl:value-of select="@code"/>) General error</xsl:when>
        <xsl:when test="@code='5'">(<xsl:value-of select="@code"/>) Channel failed to accept PCS</xsl:when>
        <xsl:when test="@code='-1'">(<xsl:value-of select="@code"/>) uPortal error</xsl:when>
        <xsl:when test="@code='6'">(<xsl:value-of select="@code"/>) User not authorized (code 6)</xsl:when>
        <xsl:when test="@code='7'">(<xsl:value-of select="@code"/>) Channel not available (code 7)</xsl:when>
      </xsl:choose>
    </p>


    <xsl:if test="$showStackTrace='true'">
      <xsl:call-template name="stackTrace"/>
    </xsl:if>

    <xsl:apply-templates select="throwable"/>

    <xsl:if test="$allowRefresh='true'">
      <p align="left"><a href="{$baseActionURL}?action=retry">Retry channel</a></p>
    </xsl:if>

    <xsl:if test="$allowReinstantiation='true'">
      <p align="left"><a href="{$baseActionURL}?action=restart">Restart channel</a></p>
    </xsl:if>

    <xsl:if test="throwable">
      <p align="left">
        <xsl:choose>
          <xsl:when test="$showStackTrace='true' and */stack">
            <a href="{$baseActionURL}?action=toggle_stack_trace">Hide stack trace</a>
          </xsl:when>
          <xsl:otherwise>
            <a href="{$baseActionURL}?action=toggle_stack_trace">Show stack trace</a>
          </xsl:otherwise>
        </xsl:choose>
      </p>
    </xsl:if>

  </xsl:template>

  <xsl:template match="throwable">
    <p align="left"><b>Problem type:</b></p>
    <p align="right">
      <small>
        <xsl:choose>
          <xsl:when test="@renderedAs='java.lang.Throwable'">(<xsl:value-of select="@code"/>) General rendering problem</xsl:when>
          <xsl:when test="@renderedAs='org.jasig.portal.InternalTimeoutException'">(<xsl:value-of select="@code"/>) Internal timeout</xsl:when>
          <xsl:when test="@renderedAs='org.jasig.portal.AuthorizationException'">(<xsl:value-of select="@code"/>) Authorization problem</xsl:when>
          <xsl:when test="@renderedAs='org.jasig.portal.MissingResourceException'">(<xsl:value-of select="@code"/>) Missing resource</xsl:when>
        </xsl:choose>
      </small>
    </p>

    <xsl:if test="@code='org.jasig.portal.InternalTimeoutException'">
      <p align="left"><b>Timeout limit:</b></p>
      <p align="right">
        <small>
          <xsl:if test="not(timeout/@value)">Timeout limit not available</xsl:if>
          <xsl:value-of select="timeout/@value"/>
        </small>
      </p>
    </xsl:if>

    <xsl:if test="@renderedAs='org.jasig.portal.MissingResourceException'">
      <p align="left"><b>Resource description:</b></p>
      <p align="right">
        <small>
          <xsl:if test="not(resource/description) or resource/description = ''">Resource description not available</xsl:if>
          <xsl:value-of select="resource/description"/>
        </small>
      </p>
      <p align="left"><b>Resource URI:</b></p>
      <p align="right">
        <small>
          <xsl:if test="not(resource/uri) or resource/uri = ''">Resource URI not available</xsl:if>
          <xsl:value-of select="resource/uri"/>
        </small>
      </p>
    </xsl:if>

    <p align="left"><b>Error message:</b></p>
    <p align="right">
      <small>
        <xsl:if test="not(message) or message = ''">Error message not available</xsl:if>
        <xsl:value-of select="message"/>
      </small>
    </p>

  </xsl:template>

  <xsl:template name="stackTrace">
    <p align="left"><b>Stack Trace:</b></p>
    <p align="right">
      <small>
         <xsl:call-template name="dollar">
           <xsl:with-param name="text" select="throwable/stack" />
         </xsl:call-template>
        <!--xsl:value-of select="exception/stack"/-->
      </small>
    </p>
  </xsl:template>

  <!-- $ must be escaped to $$ in WML.  Template taken from "http://www.dpawson.co.uk/xsl/N8343.html#d44e39089" -->
  <xsl:template match="text()" name="dollar">
     <xsl:param name="text" select="."/>
     <xsl:choose>
       <xsl:when test="contains($text,'$')">
         <xsl:value-of select="substring-before($text,'$')"/>
         <xsl:text>$$</xsl:text>
         <xsl:call-template name="dollar">
           <xsl:with-param name="text" select="substring-after($text,'$')"/>
         </xsl:call-template>
       </xsl:when>
       <xsl:otherwise>
         <xsl:value-of select="$text"/>
      </xsl:otherwise>
      </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
