package org.protege.owl.server.api;

import java.io.Serializable;

import org.semanticweb.owlapi.model.IRI;

public class RemoteOntology implements Serializable {
    private static final long serialVersionUID = 6539431654428651110L;
    private Integer revision;
    private IRI ontologyName;
    
    public RemoteOntology(IRI ontologyName, Integer revision) {
        this.ontologyName = ontologyName;
        this.revision = revision;
    }

    public Integer getRevision() {
        return revision;
    }
    public void setRevision(Integer revision) {
        this.revision = revision;
    }
    public IRI getOntologyName() {
        return ontologyName;
    } 
}
