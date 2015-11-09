package org.protege.owl.server.changes;

import static org.protege.owl.server.PizzaVocabulary.CHEESEY_PIZZA_DEFINITION;
import static org.protege.owl.server.PizzaVocabulary.NOT_CHEESEY_PIZZA_DEFINITION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.protege.owl.server.util.ChangeUtilities;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.testng.annotations.Test;

@Test(groups = { "unit.test" })
public class ChangeUtilitiesTest {
    
    @Test
    public void testRedundantAddAxiom() throws OWLOntologyCreationException {
        OWLOntologyChange addAxiom = new AddAxiom(OWLManager.createOWLOntologyManager().createOntology(), CHEESEY_PIZZA_DEFINITION);
        List<OWLOntologyChange> changes = ChangeUtilities.invertChanges(Collections.singletonList(addAxiom), Collections.singletonList(addAxiom));
        Assert.assertTrue(changes.isEmpty());
    }
    
    @Test
    public void testRedundantRemoveAxiom01() throws OWLOntologyCreationException {
        OWLOntologyChange removeAxiom = new RemoveAxiom(OWLManager.createOWLOntologyManager().createOntology(), CHEESEY_PIZZA_DEFINITION);
        List<OWLOntologyChange> changes = ChangeUtilities.invertChanges(Collections.singletonList(removeAxiom), Collections.singletonList(removeAxiom));
        Assert.assertTrue(changes.isEmpty());
    }
    
    @Test
    public void testRedundantRemoveAxiom02() throws OWLOntologyCreationException {
        OWLOntology fakeOntology = OWLManager.createOWLOntologyManager().createOntology();
        OWLOntologyChange addAxiom = new AddAxiom(fakeOntology, CHEESEY_PIZZA_DEFINITION);
        OWLOntologyChange removeAxiom = new RemoveAxiom(fakeOntology, CHEESEY_PIZZA_DEFINITION);
        List<OWLOntologyChange> baseline = new ArrayList<OWLOntologyChange>();
        baseline.add(addAxiom);
        baseline.add(removeAxiom);
        List<OWLOntologyChange> changes = ChangeUtilities.invertChanges(baseline, Collections.singletonList(removeAxiom));
        Assert.assertTrue(changes.isEmpty());
    }
    
    @Test
    public void testGoodInvert() throws OWLOntologyCreationException {
        OWLOntology fakeOntology = OWLManager.createOWLOntologyManager().createOntology();
        OWLOntologyChange addAxiom = new AddAxiom(fakeOntology, CHEESEY_PIZZA_DEFINITION);
        OWLOntologyChange removeAxiom = new RemoveAxiom(fakeOntology, CHEESEY_PIZZA_DEFINITION);
        OWLOntologyChange addOtherAxiom = new AddAxiom(fakeOntology, NOT_CHEESEY_PIZZA_DEFINITION);
        List<OWLOntologyChange> changes = ChangeUtilities.invertChanges(Collections.singletonList(addOtherAxiom), Collections.singletonList(addAxiom));
        Assert.assertEquals(Collections.singletonList(removeAxiom), changes);

    }

}
