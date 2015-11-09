package org.protege.owl.server.changes.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * 
 * @author redmond
 * @deprecated Replace with Matthew's format
 */
@Deprecated
public class OWLInputStream {
    private InputStream inputStream;
    private OWLDataFactory factory;
    private OWLOntology fakeOntology;
    
    public OWLInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            fakeOntology = manager.createOntology();
            factory = manager.getOWLDataFactory();
        }
        catch (OWLOntologyCreationException ooce) {
            throw new RuntimeException(ooce);
        }
    }
    
    public InputStream getInputStream() {
        return inputStream;
    }
    
    public OWLOntology getFakeOntology() {
        return fakeOntology;
    }
    
    public OWLDataFactory getOWLDataFactory() {
        return factory;
    }
    
    public Object read() throws IOException {
        int typeIndex = inputStream.read();
        OWLObjectType type = OWLObjectType.values()[typeIndex];
        Object o = type.read(this);
        if (o instanceof OWLAxiom) {
            @SuppressWarnings("unchecked")
            Set<OWLAnnotation> annotations = (Set<OWLAnnotation>) read();
            if (!annotations.isEmpty()) {
                return ((OWLAxiom) o).getAnnotatedAxiom(annotations);
            }
        }
        return o;
    }
    
    /*
     * Are these bad because they are slow?
     */
    public <X extends OWLObject> Set<X> readSet(Class<? extends X> javaClass) throws IOException {
        int count = IOUtils.readInt(inputStream);
        Set<X> objects = new TreeSet<X>();
        for (int i = 0; i < count; i++) {
            objects.add(javaClass.cast(read()));
        }
        return objects;
    }
    
    public <X extends OWLObject> List<X> readList(Class<? extends X> javaClass) throws IOException {
        int count = IOUtils.readInt(inputStream);
        List<X> objects = new ArrayList<X>();
        for (int i = 0; i < count; i++) {
            objects.add(javaClass.cast(read()));
        }
        return objects;
    }


}
