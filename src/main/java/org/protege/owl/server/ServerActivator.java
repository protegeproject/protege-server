package org.protege.owl.server;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Locale;

import edu.stanford.protege.metaproject.Manager;
import edu.stanford.protege.metaproject.api.ServerConfiguration;
import edu.stanford.protege.metaproject.api.exception.ObjectConversionException;

/**
 * @author Josef Hardi <johardi@stanford.edu> <br>
 * Stanford Center for Biomedical Informatics Research
 */
public class ServerActivator implements BundleActivator {

    public static final String SERVER_CONFIGURATION_PROPERTY = "org.protege.owl.server.configuration";

    private Logger logger = LoggerFactory.getLogger(ServerActivator.class);

    @Override
    public void start(BundleContext context) throws OWLOntologyCreationException  {
        String configuration = System.getProperty(SERVER_CONFIGURATION_PROPERTY);
        if (configuration != null) {
            displayPlatform(context);
            loadConfiguration(context, configuration);
        }
    }
    
    private void displayPlatform(BundleContext context) {
        logger.info("Server configuration loaded");
        logger.info("... Java: JVM " + System.getProperty("java.runtime.version") + " Memory: " + (Runtime.getRuntime().maxMemory() / 1000000) + "M");
        logger.info("... Language: " + Locale.getDefault().getLanguage() + ", Country: " + Locale.getDefault().getCountry());
        logger.info("... Framework: " + context.getProperty(Constants.FRAMEWORK_VENDOR) + " (" + context.getProperty(Constants.FRAMEWORK_VERSION) + ")");
        logger.info("... OS: " + context.getProperty(Constants.FRAMEWORK_OS_NAME) + " (" + context.getProperty(Constants.FRAMEWORK_OS_VERSION) + ")");
        logger.info("... Processor: " + context.getProperty(Constants.FRAMEWORK_PROCESSOR));
    }
    
    private void loadConfiguration(BundleContext context, String configPath) throws OWLOntologyCreationException {
        try {
            ServerConfiguration configuration = Manager.getConfigurationManager().loadServerConfiguration(new File(configPath));
            context.registerService(ServerConfiguration.class, configuration, new Hashtable<String, String>());
        }
        catch (FileNotFoundException e) {
            throw new OWLOntologyCreationException(e);
        }
        catch (ObjectConversionException e) {
            throw new OWLOntologyCreationException(e);
        }
    }
    
    @Override
    public void stop(BundleContext context) {
        // NO-OP
    }
}
