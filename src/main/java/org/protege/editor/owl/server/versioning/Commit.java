package org.protege.editor.owl.server.versioning;

import org.protege.editor.owl.server.versioning.api.RevisionMetadata;

import org.semanticweb.binaryowl.BinaryOWLMetadata;
import org.semanticweb.binaryowl.BinaryOWLOntologyChangeLog;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.change.OWLOntologyChangeData;
import org.semanticweb.owlapi.change.OWLOntologyChangeRecord;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Commit implements Serializable {

    private static final long serialVersionUID = -5600758982758166504L;

    private RevisionMetadata metadata;
    private List<OWLOntologyChange> changes = new ArrayList<>();
    
    public Commit(RevisionMetadata metadata, List<OWLOntologyChange> changes) {
        this.metadata = metadata;
        this.changes = changes;
    }

    public RevisionMetadata getMetadata() {
        return metadata;
    }

    public List<OWLOntologyChange> getChanges() {
        return changes;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        try {
            out.writeObject(getMetadata());
            BinaryOWLOntologyChangeLog log = new BinaryOWLOntologyChangeLog();
            log.appendChanges(getChanges(), System.currentTimeMillis(), BinaryOWLMetadata.emptyMetadata(), out);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            out.flush();
//            out.close();
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        OWLOntologyManager owlManager = OWLManager.createOWLOntologyManager();
        try {
            metadata = (RevisionMetadata) in.readObject();
            BinaryOWLOntologyChangeLog log = new BinaryOWLOntologyChangeLog();
            OWLOntology placeholder = owlManager.createOntology();
            final List<OWLOntologyChange> readChanges = new ArrayList<>();
            log.readChanges(in, owlManager.getOWLDataFactory(), (list, skipSetting, filePosition) -> {
                List<OWLOntologyChangeRecord> changeRecords = list.getChangeRecords();
                for (OWLOntologyChangeRecord cr : changeRecords) {
                    OWLOntologyChangeData changeData = cr.getData();
                    OWLOntologyChange change = changeData.createOntologyChange(placeholder);
                    readChanges.add(change);
                }
            });
            changes = readChanges;
        }
        catch (OWLOntologyCreationException e) {
            throw new IOException("Internal error while reading commit object", e);
        }
        finally {
//            in.close();
        }
    }
}
