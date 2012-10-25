package org.protege.owl.server.render;

import java.util.Collection;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

public class GetAxiomSourceVisitor implements OWLAxiomVisitor {
    private Collection<OWLEntity> referencedEntities = new TreeSet<OWLEntity>();
    private IRI                   referencedIRI      = null;

    public GetAxiomSourceVisitor() {
    }
    
    /*
     * ********************************************************
     */
    
    public Collection<OWLEntity> getReferencedEntities() {
        return referencedEntities;
    }
    
    public IRI getReferencedIri() {
        return referencedIRI;
    }
    
    public void reset() {
        referencedEntities = new TreeSet<OWLEntity>();
        referencedIRI      = null;
    }
    
    
    /*
     * ********************************************************
     */
    
    private void add(OWLClassExpression c) {
        if (!c.isAnonymous()) {
            referencedEntities.add(c.asOWLClass());
        }
    }
    
    private void add(OWLObjectPropertyExpression p) {
        if (!p.isAnonymous()) {
            referencedEntities.add(p.asOWLObjectProperty());
        }
    }
    
    private void add(OWLDataPropertyExpression p) {
        if (!p.isAnonymous()) {
            referencedEntities.add(p.asOWLDataProperty());
        }
    }
    
    private void add(OWLIndividual i) {
        if (!i.isAnonymous()) {
            referencedEntities.add(i.asOWLNamedIndividual());
        }
    }
    
    /*
     * ********************************************************
     */
    public void visit(OWLDeclarationAxiom axiom) {
        referencedEntities.add(axiom.getEntity());
    }

    public void visit(OWLSubClassOfAxiom axiom) {
        add(axiom.getSubClass());
    }

    public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        add(axiom.getSubject());
    }

    public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        add(axiom.getProperty());
    }

    public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
        add(axiom.getProperty());
    }

    public void visit(OWLDisjointClassesAxiom axiom) {
        for (OWLClassExpression expr : axiom.getClassExpressions()) {
            add(expr);
        }
    }

    public void visit(OWLDataPropertyDomainAxiom axiom) {
        add(axiom.getProperty());
    }

    public void visit(OWLObjectPropertyDomainAxiom axiom) {
        add(axiom.getProperty());
    }

    public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        for (OWLObjectPropertyExpression pe : axiom.getProperties()) {
            add(pe);
        }
    }

    public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        add(axiom.getSubject());
    }

    public void visit(OWLDifferentIndividualsAxiom axiom) {
        for (OWLIndividual i : axiom.getIndividuals()) {
            add(i);
        }
    }

    public void visit(OWLDisjointDataPropertiesAxiom axiom) {
        for (OWLDataPropertyExpression p : axiom.getProperties()) {
            add(p);
        }

    }

    public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        for (OWLObjectPropertyExpression p : axiom.getProperties()) {
            add(p);
        }
    }

    public void visit(OWLObjectPropertyRangeAxiom axiom) {
        add(axiom.getProperty());
    }

    public void visit(OWLObjectPropertyAssertionAxiom axiom) {
        add(axiom.getSubject());
    }

    public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        add(axiom.getProperty());
    }

    public void visit(OWLSubObjectPropertyOfAxiom axiom) {
        add(axiom.getSubProperty());
    }

    public void visit(OWLDisjointUnionAxiom axiom) {
        add(axiom.getOWLClass());
    }

    public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
        add(axiom.getProperty());
    }

    public void visit(OWLDataPropertyRangeAxiom axiom) {
        add(axiom.getProperty());
    }

    public void visit(OWLFunctionalDataPropertyAxiom axiom) {
        add(axiom.getProperty());
    }

    public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
        for (OWLDataPropertyExpression pe : axiom.getProperties()) {
            add(pe);
        }
    }

    public void visit(OWLClassAssertionAxiom axiom) {
        add(axiom.getIndividual());
    }

    public void visit(OWLEquivalentClassesAxiom axiom) {
        for (OWLClassExpression c : axiom.getClassExpressions()) {
            add(c);
        }
    }

    public void visit(OWLDataPropertyAssertionAxiom axiom) {
        add(axiom.getSubject());
    }

    public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
        add(axiom.getProperty());
    }

    public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        add(axiom.getProperty());
    }

    public void visit(OWLSubDataPropertyOfAxiom axiom) {
        add(axiom.getSubProperty());
    }

    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        add(axiom.getProperty());
    }

    public void visit(OWLSameIndividualAxiom axiom) {
        for (OWLIndividual i : axiom.getIndividuals()) {
            add(i);
        }
    }

    public void visit(OWLSubPropertyChainOfAxiom axiom) {
        add(axiom.getSuperProperty());
    }

    public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        add(axiom.getFirstProperty());
        add(axiom.getSecondProperty());
    }

    public void visit(OWLHasKeyAxiom axiom) {
        ;
    }

    public void visit(OWLDatatypeDefinitionAxiom axiom) {
        ;
    }

    public void visit(SWRLRule rule) {
        ;
    }

    public void visit(OWLAnnotationAssertionAxiom axiom) {
        OWLAnnotationSubject subject = axiom.getSubject();
        if (subject instanceof IRI) {
            referencedIRI = (IRI) subject;
        }
    }

    public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
        referencedEntities.add(axiom.getSubProperty());
    }

    public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
        referencedEntities.add(axiom.getProperty());
    }

    public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
        referencedEntities.add(axiom.getProperty());
    }

}
