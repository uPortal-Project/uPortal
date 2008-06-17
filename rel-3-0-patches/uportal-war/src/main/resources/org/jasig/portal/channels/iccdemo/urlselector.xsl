<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:param name="baseActionURL">base url not set</xsl:param>
  <xsl:param name="locale">en_US</xsl:param>

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
          <xsl:when test="@grouped='false'">Currently rendering without dependencies: 
            <input type="submit" name="groupedRendering" class="uportal-button" value="Switch to grouped rendering" />
          </xsl:when>

          <xsl:otherwise>Currently rendering using dependencies (renderingGroups): 
            <input type="submit" name="groupedRendering" class="uportal-button" value="Switch to plain rendering" />
          </xsl:otherwise>
        </xsl:choose>
      </form>

      <p>This is a demonstration of the inter-channel communications. The current channel works in cooperation with two other channels to view web pages.
      <br />

      This channel (selector) allows the user to select a URL for viewing. The user's selection is then passed to the viewer (second) channel. The viewer channel uses an <span class="uportal-channel-code">iframe</span> element to show the desired web page, and passes the current URL information to the third channel - the history channel. The history channel simply shows the sequence of the last 10 URLs that have been hit by the vewer channel. 
      <br />

      The URL Selector channel waits an extra second, on purpose, before passing the URL to the CViewer in the <span class="uportal-channel-code">setRuntimeData()</span> method. That means that most of the time, CViewer will render the old URL, and the new URL will not be shown until the user hits the browser's reload button. However, when the URL selector channel is using rendering groups, the portal rendering engine will synchronize all three channels at the <span class="uportal-channel-code">renderXML()</span> boundary, therefore providing a guarantee that CViewer will not render with the outdated URL.</p>

      Pick one of the predefined URLs: 
      <xsl:for-each select="url">
        <br />
        <a href="{$baseActionURL}?url={.}"><xsl:value-of select="." /></a>
      </xsl:for-each>
      </p>

      <p>
        <form action="{$baseActionURL}">
          Or type in a URL manually: <br />
          <input name="url" type="text" size="30" />
        </form>
      </p>
   </xsl:template>
</xsl:stylesheet>

