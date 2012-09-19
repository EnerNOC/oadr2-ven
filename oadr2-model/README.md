# OpenADR 2.0a Model #

This is a simple project that generates Java classes from the OpenADR 2.0a schemas.

To customize the package name of generated classes, edit the `<generatePackage>` 
element in [pom.xml].


## Dependencies/ Build Instructions ##

The only hard dependencies are Java 5+ and [Apache Maven 3+] (http://maven.apache.org).  
All other Java dependencies are downloaded automatically.

Build (and run unit tests) by running:
   
    $ mvn clean package
