package org.protege.owl.server.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

public class RemoteOntologyRevisions implements Serializable {
    private static final long serialVersionUID = -3112740928909735463L;
    private int maxRevision;
    private IRI ontologyName;
    private Set<Integer> markedRevisions;
    
    public RemoteOntologyRevisions(IRI ontologyName, Set<Integer> markedRevisions, int maxRevision) {
        this.ontologyName = ontologyName;
        this.markedRevisions = markedRevisions;
        this.maxRevision = maxRevision;
    }

    public int getMaxRevision() {
        return maxRevision;
    }

    public IRI getOntologyName() {
        return ontologyName;
    }

    public Set<Integer> getMarkedRevisions() {
        return Collections.unmodifiableSet(markedRevisions);
    }
    


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.write(maxRevision);
        out.writeObject(ontologyName.toString());
        out.write(markedRevisions.size());
        for (Integer revision : markedRevisions) {
            out.writeObject(revision);
        }
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        maxRevision = in.readInt();
        ontologyName = IRI.create((String) in.readObject());
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            markedRevisions.add(in.readInt());
        }
    }
    
}
