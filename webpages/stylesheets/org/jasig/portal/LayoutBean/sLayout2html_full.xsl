<?xml version="1.0"?>
<!--xsl:stylesheet xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="html"/>

<xsl:param name="activeTab" select="1"/>
<xsl:param name="userName" select="'Guest'"/>
<xsl:param name="baseActionURL">render.uP</xsl:param>

<xsl:variable name="channelHeadingColor">#83a3b8</xsl:variable>
<xsl:variable name="activeTabColor">#83a3b8</xsl:variable>
<xsl:variable name="tabColor">#a0c8b4</xsl:variable>
<xsl:variable name="fgcolor">#000000</xsl:variable>
<xsl:variable name="bgcolor">#ffffff</xsl:variable>

<xsl:variable name="mediaDir">media/org/jasig/portal/layout/tabColumn</xsl:variable>

  <xsl:template match="layout">
    <html>
      <head>
       <title><xsl:value-of select="header/title"/></title>
       <link rel="stylesheet" href="{$mediaDir}/general.css" type="text/css"/>
       <!--xsl:call-template name="javaScripts"/-->
      </head>
      <body bgcolor="white" text="#000000">
      <xsl:apply-templates select="header"/>

      <!-- Draw out the tabs-->
      <table border="0" width="100%" cellspacing="0" cellpadding="0"><tr>
      <xsl:for-each select="tab">
       <xsl:call-template name="processTabHeader"/>
      </xsl:for-each>
      </tr>
      <!-- Strip beneath tabs -->
      <tr>
      <xsl:call-template name="tabStrip"/>
      </tr>
      </table>
      <br></br>

      <xsl:apply-templates select="tab[position()=$activeTab]"/>

      </body>
    </html>
  </xsl:template>

  <xsl:template name="tabStrip">
      <td width="100%">
      <xsl:attribute name="colspan"><xsl:value-of select="2*count(tab)+1"/></xsl:attribute>
       <table border="0" cellspacing="0" width="100%">
        <tr><td bgcolor="{$activeTabColor}">
         <table border="0" cellspacing="0" cellpadding="0"><tr><td height="3"></td></tr></table>
         </td></tr>
         </table>
      </td>
  </xsl:template>

  <!-- Render the content of the active tab-->
  <xsl:template match="tab">
   <table border="0" cellpadding="0" cellspacing="4" width="100%">
     <tr>
       <xsl:apply-templates/>
     </tr>
   </table>
  </xsl:template>


  <!-- process a column within a tab-->
  <xsl:template match="column">
    <td valign="top">
	<xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute>
       <table border="0" cellpadding="0" cellspacing="5" width="100%">
        <xsl:for-each select="channel">
         <tr><td>
          <xsl:apply-templates select="."/>
         </td></tr>
       </xsl:for-each>
       </table>
    </td>
  </xsl:template>

  <!-- process a channel-->
  <xsl:template match="channel">
  <table border="1" cellpadding="0" cellspacing="0" width="100%">
   <tr>
       <!-- draw the header -->
       <xsl:call-template name="channelHeader"/>
   </tr>
    <!-- draw channel content-->
   <tr><td>
     <xsl:if test="@minimized='false'">
      <xsl:copy-of select="."/>
     </xsl:if>
    </td></tr>
  </table>
  </xsl:template>


  <!-- create channel header-->
  <xsl:template name="channelHeader">
   <td bgcolor="cccccc">
      <table border="0" cellpadding="0" cellspacing="0" width="100%" bgcolor="{$channelHeadingColor}">
        <tr>
          <td><b><xsl:text disable-output-escaping="yes"> </xsl:text><xsl:value-of select="@name"/></b></td>
          <td nowrap="true" valign="center" align="right">
           <xsl:if test="@hasHelp='true'">
            <a>
            <xsl:attribute name="href"><xsl:value-of select="$baseActionURL"/>?userLayoutTarget=<xsl:value-of select="@ID"/>&amp;action=help</xsl:attribute>
            <img border="0" width="18" height="15" src="media/org/jasig/portal/layout/tabColumn/help.gif" alt="Help"/>
            </a>
           </xsl:if>
           <xsl:if test="@editable='true'">
            <a>
            <xsl:attribute name="href"><xsl:value-of select="$baseActionURL"/>?userLayoutTarget=<xsl:value-of select="@ID"/>&amp;action=edit</xsl:attribute>
            <img border="0" width="23" height="15" src="media/org/jasig/portal/layout/tabColumn/edit.gif" alt="Edit"/>
            </a>
           </xsl:if>
           <xsl:if test="@minimizable='true'">
            <a>
            <xsl:attribute name="href"><xsl:value-of select="$baseActionURL"/>?userLayoutTarget=<xsl:value-of select="@ID"/>&amp;action=minimize</xsl:attribute>
            <img border="0" width="18" height="15" src="media/org/jasig/portal/layout/tabColumn/minimize.gif" alt="Minimize"/>
            </a>
           </xsl:if>
           <xsl:if test="@detachable='true'">
            <a>
            <xsl:attribute name="href">JavaScript:openWin('detach.jsp?tab=<xsl:number count="tab"/>&amp;column=<xsl:number count="column"/>&amp;channel=<xsl:number count="channel"/>', 'detachedWindow', 550, 450)</xsl:attribute>
            <img border="0" width="18" height="15" src="media/org/jasig/portal/layout/tabColumn/detach.gif" alt="Detach"/>
            </a>
           </xsl:if>
           <xsl:if test="@removable='true'">
            <a>
            <xsl:attribute name="href"><xsl:value-of select="$baseActionURL"/>?userLayoutTarget=<xsl:value-of select="@ID"/>&amp;action=remove</xsl:attribute>
            <img border="0" width="18" height="15" src="media/org/jasig/portal/layout/tabColumn/remove.gif" alt="Remove"/>
            </a>
           </xsl:if>
          </td>
        </tr>
      </table>
   </td>
  </xsl:template>

  <xsl:template name="processTabHeader">
    <!-- conditional on the activeTab parameter-->

   <xsl:choose>
    <xsl:when test="position()=$activeTab">
     <xsl:variable name="currentTabColor" select="$activeTabColor"/>
         <td bgcolor="{$activeTabColor}" align="center" width="20%">
         <table bgcolor="{$activeTabColor}" border="0" cellspacing="0" cellpadding="2">
         <tr align="center">
            <td>
             <font face="Arial"><a><xsl:attribute name="href"><xsl:value-of select="$baseActionURL"/>?stylesheetTarget=s&amp;activeTab=<xsl:number count="tab"/></xsl:attribute><b><xsl:value-of select="@name"/></b></a></font>
            </td>
        </tr>
        </table>
        </td>
        <td width="1%"></td>
    </xsl:when>
    <xsl:otherwise>
         <td bgcolor="{$tabColor}" align="center" width="20%">
         <table bgcolor="{$tabColor}" border="0" cellspacing="0" cellpadding="2">
         <tr align="center">
            <td>
             <font face="Arial">
              <a><xsl:attribute name="href"><xsl:value-of select="$baseActionURL"/>?stylesheetTarget=s&amp;activeTab=<xsl:number count="tab"/></xsl:attribute><b><xsl:value-of select="@name"/></b></a>
             </font>
            </td>
        </tr>
        </table>
        </td>
        <td width="1%"></td>
    </xsl:otherwise>
   </xsl:choose>

  </xsl:template>

  <xsl:template match="header">
    <table border="0" cellpadding="0" cellspacing="1" width="100%">
    <tr>
    <td width="5%">
     <xsl:apply-templates select="image"/>
    </td>
    <td width="95%">
    <xsl:apply-templates select="title"/>
    </td>
    </tr>
    </table>
  </xsl:template>

  <xsl:template match="title">
    <h1 align="right">Welcome <xsl:value-of select="$userName"/> :)</h1>
    <xsl:if test="$userName != 'Guest'"><div align="right"><a href="logout.jsp">Logout</a></div></xsl:if>
  </xsl:template>

  <xsl:template match="image">
    <a>
    <xsl:attribute name="href">
     <xsl:value-of select="link"/>
    </xsl:attribute>
    <img hspace="10" border="0">
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

  <xsl:template name="javaScripts">
  <script language="JavaScript">
   <xsl:comment>hide

   function openWin(url, title, width, height)
   {
    var newWin = window.open(url, title, 'width=' + width + ',height=' + height +',resizable=yes,scrollbars=yes')
   }
   //stop hiding</xsl:comment>
   </script>
  </xsl:template>

</xsl:stylesheet>
