package org.protege.owl.server.configuration;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.owl.server.api.ConfigurationManager;
import org.protege.owl.server.configuration.ServerBuilder;
import org.protege.owl.server.api.ServerFactory;
import org.protege.owl.server.metaproject.Vocabulary;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * This class will configure all the servers that are specified in the metaproject.  Each server is specified
 * by an OWL individual in the metaproject.
 * 
 * @author tredmond
 *
 */
public class ConfigurationManagerImpl implements ConfigurationManager {
    public final static Logger LOGGER = Logger.getLogger(ConfigurationManagerImpl.class);
    
    private Set<ServerBuilder> builders = new HashSet<ServerBuilder>();
    private Set<ServerFactory> serverFactories = new HashSet<ServerFactory>();
    
    public ConfigurationManagerImpl() {

    }
    
    public synchronized void setMetaOntology(OWLOntology ontology) {
        for (ServerBuilder c : builders) {
            c.stop();
        }
        builders.clear();
        Set<OWLIndividual> configs = Vocabulary.OWL2_SERVER_CLASS.getIndividuals(ontology);
        for (OWLIndividual config : configs) {
            ServerConfiguration serverConfiguration = new ServerConfiguration(ontology, config);
            builders.add(new ServerBuilder(serverConfiguration));
        }
        for (ServerBuilder builder : builders) {
            for (ServerFactory factory : serverFactories) {
                builder.addServerFactory(factory);
            }
        }
    }

    @Override
    public synchronized void addServerFactory(ServerFactory factory) {
        serverFactories.add(factory);
        for (ServerBuilder builder : builders) {
            builder.addServerFactory(factory);
        }
    }

    @Override
    public synchronized void removeServerFactory(ServerFactory factory) {
        serverFactories.remove(factory);
        for (ServerBuilder builder : builders) {
            builder.removeServerFactory(factory);
        }
    }

    @Override
    public void start() {
        for (ServerBuilder builder : builders) {
            builder.start();
        }    
    }

    @Override
    public void stop() {
        for (ServerBuilder builder : builders) {
            builder.stop();
        }   
    }

}
