# Restcomm Maven DU Plugin
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bhttps%3A%2F%2Fgithub.com%2FRestComm%2Fjain-slee.maven-du-plugin.svg?type=shield)](https://app.fossa.io/projects/git%2Bhttps%3A%2F%2Fgithub.com%2FRestComm%2Fjain-slee.maven-du-plugin?ref=badge_shield)


The Maven DU Plugin can be used to manage build lifecycle of JAIN SLEE 1.1 Deployable Units (DUs) jars. It provides the following goals:

1. **copy-dependencies** - copies artifacts declared as dependencies to the deployable unit, assuming these are JAIN SLEE component (sbb,events,etc.) jars.
2. **generate-descriptor** - generates the deployable unit XML descriptor, taking as input specific directories as sources of SLEE component jars and service XML descriptors.
3. **generate-ant-management-script** - generates an Apache Ant script, which can be used to (un)deploy the deployable unit jar, using file copy/delete or JMX.

_Note: parameters and properties will be represented by its name between ${}. For instance, the parameter X, when referred, will be through ${X}_

## Quick Start

To add the plugin to Maven build lifecycle simply add, to the top level <build /> element, the following XML:

```
    <plugin>  
      <groupId>org.mobicents.tools</groupId>  
      <artifactId>maven-du-plugin</artifactId>  
      <version>3.0.0.FINAL</version>  
      <executions>  
        <execution>  
          <goals>  
            <goal>copy-dependencies</goal>  
            <goal>generate-descriptor</goal>  
            <goal>generate-ant-management-script</goal>  
          </goals>  
        </execution>  
      </executions>  
    </plugin>  
```

This will tell Maven to execute all the goals provided by the plugin, in the default phases of its build lifecycle. Note that it's not mandatory to use all these goals, for instance it is expected that many usages don't declare the generate-ant-managent-script. 
To build the Deployable Unit jar, and install it in the local Maven repository, simply execute the command:

mvn install

The resulting jar will be created in the target directory.
Also, with no custom plugin configuration, upon execution, there are a few assumptions:

1. the SLEE component jars, to be added to the DU jar, are all dependencies declared in the pom, and by its parent.
2. the SLEE Service XML descriptors, to be added to the DU jar, are all XML files in the src/main/resources/services directory
 

The resulting DU jar will contain the SLEE component jars in the jars directory, and the service XML descriptors in the services directory.

 
## Plugin Goals
### copy-dependencies

The copy-dependencies goal is in fact provided by the standard [Maven Dependency Plugin (maven-dependency-plugin)](http://maven.apache.org/plugins/maven-dependency-plugin/index.html), the Mobicents plugin simply exposes it, changing its default configuration to be consistent with the usage of other Mobicents plugin goals:

* the outputDirectory parameter, which defines where the dependencies are copied to, is set to ${project.build.outputDirectory}/jars
* the excludeTransitive parameter, which defines whether dependencies of dependencies should be copied too, is set to true.

In case the reader is not used to Maven yet, dependencies are declared through the <dependencies /> element, here is an example which declares several artifacts to be copied into the deployable unit:

```
    <dependencies>  
      <dependency>  
        <groupId>${project.groupId}</groupId>  
        <artifactId>sip11-library</artifactId>  
        <version>${project.version}</version>  
      </dependency>  
      <dependency>  
        <groupId>${project.groupId}</groupId>  
        <artifactId>sip11-events</artifactId>  
        <version>${project.version}</version>  
      </dependency>  
      <dependency>  
        <groupId>${project.groupId}</groupId>  
        <artifactId>sip11-ratype</artifactId>  
        <version>${project.version}</version>  
      </dependency>  
      <dependency>  
        <groupId>${project.groupId}</groupId>  
        <artifactId>sip11-ra</artifactId>  
        <version>${project.version}</version>  
      </dependency>    
    </dependencies>  
```
 
### generate-descriptor

The generate-descriptor goal is responsible for generating the DU XML descriptor, the deployable-unit.xml file inside the META-INF directory. In short it collects the file names of jars and XML files, in specific directories, and then generates the proper XML.

It is possible to customize the plugin's usage through it's configuration parameters:

* **workDirectory** - the work directory is the base path, used to calculate other directories which may not be set. By default this parameter points to Maven property ${project.build.outputDirectory}, which by default points to the directory target/classes.
* **duXmlOutputDirectory** - the directory to be used as the output for the generated SLEE du xml descriptor. By default this parameter is set with value  ${workDirectory}/META-INF.
* **duJarDirectory** - the path, from the DU jar's root, where SLEE component jars will be packaged. If this parameter is set, and considering a jar named file.jar, then each <jar /> entry in the descriptor will be <jar>${duJarDirectory}/file.jar</jar>, otherwise it will be will be <jar>file.jar</jar>. By default this parameter value is jars.
* **jarInputDirectory** - the directory to be used as the source for SLEE component jars. If the parameter is not set, ${workDirectory}/${duJarDirectory} or ${workDirectory} will be used, depending if ${duJarDirectory} is set or not. By default  this parameter is not set.
* **duServiceDirectory** - the path, from the DU jar's root, where SLEE Service XML descriptors will be packaged. If this parameter is set, and considering a service XML descriptor named service.xml, then each <service-xml /> entry in the descriptor will be <service-xml>${duServiceDirectory}/service.xml</service-xml>, otherwise it will be will be <service-xml>service.xml</service-xml>. By default this parameter value is services.
* **serviceInputDirectory** - the path, to be used as the source for SLEE service xml descriptors. If the parameter is not set, ${workDirectory}/${duServiceDirectory} or ${workDirectory} will be used, depending if ${duServiceDirectory} is set or not. By default  this parameter is not set.

### generate-ant-management-script

As mentioned in the plugin's introduction, this goal generates an Apache Ant script, which can be used to (un)deploy the deployable unit jar, using file copy/delete or JMX.

This goal is used by Mobicents Team to generate the Ant scripts present in Mobicents JAIN SLEE binary releases.


## Deploying Deployable Unit Jars

Managing DU jars deployed in Mobicents JAIN SLEE can be done with a simple file copy/delete operations, which can be done through the Maven Antrun Plugin. Here is an example on how to deploy the built DU jar in Maven install phase, and undeploy it in clean phase:
 
```
    <plugin>  
       <artifactId>maven-antrun-plugin</artifactId>  
      <executions>  
         <execution>  
          <id>deploy-DU</id>  
          <phase>install</phase>  
          <goals>  
            <goal>run</goal>  
          </goals>  
          <configuration>  
            <tasks>  
              <copy overwrite="true" file="target/${project.build.finalName}.jar"  
                todir="${jboss.home}/server/${node}/deploy" />  
            </tasks>  
          </configuration>  
        </execution>  
        <execution>  
          <id>undeploy-DU</id>  
          <phase>clean</phase>  
          <goals>  
            <goal>run</goal>  
          </goals>  
          <configuration>  
            <tasks>  
              <delete file="${jboss.home}/server/${node}/deploy/${project.build.finalName}.jar" />  
            </tasks>  
          </configuration>  
        </execution>  
      </executions>  
    </plugin>  
```
 
## Frequent Asked Questions

* **Is the plugin compatible with M2Eclipse Plugin?** Yes, but you need to turn off Workspace Resolution or Resolve Workspace Dependencies when creating or importing the project. It is also recommended to use an external installation of Maven since the one embedded in M2Eclipse is quite different than the usual apache binaries.
* **Some jars, which are dependencies declared in the pom's parent, are bundled in the DU jar, how to avoid that?** There are several configuration parameters, which can be used by the copy-dependencies goal, to exclude one or more dependencies. See the [maven-dependency-plugin site](http://maven.apache.org/plugins/maven-dependency-plugin/copy-dependencies-mojo.html) for details and examples.

## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bhttps%3A%2F%2Fgithub.com%2FRestComm%2Fjain-slee.maven-du-plugin.svg?type=large)](https://app.fossa.io/projects/git%2Bhttps%3A%2F%2Fgithub.com%2FRestComm%2Fjain-slee.maven-du-plugin?ref=badge_large)