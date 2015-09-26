# Karaf - Cassandra

[![Build Status](https://travis-ci.org/ANierbeck/Karaf-Cassandra.svg)](https://travis-ci.org/ANierbeck/Karaf-Cassandra)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.nierbeck.cassandra/Karaf-Cassandra/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.nierbeck.cassandra/Karaf-Cassandra)


it started as a showcase, now it's three features which make it easier to start an embedded Cassandra inside Karaf, 
and use easy Karaf shell commands to communicate with a Cassandra from the Karaf shell. 
Beware, don't use an embedded node in production, for that a std. Cassandra cluster is best to be used. 

# Running Cassandra with Karaf

This Showcase will give you a complete overview of Running Apache Cassandra on Apache Karaf. It contains everything that is needed to run an embedded Apache Cassandra inside Apache Karaf. It also gives you some administrative commands to use with an embedded Cassandra instance. 

The feature file does also contain a feature for easy installation of all needed bundles to have a Apache Cassandra Client installed in Karaf. 
Besides that there is also a Client Shell addition to Karaf contained in the Features. With it you are able to connect to any Cassandra instance via the Cassandra client api. You are able to execute cql skripts and commands that are already prepared, or you can take advantage of the great Command completion of Karaf and use the CQLSH like syntax for USE, DESCRIBE, CREATE, SELECT or INSERT. 

More details about this project can be found at [blog.codecentric.de](https://blog.codecentric.de/?p=25821) and [notizblog.nierbeck.de](http://notizblog.nierbeck.de/2014/12/embedding-apache-cassandra/).  

To run this showcase:   
`git pull https://github.com/ANierbeck/CustomKaraf
mvn clean install`

'git pull https://github.com/ANierbeck/Karaf-Cassandra
mvn clean install`

start the Karaf you just build, the tar.gz to unpack can be found in the assemblies/apache-karaf/target folder. 
install the Feature of this Project in the Karaf shell: 

`feature:repo-add mvn:de.nierbeck.cassandra/Karaf-Cassandra-Feature/1.0.0-SNAPSHOT/xml/features
feature:install Karaf-Cassandra-Embedded Karaf-Cassandra-Shell`

## Embedding Cassandra

One of the features contained in the feature descriptor of this project is for installing an embedded Apache Cassandra instance inside Apache Karaf. For this just install the Karaf-Cassandra-Embedded feature. It will start a Service which takes care of the Embedded instance. This Service provides three methods to start and stop the embedded Cassandra instance or to monitor if it is running (isRunning)

## Cassandra Client

The Karaf-Cassandra-Client feature does install all required bundles to have a Apache Cassandra Client runnable inside your own bundles. 

## Shell utilities

Besides the Karaf-Cassandra-Embedded and Karaf-Cassandra-Client features there exists more, shell enhancements. One is already installed by the Karaf-Cassandra-Embedded feature its the administration commands the other one is a seperate featuer it's the Karaf-Cassandra-Shell feature. 

### Cassandra Admin
This feature will be installed together with the Karaf-Cassandra-Embedded feature. It does give you some administrative commands to asure you that the embedded Cassandra instance is running or to help with development. 

#### start
The command _cassandra-admin:start_ will start an embedded Cassandra service if it isn't already. In case the service is already started it will give you an error. 

#### stop
The command _cassandra-admin:stop_ will stop the embedded Cassandra service if it is started. 

#### isRunning
The command _cassandra-admin:isRunning_ tests wether the embedded Cassandra service is started or not. For scripting convenience reasons it does return **true** or **false**

#### cleanup
The command _cassandra-admin:cleanup_ is a command that should be handled with care. If you are in development mode you might want to cleanup the keyspaces you created while playing around with your embedded cassandra instance. This command will do so. 

### Cassandra Client shell

The Cassandra client shell is detached from the embedded Cassandra and will work with any accessible Apache Cassandra Cluster. 
Following is an overview of the supported commands. 

#### connect
The _cassandra:connect_ command is needed to start to communicate with a Apache Cassandra Cluster it take a hostname and optionally a port.    
`cassandra:connect [-p port] hostname`

#### isConnected
The _cassandra:isConnected_ command returns **true** if the current shell session is connected to a cassandra cluster. If the _USE_ command has also been used it will also print the selected keyspace: **true:keyspace**. In case the _connect_ command hasn't been used it will return a **false** cause no active cassandra session is connected to the shell session.

#### disconnect
The _cassandra:disconnect_ command will disconnect any active cassandra session bound to the shell. It does return **disconnected** in case it has been connected beforehand. 

#### cql
The _cassandra:cql_ command can be used to execute a **CQL** script or multiple lines of CQL code. You can either use the direct standard in, or provide a file name with the option:    
`cassandra:cql -f file:/location/of/file/test.cql`   
alternatevly direct input of CQL does work:    
`cassandra:cql "SELECT * FROM keyspace.table WHERE condition;"`   
If the execution contains rows these will be formated in a Table. 

### CQLSH like support
Additionally to the previous mentioned cql command it is possible to issue direct CQLSH like syntax to the shell after you are connected to a cassandra cluster. Again if you didn't read it before, make sure to use the enhanced Apache Karaf that can be found at: [ANierbeck/CustomKaraf](https://github.com/ANierbeck/CustomKaraf). 

This is needed for having better support for completion with the following cassandra CQLSH like syntax commands. 

### USE
The _cassandra:cqlsh:USE_ command supports completion and helps in finding possible keyspaces.    
`cassandra:cqlsh:USE keyspace_name`

### DESCRIBE
The _cassandra:cqlsh:DESCRIBE_ command will describe either keyspaces, tables or a given table.   
`cassandra:cqlsh:DESCRIBE (keyspace|TABLES|TABLE) [table_name]`  

### CREATE
The _cassandra:cqlsh:CREATE_ command will support you with creating a new keyspace or tables, the completion tries to help with a lot of the syntax, too. Though due to the nature of this command not everything runs smoothly with the completion. But any given CREAET command will be executed by the cassandra driver.    
`cassandra:cqlsh:CREATE ...`

### SELECT
The _cassandra:cqlsh:SELECT_ command does support creating a SELECT. The completion tries to find all possible next values, though sometimes not everything does work with it. The SELECT itself will be exectuted by the cassandra driver and due to the nature of it a table with the result is printed.    
`cassandra:cqlsh:SELECT ...`

### INSERT
The _cassandra:cqlsh:INSERT_ command does supprt creating an INSERT Statement, which is executed by the cassandra driver.   
`casssandra:cqlsh:INSERT ...`

## Feature
As usual you are able to install all of the given showcase by installing the Karaf Feature for it.
Issue the following commands in your Karaf Shell:   
`feature:repo-add mvn:de.nierbeck.cassandra/Karaf-Cassandra-Feature/1.0.0-SNAPSHOT/xml/features`    
`feature:install Karaf-Cassandra-Embedded Karaf-Cassandra-Shell`

This will install all of the above described features. 

# Deployment Considerations

There are some special deployment considerations that can improve your experience with the plugin. Some of them are listed here, but please don't hesitate to contribute more!

## Adding Hyperic SIGAR for Karaf-Cassandra-Embedded

It's not obvious what has to happen to get SIGAR set up in Karaf and it's an obscure enough JAR that there's not a lot of leads on the net. The developer does not manage the classpath in Karaf and SIGAR is a native library. It's not sufficient to simply add the native library to `$KARAF_HOME/lib/boot` as a result.
 
1. Create a directory where the libraries should live, such as `$KARAF_HOME/lib/sigar`. 
1. Unpack the contents of a SIGAR native library [such as the one found here](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.fusesource%22%20AND%20a%3A%22sigar%22%20AND%20l%3A%22native%22) into the directory you created above.
1. Edit your `$KARAF_HOME/bin/setenv` to add the path to your native library: `export KARAF_OPTS="-Djava.library.path=/Users/brian/dev/apache-karaf-4.0.1/lib/sigar"`. \(NB: don't think variable interpolation is used here, so the full path is required\).
1. Launch Karaf, load the feature, and try to install the `Karaf-Cassandra-Embedded` feature. You should get an exception. Note the name of the file that it is looking for. For instance, on OSX, the library is called `libsigar-universal64-macosx.dylib`. **You need to rename the library in `$KARAF_HOME/lib/sigar` to exactly this name!** 
1. Try launching again, the exception should not occur again.

## Setting the Karaf-Cassandra-Embedded configuration

One way to configure `Karaf-Cassandra-Embedded` in your build is by having the Karaf features system update the location of a known configuration on disk. `$KARAF-HOME/etc` is an ideal location for such a file. To lower the bar for new developers, you can combine the loading of `Karaf-Cassandra-Embedded` and the configuration in one step:

1. Add a Cassandra configuration artifact to your repository. In this example, I have added it with a `type` of 'yaml' and a `classifier` of 'config'. 
1. Create a feature for the project that will require `Karaf-Cassandra-Embedded`:
```
    <repository>mvn:de.nierbeck.cassandra/Karaf-Cassandra-Feature/1.0.1-SNAPSHOT/xml/features</repository>
    <feature name="${project.artifactId}" version="${project.version}" description="${project.artifactId}">
        <configfile finalname="etc/cassandra.yaml">mvn:${project.groupId}/${project.artifactId}/${project.version}/yaml/config</configfile>
        <config name="de.nierbeck.cassandra.embedded">
            cassandra.yaml = ${karaf.base}/etc/cassandra.yaml
        </config>
        <feature version="1.0.1-SNAPSHOT">Karaf-Cassandra-Embedded</feature>
        ...
    </feature>
```
1. When this feature is loaded, the config file will be installed and the configuration key set. \(NB: the config file may install after the `Karaf-Cassandra-Embedded` feature is launched due to dependency ordering. In that case, it won't take effect until the second launch of `Karaf-Cassandra-Embedded`. Maybe someone can update this config so that's not a problem...\)
