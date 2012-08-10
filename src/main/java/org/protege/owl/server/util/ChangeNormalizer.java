package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.ImportChange;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

public class ChangeNormalizer {
    private SetOntologyID lastOntologyIDChange;
    private Map<OWLImportsDeclaration, ImportChange> lastImportChangeMap;
    private Map<OWLAnnotation, OWLOntologyChange> lastOntologyAnnotationChangeMap;
    private Map<OWLAxiom, OWLAxiomChange> lastAxiomChangeMap;

    
    public static List<OWLOntologyChange> normalizeChangeDelta(List<OWLOntologyChange> changes) {
        return new ChangeNormalizer().run(changes);
    }
    
    private ChangeNormalizer() {
        lastImportChangeMap = new TreeMap<OWLImportsDeclaration, ImportChange>();
        lastOntologyAnnotationChangeMap = new TreeMap<OWLAnnotation, OWLOntologyChange>();
        lastAxiomChangeMap = new HashMap<OWLAxiom, OWLAxiomChange>();
    }
    
    public List<OWLOntologyChange> run(List<OWLOntologyChange> changes) {
        CollectingChangeVisitor visitor = new CollectingChangeVisitor();
        for (OWLOntologyChange change : changes) {
            change.accept(visitor);
        }
        List<OWLOntologyChange> normalizedChanges = new ArrayList<OWLOntologyChange>();
        if (lastOntologyIDChange != null) {
            normalizedChanges.add(lastOntologyIDChange);
        }
        normalizedChanges.addAll(lastImportChangeMap.values());
        normalizedChanges.addAll(lastOntologyAnnotationChangeMap.values());
        normalizedChanges.addAll(lastAxiomChangeMap.values());
        return normalizedChanges;
    }
    
    private class CollectingChangeVisitor implements OWLOntologyChangeVisitor {

        @Override
        public void visit(AddAxiom change) {
            lastAxiomChangeMap.put(change.getAxiom(), change);
        }

        @Override
        public void visit(RemoveAxiom change) {
            lastAxiomChangeMap.put(change.getAxiom(), change);
        }

        @Override
        public void visit(SetOntologyID change) {
            lastOntologyIDChange = change;
        }

        @Override
        public void visit(AddImport change) {
            lastImportChangeMap.put(change.getImportDeclaration(), change);
        }

        @Override
        public void visit(RemoveImport change) {
            lastImportChangeMap.put(change.getImportDeclaration(), change);
        }

        @Override
        public void visit(AddOntologyAnnotation change) {
            lastOntologyAnnotationChangeMap.put(change.getAnnotation(), change);
        }

        @Override
        public void visit(RemoveOntologyAnnotation change) {
            lastOntologyAnnotationChangeMap.put(change.getAnnotation(), change);
        }
        
    }

}
