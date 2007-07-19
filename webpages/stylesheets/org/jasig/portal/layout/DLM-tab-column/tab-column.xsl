<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:dlm="http://www.uportal.org/layout/dlm" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="activeTab">1</xsl:param>
<xsl:param name="userLayoutRoot">root</xsl:param>
<xsl:param name="focusedTabID">none</xsl:param>

  <xsl:variable name="activeTabIdx">
    <!-- if the activeTab is a number then it is the active tab index -->
    <!-- otherwise it is the ID of the active tab. If it is the ID -->
    <!-- then check to see if that tab is still in the layout and -->
    <!-- if so use its index. if not then default to an index of 1. -->

   <xsl:choose>
   <xsl:when test="$focusedTabID='none'">
    <xsl:choose>
     <xsl:when test="string( number( $activeTab ) )='NaN'">

      <xsl:choose>
       <xsl:when test="/layout/folder/folder[@ID=$activeTab and
                                      @type='regular' and
                                      @hidden='false']">
        <xsl:value-of select="count(/layout/folder/folder[@ID=$activeTab]/preceding-sibling::folder[@type='regular' and @hidden='false'])+1"/>
       </xsl:when>
       <xsl:otherwise>1</xsl:otherwise> <!-- if not found, use first tab -->
      </xsl:choose>

     </xsl:when>
 	
 	 <!-- if the tab index number is greater than the number of tabs, use the first tab -->
     <xsl:when test="$activeTab &gt; count(/layout/folder/folder[@type='regular' and @hidden='false'])">1</xsl:when>
     <xsl:otherwise>
       <xsl:value-of select="$activeTab"/>
     </xsl:otherwise>
    </xsl:choose>
   </xsl:when>
   <xsl:otherwise>
      <xsl:choose>
       <xsl:when test="/layout/folder/folder[@ID=$focusedTabID and
                                      @type='regular' and
                                      @hidden='false']">
        <xsl:value-of select="count(/layout/folder/folder[@ID=$focusedTabID]/preceding-sibling::folder[@type='regular' and @hidden='false'])+1"/>
       </xsl:when>
       <xsl:otherwise>1</xsl:otherwise> <!-- if not found, use first tab -->
      </xsl:choose>
   </xsl:otherwise>
   </xsl:choose>
  </xsl:variable>

  <xsl:variable name="activeTabID" select="/layout/folder/folder[@type='regular'and @hidden='false'][position() = $activeTabIdx]/@ID"/>

<!-- document fragment template. See structure stylesheet for more comments -->
<xsl:template match="layout_fragment">
   <layout_fragment>
    <xsl:call-template name="tabList"/>
    <content>
      <xsl:apply-templates/>
    </content>
   </layout_fragment>    
</xsl:template>

<xsl:template match="layout">
   <xsl:for-each select="folder[@type='root']">
  <layout>
    <xsl:if test="/layout/@dlm:fragmentName">
        <xsl:attribute name="dlm:fragmentName"><xsl:value-of select="/layout/@dlm:fragmentName"/></xsl:attribute>
    </xsl:if>

    <header>
      <xsl:for-each select="child::folder[@type='header']">
          <xsl:copy-of select=".//channel"/>
      </xsl:for-each>
    </header>
    
    <xsl:call-template name="tabList"/>

    <content>
      <xsl:choose>
        <xsl:when test="$userLayoutRoot = 'root'">
          <xsl:apply-templates select="folder[@type='regular' and @hidden='false']"/>
        </xsl:when>
        <xsl:otherwise>
          <focused>
          	<!-- Detect whether a focused channel is present in the user's layout -->
          	<xsl:attribute name="in-user-layout">
          		<xsl:choose>
          			<xsl:when test="//folder[@type='regular' and @hidden='false']/channel[@ID = $userLayoutRoot]">yes</xsl:when>
          			<xsl:otherwise>no</xsl:otherwise>
          		</xsl:choose>
          	</xsl:attribute>
            <xsl:apply-templates select="//*[@ID = $userLayoutRoot]"/>
          </focused>
        </xsl:otherwise>
      </xsl:choose>
    </content>

    <footer>
      <xsl:for-each select="child::folder[attribute::type='footer']">
	      <xsl:copy-of select=".//channel"/>
      </xsl:for-each>
    </footer>
    
  </layout>    
   </xsl:for-each>
</xsl:template>

<xsl:template name="tabList">
  <navigation>
    <xsl:for-each select="/layout/folder/folder[@type='regular' and @hidden='false']">
      <tab>
          <xsl:attribute name="ID">
            <xsl:value-of select="@ID"/>
          </xsl:attribute>
          <xsl:attribute name="immutable">
            <xsl:value-of select="@immutable"/>
          </xsl:attribute>
          <xsl:attribute name="unremovable">
            <xsl:value-of select="@unremovable"/>
          </xsl:attribute>
          <xsl:if test="@dlm:moveAllowed = 'false'">
            <xsl:attribute name="dlm:moveAllowed">false</xsl:attribute>
          </xsl:if>
          <xsl:if test="@dlm:deleteAllowed = 'false'">
            <xsl:attribute name="dlm:deleteAllowed">false</xsl:attribute>
          </xsl:if>
          <xsl:if test="@dlm:editAllowed = 'false'">
            <xsl:attribute name="dlm:editAllowed">false</xsl:attribute>
          </xsl:if>
          <xsl:if test="@dlm:addChildAllowed = 'false'">
            <xsl:attribute name="dlm:addChildAllowed">false</xsl:attribute>
          </xsl:if>
          <xsl:if test="@dlm:precedence > 0">
            <xsl:attribute name="dlm:precedence"><xsl:value-of select="@dlm:precedence"/></xsl:attribute>
          </xsl:if>
        <xsl:choose>
      	  <xsl:when test="$activeTabID = @ID">
      	    <xsl:attribute name="activeTab">true</xsl:attribute>
            <xsl:attribute name="activeTabPosition"><xsl:value-of select="$activeTabID"/></xsl:attribute>
      	  </xsl:when>
      	  <xsl:otherwise>
      	    <xsl:attribute name="activeTab">false</xsl:attribute>
      	  </xsl:otherwise>
      	</xsl:choose>
          <xsl:attribute name="priority">
            <xsl:value-of select="@priority"/>
          </xsl:attribute>
          <xsl:attribute name="name">
            <xsl:value-of select="@name"/>
          </xsl:attribute>
      </tab>
    </xsl:for-each>
  </navigation>
</xsl:template>

<xsl:template match="folder[@hidden='false']">
  <xsl:if test="$activeTabID = @ID">
    <xsl:if test="child::folder">
      <xsl:for-each select="folder">
        <column>
            <xsl:attribute name="ID">
              <xsl:value-of select="@ID"/>
            </xsl:attribute>
            <xsl:attribute name="priority">
              <xsl:value-of select="@priority"/>
            </xsl:attribute>
            <xsl:attribute name="width">
              <xsl:value-of select="@width"/>
            </xsl:attribute>
            <xsl:if test="@dlm:moveAllowed = 'false'">
              <xsl:attribute name="dlm:moveAllowed">false</xsl:attribute>
            </xsl:if>
            <xsl:if test="@dlm:deleteAllowed = 'false'">
              <xsl:attribute name="dlm:deleteAllowed">false</xsl:attribute>
            </xsl:if>
            <xsl:if test="@dlm:editAllowed = 'false'">
              <xsl:attribute name="dlm:editAllowed">false</xsl:attribute>
            </xsl:if>
            <xsl:if test="@dlm:addChildAllowed = 'false'">
              <xsl:attribute name="dlm:addChildAllowed">false</xsl:attribute>
            </xsl:if>
            <xsl:if test="@dlm:precedence > 0">
              <xsl:attribute name="dlm:precedence">
                <xsl:value-of select="@dlm:precedence"/>
              </xsl:attribute>
            </xsl:if>
          <xsl:apply-templates/>
        </column>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="child::channel">
      <column>
        <xsl:apply-templates/>
      </column>
    </xsl:if>
  </xsl:if>
</xsl:template>

<xsl:template match="channel">
  <xsl:copy-of select="."/>
</xsl:template>

<xsl:template match="parameter">
  <xsl:copy-of select="."/>
</xsl:template>

</xsl:stylesheet>
<!-- Stylesheet edited using Stylus Studio - (c)1998-2001 eXcelon Corp. --><!-- Stylesheet edited using Stylus Studio - (c)1998-2002 eXcelon Corp. -->