#!/bin/sh

cd `dirname $0`

java -Xmx3500M -Xms250M \
     -server \
     -Djava.rmi.server.hostname=`hostname` \
     -DentityExpansionLimit=100000000 \
     -Dfile.encoding=UTF-8 \
     -Dorg.protege.owl.server.configuration=metaproject.owl \
     -Dlogback.configurationFile=conf/logback.xml \
     -classpath bundles/guava.jar:bundles/slf4j-api.jar:bundles/logback-core.jar:bundles/logback-classic.jar:bin/org.apache.felix.main.jar:bin/protege-launcher.jar \
     org.protege.osgi.framework.Launcher
