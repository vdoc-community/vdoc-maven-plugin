<#-- @ftlvariable name="" type="com.vdoc.maven.plugin.deploy.vdoc.pom.ParentPOMGeneratorImpl" -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <artifactId>sdk.advanced</artifactId>
        <groupId>com.vdoc.engineering</groupId>
        <version>${targetVersion}</version>
    </parent>


    <artifactId>sdk-advanced-pack-process</artifactId>
    <packaging>pom</packaging>

    <properties>
        <includeTestDataCreation>true</includeTestDataCreation>
        <includeDependenciesSetups>true</includeDependenciesSetups>
        <packagingType>PACK_PROCESS</packagingType>
    </properties>

</project>