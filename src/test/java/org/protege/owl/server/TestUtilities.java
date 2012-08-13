package org.protege.owl.server;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.protege.osgi.framework.Launcher;
import org.xml.sax.SAXException;

public class TestUtilities {
	
	public static final File ROOT_DIRECTORY = new File("build/server/root");
	public static final File CONFIGURATION_DIRECTORY = new File("build/server/configuration");
	public static final String PREFIX;
	static {
		StringBuffer sb = new StringBuffer();
		sb.append("src");
		sb.append(File.separator);
		sb.append("test");
		sb.append(File.separator);
		sb.append("resources");
		sb.append(File.separator);
		PREFIX = sb.toString();
	}

	private TestUtilities() {
	}

	public static File initializeServerRoot() {
		delete(ROOT_DIRECTORY);
		ROOT_DIRECTORY.mkdirs();
		return ROOT_DIRECTORY;
	}
	
	public static Framework startServer(String osgiConfiguration, String serverConfiguration) throws IOException, ParserConfigurationException, SAXException, InstantiationException, IllegalAccessException, ClassNotFoundException, BundleException, InterruptedException {
		System.setProperty(Activator.SERVER_CONFIGURATION_PROPERTY, PREFIX + serverConfiguration);
		Launcher launcher = new Launcher(new File(PREFIX, osgiConfiguration));
		launcher.start(false);
		return launcher.getFramework();
	}
	
	
    private static void delete(File f) {
        if (f.isDirectory()) {
            for (File child : f.listFiles()) {
                delete(child);
            }
        }
        f.delete();
    }
}
