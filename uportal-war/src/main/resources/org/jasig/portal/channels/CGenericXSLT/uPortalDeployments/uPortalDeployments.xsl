<?xml version='1.0'?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>
    <xsl:variable name="perScreen">20</xsl:variable>
    
    <xsl:template match="/">
        <h2>uPortal Deployments</h2>
        
        <div id="portal-deployment-list">
            <xsl:call-template name="nav">
                <xsl:with-param name="position">top</xsl:with-param>
            </xsl:call-template>
            <ul class="pager-items">
                <xsl:apply-templates select="rss/channel/item"/>
            </ul>
            <xsl:call-template name="nav">
                <xsl:with-param name="position">bottom</xsl:with-param>
            </xsl:call-template>
        </div>

        <script type="text/javascript">
            up(document).ready(function(){
                fluid.pager("#portal-deployment-list", {
                    listeners: {
                        onModelChange: function(link) {
                            up("#portal-deployment-list .pager-items li").css("display", "none");
                            var i = link.pageIndex*<xsl:value-of select="$perScreen"/>;
                            up("#portal-deployment-list .pager-items li:gt(" + link.pageIndex*<xsl:value-of select="$perScreen"/> +"):lt(" + <xsl:value-of select="$perScreen"/> + ")").css("display", "block");
                            return false;
                        }
                    }
                });
            });
        </script>
        
    </xsl:template>

    <xsl:template match="item">
        <li>
            <a href="{link}" target="_blank"><xsl:value-of select="title"/></a>
        </li>
    </xsl:template>
    
    <xsl:template name="nav">
        <xsl:param name="position">top</xsl:param>
        <ul id="pager-{$position}" class="pager-{$position}">
            <li class="previous"><a href="#">&lt; previous</a></li>
            <xsl:call-template name="navLink">
                <xsl:with-param name="i">1</xsl:with-param>
                <xsl:with-param name="max">
                    <xsl:value-of select="ceiling(count(rss/channel/item) div $perScreen)"/>
                </xsl:with-param>
            </xsl:call-template>
            <li class="next"><a href="#">next &gt;</a></li>
        </ul>
    </xsl:template>
    
    <xsl:template name="navLink">
        <xsl:param name="i" />
        <xsl:param name="max" />
        <xsl:if test="$i &lt;= $max">
            <li value="1" class="page-link"><a href="#"><xsl:value-of select="$i"/></a></li>
        </xsl:if>
        <xsl:if test="$i &lt;= $max">
            <xsl:call-template name="navLink">
                <xsl:with-param name="i">
                    <xsl:value-of select="$i + 1"/>
                </xsl:with-param>
                <xsl:with-param name="max">
                    <xsl:value-of select="$max"/>
                </xsl:with-param>
            </xsl:call-template>
        </xsl:if> 
    </xsl:template>
    
</xsl:stylesheet>