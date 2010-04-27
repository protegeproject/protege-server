#!/bin/sh

java -Dlog4j.configuration=file:log4j.xml -Dfelix.config.properties=file:conf/server-config.properties -jar bin/felix.jar
