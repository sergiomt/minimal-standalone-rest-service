# Minimal standalone REST service

Minimum implementation of a standalone REST service using Jersey.

This project generates a "fat JAR" standalone-rest-1.0-jar-with-dependencies.jar which self-contains all the required dependencies for an HTTP REST service.

From the command line execute:

`mvn install`

then move to the /target folder and execute

`java -jar standalone-rest-1.0-jar-with-dependencies.jar`

To test, go to your web browser and type:

http://localhost:9999/hello/
