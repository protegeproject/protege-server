package org.protege.owl.server.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.Properties;

import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.semanticweb.owlapi.model.IRI;

public class RemoteOntologyDocumentFromProperties extends RemoteOntologyDocumentImpl {
	public static final String BACKING_STORE_PROPERTY = "server.location";
	public static final String VERSION_PROPERTY       = "version";
	public static final String HISTORY_PROPERTIES_EXTENSION = ".history-properties";

	public static RemoteOntologyDocument create(File propertiesFile, IRI backingStore, OntologyDocumentRevision revision) throws IOException {
		RemoteOntologyDocumentFromProperties remoteDoc = new RemoteOntologyDocumentFromProperties(propertiesFile, backingStore, revision);
		remoteDoc.writePropertiesFile();
		return remoteDoc;
	}
	
	public static RemoteOntologyDocument read(File propertiesFile) throws IOException {
		Properties p = new Properties();
		Reader reader = new InputStreamReader(new FileInputStream(propertiesFile), "UTF-8");
		try {
			p.load(reader);
		}
		finally {
			reader.close();
		}
		IRI backingStore = IRI.create(p.getProperty(BACKING_STORE_PROPERTY));
		OntologyDocumentRevision revision = new OntologyDocumentRevision(Integer.parseInt(p.getProperty(VERSION_PROPERTY)));		
		return new RemoteOntologyDocumentFromProperties(propertiesFile, backingStore, revision);
	}
	
	private File propertiesFile;

	
	private RemoteOntologyDocumentFromProperties(File propertiesFile, IRI backingStore, OntologyDocumentRevision revision) {
		super(backingStore, revision);
		this.propertiesFile = propertiesFile;
	}
	
	@Override
	public void setRevision(OntologyDocumentRevision revision) {
		super.setRevision(revision);
		try {
			writePropertiesFile();
		}
		catch (IOException e) {
			throw new RuntimeException("Unexpected exception writing version properties file for ontology");
		}
	}
	
	private void writePropertiesFile() throws IOException {
		Properties p = new Properties();
		p.setProperty(VERSION_PROPERTY, new Integer(getRevision().getRevision()).toString());
		p.setProperty(BACKING_STORE_PROPERTY, getServerLocation().toString());
		Writer writer = new OutputStreamWriter(new FileOutputStream(propertiesFile), "UTF-8");
		try {
			p.store(writer, "Server information stored at " + new Date());
			writer.flush();
		}
		finally {
			writer.close();
		}
	}


}
