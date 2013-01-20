package org.protege.owl.server;

import java.io.File;
import java.util.Hashtable;
import java.util.Locale;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.protege.owl.server.api.ServerConfiguration;
import org.protege.owl.server.configuration.MetaprojectVocabulary;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Activator implements BundleActivator {
	public static final String SERVER_CONFIGURATION_PROPERTY = "org.protege.owl.server.configuration";
	private Logger logger = Logger.getLogger(Activator.class.getCanonicalName());

	@Override
	public void start(BundleContext context) throws OWLOntologyCreationException  {
		String configuration = System.getProperty(SERVER_CONFIGURATION_PROPERTY);
		if (configuration != null) {
			displayPlatform(context);
			loadConfiguration(context, configuration);
		}
	}
	
    private void displayPlatform(BundleContext context) {
    	logger.info("Server configuration started.");
    	logger.info("    User id: " + System.getProperty("user.name"));
        logger.info("    Java: JVM " + System.getProperty("java.runtime.version") +
                " Memory: " + (Runtime.getRuntime().maxMemory() / 1000000) + "M");
        logger.info("    Language: " + Locale.getDefault().getLanguage() +
                ", Country: " + Locale.getDefault().getCountry());
        logger.info("    Framework: " + context.getProperty(Constants.FRAMEWORK_VENDOR) + " (" + context.getProperty(Constants.FRAMEWORK_VERSION) + ")");
        logger.info("    OS: " + context.getProperty(Constants.FRAMEWORK_OS_NAME) + " (" + context.getProperty(Constants.FRAMEWORK_OS_VERSION) + ")");
        logger.info("    Processor: " + context.getProperty(Constants.FRAMEWORK_PROCESSOR));
    }
	
	private void loadConfiguration(BundleContext context, String configuration) throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		MetaprojectVocabulary.addIRIMapper(manager);
		final OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(configuration));
		context.registerService(ServerConfiguration.class, new ServerConfiguration() {
		    @Override
		    public OWLOntology getMetaOntology() {
		        return ontology;
		    }
		}, 
		new Hashtable<String, String>());
	}
	
	@Override
	public void stop(BundleContext context) {

	}

}
