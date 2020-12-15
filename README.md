appengine-maven-plugin
=========================

A Maven plugin to support App Engine developers.

A more comprehensive document for users is available at our official Using Apache Maven documentation site.

Apache Maven is a software project management and comprehension tool. It is capable of building war files for deployment into App Engine. The App Engine Maven Plugin can further simplify development beyond what Maven provides. It does so by enabling the following features in Maven.
 - Running the App Engine development application server
 - Deployment of applications to App Engine
 
 To get started, just add the plugin to Maven with:

      <plugin>
          <groupId>com.google.appengine</groupId>
          <artifactId>appengine-maven-plugin</artifactId>
          <version>1.9.84</version>
      </plugin>

Make sure you are using Maven 3.1 or above (the 3.0.x version will not work starting with GAE 1.8.3)
Then many goals including appengine:update, appengine:rollback, and appengine:devserver are now available. You can run mvn help:describe -Dplugin=appengine to see all of the available goals.
A sample project is hosted on github

A complete sample pom is:
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>
    <packaging>war</packaging>
    <version>1.0-SNAPSHOT</version>

    <groupId>com.google.appengine.demos</groupId>
    <artifactId>guestbook</artifactId>

    <properties>
        <appengine.target.version>1.9.81</appengine.target.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- Compile/runtime dependencies -->
        <dependency>
            <groupId>com.google.appengine</groupId>
            <artifactId>appengine-api-1.0-sdk</artifactId>
            <version>${appengine.target.version}</version>
        </dependency>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>servlet-api</artifactId>
            <version>2.5</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>jstl</groupId>
            <artifactId>jstl</artifactId>
            <version>1.2</version>
        </dependency>

        <!-- Test Dependencies -->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.10</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-all</artifactId>
            <version>1.9.0</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.google.appengine</groupId>
            <artifactId>appengine-testing</artifactId>
            <version>${appengine.target.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.google.appengine</groupId>
            <artifactId>appengine-api-stubs</artifactId>
            <version>${appengine.target.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <version>2.5.1</version>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>2.3</version>
                <configuration>
                    <archiveClasses>true</archiveClasses>
                </configuration>
            </plugin>
            <plugin>
                <groupId>com.google.appengine</groupId>
                <artifactId>appengine-maven-plugin</artifactId>
                <version>${appengine.target.version}</version>
            </plugin>
        </plugins>
    </build>

</project>

If you are using a snapshot version (experimental), you'll need to add the Google Maven snapshot repository to your pom.

    <pluginRepositories>
        <pluginRepository>
            <id>google-snapshots</id>
            <name>Google Snapshots</name>
            <url>https://oss.sonatype.org/content/repositories/google-snapshots/</url>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
            <releases>
                <enabled>false</enabled>
            </releases>
        </pluginRepository>
    </pluginRepositories>
