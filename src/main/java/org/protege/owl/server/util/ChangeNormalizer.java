package org.protege.owl.server.util;

import java.util.List;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

public class ChangeNormalizer { 
    
    public static List<OWLOntologyChange> normalizeChangeDelta(List<OWLOntologyChange> changes) {
        return new ChangeNormalizer().run(changes);
        
    }
    
    private ChangeNormalizer() {
        
    }
    
    public List<OWLOntologyChange> run(List<OWLOntologyChange> changes) {
        throw new IllegalStateException("not implemented yet");
    }
    
    private class CollectingChangeVisitor implements OWLOntologyChangeVisitor {

        @Override
        public void visit(AddAxiom change) {
            
        }

        @Override
        public void visit(RemoveAxiom change) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void visit(SetOntologyID change) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void visit(AddImport change) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void visit(RemoveImport change) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void visit(AddOntologyAnnotation change) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void visit(RemoveOntologyAnnotation change) {
            // TODO Auto-generated method stub
            
        }
        
    }

}
