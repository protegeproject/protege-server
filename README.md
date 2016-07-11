OWL Ontology Server
===================

An OWL ontology server for OWL API programs, e.g., Protege Desktop
(versions 5.0 and above). Intended to be used with the [Protege
Client][1].

This branch differs from `metaproject-integration`, mainly in the use
of `HTTP` and [Undertow][2] as a web server. Otherwise it borrows
heavily from the previous `RMI` version.

### Building and running

The usual:

````
mvn clean install
````

will produce a jar file that can be added to the protege bundles. This
branch also comes with a `server-configuration.json` file that
contains the specs for a small sample project. At the top level, the
`root` directory contains the history database and snapshot for this
small ontology.

There is also a small set of [integration][3] tests, that will start a
server and run some tests end to end.


----
[1]: http://github.com/protegeproject/protege-client
[2]: http://undertow.io/
[3]: https://github.com/bdionne/protege-client-server-integration-tests
