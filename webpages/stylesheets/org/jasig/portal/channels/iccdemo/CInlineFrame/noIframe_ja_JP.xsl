<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="iframe" >
  このブラウザはインラインフレームをサポートしていません．<br/> 
  <a href="{url}" target="_blank">ここ</a>をクリックすると，別のウィンドウにコンテンツが表示されます．
</xsl:template>

</xsl:stylesheet>
