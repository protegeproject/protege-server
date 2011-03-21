#!/bin/sh

java -classpath bin/felix.jar:bin/ProtegeLauncher.jar \
     -Dorg.protege.launch.config=server-config.xml \
     -Dlog4j.configuration=file:log4j.xml \
     org.protege.osgi.framework.Launcher sample-metaproject.owl
