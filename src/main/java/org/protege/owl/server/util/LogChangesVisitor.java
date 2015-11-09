package org.protege.owl.server.util;

import java.util.logging.Level;
import java.util.logging.Logger;

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
    private Level level;

    public LogChangesVisitor(Logger logger, Level level) {
        this.logger = logger;
        this.level = level;
    }

    @Override
    public void visit(AddAxiom change) {
        logger.log(level, "\tAdding " + change.getAxiom());
    }

    @Override
    public void visit(RemoveAxiom change) {
        logger.log(level, "\tRemoving " + change.getAxiom());
    }

    @Override
    public void visit(SetOntologyID change) {
        logger.log(level, "\t" + change);
    }

    @Override
    public void visit(AddImport change) {
        logger.log(level, "\t" + change);
    }

    @Override
    public void visit(RemoveImport change) {
        logger.log(level, "\t" + change);

    }

    @Override
    public void visit(AddOntologyAnnotation change) {
        logger.log(level, "\t" + change);
    }

    @Override
    public void visit(RemoveOntologyAnnotation change) {
        logger.log(level, "\t" + change);
   }
    
}