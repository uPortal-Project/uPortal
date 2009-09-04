<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template match="/*">
        <layout username="{username}" script="classpath://org/jasig/portal/io/import-layout_v2-6.crn">
            <xsl:apply-templates select="//folder[@type = 'root']" mode="branch"/>
        </layout>
    </xsl:template>

    <!-- root -->
    <xsl:template match="folder[@type = 'root']" mode="branch">
        <xsl:variable name="me" select="."/>
        <root name="{@name}" struct-id="{@struct-id}">
            <xsl:apply-templates select="//param[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//structure-attribute[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//folder[@struct-id = $me/@child-struct-id]" mode="branch"/>
        </root>
    </xsl:template>

    <!-- header -->
    <xsl:template match="folder[@type = 'header']" mode="branch">
        <xsl:variable name="me" select="."/>
        <header name="{@name}" struct-id="{@struct-id}">
            <xsl:apply-templates select="//param[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//structure-attribute[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//channel[@struct-id = $me/@child-struct-id]"/>
        </header>
        <xsl:apply-templates select="//folder[@struct-id = $me/@next-struct-id]" mode="branch"/>
    </xsl:template>

    <!-- tab -->
    <xsl:template match="folder[@type = 'regular']" mode="branch">
        <xsl:variable name="me" select="."/>
        <tab name="{@name}" struct-id="{@struct-id}">
            <xsl:apply-templates select="//param[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//structure-attribute[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//folder[@struct-id = $me/@child-struct-id]" mode="leaf"/>
        </tab>
        <xsl:apply-templates select="//folder[@struct-id = $me/@next-struct-id]" mode="branch"/>
    </xsl:template>

    <!-- column -->
    <xsl:template match="folder[@type = 'regular']" mode="leaf">
        <xsl:variable name="me" select="."/>
        <column name="{@name}" struct-id="{@struct-id}">
            <xsl:apply-templates select="//param[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//structure-attribute[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//channel[@struct-id = $me/@child-struct-id]"/>
        </column>
        <xsl:apply-templates select="//folder[@struct-id = $me/@next-struct-id]" mode="leaf"/>
    </xsl:template>

    <!-- footer -->
    <xsl:template match="folder[@type = 'footer']" mode="branch">
        <xsl:variable name="me" select="."/>
        <footer name="{@name}" struct-id="{@struct-id}">
            <xsl:apply-templates select="//param[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//structure-attribute[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//channel[@struct-id = $me/@child-struct-id]"/>
        </footer>
        <xsl:apply-templates select="//folder[@struct-id = $me/@next-struct-id]" mode="branch"/>
    </xsl:template>

    <!-- channel -->
    <xsl:template match="channel">
        <xsl:variable name="me" select="."/>
        <channel fname="{@fname}" struct-id="{@struct-id}">
            <xsl:apply-templates select="//param[@struct-id = $me/@struct-id]"/>
            <xsl:apply-templates select="//structure-attribute[@struct-id = $me/@struct-id]"/>
        </channel>
        <xsl:apply-templates select="//channel[@struct-id = $me/@next-struct-id]"/>
    </xsl:template>

</xsl:transform>