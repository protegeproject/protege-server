#!/bin/sh
 
cd `dirname $0`
 
nohup java -Xmx4000M -Xms1500M \
     -Dorg.protege.owl.server.configuration=server-configuration.json \
     -Dorg.protege.editor.owl.server.security.ssl.keystore=protege-server.jks \
     -Dorg.protege.editor.owl.server.security.ssl.keystore.type=JKS \
     -Dorg.protege.editor.owl.server.security.ssl.password=<password-to-keystore> \
     -cp "bundles/*" \
     org.protege.editor.owl.server.http.HTTPServer </dev/null >console.txt 2>&1 &
     