#!/bin/bash

if [ ! -d "target" ]; then
    echo "Please run it under a module directory. And make sure it's not parent module directory. And make a maven install first"
    exit
fi
mvn dependency:build-classpath -DincludeTypes=jar -Dmdep.outputFile=classpath.txt
jshell --class-path `cat classpath.txt`:target/classes "$@"
