package org.protege.owl.server.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.model.IRI;

public class RemoteOntologyRevisions implements Serializable {
    private static final long serialVersionUID = -3112740928909735463L;
    private int maxRevision;
    private IRI ontologyName;
    private Map<IRI, Integer> markedRevisions;
    
    public RemoteOntologyRevisions(IRI ontologyName, Map<IRI, Integer> markedRevisions, int maxRevision) {
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

    public Map<IRI, Integer> getMarkedRevisions() {
        return Collections.unmodifiableMap(markedRevisions);
    }
    


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.write(maxRevision);
        out.writeObject(ontologyName.toString());
        out.write(markedRevisions.size());
        for (Entry<IRI, Integer> entry : markedRevisions.entrySet()) {
            out.writeObject(entry.getKey().toString());
            out.writeInt(entry.getValue());
        }
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        maxRevision = in.readInt();
        ontologyName = IRI.create((String) in.readObject());
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            markedRevisions.put(IRI.create((String) in.readObject()),  in.readInt());
        }
    }
    
}
