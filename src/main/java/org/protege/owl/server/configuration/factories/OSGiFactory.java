package org.protege.owl.server.configuration.factories;

import static org.protege.owl.server.configuration.MetaprojectVocabulary.OSGI_SHUTDOWN_FILTER;

import org.osgi.framework.BundleContext;
import org.protege.owl.server.api.Server;
import org.protege.owl.server.api.ServerFilter;
import org.protege.owl.server.osgi.OSGiAware;
import org.protege.owl.server.osgi.OSGiShutdownFilter;
import org.protege.owl.server.util.ServerComponentFactoryAdapter;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class OSGiFactory extends ServerComponentFactoryAdapter implements OSGiAware {
    private OWLOntology configuration;
    private BundleContext context;

    @Override
    public void setConfiguration(OWLOntology ontology) {
        this.configuration = ontology;
    }

    @Override
    public void activate(BundleContext context) {
        this.context = context;
    }

    @Override
    public void deactivate(BundleContext context) {
        ;
    }
    
    @Override
    public boolean hasSuitableServerFilter(OWLIndividual i) {
        OWLDataFactory factory = configuration.getOWLOntologyManager().getOWLDataFactory();
        return context != null && configuration.containsAxiom(factory.getOWLClassAssertionAxiom(OSGI_SHUTDOWN_FILTER, i));
    }
    
    @Override
    public ServerFilter createServerFilter(OWLIndividual i, Server server) {
        return new OSGiShutdownFilter(server, context);
    }
    
    @Override
    public String toString() {
        return "Plugin Infrastructure Management";
    }

}
