<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
         xmlns="http://www.w3.org/TR/xhtml1/strict">

<xsl:output method="html" indent="yes" encoding="ISO-8859-1" />

<!-- Author:   Bill Brooks  wbrooks@lug.ee.calpoly.edu                -->
<!-- Date:     Tue Jan  2 14:05:05 PST 2001                           -->
<!-- Filename: uPortalFAQ.xsl                                         -->
<!-- Version:  $Revision$                                       -->
<!-- Language: XSL:T 1.0                                              -->
<!-- Copyright: (c)2001, Trustees of the California State University  -->
<!--             and JA-SIG                                           -->
<!-- Purpose: Build file for Ant 1.2, stylesheet transforming from   -->
<!--           DocBook 4.1.2 to XHTML 1.0                             -->

<xsl:output method="html" indent="yes" encoding="ISO-8859-1" />

   <xsl:template match="abbrev">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="acronym">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="answer">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="article">
      <html>
         <head>
            <title>
               <xsl:value-of select="articleinfo/title"/>
            </title>
         </head>
         <body>
             <h1>
                <xsl:value-of select="articleinfo/title" />
             </h1>
             <p>
                <xsl:apply-templates select="articleinfo/author" />
             </p>
             <p>
                <xsl:apply-templates select="articleinfo/author/affiliation/orgdiv" />
             </p>
             <p>
                <xsl:apply-templates select="articleinfo/author/affiliation/orgname" />
             </p>
             <p>
                <xsl:apply-templates select="articleinfo/date" />
             </p>

             <xsl:apply-templates />

         </body>
      </html>
   </xsl:template>

   <xsl:template match="article/articleinfo">
      <!-- do nothing -->
   </xsl:template>

   <xsl:template match="author">
      <xsl:apply-templates select="firstname" />
      <xml:space />
      <xsl:apply-templates select="surname" />      
   </xsl:template>

   <xsl:template match="authorgroup">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="bibliography">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="bibliodiv">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="bibliodiv/title">
      <h4>
         <xsl:apply-templates />
      </h4>
   </xsl:template>

   <xsl:template match="biblioentry">
      <p>
         <xsl:apply-templates />
      </p>
   </xsl:template>

   <xsl:template match="bibliography/title">
      <h3>
         <xsl:apply-templates />
      </h3>
   </xsl:template>

   <xsl:template match="biblioset">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="copyright">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="date">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="emphasis">
      <em>
         <xsl:apply-templates />
      </em>
   </xsl:template>

   <xsl:template match="filename">
      <code>
         <xsl:apply-templates />
      </code>
   </xsl:template>

   <xsl:template match="firstname">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="foreignphrase">
      <em>
         <xsl:apply-templates />
      </em>
   </xsl:template>

   <xsl:template match="holder">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="isbn">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="itemizedlist">
      <ul>
         <xsl:apply-templates />
      </ul>
   </xsl:template>

   <xsl:template match="listitem">
      <li>
         <xsl:apply-templates />
      </li>
   </xsl:template>

   <xsl:template match="orgdiv">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="orgname">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="para">
      <p>
         <xsl:apply-templates />
      </p>
   </xsl:template>

   <xsl:template match="pagenums">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="productname">
      <xsl:apply-templates />
      <sup><font size="-2">TM</font></sup>
   </xsl:template>

   <xsl:template match="publisher">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="publishername">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="qandaentry">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="question">
      <h3>
         <xsl:apply-templates />
      </h3>
   </xsl:template>

   <xsl:template match="sect1">
      <p>
         <xsl:apply-templates />
      </p>
   </xsl:template>

   <xsl:template match="sect1/title">
      <h2>
         <xsl:apply-templates />
      </h2>
   </xsl:template>

   <xsl:template match="sect1/qandaset">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="surname">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="title">
      <xsl:apply-templates />
   </xsl:template>

   <xsl:template match="ulink">
      <a href="{@url}">
      <xsl:apply-templates />
      </a>
   </xsl:template>

   <xsl:template match="year">
      <xsl:apply-templates />
   </xsl:template>

   <!-- handle the bibliography -->


   <xsl:template match="*">
      <xsl:message>No template matches <xsl:value-of select="name(.)"/>.
      </xsl:message>
      <font color="red">
         <xsl:text>&lt;</xsl:text>
         <xsl:value-of select="name(.)"/>
         <xsl:text>&gt;</xsl:text>
         <xsl:apply-templates/> 
         <xsl:text>&lt;/</xsl:text>
         <xsl:value-of select="name(.)"/>
         <xsl:text>&gt;</xsl:text>
      </font>
   </xsl:template>

</xsl:stylesheet>   


