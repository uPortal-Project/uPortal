<?xml version='1.0'?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>
    <xsl:variable name="perScreen">20</xsl:variable>
    
    <xsl:template match="/">
        
        <style type="text/css">
            #portal-deployment-list ul {margin:0;}
            #portal-deployment-list ul.pager-nav {text-align:right}
            #portal-deployment-list ul.pager-nav li {list-style-type:none; display:inline; padding:5px}
            #portal-deployment-list .pager-items li {line-height:1.7em}
            #portal-deployment-list .disabled a {color: #777777; border: 0; text-decoration: none; cursor: default;}
            #portal-deployment-list .current-page a { color: #000000; border: 0; text-decoration: none; cursor: default;}
        </style>
        
        <h2>uPortal Deployments</h2>
        
        <div id="portal-deployment-list" class="fluid-pager">
            <xsl:call-template name="nav">
                <xsl:with-param name="position">top</xsl:with-param>
            </xsl:call-template>
            <br/>
            
            <ul class="pager-items">
                <xsl:apply-templates select="rss/channel/item"/>
            </ul>
            
            <br/>
            <xsl:call-template name="nav">
                <xsl:with-param name="position">bottom</xsl:with-param>
            </xsl:call-template>
        </div>

        <script type="text/javascript">
            up.jQuery(document).ready(function(){
                up.fluid.pager("#portal-deployment-list", {
                    listeners: {
                        onModelChange: function(link) {
                            up.jQuery("#portal-deployment-list .pager-items li").css("display", "none");
                            var i = link.pageIndex*<xsl:value-of select="$perScreen"/>;
                            up.jQuery("#portal-deployment-list .pager-items li:gt(" + link.pageIndex*<xsl:value-of select="$perScreen"/> +"):lt(" + <xsl:value-of select="$perScreen"/> + ")").css("display", "block");
                            return false;
                        }
                    }
                });
                up.jQuery("#portal-deployment-list .pager-items a").tooltip();
            });
        </script>
        
    </xsl:template>

    <xsl:template match="item">
        <li>
            <xsl:choose>
                <xsl:when test="link">
                    <xsl:variable name="description">
                        <xsl:choose>
                            <xsl:when test="description"><xsl:value-of select="description"/></xsl:when>
                            <xsl:otherwise><xsl:value-of select="title"/></xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <a href="{link}" title="{$description}" target="_blank"><xsl:value-of select="title"/></a>
                </xsl:when>
                <xsl:otherwise><xsl:value-of select="title"/></xsl:otherwise>
            </xsl:choose>
        </li>
    </xsl:template>
    
    <xsl:template name="nav">
        <xsl:param name="position">top</xsl:param>
        <ul class="pager-nav pager-{$position}">
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
            <li value="{$i}" class="page-link">
                <a href="#"><xsl:value-of select="$i"/></a>    
            </li>
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