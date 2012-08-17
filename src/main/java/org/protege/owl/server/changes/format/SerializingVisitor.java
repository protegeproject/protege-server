package org.protege.owl.server.changes.format;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.model.SetOntologyID;

/**
 * 
 * @author redmond
 * @deprecated Replace with Matthew's format
 */
@Deprecated
public class SerializingVisitor implements OWLObjectVisitor, OWLOntologyChangeVisitor {
    private OWLOutputStream owlOutputStream;
    
    public SerializingVisitor(OWLOutputStream owlOutputStream) {
        this.owlOutputStream = owlOutputStream;
    }
    
    public OWLOutputStream getOWLOutputStream() {
        return owlOutputStream;
    }
    

    @Override
    public void visit(OWLAnnotationAssertionAxiom axiom) {
        owlOutputStream.write(OWLObjectType.ANNOTATION_ASSERTION_AXIOM, axiom);

    }

    @Override
    public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLClass ce) {
        owlOutputStream.write(OWLObjectType.OWL_CLASS, ce);
    }

    @Override
    public void visit(OWLObjectIntersectionOf ce) {
        owlOutputStream.write(OWLObjectType.OBJECT_INTERSECTION_OF, ce);
    }

    @Override
    public void visit(OWLObjectUnionOf ce) {
        owlOutputStream.write(OWLObjectType.OBJECT_UNION_OF, ce);
    }

    @Override
    public void visit(OWLObjectComplementOf ce) {
        owlOutputStream.write(OWLObjectType.OBJECT_COMPLEMENT_OF, ce);
    }

    @Override
    public void visit(OWLObjectSomeValuesFrom ce) {
        owlOutputStream.write(OWLObjectType.OBJECT_SOME_VALUES_FROM, ce);
    }

    @Override
    public void visit(OWLObjectAllValuesFrom ce) {
        owlOutputStream.write(OWLObjectType.OBJECT_ALL_VALUES_FROM, ce);
    }

    @Override
    public void visit(OWLObjectHasValue ce) {
        owlOutputStream.write(OWLObjectType.OBJECT_HAS_VALUE, ce);
    }

    @Override
    public void visit(OWLObjectMinCardinality ce) {
        owlOutputStream.write(OWLObjectType.OBJECT_MIN_CARDINALITY, ce);
    }

    @Override
    public void visit(OWLObjectExactCardinality ce) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLObjectMaxCardinality ce) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLObjectHasSelf ce) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLObjectOneOf ce) {
        owlOutputStream.write(OWLObjectType.OBJECT_ONE_OF, ce);
    }

    @Override
    public void visit(OWLDataSomeValuesFrom ce) {
        owlOutputStream.write(OWLObjectType.DATA_SOME_VALUES_FROM, ce);
    }

    @Override
    public void visit(OWLDataAllValuesFrom ce) {
        owlOutputStream.write(OWLObjectType.DATA_ALL_VALUES_FROM, ce);
    }

    @Override
    public void visit(OWLDataHasValue ce) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLDataMinCardinality ce) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLDataExactCardinality ce) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLDataMaxCardinality ce) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLDeclarationAxiom axiom) {
        owlOutputStream.write(OWLObjectType.OWL_DECLARATION_AXIOM, axiom);
    }

    @Override
    public void visit(OWLSubClassOfAxiom axiom) {
        owlOutputStream.write(OWLObjectType.SUB_CLASS_OF_AXIOM, axiom);
    }

    @Override
    public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLDisjointClassesAxiom axiom) {
        owlOutputStream.write(OWLObjectType.DISJOINT_CLASSES_AXIOM, axiom);
    }

    @Override
    public void visit(OWLDataPropertyDomainAxiom axiom) {
        owlOutputStream.write(OWLObjectType.DATA_PROPERTY_DOMAIN_AXIOM, axiom);
    }

    @Override
    public void visit(OWLObjectPropertyDomainAxiom axiom) {
        owlOutputStream.write(OWLObjectType.OBJECT_PROPERTY_DOMAIN_AXIOM, axiom);
    }

    @Override
    public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLDifferentIndividualsAxiom axiom) {
        owlOutputStream.write(OWLObjectType.DIFFERENT_INDIVIDUALS_AXIOM, axiom);
    }

    @Override
    public void visit(OWLDisjointDataPropertiesAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLObjectPropertyRangeAxiom axiom) {
        owlOutputStream.write(OWLObjectType.OBJECT_PROPERTY_RANGE_AXIOM, axiom);
    }

    @Override
    public void visit(OWLObjectPropertyAssertionAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        owlOutputStream.write(OWLObjectType.FUNCTIONAL_OBJECT_PROPERTY, axiom);
    }

    @Override
    public void visit(OWLSubObjectPropertyOfAxiom axiom) {
        owlOutputStream.write(OWLObjectType.SUB_OBJECT_PROPERTY_OF, axiom);
    }

    @Override
    public void visit(OWLDisjointUnionAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLDataPropertyRangeAxiom axiom) {
        owlOutputStream.write(OWLObjectType.DATA_PROPERTY_RANGE_AXIOM, axiom);
    }

    @Override
    public void visit(OWLFunctionalDataPropertyAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLClassAssertionAxiom axiom) {
        owlOutputStream.write(OWLObjectType.CLASS_ASSERTION, axiom);
    }

    @Override
    public void visit(OWLEquivalentClassesAxiom axiom) {
        owlOutputStream.write(OWLObjectType.EQUIVALENT_CLASSES_AXIOM, axiom);
    }

    @Override
    public void visit(OWLDataPropertyAssertionAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
        owlOutputStream.write(OWLObjectType.TRANSITIVE_OBJECT_PROPERTY, axiom);
    }

    @Override
    public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLSubDataPropertyOfAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        owlOutputStream.write(OWLObjectType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY, axiom);
    }

    @Override
    public void visit(OWLSameIndividualAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLSubPropertyChainOfAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        owlOutputStream.write(OWLObjectType.INVERSE_OBJECT_PROPERTIES, axiom);
    }

    @Override
    public void visit(OWLHasKeyAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLDatatypeDefinitionAxiom axiom) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(SWRLRule rule) {
        throw new IllegalStateException("Not implemented yet");

    }

    @Override
    public void visit(OWLLiteral node) {
        if (node.hasLang()) {
            owlOutputStream.write(OWLObjectType.LITERAL_WITH_LANG, node);
        }
        else {
            owlOutputStream.write(OWLObjectType.LITERAL_WITHOUT_LANG, node);
        }
    }

    @Override
    public void visit(OWLFacetRestriction node) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(OWLDatatype node) {
        owlOutputStream.write(OWLObjectType.OWL_DATATYPE, node);
    }

    @Override
    public void visit(OWLDataOneOf node) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(OWLDataComplementOf node) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(OWLDataIntersectionOf node) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(OWLDataUnionOf node) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(OWLDatatypeRestriction node) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(OWLObjectProperty property) {
        owlOutputStream.write(OWLObjectType.OWL_OBJECT_PROPERTY, property);
    }

    @Override
    public void visit(OWLObjectInverseOf property) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(OWLDataProperty property) {
        owlOutputStream.write(OWLObjectType.OWL_DATA_PROPERTY, property);
    }

    @Override
    public void visit(OWLNamedIndividual individual) {
        owlOutputStream.write(OWLObjectType.OWL_NAMED_INDIVIDUAL, individual);
    }

    @Override
    public void visit(OWLAnnotationProperty property) {
        owlOutputStream.write(OWLObjectType.OWL_ANNOTATION_PROPERTY, property);
    }

    @Override
    public void visit(OWLAnonymousIndividual individual) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(IRI iri) {
        owlOutputStream.write(OWLObjectType.IRI_TYPE, iri);
    }

    @Override
    public void visit(OWLAnnotation node) {
        owlOutputStream.write(OWLObjectType.ANNOTATION, node);
    }

    @Override
    public void visit(SWRLClassAtom node) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(SWRLDataRangeAtom node) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(SWRLObjectPropertyAtom node) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(SWRLDataPropertyAtom node) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(SWRLBuiltInAtom node) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(SWRLVariable node) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(SWRLIndividualArgument node) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(SWRLLiteralArgument node) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(SWRLSameIndividualAtom node) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(SWRLDifferentIndividualsAtom node) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(OWLOntology ontology) {
        throw new IllegalStateException("Not implemented yet");
        
    }

    @Override
    public void visit(AddAxiom change) {
        owlOutputStream.write(OWLObjectType.ADD_AXIOM, change);
    }

    @Override
    public void visit(RemoveAxiom change) {
        owlOutputStream.write(OWLObjectType.REMOVE_AXIOM, change);
    }

    @Override
    public void visit(SetOntologyID change) {
        owlOutputStream.write(OWLObjectType.SET_ONTOLOGY_ID, change);
    }

    @Override
    public void visit(AddImport change) {
        owlOutputStream.write(OWLObjectType.ADD_IMPORT, change);
    }

    @Override
    public void visit(RemoveImport change) {
        owlOutputStream.write(OWLObjectType.REMOVE_IMPORT, change);
    }

    @Override
    public void visit(AddOntologyAnnotation change) {
        owlOutputStream.write(OWLObjectType.ADD_ONTOLOGY_ANNOTATION, change);
    }

    @Override
    public void visit(RemoveOntologyAnnotation change) {
        owlOutputStream.write(OWLObjectType.REMOVE_ONTOLOGY_ANNOTATION, change);
    }

}
