#!/bin/sh

cd `dirname $0`

java -Xmx3500M -Xms250M \
     -server \
     -DentityExpansionLimit=100000000 \
     -Dfile.encoding=UTF-8 \
     -Dorg.protege.owl.server.configuration=metaproject.pprj
     -classpath lib/felix.jar:lib/ProtegeLauncher.jar \
     org.protege.osgi.framework.Launcher
