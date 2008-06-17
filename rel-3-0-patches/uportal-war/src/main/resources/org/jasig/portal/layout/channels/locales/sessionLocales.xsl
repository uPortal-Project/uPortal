<?xml version='1.0' encoding='utf-8' ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">default</xsl:param>
  <xsl:param name="localesParam">uP_locales</xsl:param>

  <xsl:template match="/">
      <xsl:apply-templates select="locales"/>
  </xsl:template>

  <xsl:template match="locales">
    <form action="{$baseActionURL}" method="post">
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
      <tr class="uportal-background-dark">
        <td align="right" nowrap="nowrap" class="uportal-background-dark" colspan="1" rowspan="1">
          <select class="uportal-button" name="{$localesParam}">
              <xsl:apply-templates select="locale"/>
          </select>
          <input type="submit" value="Go" name="Go" class="uportal-button"/>
        </td>
      </tr>
    </table>
    </form>
  </xsl:template>

  <xsl:template match="locale">
      <option value="{@code}">
          <xsl:if test="@selected='true'">
              <xsl:attribute name="selected">selected</xsl:attribute>
          </xsl:if>
          <xsl:value-of select="@displayName"/>
      </option>
  </xsl:template> 

</xsl:stylesheet>

