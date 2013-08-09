<%@ include file="/WEB-INF/jsp/include.jsp" %>

<div id="webSearchContainer" class="fl-widget">
  <div class="fl-widget-inner">
  	<div class="fl-widget-titlebar">
      <xsl:choose>
        <xsl:when test="$USE_SIDEBAR_TOGGLE='true'">
          <a href="javascript:;" title="{upMsg:getMessage('sidebar.titlebar.close', $USER_LANG)}">
             <span class="icon"></span>
             <h2><label for="webSearchInput"><xsl:value-of select="upMsg:getMessage('web.search', $USER_LANG)"/></label></h2>
             <span class="labelopen"><xsl:value-of select="upMsg:getMessage('sidebar.titlebar.open', $USER_LANG)"/></span>
             <span class="labelclose"><xsl:value-of select="upMsg:getMessage('sidebar.titlebar.close', $USER_LANG)"/></span>
           </a>
         </xsl:when>
         <xsl:otherwise><h2><label for="webSearchInput"><xsl:value-of select="upMsg:getMessage('web.search', $USER_LANG)"/></label></h2></xsl:otherwise>
      </xsl:choose>
    </div>
    
    <div class="fl-widget-content">
	    <c:set var="searchLabel"><spring:message code="search"/></c:set>
	    
    	home ${searchUrl } 
        <form method="post" action="${searchUrl}" id="webSearchForm">
          <input id="webSearchInput" value="" name="query" type="text" />
          <input id="webSearchSubmit" type="submit" name="submit" value="${searchLabel}" />
        </form>
    </div>
  </div>
</div>
    	<!-- 
    	<portlet:actionURL var="searchUrl" windowState="MAXIMIZED"/>
	    <c:set var="searchLabel"><spring:message code="search"/></c:set>
	    -->

<!-- 
  <xsl:template name="web.search">
    <div id="webSearchContainer" class="fl-widget">
      <div class="fl-widget-inner">
      	<div class="fl-widget-titlebar">
          <xsl:choose>
            <xsl:when test="$USE_SIDEBAR_TOGGLE='true'">
              <a href="javascript:;" title="{upMsg:getMessage('sidebar.titlebar.close', $USER_LANG)}">
                 <span class="icon"></span>
                 <h2><label for="webSearchInput"><xsl:value-of select="upMsg:getMessage('web.search', $USER_LANG)"/></label></h2>
                 <span class="labelopen"><xsl:value-of select="upMsg:getMessage('sidebar.titlebar.open', $USER_LANG)"/></span>
                 <span class="labelclose"><xsl:value-of select="upMsg:getMessage('sidebar.titlebar.close', $USER_LANG)"/></span>
               </a>
             </xsl:when>
             <xsl:otherwise><h2><label for="webSearchInput"><xsl:value-of select="upMsg:getMessage('web.search', $USER_LANG)"/></label></h2></xsl:otherwise>
          </xsl:choose>
        </div>
        <div class="fl-widget-content">
            <xsl:variable name="searchUrl">
                <xsl:call-template name="portalUrl">
                    <xsl:with-param name="url">
                        <url:portal-url type="ACTION">
                            <url:fname>search</url:fname>
                            <url:portlet-url state="MAXIMIZED" />
                        </url:portal-url>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:variable>
            <form method="post" action="{$searchUrl}" id="webSearchForm">
              <input id="webSearchInput" value="" name="query" type="text" />
              <input id="webSearchSubmit" type="submit" name="submit" value="{upMsg:getMessage('search', $USER_LANG)}" />
            </form>
        </div>
      </div>
    </div>
  </xsl:template>
-->
