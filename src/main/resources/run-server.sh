#!/bin/sh

cd `dirname $0`

java -Xmx3500M -Xms250M \
     -server \
     -Dlog4j.configuration=file:log4j.xml \
     -DentityExpansionLimit=100000000 \
     -Dfile.encoding=UTF-8 \
     -classpath bin/felix.jar:bin/ProtegeLauncher.jar \
     -Dorg.protege.launch.config=./server-config.xml \
     org.protege.osgi.framework.Launcher
