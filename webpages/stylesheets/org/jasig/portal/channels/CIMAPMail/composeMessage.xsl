<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="baseActionURL">default</xsl:param>
<xsl:output method="html"/>
<xsl:include href="lookAndFeel.xsl"/>

<xsl:template match="composeMessage">
 <xsl:apply-templates select="navigationBar"/>
 <xsl:apply-templates select="headerBar"/>
 <xsl:choose>
 <xsl:when test="errors"><xsl:apply-templates select="errors"/></xsl:when>
 <xsl:when test="sentmsg"><xsl:apply-templates select="sentmsg"/></xsl:when>
 <xsl:when test="savedmsg"><xsl:apply-templates select="savedmsg"/></xsl:when>
 <xsl:when test="cancelled"><xsl:apply-templates select="cancelled"/></xsl:when>
 <xsl:otherwise>
 <form action="{$baseActionURL}?action=composeMessage" enctype="multipart/form-data" method="POST">
  <xsl:apply-templates select="hidden"/>
  <xsl:apply-templates select="controls"/>
  <table>
   <tr>
    <td>
     <table>
       <xsl:apply-templates select="recipient"/>
      <tr>
       <td align="right">Subject:</td>
       <td><input type="text" size="40" name="subject"><xsl:attribute name="value"><xsl:value-of select="subject"/></xsl:attribute></input></td>
      </tr>
     </table>
    </td>
    <td align="center">
Directory Service<br/>
My Address Book<br/>
A-F G-L M-R S-Z<br/>
All Group<br/>
    </td>
   </tr>
  </table>
  <table>
   <tr>
    <td>
     <textarea name="body" rows="15" cols="72" wrap="virtual">
      <xsl:value-of select="body"/>
     </textarea>
    </td>
    <td valign="top">
     <xsl:apply-templates select="attachments"/>
    </td>
   </tr>
  </table>
 </form>
 </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template match="recipient">
      <tr>
<td align="right">
<input type="submit" name="submit">
<xsl:attribute name="value">
<xsl:value-of select="concat(@tag, ':')"/>
</xsl:attribute>
</input>
</td>
<td>
<input type="text" size="40">
<xsl:attribute name="name">
<xsl:value-of select="@tag"/>
</xsl:attribute>
<xsl:attribute name="value">
<xsl:apply-templates select="addresses"/>
</xsl:attribute>
</input>
</td>
      </tr>
</xsl:template>

<xsl:template match="addresses">
 <xsl:for-each select="address">
  <xsl:choose>
   <xsl:when test="personal">
    "<xsl:value-of select="personal"/>" <xsl:value-of select="concat(&lt;,email, &gt;)"/>
   </xsl:when>
   <xsl:otherwise><xsl:value-of select="email"/></xsl:otherwise>
  </xsl:choose>
  <xsl:if test="position() != last()">, </xsl:if>
 </xsl:for-each>
</xsl:template>

<xsl:template match="attachments">
 <strong>Attachments:</strong><br/>

 <xsl:for-each select="attachment">
   <input name="submit" type="submit">
   <xsl:attribute name="value">Remove <xsl:value-of select="."/></xsl:attribute>
   </input>
   <br/>
 </xsl:for-each>

 <xsl:for-each select="getattachment">
  <input type="file" name="attachment"/><br/>
 </xsl:for-each>

</xsl:template>

<xsl:template match="controls">
 <table border="0" cellpadding="2" cellspacing="3" width="100%">
  <tr><xsl:attribute name="bgcolor"><xsl:value-of select="@bgcolor"/></xsl:attribute>
  <td>
   <xsl:for-each select="button">
    <input type="submit" name="submit"><xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute></input>
   </xsl:for-each>
  </td>
  </tr>
 </table>
</xsl:template>

<xsl:template match="sentmsg">
Your message <strong><xsl:value-of select="subject"/></strong> has been sent and a copy of it
has been placed in the folder <xsl:value-of select="savedFolder"/>.
</xsl:template>

<xsl:template match="savedmsg">
A draft of your message <strong><xsl:value-of select="subject"/></strong> has saved in the folder <xsl:value-of select="savedFolder"/>.
</xsl:template>

<xsl:template match="cancelled">
The message has not been sent.
</xsl:template>

</xsl:stylesheet>

