#!/bin/sh

mkdir -p out;
javac -d out src/build/*.java -cp lib/commons-math3-3.6.1.jar;
javac -d out -cp out:lib/junit-platform-console-standalone-1.5.2.jar src/test/*.java;
java -jar lib/junit-platform-console-standalone-1.5.2.jar --class-path out:lib/commons-math3-3.6.1.jar --scan-class-path;
