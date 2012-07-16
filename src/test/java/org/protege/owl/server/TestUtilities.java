package org.protege.owl.server;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.protege.osgi.framework.Launcher;
import org.xml.sax.SAXException;

public class TestUtilities {
	
	public static final File ROOT_DIRECTORY = new File("build/server.root");

	private TestUtilities() {
	}

	public static File initializeServerRoot() {
		delete(ROOT_DIRECTORY);
		ROOT_DIRECTORY.mkdirs();
		return ROOT_DIRECTORY;
	}
	
	public static Framework startServer(File configuration) throws IOException, ParserConfigurationException, SAXException, InstantiationException, IllegalAccessException, ClassNotFoundException, BundleException, InterruptedException {
		Launcher launcher = new Launcher(configuration);
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
