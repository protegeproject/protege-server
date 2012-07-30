package org.protege.owl.server.changes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.TreeMap;

import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class ChangeDocumentUtilities {
	
	public static void writeEmptyChanges(DocumentFactory factory, File historyFile) throws IOException {
		ChangeDocument changes = factory.createChangeDocument(new ArrayList<OWLOntologyChange>(), new TreeMap<OntologyDocumentRevision, ChangeMetaData>(), OntologyDocumentRevision.START_REVISION);
		ChangeDocumentUtilities.writeChanges(changes, historyFile);
	}


	public static void writeChanges(ChangeDocument changes, File historyFile) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(historyFile));
		try {
			oos.writeObject(changes);
			oos.flush();
		}
		finally {
			oos.close();
		}
	}
	
	public static ChangeDocument readChanges(File historyFile) throws IOException {
		ChangeDocument changes;
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(historyFile));
		try {
			changes = (ChangeDocument) ois.readObject();
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException("Really?!", e);
		}
		return changes;
	}

}
