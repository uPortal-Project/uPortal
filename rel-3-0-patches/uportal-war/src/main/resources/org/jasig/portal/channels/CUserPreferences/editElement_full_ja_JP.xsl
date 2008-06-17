<?xml version='1.0'?>

<!--xsl:stylesheet xmlns:xsl='http://www.w3.org/XSL/Transform/1.0'-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="baseActionURL">index.jsp</xsl:param>
<xsl:param name="locale">ja_JP</xsl:param>

<xsl:template match="editelement">
<html>
<body>

<p align="center"><xsl:value-of select="type"/> "<xsl:value-of select="name"/>"の編集：</p>
<form method="post">
<xsl:attribute name="action"><xsl:value-of select="$baseActionURL"/></xsl:attribute>
<input type="hidden" name="action" value="submitEditValues"/>


<table border="1" cellpadding="5" cellspacing="0" align="center">
<tr><td><b>変数</b></td><td><b>値</b></td><td><b>説明</b></td></tr>
<tr><td colspan="3" align="right"> 内部<xsl:value-of select="type"/>属性</td></tr>
<tr><td><xsl:value-of select="type"/>名前</td><td><input type="text" name="name" value="{name}"/></td><td><xsl:value-of select="type"/>名前</td></tr>

<!-- eventually this should check if there are any attributes at all of this class before drawing a table row-->
<!-- process structure stylesheet attributes-->
<tr><td colspan="3" align="right">構造スタイルシート属性</td></tr>
<xsl:for-each select="structureattributes/attribute">
<xsl:call-template name="processAttribute"/>
</xsl:for-each>

<!-- process theme stylesheet attributes-->
<tr><td colspan="3" align="right">テーマスタイルシート属性</td></tr>
<xsl:for-each select="themeattributes/attribute">
<xsl:call-template name="processAttribute"/>
</xsl:for-each>
</table>

<p align="center">
<input type="submit" name="submit" value="保存"/>
<input type="submit" name="submit" value="キャンセル"/>
</p>

</form>
</body>
</html>
</xsl:template>

<xsl:template name="processAttribute">
<tr><td><xsl:value-of select="name"/></td>
<td><input type="text" name="{name}" value="{value}"/></td>
<td><xsl:value-of select="description"/></td>
</tr>
</xsl:template>

</xsl:stylesheet>