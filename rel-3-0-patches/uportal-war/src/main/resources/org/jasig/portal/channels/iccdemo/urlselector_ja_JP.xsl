<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:param name="baseActionURL">base url not set</xsl:param>
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
      <form action="{$baseActionURL}">
        <xsl:choose>
          <xsl:when test="@grouped='false'">現在，依存関係なしにレンダリングされています： 
            <input type="submit" name="groupedRendering" class="uportal-button" value="グループ化されたレンダリングに変更" />
          </xsl:when>

          <xsl:otherwise>現在，依存関係 (renderingGroups) を用いてレンダリングされています： 
            <input type="submit" name="groupedRendering" class="uportal-button" value="プレーンレンダリングに変更" />
          </xsl:otherwise>
        </xsl:choose>
      </form>

      <p>これは，チャネル間通信のデモンストレーションです．現在のチャネルは2つの他のチャネルとの協調して Web ページを表示しています．
      <br />

      このセレクタチャネルを用いると，表示する URL をユーザは選択できるようになります．ビューアチャネルは <span class="uportal-channel-code">iframe</span> エレメントを用いて所望の Web ページを表示し，現在の URL 情報を第3のチャネルであるヒストリチャネルに渡されます．ヒストリチャネルは，ビューアチャネルが表示した過去 10 URL を単に表示するだけのものです． 
      <br />

      URL セレクタチャネルが<span class="uportal-channel-code">setRuntimeData()</span> メソッドにおいて CViewer に URL を渡す前にさらに1秒待ちます．つまり，ほとんどの時間は，CViewer は以前の URL をレンダリングし，ユーザがブラウザのリロードボタンをクリックするまで新しい URL は表示されません．しかしながら，URL セレクタチャネルはレンダリンググループを用いて，ポータルのレンダリングエンジンが <span class="uportal-channel-code">renderXML()</span> バウンダリで3つのチャネルすべてをシンクロナイズし，その結果，CViewer は古くなった URL をレンダリングしないことを保証します．</p>

      Pick one of the predefined URLs: 
      <xsl:for-each select="url">
        <br />
        <a href="{$baseActionURL}?url={.}"><xsl:value-of select="." /></a>
      </xsl:for-each>
      </p>

      <p>
        <form action="{$baseActionURL}">
          または，URL を手動で入力します：
          <input name="url" type="text" size="30" />
        </form>
      </p>
   </xsl:template>
</xsl:stylesheet>

