<#-- @ftlvariable name="" type="com.vdoc.maven.plugin.deploy.vdoc.pom.ParentPOMGeneratorImpl" -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

	<groupId>com.vdoc.engineering</groupId>
    <artifactId>vdoc.suite</artifactId>
    <version>${targetVersion}</version>
    <packaging>pom</packaging>

    <dependencies>
    	<dependency>
			<groupId>com.axemble.vdoc</groupId>
			<artifactId>VDocEAR</artifactId>
			<version>${targetVersion}</version>
			<scope>provided</scope>
			<type>pom</type>
		</dependency>
    </dependencies>

</project>