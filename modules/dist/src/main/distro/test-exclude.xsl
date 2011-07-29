<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:m="http://maven.apache.org/POM/4.0.0">
  <xsl:param name="targetName"/>
  <xsl:output method="text"/>
  <xsl:template match="/">
    <xsl:for-each select="//m:profile[m:id=$targetName]/m:build/m:plugins/m:plugin[m:artifactId='maven-surefire-plugin']//m:excludes">
      <xsl:for-each select="m:exclude|comment()">
        <xsl:if test="self::comment()">
          <xsl:text>#</xsl:text>
        </xsl:if>
        <xsl:value-of select="."/><xsl:text>&#10;</xsl:text>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet> 
