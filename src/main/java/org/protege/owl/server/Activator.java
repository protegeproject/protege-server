package org.protege.owl.server;

import java.io.File;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.protege.owl.server.api.Builder;
import org.protege.owl.server.configuration.MetaprojectVocabulary;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Activator implements BundleActivator {
	public static final String SERVER_CONFIGURATION_PROPERTY = "org.protege.owl.server.configuration";

	@Override
	public void start(BundleContext context) throws OWLOntologyCreationException  {
		String configuration = System.getProperty(SERVER_CONFIGURATION_PROPERTY);
		if (configuration != null) {
			loadConfiguration(context, configuration);
		}
	}
	
	private void loadConfiguration(BundleContext context, String configuration) throws OWLOntologyCreationException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		MetaprojectVocabulary.addIRIMapper(manager);
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(configuration));
		loadConfiguration(context, ontology);
	}

	private void loadConfiguration(final BundleContext context, final OWLOntology configuration) {
		ServiceReference<Builder> builderSr = context.getServiceReference(Builder.class);
		if (builderSr != null) {
			Builder builder = context.getService(builderSr);
			try {
				builder.setConfiguration(configuration);
			}
			finally {
				context.ungetService(builderSr);
			}
		}
		else {
			final ServiceListener listener = new ServiceListener() {

				@Override
				public void serviceChanged(ServiceEvent e) {
					if (e.getType() == ServiceEvent.REGISTERED) {
						Object o = context.getService(e.getServiceReference());
						if (o instanceof Builder) {
							Builder builder = (Builder) o;
							builder.setConfiguration(configuration);
							context.ungetService(e.getServiceReference());
						}
					}
				}

			};
			try {
				context.addServiceListener(listener, "(" + Constants.OBJECTCLASS + "=" + Builder.class.getCanonicalName() + ")");
			}
			catch (InvalidSyntaxException ise) {
				throw new RuntimeException("Unexpected service exception", ise);
			}
		}
	}
	
	@Override
	public void stop(BundleContext context) {
		// TODO Auto-generated method stub

	}

}
