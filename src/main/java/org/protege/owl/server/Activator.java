package org.protege.owl.server;

import java.io.File;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.protege.owl.server.api.ConfigurationManager;
import org.protege.owl.server.metaproject.MetaProject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Activator implements BundleActivator {
    private Logger LOGGER = Logger.getLogger(Activator.class);
    private BundleContext context;
    private String metaproject;
    private ServiceListener listener = new ServiceListener() {
        public void serviceChanged(ServiceEvent event) {
            if (loadCommandLineMetaproject()) {
                context.removeServiceListener(listener);
            }
        }
    };


    @Override
    public void start(final BundleContext context) throws Exception {
        this.context = context;
        final String metaproject = System.getProperty("command.line.arg.0");
        
        LOGGER.info("***********************************************");
        LOGGER.info("\tAttempting to start server using metaproject " + metaproject);
        
        this.metaproject = metaproject;
        if (metaproject != null) {
            if (!loadCommandLineMetaproject()) {
                context.addServiceListener(listener);
            }
        }
    }
    
    
    
    private boolean loadCommandLineMetaproject() {
        try {
            ServiceReference sr = context.getServiceReference(ConfigurationManager.class.getCanonicalName());
            if (sr != null) {
                ConfigurationManager configManager = (ConfigurationManager) context.getService(sr);
                OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
                MetaProject.addMetaProjectIRIMapper(manager);
                OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(metaproject));
                configManager.setMetaOntology(ontology);
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.error("Exception caught trying to load metaproject: " + metaproject, e);
        }
        return false;
    }
    


    @Override
    public void stop(BundleContext context) throws Exception {
    	LOGGER.info("Uninstalling server...");
    }

}
