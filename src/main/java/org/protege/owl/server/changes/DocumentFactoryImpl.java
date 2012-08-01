package org.protege.owl.server.changes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.protege.owl.server.api.ChangeDocument;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.VersionedOWLOntology;
import org.protege.owl.server.impl.RemoteOntologyDocumentImpl;
import org.protege.owl.server.impl.VersionedOWLOntologyImpl;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.xml.sax.SAXException;

public class DocumentFactoryImpl implements DocumentFactory, Serializable {
    private static final long serialVersionUID = -4952738108103836430L;
    public static Logger logger = Logger.getLogger(DocumentFactoryImpl.class.getCanonicalName());
	
	@SuppressWarnings("deprecation")
	@Override
	public ChangeDocument createChangeDocument(List<OWLOntologyChange> changes,
											   Map<OntologyDocumentRevision, ChangeMetaData> metaData, 
											   OntologyDocumentRevision start) {
		return new ChangeDocumentImpl(this, start, changes, metaData);
	}
	
	@Override
	public VersionedOWLOntology createVersionedOntology(OWLOntology ontology,
													    RemoteOntologyDocument serverDocument,
													    OntologyDocumentRevision revision) {
		ChangeDocument localChanges = emptyChangeDocument();
		return new VersionedOWLOntologyImpl(ontology, serverDocument, revision, localChanges);
	}
	
	@Override
	public boolean hasServerMetadata(OWLOntology ontology) {
		File ontologyFile = VersionedOWLOntologyImpl.getBackingStore(ontology);
		if (ontologyFile == null) {
			return false;
		}
		return VersionedOWLOntologyImpl.getVersioningPropertiesFile(ontologyFile).exists();
	}

	@Override
	public VersionedOWLOntology createVersionedOntology(OWLOntology ontology) throws IOException {
		File ontologyFile = VersionedOWLOntologyImpl.getBackingStore(ontology);
		Properties p = new Properties();
		FileInputStream in = new FileInputStream(VersionedOWLOntologyImpl.getVersioningPropertiesFile(ontologyFile));
		try {
			p.load(in);
		}
		finally {
			in.close();
		}
		OntologyDocumentRevision revision = new OntologyDocumentRevision(Integer.parseInt(p.getProperty(VersionedOWLOntologyImpl.VERSION_PROPERTY)));
		RemoteOntologyDocument serverDocument = new RemoteOntologyDocumentImpl(IRI.create(p.getProperty(VersionedOWLOntologyImpl.BACKING_STORE_PROPERTY)));
		File historyFile = VersionedOWLOntologyImpl.getHistoryFile(ontologyFile);
		ChangeDocument localChanges;
		if (historyFile.exists()) {
			localChanges = ChangeDocumentUtilities.readChanges(this, historyFile, null, null);
		}
		else {
			localChanges = emptyChangeDocument();
		}
		return new VersionedOWLOntologyImpl(ontology, serverDocument, revision, localChanges);
	}
	
	/*
     * This algorithm involves a copy of the input stream which would be nice to avoid.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public ChangeDocument readChangeDocument(InputStream in,
											 OntologyDocumentRevision start, OntologyDocumentRevision end) throws IOException {
        ObjectInputStream ois;
	    try {
			if (in instanceof ObjectInputStream) {
				ois = (ObjectInputStream) in;
			}
			else {
				ois = new ObjectInputStream(in);
			}
			OntologyDocumentRevision startRevision = (OntologyDocumentRevision) ois.readObject();
			@SuppressWarnings("unchecked")
			Map<OntologyDocumentRevision, ChangeMetaData> metaData = (Map<OntologyDocumentRevision, ChangeMetaData>) ois.readObject();
			

			OWLOntology changesOntology = readOntology(ois);
			OntologyToChangesUtil otcu = new OntologyToChangesUtil(changesOntology, startRevision);
			otcu.initialise();
			List<OWLOntologyChange> changes = otcu.getChanges();
			ChangeDocument fullDoc = new ChangeDocumentImpl(this, startRevision, changes, metaData);
			if (start == null) {
			    start = fullDoc.getStartRevision();
			}
			if (end == null) {
			    end = fullDoc.getEndRevision();
			}
			return fullDoc.cropChanges(start, end)    ;
		}
		catch (OWLOntologyCreationException e) {
			logger.log(Level.WARNING, "Exception caught deserializing change document", e);
			throw new OntologyCreationIOException(e);
		}
		catch (IOException ioe) {
			logger.log(Level.WARNING, "Exception caught deserializing change document", ioe);
			throw ioe;
		}
		catch (Exception e) {
			logger.log(Level.WARNING, "Exception caught deserializing change document", e);
			throw new IOException(e);
		}
		catch (Error e) {
			logger.log(Level.WARNING, "Exception caught deserializing change document", e);
			throw e;
		}
	}
	
	/*
	 * The reason that the parser is getting wired in here is that the default implementation copies the stream to memory.
	 * We have run out of memory doing this even for the NCI Thesaurus.
	 */
	private OWLOntology readOntology(InputStream providedInputStream) throws OWLOntologyCreationException, ParserConfigurationException, SAXException, IOException {
        InputStream is = new UncloseableInputStream(new BufferedInputStream(providedInputStream));
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology changesOntology = manager.createOntology();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser parser = factory.newSAXParser();
        OWLXMLParserHandler handler = new OWLXMLParserHandler(changesOntology, new OWLOntologyLoaderConfiguration());
        parser.parse(is, handler);
        return changesOntology;
	}
	
	@SuppressWarnings("deprecation")
	private ChangeDocument emptyChangeDocument() {
		return new ChangeDocumentImpl(this, OntologyDocumentRevision.START_REVISION, new ArrayList<OWLOntologyChange>(), new TreeMap<OntologyDocumentRevision, ChangeMetaData>());
	}

}
