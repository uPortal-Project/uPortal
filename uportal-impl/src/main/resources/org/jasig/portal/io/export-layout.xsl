<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template match="/*">
        <layout username="{username}" script="classpath://org/jasig/portal/io/import-layout_v3-0.crn">
            <xsl:apply-templates select="//folder[@type = 'root']" mode="branch"/>
        </layout>
    </xsl:template>

    <!-- root -->
    <xsl:template match="folder[@type = 'root']" mode="branch">
        <xsl:variable name="me" select="."/>
        <root name="{@name}" hidden="{@hidden}" immutable="{@immutable}" unremovable="{@unremovable}">
            <xsl:apply-templates select="//param[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//structure-attribute[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//folder[@struct-id = $me/@child-struct-id]" mode="branch"/>
        </root>
    </xsl:template>

    <!-- header -->
    <xsl:template match="folder[@type = 'header']" mode="branch">
        <xsl:variable name="me" select="."/>
        <header name="{@name}" hidden="{@hidden}" immutable="{@immutable}" unremovable="{@unremovable}">
            <xsl:apply-templates select="//param[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//structure-attribute[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//channel[@struct-id = $me/@child-struct-id]"/>
        </header>
        <xsl:apply-templates select="//folder[@struct-id = $me/@next-struct-id]" mode="branch"/>
    </xsl:template>

    <!-- tab -->
    <xsl:template match="folder[@type = 'regular']" mode="branch">
        <xsl:variable name="me" select="."/>
        <tab name="{@name}" hidden="{@hidden}" immutable="{@immutable}" unremovable="{@unremovable}">
            <xsl:apply-templates select="//param[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//structure-attribute[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//folder[@struct-id = $me/@child-struct-id]" mode="leaf"/>
        </tab>
        <xsl:apply-templates select="//folder[@struct-id = $me/@next-struct-id]" mode="branch"/>
    </xsl:template>

    <!-- column -->
    <xsl:template match="folder[@type = 'regular']" mode="leaf">
        <xsl:variable name="me" select="."/>
        <column name="{@name}" hidden="{@hidden}" immutable="{@immutable}" unremovable="{@unremovable}">
            <xsl:apply-templates select="//param[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//structure-attribute[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//channel[@struct-id = $me/@child-struct-id]"/>
        </column>
        <xsl:apply-templates select="//folder[@struct-id = $me/@next-struct-id]" mode="leaf"/>
    </xsl:template>

    <!-- footer -->
    <xsl:template match="folder[@type = 'footer']" mode="branch">
        <xsl:variable name="me" select="."/>
        <footer name="{@name}" hidden="{@hidden}" immutable="{@immutable}" unremovable="{@unremovable}">
            <xsl:apply-templates select="//param[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//structure-attribute[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//channel[@struct-id = $me/@child-struct-id]"/>
        </footer>
        <xsl:apply-templates select="//folder[@struct-id = $me/@next-struct-id]" mode="branch"/>
    </xsl:template>

    <!-- channel -->
    <xsl:template match="channel">
        <xsl:variable name="me" select="."/>
        <channel fname="{@fname}">
            <xsl:apply-templates select="//param[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//structure-attribute[@struct-id = $me/@struct-id]"/>
        </channel>
        <xsl:apply-templates select="//channel[@struct-id = $me/@next-struct-id]"/>
    </xsl:template>

    <!-- param -->
    <xsl:template match="param">
        <param>
            <name><xsl:value-of select="name"/></name>
            <value><xsl:value-of select="value"/></value>
        </param>
    </xsl:template>

    <!-- structure-attribute -->
    <xsl:template match="structure-attribute">
        <structure-attribute>
            <xsl:choose>
                <xsl:when test="@type = '1'">
                    <xsl:attribute name="type">stylesheet</xsl:attribute>
                </xsl:when>
                <xsl:when test="@type = '2'">
                    <xsl:attribute name="type">folder</xsl:attribute>
                </xsl:when>
                <xsl:when test="@type = '3'">
                    <xsl:attribute name="type">channel</xsl:attribute>
                </xsl:when>
            </xsl:choose>
            <name><xsl:value-of select="name"/></name>
            <value><xsl:value-of select="value"/></value>
        </structure-attribute>
    </xsl:template>

</xsl:transform>