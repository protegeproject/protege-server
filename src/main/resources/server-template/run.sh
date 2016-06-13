#!/bin/sh

cd `dirname $0`

java -Xmx3500M -Xms250M \
     -Dorg.protege.owl.server.configuration=server-configuration.json \
     -cp "bundles/*" \
     org.protege.editor.owl.server.http.HTTPServer
