package org.protege.owl.server.core;

import java.io.Serializable;

import org.protege.owl.server.api.server.ServerPath;
import org.protege.owl.server.changes.api.RemoteOntologyDocument;
import org.protege.owl.server.changes.api.RemoteServerDocument;
import org.protege.owl.server.changes.api.ServerOntologyDocument;

import org.semanticweb.owlapi.model.IRI;

/**
 * This class represents an ontology document that is obtained from an OWL
 * server by giving a location and a revision.
 * <p>
 * From the OWL 2 Specification, an Ontology Document is defined as follows:
 * "Each ontology is associated with an <em>ontology document</em>, which
 * physically contains the ontology stored in a particular way. The name
 * "ontology document" reflects the expectation that a large number of
 * ontologies will be stored in physical text documents written in one of the
 * syntaxes of OWL 2." By referencing a location on an OWL Server and providing
 * a revision, enough information has been given to instantiate an ontology.
 * Thus this would appear to be a reasonable definition of an Ontology Document
 * in the OWL 2 sense.
 * 
 * @author tredmond
 */
public class ServerOntologyDocumentImpl extends ServerDocumentImpl implements ServerOntologyDocument, Serializable {
 
    private static final long serialVersionUID = 8685750766323114980L;

    public ServerOntologyDocumentImpl(ServerPath path) {
        super(path);
    }

    public RemoteOntologyDocument createRemoteDocument(final String scheme, final String host, final int port) {
        return new RemoteOntologyDocumentImpl(scheme, host, port);
    }

    @Override
    public String toString() {
        return "<Doc: " + getServerPath() + ">";
    }

    private class RemoteOntologyDocumentImpl implements RemoteOntologyDocument, Serializable {
        private static final long serialVersionUID = -6446764435372376878L;
        private String scheme;
        private String host;
        private int port;

        public RemoteOntologyDocumentImpl(final String scheme, final String host, final int port) {
            this.scheme = scheme;
            this.host = host;
            this.port = port;
        }

        @Override
        public IRI getServerLocation() {
            return getServerPath().getIRI(scheme, host, port);
        }

        @Override
        public Object getProperty(String key) {
            return ServerOntologyDocumentImpl.this.getProperty(key);
        }

        @Override
        public ServerOntologyDocument createServerDocument() {
            return ServerOntologyDocumentImpl.this;
        }

        @Override
        public String toString() {
            return "<Doc: " + getServerLocation() + ">";
        }

        @Override
        public int compareTo(RemoteServerDocument o) {
            return getServerLocation().compareTo(o.getServerLocation());
        }
    }
}
