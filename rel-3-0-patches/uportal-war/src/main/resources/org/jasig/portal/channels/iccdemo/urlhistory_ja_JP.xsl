<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:param name="baseActionURL">base url not set</xsl:param>
  <xsl:param name="CURLSelectId">CURLSelect id is not set</xsl:param>
  <xsl:param name="passExternally">false</xsl:param>
  <xsl:param name="locale">ja_JP</xsl:param>

  <xsl:template match="urlselector">

    <xsl:for-each select="warning">
      <p>
        <span class="uportal-channel-warning">
          <xsl:apply-templates />
        </span>
      </p>
    </xsl:for-each>

    <p>
    <p>これは一種の「履歴」チャネルで，チャネル間通信のデモンストレーションでもあります．このチャネルには，ビューアチャネルに表示された最後の 10 URL が表示されています．URL をクリックすると，CViewer チャネルに再度表示されます．この操作により CViewer は履歴を更新します．</p>

    <span class="uportal-label">URL 履歴</span>： 
    <xsl:for-each select="url">
    <br />

    <xsl:value-of select="position()" />.
    <a class="uportal-channel-code">
      <xsl:attribute name="href">
        <xsl:choose>
          <xsl:when test="$passExternally='true'">
            <xsl:value-of select="$baseActionURL" />?uP_channelTarget=<xsl:value-of select="$CURLSelectId" />&amp;url=<xsl:value-of select="." />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$baseActionURL" />?urlN=<xsl:value-of select="position()" />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      
      <xsl:value-of select="." />
    </a>
    </xsl:for-each>
    </p>

    <p>上記の一覧から URL をクリックすると，CHistory は CViewer チャネルにその URL を渡します．これを実現する1つの方法として考えられるのは， 内部的に行う方法です．もう1つ uPortal の URL シンタックス(uP_channelTarget)を用いる方法で，"url" パラメータが CURLSelector に渡されますので，セレクタチャネルは CViewer に情報を伝えることができます． 
    <form action="{$baseActionURL}">
      <xsl:choose>
        <xsl:when test="$passExternally='true'">現在，uP_channelTarget により外部的に URL を渡しています： 
          <input type="submit" name="passExternally" class="uportal-button" value="Switch to pass internally" />
        </xsl:when>
        <xsl:otherwise>現在，JNDI により内部的に CViewer に URL を渡しています： 
          <input type="submit" name="passExternally" class="uportal-button" value="Switch to pass using uP_channelTarget" />
        </xsl:otherwise>
      </xsl:choose>
    </form>
    </p>
  </xsl:template>
</xsl:stylesheet>

