<#-- @ftlvariable name="" type="com.vdoc.maven.plugin.deploy.vdoc.pom.ParentPOMGeneratorImpl" -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <artifactId>sdk</artifactId>
        <groupId>com.vdoc.engineering</groupId>
        <version>${targetVersion}</version>
    </parent>


    <artifactId>sdk.advanced</artifactId>
    <packaging>pom</packaging>

    <properties>
        <include.other.modules>false</include.other.modules>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.axemble.vdoc</groupId>
            <artifactId>VDocEAR</artifactId>
            <version>${r"${vdoc.version}"}</version>
            <scope>provided</scope>
            <type>pom</type>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- active default parent plugins -->
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

    <profiles>

        <profile>
            <!-- enable the setup build -->
            <id>packager</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-dependency-plugin</artifactId>
                    </plugin>
                    <plugin>
                        <groupId>com.vdoc.maven</groupId>
                        <artifactId>vdoc-maven-plugin</artifactId>
                        <version>${r"${vdoc-maven-plugin.version}"}</version>
                        <executions>
                            <execution>
                                <phase>package</phase>
                                <goals>
                                    <goal>create-setup</goal>
                                </goals>
                                <configuration>
                                    <setupName>${r"${project.artifactId}-${project.version} for VDoc${vdoc.version}"}</setupName>
                                    <packagingType>APPS</packagingType>
                                    <includeOtherModules>${r"${include.other.modules}"}</includeOtherModules>
                                    <vdocHome>${r"${VDOC_HOME}"}</vdocHome>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>

        <profile>
            <!-- enable the hard deploy (require 2 next) -->
            <id>deployer</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>com.vdoc.maven</groupId>
                        <artifactId>vdoc-maven-plugin</artifactId>
                        <version>${r"${vdoc-maven-plugin.version}"}</version>
                        <executions>
                            <execution>
                                <phase>package</phase>
                                <goals>
                                    <goal>hard-deploy</goal>
                                </goals>
                                <configuration>
                                    <includeTest>true</includeTest>
                                    <vdocHome>${r"${VDOC_HOME}"}</vdocHome>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>

		<profile>
            <id>release</id>
            <build>
                <plugins>
                    <!-- copy setup -->
                    <plugin>
                        <artifactId>maven-antrun-plugin</artifactId>
                        <version>1.8</version>
                        <executions>
                            <execution>
                                <phase>verify</phase>
                                <goals>
                                    <goal>run</goal>
                                </goals>
                                <configuration>
                                    <target>
                                        <copy todir="setup">
                                            <fileset dir="${r"${project.build.directory}"}">
                                                <include name="*-setup.zip" />
                                            </fileset>
                                        </copy>
                                    </target>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                    <!-- add setup to scm -->
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-scm-plugin</artifactId>
                        <version>1.9.4</version>
                        <executions>
                            <execution>
                                <id>remove old setup</id>
                                <phase>prepare-package</phase>
                                <goals>
                                    <goal>remove</goal>
                                    <goal>checkin</goal>
                                </goals>
                                <configuration>
                                    <basedir>setup</basedir>
                                    <includes>*</includes>
                                    <message>remove old setup</message>
                                </configuration>
                            </execution>
                            <execution>
                                <id>add new setup</id>
                                <phase>install</phase>
                                <goals>
                                    <goal>add</goal>
                                    <goal>checkin</goal>
                                </goals>
                                <configuration>
                                    <basedir>setup</basedir>
                                    <includes>*</includes>
                                    <message>add setup</message>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-release-plugin</artifactId>
                        <version>2.5.3</version>
                        <configuration>
                            <preparationGoals>clean deploy</preparationGoals>
                            <tagNameFormat>${r"@{project.artifactId}"} v${r"@{project.version}"} for ${r"${vdoc.version}"}</tagNameFormat>
                            <tagBase>${r"${svn.url}"}/tags</tagBase>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>

</project>