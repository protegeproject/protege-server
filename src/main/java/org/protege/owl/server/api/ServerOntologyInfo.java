package org.protege.owl.server.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

public class ServerOntologyInfo implements Serializable {
    private static final long serialVersionUID = -3112740928909735463L;
    private int maxRevision;
    private IRI ontologyName;
    private String shortName;
    private Set<Integer> markedRevisions;
    
    public ServerOntologyInfo(IRI ontologyName, String shortName, Set<Integer> markedRevisions, int maxRevision) {
        this.ontologyName = ontologyName;
        this.shortName = shortName;
        this.markedRevisions = markedRevisions;
        this.maxRevision = maxRevision;
    }

    public IRI getOntologyName() {
        return ontologyName;
    }
    
    public String getShortName() {
        return shortName;
    }

    public int getMaxRevision() {
        return maxRevision;
    }
    
    public Set<Integer> getMarkedRevisions() {
        return new HashSet<Integer>(markedRevisions);
    }
    
    public void setMarkedRevisions(Set<Integer> markedRevisions) {
        this.markedRevisions = markedRevisions;
    }

    public void addMarkedRevision(int revision) {
        markedRevisions.add(revision);
    }

    public Integer getLatestMarkedRevision(Integer revision) {
        Integer latest = null;
        for (int marked : markedRevisions) {
            if (revision != null && marked > revision) {
                continue;
            }
            if (latest == null || marked > latest) {
                latest = marked;
            }
        }
        return latest;
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
