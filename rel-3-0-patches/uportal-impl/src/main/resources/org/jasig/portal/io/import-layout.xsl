<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template match="/layout">
        <layout username="{@username}">
            <xsl:apply-templates select="root">
                <xsl:with-param name="struct-id">1</xsl:with-param>
            </xsl:apply-templates>
        </layout>
    </xsl:template>

    <xsl:template match="root | header | tab | footer | column">
        <xsl:param name="struct-id"/>
        <struct struct-id="{$struct-id}" name="{@name}" immutable="{@immutable}" unremovable="{@unremovable}">
            <xsl:choose>
                <xsl:when test="@hidden">
                    <xsl:attribute name="hidden"><xsl:value-of select="@hidden"/></xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="hidden">N</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="name() = 'root' or name() = 'header' or name() = 'footer'">
                    <xsl:attribute name="type"><xsl:value-of select="name()"/></xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="type">regular</xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="(following-sibling::header | following-sibling::tab | following-sibling::footer | following-sibling::column | following-sibling::channel)">
                <xsl:attribute name="next-struct-id"><xsl:value-of select="$struct-id + count(descendant::header | descendant::tab | descendant::footer | descendant::column | descendant::channel) + 1"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="header | tab | footer | column | channel">
                <xsl:attribute name="child-struct-id"><xsl:value-of select="$struct-id + 1"/></xsl:attribute>
            </xsl:if>
        </struct>
        <xsl:apply-templates select="param">
            <xsl:with-param name="struct-id" select="$struct-id"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="structure-attribute">
            <xsl:with-param name="struct-id" select="$struct-id"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="(header | tab | footer | column | channel)[position() = 1]">
            <xsl:with-param name="struct-id"><xsl:value-of select="$struct-id + 1"/></xsl:with-param>
        </xsl:apply-templates>
        <xsl:apply-templates select="(following-sibling::header | following-sibling::tab | following-sibling::footer | following-sibling::column | following-sibling::channel)[position() = 1]">
            <xsl:with-param name="struct-id"><xsl:value-of select="$struct-id + count(descendant::header | descendant::tab | descendant::footer | descendant::column | descendant::channel) + 1"/></xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="channel">
        <xsl:param name="struct-id"/>
        <channel struct-id="{$struct-id}" fname="{@fname}">
            <xsl:if test="following-sibling::channel">
                <xsl:attribute name="next-struct-id"><xsl:value-of select="$struct-id + 1"/></xsl:attribute>
            </xsl:if>
        </channel>
        <xsl:apply-templates select="param">
            <xsl:with-param name="struct-id" select="$struct-id"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="following-sibling::channel[position() = 1]">
            <xsl:with-param name="struct-id"><xsl:value-of select="$struct-id + 1"/></xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="param">
        <xsl:param name="struct-id"/>
        <param struct-id="{$struct-id}">
            <name><xsl:value-of select="name"/></name>
            <value><xsl:value-of select="value"/></value>
        </param>
    </xsl:template>

    <xsl:template match="structure-attribute">
        <xsl:param name="struct-id"/>
        <structure-attribute struct-id="{$struct-id}">
        	<xsl:choose>
        		<xsl:when test="@type = 'stylesheet'">
        			<xsl:attribute name="type">1</xsl:attribute>
        		</xsl:when>
        		<xsl:when test="@type = 'folder'">
        			<xsl:attribute name="type">2</xsl:attribute>
        		</xsl:when>
        		<xsl:when test="@type = 'channel'">
        			<xsl:attribute name="type">3</xsl:attribute>
        		</xsl:when>
        	</xsl:choose>
            <name><xsl:value-of select="name"/></name>
            <value><xsl:value-of select="value"/></value>
        </structure-attribute>
    </xsl:template>

</xsl:transform>