package org.protege.owl.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.protege.osgi.framework.Launcher;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.Client;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.protege.owl.server.api.exception.OWLServerException;
import org.protege.owl.server.util.ClientUtilities;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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
	
    public static void commit(Client client, VersionedOntologyDocument vont, OWLOntologyChange... changes) throws OWLServerException {
        OWLOntology ontology = vont.getOntology();
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        List<OWLOntologyChange> changeList = new ArrayList<OWLOntologyChange>();
        for (OWLOntologyChange change : changes) {
            changeList.add(change);
        }
        manager.applyChanges(changeList);
        ClientUtilities.commit(client, new ChangeMetaData(), vont);
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
