package org.protege.owl.server.util;

import org.slf4j.Logger;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

public class LogChangesVisitor implements OWLOntologyChangeVisitor {
    private Logger logger;

    public LogChangesVisitor(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void visit(AddAxiom change) {
        logger.info("\tAdding " + change.getAxiom());
    }

    @Override
    public void visit(RemoveAxiom change) {
        logger.info("\tRemoving " + change.getAxiom());
    }

    @Override
    public void visit(SetOntologyID change) {
        logger.info("\t" + change);
    }

    @Override
    public void visit(AddImport change) {
        logger.info("\t" + change);
    }

    @Override
    public void visit(RemoveImport change) {
        logger.info("\t" + change);

    }

    @Override
    public void visit(AddOntologyAnnotation change) {
        logger.info("\t" + change);
    }

    @Override
    public void visit(RemoveOntologyAnnotation change) {
        logger.info("\t" + change);
   }
    
}