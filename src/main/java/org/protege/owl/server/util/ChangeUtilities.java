package org.protege.owl.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;

import org.protege.owl.server.changes.ChangeMetaData;
import org.protege.owl.server.changes.api.ChangeHistory;
import org.protege.owl.server.changes.api.DocumentFactory;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

@Deprecated
public class ChangeUtilities {
	
	private ChangeUtilities() {
	}	

    /**
     * The purpose of this routine is to calculate a minimal set of changes that will have the 
     * same effect on a collection of ontologies as the given changes.  Note that this routine
     * <ol>
     * <li> ignores the original contents of the ontologies.  The ontologies provided with the given 
     *      change set are simply regarded as the names of a collection of ontologies to be changed.
     * <li> removes all set ontology id changes.
     * </ol>
     * That is to say that changes that will have no effect on the net result are removed.
     *
     * @param changes   List of ontology changes
     * @return List of normalized changes
     */
    public static List<OWLOntologyChange> normalizeChangeDelta(List<OWLOntologyChange> changes) {
        return ChangeNormalizer.normalizeChangeDelta(changes);
    }
    
    
    public static List<OWLOntologyChange> invertChanges(List<OWLOntologyChange> baseline, List<OWLOntologyChange> changes) {
        List<OWLOntologyChange> normalizedChanges = normalizeChangeDelta(changes);
        InvertChangesVisitor invertingVisitor = new InvertChangesVisitor(baseline);
        for (OWLOntologyChange change : normalizedChanges) {
            change.accept(invertingVisitor);
        }
        return invertingVisitor.getInvertedChanges();
    }
    
    public static ChangeHistory swapOrderOfChangeLists(DocumentFactory factory, ChangeHistory doc1, ChangeHistory doc2) {
        try {
            OWLOntology fakeOntology = OWLManager.createOWLOntologyManager().createOntology();
            List<OWLOntologyChange> doc3 =swapOrderOfChangeLists(doc1.getChanges(fakeOntology), doc2.getChanges(fakeOntology));
            return factory.createChangeDocument(doc3, new ChangeMetaData(), doc2.getEndRevision());
        }
        catch (OWLOntologyCreationException ooce) {
            throw new RuntimeException("Why would this happen?", ooce);
        }
    }
    
    /**
     * This routine calculates a minimal set of changes x such that 
     * <center>
     *    firstChanges o secondChanges = secondChanges o x
     * </center>
     * where composition goes from left to right.
     * <p>
     * ToDo - this routine is quadratic time when it should be linear time.
     * I suspect that this is rarely a problem.
     * <p>
     * @param firstChanges	firstChanges
     * @param secondChanges	secondChanges
     * @return a minimal set of changes that when applied after secondChanges will result in the same result as if 
     *      we had first made the changes <i>firstChanges</i> and then made the changes <i>secondChanges</i>
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

    public static Set<OWLOntology> getChangedOntologies(List<OWLOntologyChange> changes) {
        Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
        for (OWLOntologyChange change : changes) {
            ontologies.add(change.getOntology());
        }
        return ontologies;
    }
    
    public static void logOntology(String message, OWLOntology ontology, Logger logger) {
        logger.info(message);
        StringDocumentTarget out = new StringDocumentTarget();
        try {
            ontology.getOWLOntologyManager().saveOntology(ontology, new OWLXMLOntologyFormat(), out);
        } catch (OWLOntologyStorageException e) {
            logger.info("Could not display ontology", e);
        }
        logger.info(out.toString());
    }
    
    public static void logChanges(String message, Collection<OWLOntologyChange> changes, Logger logger) {
        LogChangesVisitor visitor = new LogChangesVisitor(logger);
        logger.info(message);
        for (OWLOntologyChange change : changes) {
            change.accept(visitor);
        }
    }

}
