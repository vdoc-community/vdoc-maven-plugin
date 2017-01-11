<#-- @ftlvariable name="" type="com.vdoc.maven.plugin.deploy.vdoc.pom.ParentPOMGeneratorImpl" -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.vdoc.engineering</groupId>
    <artifactId>sdk</artifactId>
    <version>${targetVersion}</version>

    <packaging>pom</packaging>

    <organization>
        <name>Visiativ Software</name>
        <url>http://www.myvdoc.net</url>
    </organization>

    <properties>
        <vdoc.version>${targetVersion}</vdoc.version>
        <project.build.sourceEncoding>cp1252</project.build.sourceEncoding>
        <skipTests>true</skipTests>
        <vdoc-maven-plugin.version>${maven.pluginDescriptor.version}</vdoc-maven-plugin.version>
    </properties>

    <dependencies>
    </dependencies>

    <build>
        <pluginManagement>
            <plugins>
                <!-- change default to JDK 1.7-->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.1</version>
                    <configuration>
                        <source>
                        1.7</source>
                        <target>1.7</target>
                        <fork>true</fork>
                    </configuration>
                </plugin>

                <!-- create a nice jar -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-jar-plugin</artifactId>
                    <version>2.5</version>
                    <configuration>
                        <archive>
                            <index>true</index>
                            <manifest>
                                <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
                                <addDefaultSpecificationEntries>true</addDefaultSpecificationEntries>
                                <addClasspath>true</addClasspath>
                            </manifest>
                            <manifestEntries>
                                <Build-Time>${r"${maven.build.timestamp}"}</Build-Time>
                                <Build-Maven>Maven ${r"${maven.version}"}</Build-Maven>
                                <Build-Label>${r"${project.version}"}</Build-Label>
                            </manifestEntries>
                        </archive>
                    </configuration>
                </plugin>

                <!-- package the vdoc apps -->
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-dependency-plugin</artifactId>
                    <executions>
                        <execution>
                            <id>copy-dependencies</id>
                            <phase>prepare-package</phase>
                            <goals>
                                <goal>copy-dependencies</goal>
                            </goals>
                            <configuration>
                                <excludeScope>provided</excludeScope>
                                <outputDirectory>${r"${project.build.directory}"}/lib</outputDirectory>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>


            </plugins>
        </pluginManagement>
    </build>

    <profiles>
        <profile>
            <id>vdoc-configuration</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.codehaus.mojo</groupId>
                        <artifactId>properties-maven-plugin</artifactId>
                        <version>1.0-alpha-2</version>
                        <executions>
                            <execution>
                                <phase>initialize</phase>
                                <goals>
                                    <goal>read-project-properties</goal>
                                </goals>
                                <configuration>
                                    <files>
                                        <file>home.properties</file>
                                        <file>../home.properties</file>
                                    </files>
                                    <quiet>true</quiet>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>

    <distributionManagement>
        <repository>
            <id>vdoc</id>
            <name>vdoc repository</name>
            <url>${r"${vdoc.deploy.server}"}/nexus/content/repositories/releases/</url>
        </repository>
        <snapshotRepository>
            <id>vdoc.snapshot</id>
            <name>vdoc snapshot repository</name>
            <url>${r"${vdoc.deploy.server}"}/nexus/content/repositories/snapshots/</url>
        </snapshotRepository>
    </distributionManagement>

</project>
