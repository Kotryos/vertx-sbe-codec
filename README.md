# Introduction
This is an example project that features a Vert.x Event Bus message codec powered by 
[Twitter Serial framework](https://github.com/twitter/Serial). 

# Benchmarks
Comparing to `JsonObjectMessageCodec`, the message codec based on Twitter Serial is on average from 2 to 4 times faster .

To run JMH benchmarks:
1. `mvn clean install` from the root of the project.
2. `java -jar message-codec-vertx-serial.jar` from build directory.

# Notes
Usage of this message codec limits the language choices for Vert.x verticles to just Java.