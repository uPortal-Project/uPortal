<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:param name="locale">ja_JP</xsl:param>

<xsl:template match="iframe" >
  このブラウザはインラインフレームをサポートしていません．<br/> 
  別のウィンドウでコンテンツを表示する場合は，<a href="{url}" target="_blank">ここt</a> をクリックします．
</xsl:template>

</xsl:stylesheet>
