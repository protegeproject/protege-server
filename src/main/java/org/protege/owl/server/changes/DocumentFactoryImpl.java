package org.protege.owl.server.changes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.protege.owl.server.api.ChangeHistory;
import org.protege.owl.server.api.ChangeMetaData;
import org.protege.owl.server.api.DocumentFactory;
import org.protege.owl.server.api.OntologyDocumentRevision;
import org.protege.owl.server.api.RemoteOntologyDocument;
import org.protege.owl.server.api.VersionedOntologyDocument;
import org.protege.owl.server.changes.format.OWLInputStream;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

public class DocumentFactoryImpl implements DocumentFactory, Serializable {
    private static final long serialVersionUID = -4952738108103836430L;
    public static Logger logger = Logger.getLogger(DocumentFactoryImpl.class.getCanonicalName());
	
    @Override
    public ChangeHistory createEmptyChangeDocument(OntologyDocumentRevision revision) {
        return new ChangeDocumentImpl(this, revision, null, null);
    }
    
	@Override
	public ChangeHistory createChangeDocument(List<OWLOntologyChange> changes,
											   ChangeMetaData metaData, 
											   OntologyDocumentRevision start) {
		return new ChangeDocumentImpl(this, start, changes, metaData);
	}
	
	@Override
	public VersionedOntologyDocument createVersionedOntology(OWLOntology ontology,
													    RemoteOntologyDocument serverDocument,
													    OntologyDocumentRevision revision) {
		ChangeHistory localChanges = createEmptyChangeDocument(OntologyDocumentRevision.START_REVISION);
		return new VersionedOntologyDocumentImpl(ontology, serverDocument, revision, localChanges);
	}
	
	@Override
	public boolean hasServerMetadata(OWLOntology ontology) {
		File ontologyFile = VersionedOntologyDocumentImpl.getBackingStore(ontology);
		if (ontologyFile == null) {
			return false;
		}
		return VersionedOntologyDocumentImpl.getHistoryFile(ontologyFile).exists();
	}
	
	@Override
	public IRI getServerLocation(OWLOntology ontology) throws IOException {
	    try {

	        File ontologyFile = VersionedOntologyDocumentImpl.getBackingStore(ontology);
	        File historyFile = VersionedOntologyDocumentImpl.getHistoryFile(ontologyFile);
	        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(historyFile));
	        RemoteOntologyDocument serverDocument = (RemoteOntologyDocument) ois.readObject();
	        return serverDocument.getServerLocation();
	    }
	    catch (ClassNotFoundException cnfe) {
	        throw new IOException("Class Loader issues when hydrating ontology history document", cnfe);
	    }
	}

	@Override
	public VersionedOntologyDocument getVersionedOntologyDocument(OWLOntology ontology) throws IOException {
	    try {
	        File ontologyFile = VersionedOntologyDocumentImpl.getBackingStore(ontology);
	        File historyFile = VersionedOntologyDocumentImpl.getHistoryFile(ontologyFile);
	        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(historyFile));
	        
	        RemoteOntologyDocument serverDocument = (RemoteOntologyDocument) ois.readObject();
	        OntologyDocumentRevision revision = (OntologyDocumentRevision) ois.readObject();
	        ChangeHistory localChanges = (ChangeHistory) ois.readObject();
	        
	        return new VersionedOntologyDocumentImpl(ontology, serverDocument, revision, localChanges);
	    }
	    catch (ClassNotFoundException cnfe) {
	        throw new IOException("Class Loader issues when hydrating ontology history document", cnfe);
	    }
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public ChangeHistory readChangeDocument(InputStream in,
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
			if (start == null) {
			    start = startRevision;
			}
			@SuppressWarnings("unchecked")
			SortedMap<OntologyDocumentRevision, ChangeMetaData> metaData = (SortedMap<OntologyDocumentRevision, ChangeMetaData>) ois.readObject();
			List<List<OWLOntologyChange>> changes = new ArrayList<List<OWLOntologyChange>>();
			OWLInputStream owlStream = new OWLInputStream(ois);
			int count = ois.readInt();
			OntologyDocumentRevision revision = startRevision;
			for (int i = 0; i < count; i++,revision = revision.next()) {
			    @SuppressWarnings("unchecked")
                List<OWLOntologyChange> changeList = (List<OWLOntologyChange>) owlStream.read();
			    if (revision.compareTo(start) >= 0 && (end == null || revision.compareTo(end) < 0)) {
			        changes.add(changeList);
			    }
			}
			if (end == null) {
			    end = start.add(changes.size());
			}
			metaData = metaData.tailMap(start).headMap(end);
            return new ChangeDocumentImpl(start, this, changes, metaData); 
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



}
