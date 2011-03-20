#!/bin/sh

java -classpath bin/felix.jar:bin/ProtegeLauncher.jar \
     -Dorg.protege.launch.config=server-config.xml \
     org.protege.osgi.framework.Launcher
