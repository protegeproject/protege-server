package org.protege.owl.server.metaproject;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public class Vocabulary {
    private static final OWLDataFactory factory = OWLManager.getOWLDataFactory();
    public static final String NS = "http://smi-protege.stanford.edu/ontologies/metaproject.owl";
    
    /* **********************************************************
     * Classes
     */

    public static final OWLClass DATABASE_SERVER_BACKEND_CLASS = factory.getOWLClass(IRI.create(NS + "#DatabaseServerBackend"));

    public static final OWLClass RESTFUL_SERVICES_CONNECTIONMANAGER_CLASS = factory.getOWLClass(IRI.create(NS + "#RestfulServicesConnectionManager"));

    public static final OWLClass SERVER_CLASS = factory.getOWLClass(IRI.create(NS + "#Server"));

    public static final OWLClass STRICT_CONFLICT_MANAGER_CLASS = factory.getOWLClass(IRI.create(NS + "#StrictConflictManager"));

    /* **********************************************************
     * Object Properties
     */    
    public static final OWLObjectProperty CONFLICT_MANAGER_COMPONENT_PROPERTY = factory.getOWLObjectProperty(IRI.create(NS + "#conflictManagerComponent"));

    public static final OWLObjectProperty CONNECTION_COMPONENT_PROPERTY = factory.getOWLObjectProperty(IRI.create(NS + "#connectionComponent"));

    public static final OWLObjectProperty SERVER_BAKEND_COMPONENT_PROPERTY = factory.getOWLObjectProperty(IRI.create(NS+"#serverBakendComponent"));

    /* **********************************************************
     * Data Properties
     */

    public static final OWLDataProperty URL_PROPERTY = factory.getOWLDataProperty(IRI.create(NS + "#url"));

    public static final OWLDataProperty PASSWORD_PROPERTY = factory.getOWLDataProperty(IRI.create(NS + "#password"));

    public static final OWLDataProperty USERNAME_PROPERTY = factory.getOWLDataProperty(IRI.create(NS + "#username"));

}
