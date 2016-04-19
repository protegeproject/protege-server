package org.protege.editor.owl.server.versioning.format;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.protege.editor.owl.server.api.exception.RuntimeIOException;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * 
 * @author redmond
 * @deprecated Replace with Matthew's format
 */
@Deprecated
public class OWLOutputStream {
    private OutputStream outputStream;
    private int compressionLimit = 1000;
    private SerializingVisitor visitor;
    
    public OWLOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        visitor = new SerializingVisitor(this);
    }
    
    public OutputStream getOutputStream() {
        return outputStream;
    }
    
    public int getCompressionLimit() {
        return compressionLimit;
    }
    
    public void setCompressionLimit(int compressionLimit) {
        this.compressionLimit = compressionLimit;
    }
    
    public void write(OWLObject owlObject) throws IOException {
        try {
            owlObject.accept(visitor);
        }
        catch (RuntimeIOException rioe) {
            throw rioe.getCause();
        }
    }
    
    public <X extends OWLObject> void write(Collection<X> objects) throws IOException {
        IOUtils.writeInt(getOutputStream(), objects.size());
        for (OWLObject object : objects) {
            write(object);
        }
    }
    
    public void write(OWLOntologyChange change) throws IOException {
        try {
            change.accept(visitor);
        }
        catch (RuntimeIOException rioe) {
            throw rioe.getCause();
        }
    }
    
    public void writeWithCompression(List<OWLOntologyChange> changes) throws IOException {
        if (compressionLimit > 0 && changes.size() > compressionLimit) {
            outputStream.write(OWLObjectType.COMPRESSED.ordinal());
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            GZIPOutputStream compressingOutputStream = new GZIPOutputStream(bout);
            OWLOutputStream compressingOwlOutputStream = new OWLOutputStream(compressingOutputStream);
            compressingOwlOutputStream.write(changes);
            compressingOutputStream.flush();
            compressingOutputStream.close();
            byte[] bytes = bout.toByteArray();
            IOUtils.writeInt(outputStream, bytes.length);
            outputStream.write(bytes);
        }
        else {
            write(changes);
        }
    }
    
    public void write(List<OWLOntologyChange> changes) throws IOException {
        outputStream.write(OWLObjectType.LIST_OF_CHANGES.ordinal());
        OWLObjectType.LIST_OF_CHANGES.write(this, changes);
    }
    
    public void write(OWLOntologyID id) throws IOException {
        outputStream.write(OWLObjectType.OWL_ONTOLOGY_ID.ordinal());
        OWLObjectType.OWL_ONTOLOGY_ID.write(this, id);
    }
    
    public void write(OWLObjectType owlType, Object o) throws RuntimeIOException {
        try {
            outputStream.write(owlType.ordinal());
            owlType.write(this, o);
            if (o instanceof OWLAxiom) {
                Set<OWLAnnotation> annotations = ((OWLAxiom) o).getAnnotations();
                if (annotations.size() == 0) {
                    write(OWLObjectType.EMPTY_ANNOTATION_SET, annotations);
                }
                else {
                    write(OWLObjectType.ANNOTATION_SET, annotations);
                }
            }
        }
        catch (IOException ioe) {
            throw new RuntimeIOException(ioe);
        }
    }

}
