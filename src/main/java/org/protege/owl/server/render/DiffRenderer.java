package org.protege.owl.server.render;

import java.io.PrintWriter;
import java.util.List;

import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.ImportChange;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;


public class DiffRenderer {
    private static final String TAB = "    ";
    
    private DiffsByEntity diffs;
    private OWLObjectRenderer renderer;
    private PrintWriter writer;
    private int counter;
    private int limit;

    
    public void renderDiff(List<OWLOntologyChange> changes, PrintWriter writer, int limit) {
        counter = 0;
        DiffsByEntity diffs = new DiffsByEntity(changes);
        OWLObjectRenderer renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
        writeImports();
        writeRegularAxioms();
    }
    
    private void writeImports() {
        for (OWLOntologyChange change : diffs.getUncategorizedChanges()) {
            if (change instanceof ImportChange) {
                writer.write((change instanceof AddImport) ? "Add Import:\n" : "Remove Import\n");
                writer.write(TAB);
                IRI importedIRI = ((ImportChange) change).getImportDeclaration().getIRI();
                writer.write(importedIRI.toString());
                counter++;
            }
        }
    }
    
    private void writeRegularAxioms() {
    }
}
