package org.protege.owl.server.render;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.ImportChange;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;


public class DiffRenderer {    
    private DiffsByEntity diffs;
    private OWLObjectRenderer renderer;
    private PrintWriter writer;
    private int counter;
    private int limit;
    private Set<IRI> displayedIriAnnotations = new TreeSet<IRI>();

    
    public void renderDiff(List<OWLOntologyChange> changes, OWLObjectRenderer renderer, PrintWriter writer, int limit) {
        this.renderer = renderer;
        diffs = new DiffsByEntity(changes);
        this.writer   = writer;
        this.limit    = limit;
        counter = 0;
        writeImports();
        writeOntologyAnnotations();
        writeRegularAxioms();
        writeMissingAnnotations();
        writeGeneralizedAxioms();
        writer.flush();
    }
    
    private void writeImports() {
        for (OWLOntologyChange change : diffs.getUncategorizedChanges()) {
            if (change instanceof ImportChange) {
                writeChange(change);
                counter++;
            }
        }
    }
    
    private void writeOntologyAnnotations() {
        for (OWLOntologyChange change : diffs.getUncategorizedChanges()) {
            if (change instanceof AddOntologyAnnotation || change instanceof RemoveOntologyAnnotation) {
                if (limit > 0 && counter++ >= limit) {
                    break;
                }
                writeChange(change);
            }
        }
    }
    
    private void writeRegularAxioms() {
        for (Entry<OWLEntity, Set<OWLOntologyChange>> entry : diffs.getReferencedEntityMap().entrySet()) {
            OWLEntity entity = entry.getKey();
            Set<OWLOntologyChange> changes = entry.getValue();
            writer.write(" ------------------- ");
            writer.write(renderer.render(entity));
            writer.write(" -------------------\n");
            writeIriAnnotations(entity.getIRI());
            writeChanges(changes);
            writer.write('\n');
        }
    }
    
    private void writeMissingAnnotations() { 
        for (Entry<IRI, Set<OWLOntologyChange>> entry : diffs.getReferencedIRIMap().entrySet()) {
            IRI iri = entry.getKey();
            Set<OWLOntologyChange> changes = entry.getValue();
            if (!displayedIriAnnotations.contains(iri)) {
                writeChanges(changes);
            }
        }
    }
    
    private void writeGeneralizedAxioms() {
        for (OWLOntologyChange change : diffs.getUncategorizedChanges()) {
            if (!(change instanceof AddOntologyAnnotation) && !(change instanceof RemoveOntologyAnnotation)) {
                if (limit > 0 && counter++ >= limit) {
                    break;
                }
                writeChange(change);
            }
        }
    }
    
    private void writeIriAnnotations(IRI iri) {
        Set<OWLOntologyChange> changes = diffs.getReferencedIRIMap().get(iri);
        if (changes != null) {
            writeChanges(changes);
            displayedIriAnnotations.add(iri);
        }
    }
    
    private void writeChanges(Collection<OWLOntologyChange> changes) {
        for (OWLOntologyChange change : changes) {
            if (limit > 0 && counter++ >= limit) {
                break;
            }
            writeChange(change);
        }
    }
    
    private void writeChange(OWLOntologyChange change) {
        RenderOntologyChangeVisitor visitor = new RenderOntologyChangeVisitor(renderer);
        change.accept(visitor);
        writer.write(visitor.getRendering());
        writer.write('\n');
    }
}
