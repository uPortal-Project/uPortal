<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html" indent="yes"/>
  <xsl:param name="baseActionURL">baseActionURL_false</xsl:param>
  <xsl:param name="locale">ja_JP</xsl:param>
  <xsl:variable name="baseMediaURL">media/org/jasig/portal/channels/CError/</xsl:variable>
  <xsl:param name="allowRefresh">true</xsl:param>
  <xsl:param name="allowReinstantiation">true</xsl:param>
  <xsl:param name="showStackTrace">true</xsl:param>

  <xsl:template match="error">
    <table border="0" width="100%" cellspacing="0" cellpadding="4">
      <tr>
        <td colspan="2" nowrap="nowrap" align="center" class="uportal-background-med">
          <p class="uportal-channel-title">エラーレ�?ート</p>
        </td>
      </tr>
      <tr>
        <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
          <p class="uportal-channel-error">�?ャ�?ル ID：</p>
        </td>
        <td width="100%" valign="top" align="left" class="uportal-channel-error">
          <xsl:value-of select="channel/id"/>
        </td>
      </tr>
      <tr>
        <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
          <p class="uportal-channel-error">メッセージ：</p>
        </td>
        <td width="100%" valign="top" align="left" class="uportal-channel-error">
          <xsl:if test="not(message) or message = ''">メッセージ�?�利用�?��??�?��?�ん</xsl:if>
          <xsl:value-of select="message"/>
        </td>
      </tr>
      <tr>
        <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
          <p class="uportal-channel-error">エラータイプ：</p>
        </td>
        <td width="100%" valign="top" align="left" class="uportal-channel-error">
          <xsl:choose>
            <xsl:when test="@code='4'">�?ャ�?ル�?�タイムアウト（コード 4）</xsl:when>
            <xsl:when test="@code='1'">レンダリング�?�失敗（コード 1）</xsl:when>
            <xsl:when test="@code='2'">�?期化�?�失敗（コード 2）</xsl:when>
            <xsl:when test="@code='3'">ランタイムデータ�?��?�得�?�失敗（コード 3）</xsl:when>
            <xsl:when test="@code='0'">何ら�?��?�エラー（コード 0）</xsl:when>
            <xsl:when test="@code='5'">PCS �?��?�得�?�失敗（コード 5）</xsl:when>
            <xsl:when test="@code='6'">権�?�?��?��?�ユーザ（コード 6）</xsl:when>
            <xsl:when test="@code='7'">利用�?�?�能（コード 7）</xsl:when>
            <xsl:when test="@code='-1'">uPortal エラー（コード -1）</xsl:when>
          </xsl:choose>
        </td>
      </tr>
      <xsl:apply-templates select="exception"/>
      <tr>
        <td valign="top" align="right" class="uportal-background-med">
          <xsl:if test="$allowRefresh='true'">
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="string($baseActionURL)"/>?action=retry</xsl:attribute>
              <img border="0" width="16" height="16" alt="�?ャ�?ル�?�リトライ">
                <xsl:attribute name="src">
                  <xsl:value-of select="string($baseMediaURL)"/>error_refresh.gif</xsl:attribute>
              </img>
            </a>
            <img alt="interface image" border="0" width="10" height="10">
              <xsl:attribute name="src">
                <xsl:value-of select="string($baseMediaURL)"/>transparent.gif</xsl:attribute>
            </img>
          </xsl:if>
          <xsl:if test="$allowReinstantiation='true'">
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="string($baseActionURL)"/>?action=restart</xsl:attribute>
              <img border="0" width="16" height="16" alt="�?ャ�?ル�?��?起動">
                <xsl:attribute name="src">
                  <xsl:value-of select="string($baseMediaURL)"/>error_reboot.gif</xsl:attribute>
              </img>
            </a>
            <img alt="interface image" border="0" width="10" height="10">
              <xsl:attribute name="src">
                <xsl:value-of select="string($baseMediaURL)"/>transparent.gif</xsl:attribute>
            </img>
          </xsl:if>
          <xsl:if test="exception">
            <xsl:choose>
              <xsl:when test="$showStackTrace='true' and */stack">
                <a>
                  <xsl:attribute name="href">
                    <xsl:value-of select="string($baseActionURL)"/>?action=toggle_stack_trace</xsl:attribute>
                  <img border="0" width="16" height="16" alt="Hide stack trace">
                    <xsl:attribute name="src">
                      <xsl:value-of select="string($baseMediaURL)"/>error_hide_trace.gif</xsl:attribute>
                  </img>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <a>
                  <xsl:attribute name="href">
                    <xsl:value-of select="string($baseActionURL)"/>?action=toggle_stack_trace</xsl:attribute>
                  <img border="0" width="16" height="16" alt="スタックトレース�?�表示">
                    <xsl:attribute name="src">
                      <xsl:value-of select="string($baseMediaURL)"/>error_show_trace.gif</xsl:attribute>
                  </img>
                </a>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
        </td>
        <td valign="middle" align="center" class="uportal-background-med"/>
      </tr>
      <tr>
        <td/>
      </tr>
    </table>

    <xsl:if test="$showStackTrace='true'">
      <xsl:call-template name="stackTrace"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="exception">
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
        <p class="uportal-channel-error">�?題種別：</p>
      </td>
      <td width="100%" valign="top" align="left" class="uportal-channel-error">
        <xsl:choose>
          <xsl:when test="@code='-1'">ランタイム例外（コード -1）</xsl:when>
          <xsl:when test="@code='1'">一般的�?�レンダリング�?題（コード 1）</xsl:when>
          <xsl:when test="@code='2'">内部タイムアウト（コード 2）</xsl:when>
          <xsl:when test="@code='3'">権�?�?��?題（コード 3）</xsl:when>
          <xsl:when test="@code='4'">リソース�?��?足（コード 4）</xsl:when>
        </xsl:choose>
      </td>
    </tr>
    <xsl:if test="@code='2'">
      <tr>
        <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
          <p class="uportal-channel-error">タイムアウト制�?</p>
        </td>
        <td width="100%" valign="top" align="left" class="uportal-channel-error">
          <xsl:if test="not(timeout/@value)">タイムアウト制�?�?�利用�?��??�?��?�ん</xsl:if>
          <xsl:value-of select="timeout/@value"/>
        </td>
      </tr>
    </xsl:if>
    <xsl:if test="@code='4'">
      <tr>
        <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
          <p class="uportal-channel-error">リソース�?�説明</p>
        </td>
        <td width="100%" valign="top" align="left" class="uportal-channel-error">
          <xsl:if test="not(resource/description) or resource/description = ''">リソース�?�説明�?�利用�?��??�?��?�ん</xsl:if>
          <xsl:value-of select="resource/description"/>
        </td>
      </tr>
      <tr>
        <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
          <p class="uportal-channel-error">リソース�?� URI</p>
        </td>
        <td width="100%" valign="top" align="left" class="uportal-channel-error">
          <xsl:if test="not(resource/uri) or resource/uri = ''">リソース�?� URI �?�利用�?��??�?��?�ん</xsl:if>
          <xsl:value-of select="resource/uri"/>
        </td>
      </tr>
    </xsl:if>
    <tr>
      <td nowrap="nowrap" valign="top" align="right" class="uportal-background-light">
        <p class="uportal-channel-error">エラーメッセージ</p>
      </td>
      <td width="100%" valign="top" align="left" class="uportal-channel-error">
        <xsl:if test="not(message) or message = ''">エラーメッセージ�?�利用�?��??�?��?�ん</xsl:if>
        <xsl:value-of select="message"/>
      </td>
    </tr>
  </xsl:template>
  
  <xsl:template name="stackTrace">
    <br/>
    <table border="0" width="100%" cellspacing="0" cellpadding="4">
      <tr>
        <td valign="top" align="center" class="uportal-background-med">
          <p class="uportal-channel-title">スタックトレース</p>
        </td>
      </tr>
      <xsl:if test="exception/stack/innerException">
      <tr>
        <td valign="top" align="left" class="uportal-background-light">
          <span class="uportal-channel-error">Inner exception:</span>
        </td>
      </tr>
      <tr>
        <td valign="top" align="left" class="uportal-channel-code">
          <span class="uportal-channel-code">
            <pre><xsl:value-of select="exception/stack/innerException"/></pre>
          </span>
        </td>
      </tr>
      </xsl:if>
      <xsl:if test="exception/stack/outerException">
      <tr>
        <td valign="top" align="left" class="uportal-background-light">
          <span class="uportal-channel-error">Outer exception:</span>
        </td>
      </tr>
      <tr>
        <td valign="top" align="left" class="uportal-channel-code">
          <span class="uportal-channel-code">
            <pre><xsl:value-of select="exception/stack/outerException"/></pre>
          </span>
        </td>
      </tr>
      </xsl:if>
      <tr>
        <td valign="top" align="left" class="uportal-background-med">
          <img border="0" src="transparent.gif" width="1" height="1"/>
        </td>
      </tr>
    </table>
  </xsl:template>
</xsl:stylesheet>
