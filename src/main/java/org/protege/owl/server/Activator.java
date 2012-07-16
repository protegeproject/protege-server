package org.protege.owl.server;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	public static final String SERVER_CONFIGURATION_PROPERTY = "org.protege.owl.server.configuration";

	@Override
	public void start(BundleContext context) throws Exception {
		String configuration = System.getProperty(SERVER_CONFIGURATION_PROPERTY);
		if (configuration != null) {
			loadConfiguration(configuration);
		}
	}
	
	private void loadConfiguration(String configuration) {
		
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
