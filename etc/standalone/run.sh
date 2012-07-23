#!/bin/sh

cd `dirname $0`

java -Xmx3500M -Xms250M \
     -server \
     -DentityExpansionLimit=100000000 \
     -Dfile.encoding=UTF-8 \
     -Dorg.protege.owl.server.configuration=metaproject.owl \
     -Djava.util.logging.SimpleFormatter.format="%4$s: %5$s%n" \
     -classpath lib/felix.jar:lib/ProtegeLauncher.jar \
     org.protege.osgi.framework.Launcher
