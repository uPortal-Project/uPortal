<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="baseActionURL">default</xsl:param>
<xsl:variable name="folderindex" select="1"/>
<xsl:output method="html"/>

<xsl:include href="lookAndFeel.xsl"/>

<xsl:template match="listMessages">
 <xsl:apply-templates select="navigationBar"/>
 <xsl:apply-templates select="headerBar"/>
 <xsl:choose>
  <xsl:when test="errors"><apply-templates select="errors"/></xsl:when>
  <xsl:otherwise>
   <form method="POST" action="{$baseActionURL}?action=listMessages">
    <xsl:choose>
     <xsl:when test="searchFolder"><xsl:apply-templates select="searchFolder"/></xsl:when>
     <xsl:otherwise>
      <xsl:apply-templates select="controls"/>
      <xsl:apply-templates select="messages"/>
      <xsl:apply-templates select="controls"/>
     </xsl:otherwise>
    </xsl:choose>
   </form>
   <xsl:apply-templates select="navigationBar"/>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="controls">
 <table>
 <xsl:apply-templates select="buttons"/>
 <xsl:apply-templates select="navigation"/>
 </table>
</xsl:template>

<xsl:template match="controls/buttons">
 <tr>
  <td align="left">
   <table border="0" cellpadding="2" cellspacing="2" width="100%">
    <tr>
     <td><xsl:attribute name="bgcolor"><xsl:value-of select="bgcolor"/></xsl:attribute>
      <table>
       <tr>
        <td align="left">
         <input type="submit" name="submit" value="Check for new mail"/></td>
         <td>
         <xsl:choose>
          <xsl:when test="//@filtered">
           <input type="submit" name="submit" value="Clear Search"/>
          </xsl:when>
          <xsl:otherwise>
           <input type="submit" name="submit" value="Search"/>
          </xsl:otherwise>
         </xsl:choose>
         </td>

         <td wrap="nowrap" align="left">
          <table border="1" cellpadding="0" cellspacing="0">
           <tr wrap="nowrap">
            <td align="left">
             <table border="0" cellpadding="1" cellspacing="1">
              <tr>
               <td align="left"><input type="submit" name="submit" value="Delete"></input></td>
               <td>
                <table border="1" cellpadding="0" cellspacing="0">
                 <tr>
                  <td>
                   <table border="0" cellpadding="0" cellspacing="1">
                    <tr>
                     <td align="left">
                      <input type="submit" name="submit" value="Move"></input>
                     </td>
                     <td align="left">
                      <xsl:apply-templates select="folders"/>
                     </td>
                    </tr>
                   </table>
                  </td>
                 </tr>
                </table>
               </td>
               <td><input type="checkbox" name="AllMessages"/> All messages</td>
              </tr>
             </table>
            </td>
           </tr>
          </table>
         </td>

       </tr>
      </table>
     </td>
    </tr>
   </table>
  </td>
 </tr>
</xsl:template>

<xsl:template match="buttons/folders">
 <select><xsl:attribute name="name"><xsl:value-of select="concat('destinationFolder', $folderindex)"/></xsl:attribute>
  <option>- Choose Folder -</option>
  <xsl:for-each select="folder">
   <option><xsl:attribute name="value"><xsl:value-of select="@value"/></xsl:attribute><xsl:value-of select="."/></option>
  </xsl:for-each>
 </select>
</xsl:template>

<xsl:template match="controls/pagination">
 <tr><td align="left">
  <xsl:if test="(@first)">
   <a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL, '&amp;action=listMessages&amp;page=first')"/></xsl:attribute>First</a> |
  </xsl:if>
  <xsl:if test="(@prev)">
   <a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL, '&amp;action=listMessages&amp;page=prev')"/></xsl:attribute>Previous</a> |
  </xsl:if>
  <xsl:value-of select="@start"/> - <xsl:value-of select="@end"/>
  <xsl:if test="(@next)">
   | <a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL, '&amp;action=listMessages&amp;page=next')"/></xsl:attribute>Next</a>
  </xsl:if>
  <xsl:if test="(@last)">
   | <a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL, '&amp;action=listMessages&amp;page=last')"/></xsl:attribute>Last</a>
  </xsl:if>
 </td></tr>
</xsl:template>

<xsl:template match="messages">
 <table border="0" cellpadding="2" cellspacing="3" width="100%"><xsl:attribute name="bgcolor"><xsl:value-of select="@bgcolor"/></xsl:attribute>
  <xsl:apply-templates select="//pagination"/>
  <table border="0" cellpadding="2" cellspacing="3" width="100%">
  <xsl:apply-templates select="headers"/>
  <xsl:apply-templates select="message"/>
  </table>
  <xsl:apply-templates select="//pagination"/>
 </table>
</xsl:template>

<xsl:template match="message">
 <tr><xsl:attribute name="bgcolor"><xsl:value-of select="@bgcolor"/></xsl:attribute>
  <td align="center">
   <input type="checkbox" name="msg">
   <xsl:attribute name="value"><xsl:value-of select="@msg"/></xsl:attribute>
   </input>
  </td>
  <td>
   <xsl:value-of select="@status"/>
  </td>
  <xsl:apply-templates select="from"/>
  <td>
   <a>
    <xsl:attribute name="href">
     <xsl:choose>
      <xsl:when test="@draft">
       <xsl:value-of select="concat($baseActionURL, '?prevMsg=', @msg, '&amp;action=composeMessage&amp;mode=draft')"/>
      </xsl:when>
      <xsl:otherwise>
       <xsl:value-of select="concat($baseActionURL, '?msg=', @msg, '&amp;action=displayMessage')"/>
      </xsl:otherwise>
     </xsl:choose>
    </xsl:attribute>
   <xsl:value-of select="subject"/>
   </a>
  </td>
  <td>
   <xsl:value-of select="date"/>
  </td>
  <td align="right">
   <xsl:value-of select="@size"/>
  </td>

 </tr>
</xsl:template>

<xsl:template match="messages/headers">
  <tr><xsl:attribute name="bgcolor"><xsl:value-of select="@bgcolor"/></xsl:attribute>
   <xsl:for-each select="header">
    <th>
    <xsl:attribute name="align"><xsl:value-of select="@align"/></xsl:attribute>
     <xsl:choose>
      <xsl:when test="@value">
       <a><xsl:attribute name="href"><xsl:value-of select="concat($baseActionURL, '?action=listMessages&amp;submit=sort&amp;sortBy=', @value)"/></xsl:attribute>
        <xsl:choose>
         <xsl:when test="@active"><em><xsl:value-of select="."/></em></xsl:when>
         <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
        </xsl:choose>
       </a>
      </xsl:when>
      <xsl:otherwise>
       <xsl:value-of select="."/>
      </xsl:otherwise>
     </xsl:choose>
    </th>
   </xsl:for-each>
  </tr>
</xsl:template>

<xsl:template match="from">
 <td>
 <xsl:choose>
  <xsl:when test="address/personal">
   <xsl:attribute name="title"><xsl:value-of select="concat(' ',address/email, ' ')"/></xsl:attribute>
<xsl:value-of select="address/personal"/>

  </xsl:when>
  <xsl:otherwise><xsl:value-of select="address/email"/></xsl:otherwise>
 </xsl:choose>
 </td>
</xsl:template>

<xsl:template match="searchFolder">
Search folder:
 <table border="0">
  <tr><td>Criteria <input name="criteriatext" size="30"/></td></tr>
  <tr><td><input type="radio" name="criteria" value="Sender">Sender</input></td></tr>
  <tr><td><input type="radio" name="criteria" value="Subject">Subject</input></td></tr>
  <tr><td><input type="submit" name="submit" value="Search Folder"/></td></tr>
 </table>
</xsl:template>
</xsl:stylesheet>
