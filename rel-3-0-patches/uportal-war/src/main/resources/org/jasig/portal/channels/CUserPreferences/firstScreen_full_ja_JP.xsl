<?xml version='1.0'?>

<!--xsl:stylesheet xmlns:xsl='http://www.w3.org/XSL/Transform/1.0'-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="baseActionURL">index.jsp</xsl:param>
<xsl:param name="locale">ja_JP</xsl:param>

<xsl:template match="profile">
<html>
<body>

<p align="center">何を編集しますか??</p>
<form method="post">
<xsl:attribute name="action"><xsl:value-of select="$baseActionURL"/></xsl:attribute>
<input type="hidden" name="action" value="submitEditChoice"/>
<p align="center">
<select name="manageTarget">
<option selected="" value="layout">ユーザレイアウトの管理</option>
<option value="gpref">グローバル設定の管理</option>
<option value="prof">プロファイル</option>
</select>
<br/>
<input type="submit" name="submit" value="Proceed"/>
</p>




</form>
</body>
</html>
</xsl:template>
</xsl:stylesheet>
