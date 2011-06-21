package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.ImportChange;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomChange;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

public class ChangeUtilities {

    /**
     * The purpose of this routine is to calculate a minimal set of changes that will have the 
     * same effect on a collection of ontologies as the given changes.  Note that this routine
     * <ol>
     * <li> ignores the original contents of the ontologies.  The ontologies provided with the given 
     *      change set are simply regarded as the names of a collection of ontologies to be changed.
     * <li> removes all set ontology id changes.
     * </ol>
     * That is to say that changes that will have no effect on the net result are removed.
     */
    public static List<OWLOntologyChange> normalizeChangeDelta(List<OWLOntologyChange> changes) {
        List<OWLOntologyChange> result = new ArrayList<OWLOntologyChange>();
        for (int i = 0; i < changes.size(); i++) {
            OWLOntologyChange change1 = changes.get(i);
            if (!(change1 instanceof SetOntologyID)) {
                boolean overlap = false;
                for (int j = i + 1; j < changes.size(); j++) {
                    OWLOntologyChange change2 = changes.get(j);
                    if (overlappingChange(change1, change2)) {
                        overlap = true;
                        break;
                    }
                }
                if (!overlap) {
                    result.add(change1);
                }
            }
        }
        return result;
    }
    
    /**
     * The purpose of this routine is to calculate a minimal set of changes that will have the 
     * same effect on a collection of ontologies as the given changes.  This routine differs from the 
     * previous routine in that it considers the contents of the ontologies that are being changed.
     * Thus a change that deletes an axiom that is already contained in an ontology is regarded as 
     * redundant.  Not that this routine removes all set ontology id changes.
     */
    public static List<OWLOntologyChange> normalizeChangesToBase(List<OWLOntologyChange> changes) {
    	return normalizeChangesToBase(changes, new OntologyContainerPredicatesImpl());
    }
    
    /**
     * This routine is exactly the same as the above except that it uses the ontology container predicates 
     * class to determine what is present in the baseline ontologies rather than the contents of the ontologies
     * themselves.  This is useful to consider the effect of a collection of changes on some version of the
     * ontologies other than the current version.
     * 
     * @param changes
     * @param container
     * @return
     */
    public static List<OWLOntologyChange> normalizeChangesToBase(List<OWLOntologyChange> changes,
    		                                                     OntologyContainerPredicates container) {
        changes = ChangeUtilities.normalizeChangeDelta(changes);
        List<OWLOntologyChange> reducedList = new ArrayList<OWLOntologyChange>();
        AcceptRejectChangeVisitor visitor = new AcceptRejectChangeVisitor(container);
        for (OWLOntologyChange change : changes) {
            change.accept(visitor);
            if (visitor.isAccepted()) {
                reducedList.add(change);
            }
        }
        return reducedList;
    }
    
    private static class AcceptRejectChangeVisitor implements OWLOntologyChangeVisitor {
        private boolean accepted;
        private OntologyContainerPredicates base;
        
        public AcceptRejectChangeVisitor(OntologyContainerPredicates base) {
            this.base = base;
        }
        
        public boolean isAccepted() {
            return accepted;
        }

        public void visit(AddAxiom change) {
            accepted = !base.contains(change.getOntology(), change.getAxiom());
        }

        public void visit(RemoveAxiom change) {
            accepted = base.contains(change.getOntology(), change.getAxiom());
        }

        public void visit(SetOntologyID change) {
            accepted = false;
        }

        public void visit(AddImport change) {
            accepted = !base.contains(change.getOntology(), change.getImportDeclaration());
        }

        public void visit(RemoveImport change) {
            accepted = base.contains(change.getOntology(), change.getImportDeclaration());    
        }

        public void visit(AddOntologyAnnotation change) {
            accepted = !base.contains(change.getOntology(), change.getAnnotation());
        }

        public void visit(RemoveOntologyAnnotation change) {
            accepted = base.contains(change.getOntology(), change.getAnnotation());
        }
        
    }
    
    /**
     * This routine calculates a minimal set of changes x such that 
     * <center>
     *    firstChanges o secondChanges = secondChanges o x
     * </center>
     * where composition goes from left to right.
     * 
     * @param firstChanges 
     * @param secondChanges 
     * @return a minimal set of changes that when applied after secondChanges will result in the same result as if 
     *      we had first made the changes {@link firstChanges} and then made the changes {@link secondChanges}
     */
    public static List<OWLOntologyChange> swapOrderOfChangeLists(List<OWLOntologyChange> firstChanges, List<OWLOntologyChange> secondChanges) {
    	List<OWLOntologyChange> result = normalizeChangeDelta(firstChanges);
    	secondChanges = normalizeChangeDelta(secondChanges);
    	List<OWLOntologyChange> toRemove = new ArrayList<OWLOntologyChange>();
    	for (OWLOntologyChange firstChange : result) {
			if (overlappingChange(firstChange, secondChanges)) {
				toRemove.add(firstChange);
			}
    	}
    	result.removeAll(toRemove);
    	return result;
    }

    public static boolean overlappingChange(OWLOntologyChange change1, OWLOntologyChange change2) {
        if (change1.getOntology().equals(change2.getOntology())) {
            OverlapVisitor visitor = new OverlapVisitor(change1);
            change2.accept(visitor);
            return visitor.isOverlapping();
        }
        return false;
    }
    
    public static boolean overlappingChange(OWLOntologyChange change1, List<OWLOntologyChange> changes) {
    	boolean overlapping = false;
    	for (OWLOntologyChange change2 : changes) {
    		if (overlappingChange(change1, change2)) {
    			overlapping = true;
    			break;
    		}
    	}
    	return overlapping;
    }

    protected static class OverlapVisitor implements OWLOntologyChangeVisitor {
        private OWLOntologyChange testChange;
        private boolean overlapping;
        
        public OverlapVisitor(OWLOntologyChange change) {
            testChange = change;
            overlapping = false;
        }
        
        public boolean isOverlapping() {
            return overlapping;
        }
    
        
        public void visit(AddAxiom change) {
            overlapping = testChange instanceof OWLAxiomChange 
                                && ((OWLAxiomChange) testChange).getAxiom().equals(change.getAxiom());
        }
    
        
        public void visit(RemoveAxiom change) {
            overlapping = testChange instanceof OWLAxiomChange 
                                && ((OWLAxiomChange) testChange).getAxiom().equals(change.getAxiom());
        }
    
        
        public void visit(SetOntologyID change) {
            overlapping = testChange instanceof SetOntologyID;
        }
    
        
        public void visit(AddImport change) {
            overlapping = testChange instanceof ImportChange 
                               && ((ImportChange) testChange).getImportDeclaration().equals(change.getImportDeclaration());
        }
    
        
        public void visit(RemoveImport change) {
            overlapping = testChange instanceof ImportChange 
                              && ((ImportChange) testChange).getImportDeclaration().equals(change.getImportDeclaration());
        }
    
        
        public void visit(AddOntologyAnnotation change) {
            if (testChange instanceof AddOntologyAnnotation) {
                overlapping = ((AddOntologyAnnotation) testChange).getAnnotation().equals(change.getAnnotation());
            }
            else if (testChange instanceof RemoveOntologyAnnotation) {
                overlapping = ((RemoveOntologyAnnotation) testChange).getAnnotation().equals(change.getAnnotation());
            }
        }
    
        
        public void visit(RemoveOntologyAnnotation change) {
            if (testChange instanceof AddOntologyAnnotation) {
                overlapping = ((AddOntologyAnnotation) testChange).getAnnotation().equals(change.getAnnotation());
            }
            else if (testChange instanceof RemoveOntologyAnnotation) {
                overlapping = ((RemoveOntologyAnnotation) testChange).getAnnotation().equals(change.getAnnotation());
            }
        }
        
    }
    
    public static Set<OWLOntology> getChangedOntologies(List<OWLOntologyChange> changes) {
        Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
        for (OWLOntologyChange change : changes) {
            ontologies.add(change.getOntology());
        }
        return ontologies;
    }
    
    public static void logOntology(String message, OWLOntology ontology, Logger logger, Level level) {
        if (logger.isEnabledFor(level)) {
            logger.log(level, message);
            StringDocumentTarget out = new StringDocumentTarget();
            try {
                ontology.getOWLOntologyManager().saveOntology(ontology, new OWLXMLOntologyFormat(), out);
            } catch (OWLOntologyStorageException e) {
                logger.log(level, "Could not display ontology", e);
            }
            logger.log(level, out.toString());
            
        }
    }
    
    public static void logChanges(String message, Collection<OWLOntologyChange> changes, Logger logger, Level level) {
        if (logger.isEnabledFor(level)) {
            LogChangesVisitor visitor = new LogChangesVisitor(logger, level);
            logger.log(level, message);
            for (OWLOntologyChange change : changes) {
                change.accept(visitor);
            }
        }
    }
    
    private static class LogChangesVisitor implements OWLOntologyChangeVisitor {
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

}
