#!/bin/bash

# Build with maven
mvn package -B -DskipTests=true -f pom.xml

# Use Java 20
env JAVA_HOME=/home/daniel/Workspace/IntelliJ/.sdks/openjdk-20
env PATH=$JAVA_HOME/bin:$PATH

# Create rpm package
jpackage --input target --main-jar viro-1.0.jar --module-path .sdk/javafx-sdk-21.0.2/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics --name viro --type rpm
