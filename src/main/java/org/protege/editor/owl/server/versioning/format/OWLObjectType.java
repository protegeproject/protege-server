package org.protege.editor.owl.server.versioning.format;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLFacet;

/**
 * 
 * @author redmond
 * @deprecated Replace with Matthew's format
 */
@Deprecated
public enum OWLObjectType {
    COMPRESSED {
        @Override
        public Object read(OWLInputStream in) throws IOException {
            int compressedLength = IOUtils.readInt(in.getInputStream());
            byte[] compressedData = IOUtils.readBytes(in.getInputStream(), compressedLength);
            ByteArrayInputStream compressedInputStream = new ByteArrayInputStream(compressedData);
            GZIPInputStream decompressingInputStream = new GZIPInputStream(compressedInputStream);
            OWLInputStream decompressingOwlInputStream = new OWLInputStream(decompressingInputStream);
            try {
                return decompressingOwlInputStream.read();
            }
            finally {
                decompressingInputStream.close();
            }
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            throw new IllegalStateException("Programmer should not have called this method");
        }
    },
    LIST_OF_CHANGES {
        @Override
        public List<OWLOntologyChange> read(OWLInputStream in) throws IOException {
            int count = IOUtils.readInt(in.getInputStream());
            List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
            for (int i = 0; i < count; i++) {
                changes.add((OWLOntologyChange) in.read());
            }
            return changes;
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            @SuppressWarnings("unchecked")
            List<OWLOntologyChange> changes = (List<OWLOntologyChange>) o;
            IOUtils.writeInt(out.getOutputStream(), changes.size());
            for (OWLOntologyChange change : changes) {
                out.write(change);
            }
        }
    },
    SET_ONTOLOGY_ID {

        @Override
        public SetOntologyID read(OWLInputStream in) throws IOException {
            OWLOntologyID id = (OWLOntologyID) in.read();
            return new SetOntologyID(in.getFakeOntology(), id);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            SetOntologyID setId = (SetOntologyID) o;
            out.write(setId.getNewOntologyID());
        }
        
    },
    ADD_ONTOLOGY_ANNOTATION {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLAnnotation annotation = (OWLAnnotation) in.read();
            return new AddOntologyAnnotation(in.getFakeOntology(), annotation);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            AddOntologyAnnotation change = (AddOntologyAnnotation) o;
            out.write(change.getAnnotation());
        }
        
    },
    REMOVE_ONTOLOGY_ANNOTATION {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLAnnotation annotation = (OWLAnnotation) in.read();
            return new RemoveOntologyAnnotation(in.getFakeOntology(), annotation);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            RemoveOntologyAnnotation change = (RemoveOntologyAnnotation) o;
            out.write(change.getAnnotation());
        }
        
    },
    ADD_IMPORT {

        @Override
        public AddImport read(OWLInputStream in) throws IOException {
            IRI importedOntologyIRI = (IRI) in.read();
            OWLImportsDeclaration importDeclaration = in.getOWLDataFactory().getOWLImportsDeclaration(importedOntologyIRI);
            return new AddImport(in.getFakeOntology(), importDeclaration);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            AddImport change = (AddImport) o;
            out.write(change.getImportDeclaration().getIRI());
        }
        
    },
    REMOVE_IMPORT {

        @Override
        public RemoveImport read(OWLInputStream in) throws IOException {
            IRI importedOntologyIRI = (IRI) in.read();
            OWLImportsDeclaration importDeclaration = in.getOWLDataFactory().getOWLImportsDeclaration(importedOntologyIRI);
            return new RemoveImport(in.getFakeOntology(), importDeclaration);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            RemoveImport change = (RemoveImport) o;
            out.write(change.getImportDeclaration().getIRI());
        }
        
    },    
    ADD_AXIOM {

        @Override
        public AddAxiom read(OWLInputStream in) throws IOException {
            OWLAxiom axiom = (OWLAxiom) in.read();
            return new AddAxiom(in.getFakeOntology(), axiom);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            AddAxiom addAxiom = (AddAxiom) o;
            out.write(addAxiom.getAxiom());
        }
        
    },
    REMOVE_AXIOM {

        @Override
        public RemoveAxiom read(OWLInputStream in) throws IOException {
            OWLAxiom axiom = (OWLAxiom) in.read();
            return new RemoveAxiom(in.getFakeOntology(), axiom);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            RemoveAxiom addAxiom = (RemoveAxiom) o;
            out.write(addAxiom.getAxiom());
        }
        
    },
    OWL_ONTOLOGY_ID {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            int count = IOUtils.readInt(in.getInputStream());
            if (count == 0) {
                return new OWLOntologyID();
            }
            IRI ontologyIRI = (IRI) in.read();
            if (count == 1) {
                return new OWLOntologyID(ontologyIRI);
            }
            else {
                IRI versionIRI = (IRI) in.read();
                return new OWLOntologyID(ontologyIRI, versionIRI);
            }
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLOntologyID id = (OWLOntologyID) o;
            int count;
            if (id.isAnonymous()) {
                count = 0;
            }
            else if (!id.getVersionIRI().isPresent()) {
                count = 1;
            }
            else {
                count = 2;
            }
            IOUtils.writeInt(out.getOutputStream(), count);
            if (!id.isAnonymous()) {
                out.write((OWLObject) id.getOntologyIRI().get());
            }
            if (id.getVersionIRI().isPresent()) {
                out.write((OWLObject) id.getVersionIRI().get());
            }
        }
        
    },
    
    IRI_TYPE {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            String iriString = IOUtils.readString(in.getInputStream());
            return IRI.create(iriString);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            IRI iri = (IRI) o;
            String iriString = iri.toString();
            IOUtils.writeString(out.getOutputStream(), iriString);
        }
        
    },
    ANNOTATION{
    
        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLAnnotationProperty property = (OWLAnnotationProperty) in.read();
            OWLAnnotationValue value = (OWLAnnotationValue) in.read();
            return in.getOWLDataFactory().getOWLAnnotation(property, value);
        }
    
        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLAnnotation annotation = (OWLAnnotation) o;
            out.write(annotation.getProperty());
            out.write(annotation.getValue());
        }
        
    }, 
    LITERAL_WITH_LANG {

        @Override
        public OWLLiteral read(OWLInputStream in) throws IOException {
            String literal = IOUtils.readString(in.getInputStream());
            String lang = IOUtils.readString(in.getInputStream());
            return in.getOWLDataFactory().getOWLLiteral(literal, lang);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLLiteral literal = (OWLLiteral) o;
            IOUtils.writeString(out.getOutputStream(), literal.getLiteral());
            IOUtils.writeString(out.getOutputStream(), literal.getLang());
        }
        
    },
    LITERAL_WITHOUT_LANG {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            String lexicalValue = IOUtils.readString(in.getInputStream());
            OWLDatatype datatype = (OWLDatatype) in.read();
            return in.getOWLDataFactory().getOWLLiteral(lexicalValue, datatype);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLLiteral literal = (OWLLiteral) o;
            IOUtils.writeString(out.getOutputStream(), literal.getLiteral());
            out.write(literal.getDatatype());
        }
        
    },
    OWL_CLASS {

        @Override
        public OWLClass read(OWLInputStream in) throws IOException {
            IRI iri = (IRI) in.read();
            return in.getOWLDataFactory().getOWLClass(iri);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLClass cls = (OWLClass) o;
            out.write(cls.getIRI());
        }
    },
    OWL_OBJECT_PROPERTY {

        @Override
        public OWLObjectProperty read(OWLInputStream in) throws IOException {
            IRI iri = (IRI) in.read();
            return in.getOWLDataFactory().getOWLObjectProperty(iri);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLObjectProperty entity = (OWLObjectProperty) o;
            out.write(entity.getIRI());
        }
    },
    OWL_DATA_PROPERTY {

        @Override
        public OWLDataProperty read(OWLInputStream in) throws IOException {
            IRI iri = (IRI) in.read();
            return in.getOWLDataFactory().getOWLDataProperty(iri);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDataProperty entity = (OWLDataProperty) o;
            out.write(entity.getIRI());
        }
    },
    OWL_ANNOTATION_PROPERTY {

        @Override
        public OWLAnnotationProperty read(OWLInputStream in) throws IOException {
            IRI iri = (IRI) in.read();
            return in.getOWLDataFactory().getOWLAnnotationProperty(iri);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLAnnotationProperty entity = (OWLAnnotationProperty) o;
            out.write(entity.getIRI());
        }
    },
    OWL_NAMED_INDIVIDUAL {

        @Override
        public OWLNamedIndividual read(OWLInputStream in) throws IOException {
            IRI iri = (IRI) in.read();
            return in.getOWLDataFactory().getOWLNamedIndividual(iri);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLNamedIndividual entity = (OWLNamedIndividual) o;
            out.write(entity.getIRI());
        }
    },
    OWL_ANONYMOUS_INDIVIDUAL {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            String id = IOUtils.readString(in.getInputStream());
            return in.getOWLDataFactory().getOWLAnonymousIndividual(id);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLAnonymousIndividual i = (OWLAnonymousIndividual) o;
            IOUtils.writeString(out.getOutputStream(), i.getID().getID());
        }
        
    },
    OWL_DATATYPE {

        @Override
        public OWLDatatype read(OWLInputStream in) throws IOException {
            IRI iri = (IRI) in.read();
            return in.getOWLDataFactory().getOWLDatatype(iri);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDatatype entity = (OWLDatatype) o;
            out.write(entity.getIRI());
        }
    },
    
    DATATYPE_RESTRICTION {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLDatatype datatype = (OWLDatatype) in.read();
            int facetRestrictionCount = IOUtils.readInt(in.getInputStream());
            Set<OWLFacetRestriction> facetRestrictions = new TreeSet<OWLFacetRestriction>();
            for (int i = 0; i < facetRestrictionCount; i++) {
                facetRestrictions.add((OWLFacetRestriction) in.read());
            }
            return in.getOWLDataFactory().getOWLDatatypeRestriction(datatype, facetRestrictions);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDatatypeRestriction dr =(OWLDatatypeRestriction) o;
            out.write(dr.getDatatype());
            IOUtils.writeInt(out.getOutputStream(), dr.getFacetRestrictions().size());
            for (OWLFacetRestriction facetRestriction : dr.getFacetRestrictions()) {
                out.write(facetRestriction);
            }
        }
        
    },
    
    FACET_RESTRICTION {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            int ordinal = IOUtils.readInt(in.getInputStream());
            OWLFacet facet = OWLFacet.values()[ordinal];
            OWLLiteral literal = (OWLLiteral) in.read();
            return in.getOWLDataFactory().getOWLFacetRestriction(facet, literal);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLFacetRestriction facetRestriction = (OWLFacetRestriction) o;
            IOUtils.writeInt(out.getOutputStream(), facetRestriction.getFacet().ordinal());
            out.write(facetRestriction.getFacetValue());
        }
        
    },
    
    EMPTY_ANNOTATION_SET {

        @Override
        public Set<OWLAnnotation> read(OWLInputStream in) throws IOException {
            return Collections.emptySet();
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            ; // nothing to do...
        }
        
    },
    ANNOTATION_SET {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            int count = IOUtils.readInt(in.getInputStream());
            Set<OWLAnnotation> annotations = new TreeSet<OWLAnnotation>();
            for (int i = 0; i < count; i++) {
                annotations.add((OWLAnnotation) in.read());
            }
            return annotations;
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            @SuppressWarnings("unchecked")
            Set<OWLAnnotation> annotations = (Set<OWLAnnotation>) o;
            IOUtils.writeInt(out.getOutputStream(), annotations.size());
            for (OWLAnnotation annotation : annotations) {
                out.write(annotation);
            }
        }
        
    },
    OWL_DECLARATION_AXIOM {

        @Override
        public OWLDeclarationAxiom read(OWLInputStream in) throws IOException {
            OWLEntity entity = (OWLEntity) in.read();
            return in.getOWLDataFactory().getOWLDeclarationAxiom(entity);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDeclarationAxiom axiom = (OWLDeclarationAxiom) o;
            out.write(axiom.getEntity());
        }
        
    },
    SUB_CLASS_OF_AXIOM {

        @Override
        public OWLSubClassOfAxiom read(OWLInputStream in) throws IOException {
            OWLClassExpression subClass = (OWLClassExpression) in.read();
            OWLClassExpression superClass = (OWLClassExpression) in.read();
            return in.getOWLDataFactory().getOWLSubClassOfAxiom(subClass, superClass);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLSubClassOfAxiom axiom = (OWLSubClassOfAxiom) o;
            out.write(axiom.getSubClass());
            out.write(axiom.getSuperClass());
        }
        
    },
    EQUIVALENT_CLASSES_AXIOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            int count = IOUtils.readInt(in.getInputStream());
            OWLClassExpression[] classExpressions = new OWLClassExpression[count];
            for (int i = 0; i < count; i++) {
                classExpressions[i] = (OWLClassExpression) in.read();
            }
            return in.getOWLDataFactory().getOWLEquivalentClassesAxiom(classExpressions);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLEquivalentClassesAxiom axiom = (OWLEquivalentClassesAxiom) o;
            IOUtils.writeInt(out.getOutputStream(), axiom.getClassExpressions().size());
            for (OWLClassExpression ce : axiom.getClassExpressions()) {
                out.write(ce);
            }
        }
        
    },
    DISJOINT_UNION_OF_AXIOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLClass c = (OWLClass) in.read();
            int count = IOUtils.readInt(in.getInputStream());
            Set<OWLClassExpression> classExpressions = new TreeSet<OWLClassExpression>();
            for (int i = 0; i < count; i++) {
                classExpressions.add((OWLClassExpression) in.read());
            }
            return in.getOWLDataFactory().getOWLDisjointUnionAxiom(c, classExpressions);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDisjointUnionAxiom axiom = (OWLDisjointUnionAxiom) o;
            out.write(axiom.getOWLClass());
            Set<OWLClassExpression> conjuncts = axiom.getClassExpressions();
            IOUtils.writeInt(out.getOutputStream(), conjuncts.size());
            for (OWLClassExpression conjunct : conjuncts) {
                out.write(conjunct);
            }
        }
        
    },
    HAS_KEY_AXIOM {

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLClassExpression ce = (OWLClassExpression) in.read();
            Set<OWLPropertyExpression> properties = in.readSet(OWLPropertyExpression.class);
            return in.getOWLDataFactory().getOWLHasKeyAxiom(ce, (Set) properties);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLHasKeyAxiom axiom = (OWLHasKeyAxiom) o;
            out.write(axiom.getClassExpression());
            out.write(axiom.getPropertyExpressions());
        }
        
    },
    DISJOINT_CLASSES_AXIOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            int count = IOUtils.readInt(in.getInputStream());
            OWLClassExpression[] classExpressions = new OWLClassExpression[count];
            for (int i = 0; i < count; i++) {
                classExpressions[i] = (OWLClassExpression) in.read();
            }
            return in.getOWLDataFactory().getOWLDisjointClassesAxiom(classExpressions);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDisjointClassesAxiom axiom = (OWLDisjointClassesAxiom) o;
            IOUtils.writeInt(out.getOutputStream(), axiom.getClassExpressions().size());
            for (OWLClassExpression ce : axiom.getClassExpressions()) {
                out.write(ce);
            }
        }
        
    },
    SUB_ANNOTATION_PROPERTY_OF {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLAnnotationProperty subProperty = (OWLAnnotationProperty) in.read();
            OWLAnnotationProperty superProperty = (OWLAnnotationProperty) in.read();
            return in.getOWLDataFactory().getOWLSubAnnotationPropertyOfAxiom(subProperty, superProperty);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLSubAnnotationPropertyOfAxiom axiom = (OWLSubAnnotationPropertyOfAxiom) o;
            out.write(axiom.getSubProperty());
            out.write(axiom.getSuperProperty());
        }
        
    },
    
    ANNOTATION_PROPERTY_DOMAIN {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLAnnotationProperty p = (OWLAnnotationProperty) in.read();
            IRI domain = (IRI) in.read();
            return in.getOWLDataFactory().getOWLAnnotationPropertyDomainAxiom(p, domain);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLAnnotationPropertyDomainAxiom axiom = (OWLAnnotationPropertyDomainAxiom) o;
            out.write(axiom.getProperty());
            out.write(axiom.getDomain());
        }
        
    },
    ANNOTATION_PROPERTY_RANGE {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLAnnotationProperty p = (OWLAnnotationProperty) in.read();
            IRI range = (IRI) in.read();
            return in.getOWLDataFactory().getOWLAnnotationPropertyRangeAxiom(p, range);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLAnnotationPropertyRangeAxiom axiom = (OWLAnnotationPropertyRangeAxiom) o;
            out.write(axiom.getProperty());
            out.write(axiom.getRange());
        }
        
    },
    
    INVERSE_OBJECT_PROPERTIES_AXIOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression firstProperty = (OWLObjectPropertyExpression) in.read();
            OWLObjectPropertyExpression secondProperty = (OWLObjectPropertyExpression) in.read();
            return in.getOWLDataFactory().getOWLInverseObjectPropertiesAxiom(firstProperty, secondProperty);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLInverseObjectPropertiesAxiom axiom = (OWLInverseObjectPropertiesAxiom) o;
            out.write(axiom.getFirstProperty());
            out.write(axiom.getSecondProperty());
        }
        
    },
    FUNCTIONAL_OBJECT_PROPERTY {

        @Override
        public OWLFunctionalObjectPropertyAxiom read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) in.read();
            return in.getOWLDataFactory().getOWLFunctionalObjectPropertyAxiom(property);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLFunctionalObjectPropertyAxiom axiom = (OWLFunctionalObjectPropertyAxiom) o;
            out.write(axiom.getProperty());
        }
        
    },
    INVERSE_FUNCTIONAL_OBJECT_PROPERTY {

        @Override
        public OWLInverseFunctionalObjectPropertyAxiom read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) in.read();
            return in.getOWLDataFactory().getOWLInverseFunctionalObjectPropertyAxiom(property);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLInverseFunctionalObjectPropertyAxiom axiom = (OWLInverseFunctionalObjectPropertyAxiom) o;
            out.write(axiom.getProperty());
        }
        
    },    
    TRANSITIVE_OBJECT_PROPERTY {

        @Override
        public OWLTransitiveObjectPropertyAxiom read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) in.read();
            return in.getOWLDataFactory().getOWLTransitiveObjectPropertyAxiom(property);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLTransitiveObjectPropertyAxiom axiom = (OWLTransitiveObjectPropertyAxiom) o;
            out.write(axiom.getProperty());
        }
        
    },
    SYMMETRIC_OBJECT_PROPERTY {

        @Override
        public OWLSymmetricObjectPropertyAxiom read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) in.read();
            return in.getOWLDataFactory().getOWLSymmetricObjectPropertyAxiom(property);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLSymmetricObjectPropertyAxiom axiom = (OWLSymmetricObjectPropertyAxiom) o;
            out.write(axiom.getProperty());
        }
        
    },
    ASYMMETRIC_OBJECT_PROPERTY {

        @Override
        public OWLObject read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) in.read();
            return in.getOWLDataFactory().getOWLAsymmetricObjectPropertyAxiom(property);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLAsymmetricObjectPropertyAxiom axiom = (OWLAsymmetricObjectPropertyAxiom) o;
            out.write(axiom.getProperty());
        }
        
    },
    REFLEXIVE_OBJECT_PROPERTY {

        @Override
        public OWLObject read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) in.read();
            return in.getOWLDataFactory().getOWLReflexiveObjectPropertyAxiom(property);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLReflexiveObjectPropertyAxiom axiom = (OWLReflexiveObjectPropertyAxiom) o;
            out.write(axiom.getProperty());
        }
        
    },
    IRREFLEXIVE_OBJECT_PROPERTY {

        @Override
        public OWLObject read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) in.read();
            return in.getOWLDataFactory().getOWLIrreflexiveObjectPropertyAxiom(property);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLIrreflexiveObjectPropertyAxiom axiom = (OWLIrreflexiveObjectPropertyAxiom) o;
            out.write(axiom.getProperty());
        }
        
    },
    OBJECT_PROPERTY_DOMAIN_AXIOM {

        @Override
        public OWLObjectPropertyDomainAxiom read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) in.read();
            OWLClassExpression classExpression = (OWLClassExpression) in.read();
            return in.getOWLDataFactory().getOWLObjectPropertyDomainAxiom(property, classExpression);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLObjectPropertyDomainAxiom axiom = (OWLObjectPropertyDomainAxiom) o;
            out.write(axiom.getProperty());
            out.write(axiom.getDomain());
        }
        
    },
    OBJECT_PROPERTY_RANGE_AXIOM {

        @Override
        public OWLObjectPropertyRangeAxiom read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) in.read();
            OWLClassExpression classExpression = (OWLClassExpression) in.read();
            return in.getOWLDataFactory().getOWLObjectPropertyRangeAxiom(property, classExpression);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLObjectPropertyRangeAxiom axiom = (OWLObjectPropertyRangeAxiom) o;
            out.write(axiom.getProperty());
            out.write(axiom.getRange());
        }
        
    },
    SUB_OBJECT_PROPERTY_OF{
    
        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression subProperty = (OWLObjectPropertyExpression) in.read();
            OWLObjectPropertyExpression superProperty = (OWLObjectPropertyExpression) in.read();
            return in.getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(subProperty, superProperty);
        }
    
        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLSubObjectPropertyOfAxiom axiom = (OWLSubObjectPropertyOfAxiom) o;
            out.write(axiom.getSubProperty());
            out.write(axiom.getSuperProperty());
        }
        
    }, 
    SUB_OBJECT_PROPERTY_CHAIN_AXIOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            List<OWLObjectPropertyExpression> chain = in.readList(OWLObjectPropertyExpression.class);
            OWLObjectPropertyExpression superProperty = (OWLObjectPropertyExpression) in.read();
            return in.getOWLDataFactory().getOWLSubPropertyChainOfAxiom(chain, superProperty);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLSubPropertyChainOfAxiom axiom = (OWLSubPropertyChainOfAxiom) o;
            out.write(axiom.getPropertyChain());
            out.write(axiom.getSuperProperty());
        }
        
    },
    EQUIVALENT_OBJECT_PROPERTIES_AXIOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            int count = IOUtils.readInt(in.getInputStream());
            Set<OWLObjectPropertyExpression> properties = new TreeSet<OWLObjectPropertyExpression>();
            for (int i = 0; i < count; i++) {
                properties.add((OWLObjectPropertyExpression) in.read());
            }
            return in.getOWLDataFactory().getOWLEquivalentObjectPropertiesAxiom(properties);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLEquivalentObjectPropertiesAxiom axiom = (OWLEquivalentObjectPropertiesAxiom) o;
            Set<OWLObjectPropertyExpression> properties = axiom.getProperties();
            IOUtils.writeInt(out.getOutputStream(), properties.size());
            for (OWLObjectPropertyExpression property : properties) {
                out.write(property);
            }
        }
        
    },
    DISJOINT_OBJECT_PROPERTIES_AXIOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            int count = IOUtils.readInt(in.getInputStream());
            Set<OWLObjectPropertyExpression> properties = new TreeSet<OWLObjectPropertyExpression>();
            for (int i = 0; i < count; i++) {
                properties.add((OWLObjectPropertyExpression) in.read());
            }
            return in.getOWLDataFactory().getOWLDisjointObjectPropertiesAxiom(properties);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDisjointObjectPropertiesAxiom axiom = (OWLDisjointObjectPropertiesAxiom) o;
            Set<OWLObjectPropertyExpression> pes = axiom.getProperties();
            IOUtils.writeInt(out.getOutputStream(), pes.size());
            for (OWLObjectPropertyExpression pe : pes) {
                out.write(pe);
            }
        }
        
    },
    DATA_PROPERTY_DOMAIN_AXIOM {

        @Override
        public OWLDataPropertyDomainAxiom read(OWLInputStream in) throws IOException {
            OWLDataPropertyExpression property = (OWLDataPropertyExpression) in.read();
            OWLClassExpression classExpression = (OWLClassExpression) in.read();
            return in.getOWLDataFactory().getOWLDataPropertyDomainAxiom(property, classExpression);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDataPropertyDomainAxiom axiom = (OWLDataPropertyDomainAxiom) o;
            out.write(axiom.getProperty());
            out.write(axiom.getDomain());
        }
        
    },
    DATA_PROPERTY_RANGE_AXIOM {

        @Override
        public OWLDataPropertyRangeAxiom read(OWLInputStream in) throws IOException {
            OWLDataPropertyExpression property = (OWLDataPropertyExpression) in.read();
            OWLDataRange dataRange = (OWLDataRange) in.read();
            return in.getOWLDataFactory().getOWLDataPropertyRangeAxiom(property, dataRange);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDataPropertyRangeAxiom axiom = (OWLDataPropertyRangeAxiom) o;
            out.write(axiom.getProperty());
            out.write(axiom.getRange());
        }
        
    },
    FUNCTIONAL_DATA_PROPERTY_AXIOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLDataPropertyExpression property = (OWLDataPropertyExpression) in.read();
            return in.getOWLDataFactory().getOWLFunctionalDataPropertyAxiom(property);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLFunctionalDataPropertyAxiom axiom = (OWLFunctionalDataPropertyAxiom) o;
            out.write(axiom.getProperty());
        }
        
    },
    SUB_DATA_PROPERTY_OF{
        
        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLDataPropertyExpression subProperty = (OWLDataPropertyExpression) in.read();
            OWLDataPropertyExpression superProperty = (OWLDataPropertyExpression) in.read();
            return in.getOWLDataFactory().getOWLSubDataPropertyOfAxiom(subProperty, superProperty);
        }
    
        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLSubDataPropertyOfAxiom axiom = (OWLSubDataPropertyOfAxiom) o;
            out.write(axiom.getSubProperty());
            out.write(axiom.getSuperProperty());
        }
        
    }, 
    EQUIVALENT_DATA_PROPERTIES_AXIOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            int count = IOUtils.readInt(in.getInputStream());
            Set<OWLDataPropertyExpression> properties = new TreeSet<OWLDataPropertyExpression>();
            for (int i = 0; i < count; i++) {
                properties.add((OWLDataPropertyExpression) in.read());
            }
            return in.getOWLDataFactory().getOWLEquivalentDataPropertiesAxiom(properties);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLEquivalentDataPropertiesAxiom axiom = (OWLEquivalentDataPropertiesAxiom) o;
            Set<OWLDataPropertyExpression> properties = axiom.getProperties();
            IOUtils.writeInt(out.getOutputStream(), properties.size());
            for (OWLDataPropertyExpression property : properties) {
                out.write(property);
            }
        }
        
    },
    DISJOINT_DATA_PROPERTIES_AXIOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            int count = IOUtils.readInt(in.getInputStream());
            Set<OWLDataPropertyExpression> properties = new TreeSet<OWLDataPropertyExpression>();
            for (int i = 0; i < count; i++) {
                properties.add((OWLDataPropertyExpression) in.read());
            }
            return in.getOWLDataFactory().getOWLDisjointDataPropertiesAxiom(properties);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDisjointDataPropertiesAxiom axiom = (OWLDisjointDataPropertiesAxiom) o;
            Set<OWLDataPropertyExpression> pes = axiom.getProperties();
            IOUtils.writeInt(out.getOutputStream(), pes.size());
            for (OWLDataPropertyExpression pe : pes) {
                out.write(pe);
            }
        }
        
    },
    CLASS_ASSERTION {

        @Override
        public OWLClassAssertionAxiom read(OWLInputStream in) throws IOException {
            OWLClassExpression classExpression = (OWLClassExpression) in.read();
            OWLIndividual individual = (OWLIndividual) in.read();
            return in.getOWLDataFactory().getOWLClassAssertionAxiom(classExpression, individual);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLClassAssertionAxiom axiom = (OWLClassAssertionAxiom) o;
            out.write(axiom.getClassExpression());
            out.write(axiom.getIndividual());
        }
        
    },
    DIFFERENT_INDIVIDUALS_AXIOM {

        @Override
        public OWLDifferentIndividualsAxiom read(OWLInputStream in) throws IOException {
            int count = IOUtils.readInt(in.getInputStream());
            OWLIndividual[] individuals = new OWLIndividual[count];
            for (int i = 0; i < count; i++) {
                individuals[i] = (OWLIndividual) in.read();
            }
            return in.getOWLDataFactory().getOWLDifferentIndividualsAxiom(individuals);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDifferentIndividualsAxiom axiom = (OWLDifferentIndividualsAxiom) o;
            IOUtils.writeInt(out.getOutputStream(), axiom.getIndividuals().size());
            for (OWLIndividual i : axiom.getIndividuals()) {
                out.write(i);
            }
        }
        
    },
    DATATYPE_DEFINITION {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLDatatype datatype = (OWLDatatype) in.read();
            OWLDataRange dataRange = (OWLDataRange) in.read();
            return in.getOWLDataFactory().getOWLDatatypeDefinitionAxiom(datatype, dataRange);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDatatypeDefinitionAxiom axiom = (OWLDatatypeDefinitionAxiom) o;
            out.write(axiom.getDatatype());
            out.write(axiom.getDataRange());
        }
        
    },
    DATA_ONE_OF {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            Set<OWLLiteral> values = in.readSet(OWLLiteral.class);
            return in.getOWLDataFactory().getOWLDataOneOf(values);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDataOneOf node = (OWLDataOneOf) o;
            out.write(node.getValues());
        }
        
    },
    DATA_COMPLEMENT_OF {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLDataRange range = (OWLDataRange) in.read();
            return in.getOWLDataFactory().getOWLDataComplementOf(range);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDataComplementOf complement = (OWLDataComplementOf) o;
            out.write(complement.getDataRange());
        }
        
    },
    DATA_INTERSECTION_OF {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            Set<OWLDataRange> operands = in.readSet(OWLDataRange.class);
            return in.getOWLDataFactory().getOWLDataIntersectionOf(operands);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDataIntersectionOf intersection = (OWLDataIntersectionOf) o;
            out.write(intersection.getOperands());
        }
        
    },
    
    DATA_UNION_OF {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            Set<OWLDataRange> operands = in.readSet(OWLDataRange.class);
            return in.getOWLDataFactory().getOWLDataUnionOf(operands);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDataUnionOf union = (OWLDataUnionOf) o;
            out.write(union.getOperands());
        }
        
    },
    ANNOTATION_ASSERTION_AXIOM {

        @Override
        public OWLAnnotationAssertionAxiom read(OWLInputStream in) throws IOException {
            OWLAnnotationProperty property = (OWLAnnotationProperty) in.read();
            OWLAnnotationSubject subject = (OWLAnnotationSubject) in.read();
            OWLAnnotationValue value = (OWLAnnotationValue) in.read();
            return in.getOWLDataFactory().getOWLAnnotationAssertionAxiom(property, subject, value);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLAnnotationAssertionAxiom annotationAssertion = (OWLAnnotationAssertionAxiom) o;
            out.write(annotationAssertion.getProperty());
            out.write(annotationAssertion.getSubject());
            out.write(annotationAssertion.getValue());
        }
        
    },
    OBJECT_INTERSECTION_OF {

        @Override
        public OWLObjectIntersectionOf read(OWLInputStream in) throws IOException {
            int count = IOUtils.readInt(in.getInputStream());
            OWLClassExpression[] operands = new OWLClassExpression[count];
            for (int i = 0; i < count; i++) {
                operands[i] = (OWLClassExpression) in.read();
            }
            return in.getOWLDataFactory().getOWLObjectIntersectionOf(operands);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) o;
            IOUtils.writeInt(out.getOutputStream(), intersection.getOperands().size());
            for (OWLClassExpression ce : intersection.getOperands()) {
                out.write(ce);
            }
        }
        
    },
    OBJECT_UNION_OF {

        @Override
        public OWLObjectUnionOf read(OWLInputStream in) throws IOException {
            int count = IOUtils.readInt(in.getInputStream());
            OWLClassExpression[] operands = new OWLClassExpression[count];
            for (int i = 0; i < count; i++) {
                operands[i] = (OWLClassExpression) in.read();
            }
            return in.getOWLDataFactory().getOWLObjectUnionOf(operands);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLObjectUnionOf union = (OWLObjectUnionOf) o;
            IOUtils.writeInt(out.getOutputStream(), union.getOperands().size());
            for (OWLClassExpression ce : union.getOperands()) {
                out.write(ce);
            }
        }
        
    },
    OBJECT_COMPLEMENT_OF {

        @Override
        public OWLObjectComplementOf read(OWLInputStream in) throws IOException {
            OWLClassExpression operand = (OWLClassExpression) in.read();
            return in.getOWLDataFactory().getOWLObjectComplementOf(operand);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLObjectComplementOf ce = (OWLObjectComplementOf) o;
            out.write(ce.getOperand());
        }
        
    },
    OBJECT_SOME_VALUES_FROM {

        @Override
        public OWLObjectSomeValuesFrom read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) in.read();
            OWLClassExpression classExpression = (OWLClassExpression) in.read();
            return in.getOWLDataFactory().getOWLObjectSomeValuesFrom(property, classExpression);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLObjectSomeValuesFrom ce = (OWLObjectSomeValuesFrom) o;
            out.write(ce.getProperty());
            out.write(ce.getFiller());
        }
    },
    
    OBJECT_ALL_VALUES_FROM {

        @Override
        public OWLObjectAllValuesFrom read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) in.read();
            OWLClassExpression classExpression = (OWLClassExpression) in.read();
            return in.getOWLDataFactory().getOWLObjectAllValuesFrom(property, classExpression);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLObjectAllValuesFrom ce = (OWLObjectAllValuesFrom) o;
            out.write(ce.getProperty());
            out.write(ce.getFiller());
        }
    },
    
    OBJECT_MIN_CARDINALITY {

        @Override
        public OWLObjectMinCardinality read(OWLInputStream in) throws IOException {
            int cardinality = IOUtils.readInt(in.getInputStream());
            OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) in.read();
            OWLClassExpression classExpression = (OWLClassExpression) in.read();
            return in.getOWLDataFactory().getOWLObjectMinCardinality(cardinality, property, classExpression);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLObjectMinCardinality ce = (OWLObjectMinCardinality) o;
            IOUtils.writeInt(out.getOutputStream(), ce.getCardinality());
            out.write(ce.getProperty());
            out.write(ce.getFiller());
        }
        
    },
    
    OBJECT_MAX_CARDINALITY {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            int cardinality = IOUtils.readInt(in.getInputStream());
            OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) in.read();
            OWLClassExpression classExpression = (OWLClassExpression) in.read();
            return in.getOWLDataFactory().getOWLObjectMaxCardinality(cardinality, property, classExpression);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLObjectMaxCardinality ce = (OWLObjectMaxCardinality) o;
            IOUtils.writeInt(out.getOutputStream(), ce.getCardinality());
            out.write(ce.getProperty());
            out.write(ce.getFiller());
        }
        
    },
    
    OBJECT_EXACT_CARDINALITY {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression p = (OWLObjectPropertyExpression) in.read();
            int cardinality = IOUtils.readInt(in.getInputStream());
            OWLClassExpression filler = (OWLClassExpression) in.read();
            return in.getOWLDataFactory().getOWLObjectExactCardinality(cardinality, p, filler);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLObjectExactCardinality ce = (OWLObjectExactCardinality) o;
            out.write(ce.getProperty());
            IOUtils.writeInt(out.getOutputStream(), ce.getCardinality());
            out.write(ce.getFiller());
        }
        
    },
    
    OBJECT_HAS_VALUE {

        @Override
        public OWLObjectHasValue read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) in.read();
            OWLIndividual individual = (OWLIndividual) in.read();
            return in.getOWLDataFactory().getOWLObjectHasValue(property, individual);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLObjectHasValue ce = (OWLObjectHasValue) o;
            out.write(ce.getProperty());
            out.write(ce.getValue());
        }
        
    },
    
    OBJECT_SELF {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression pe = (OWLObjectPropertyExpression) in.read();
            return in.getOWLDataFactory().getOWLObjectHasSelf(pe);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLObjectHasSelf ce = (OWLObjectHasSelf) o;
            out.write(ce.getProperty());
        }
        
    },
    
    DATA_SOME_VALUES_FROM {

        @Override
        public OWLDataSomeValuesFrom read(OWLInputStream in) throws IOException {
            OWLDataProperty property = (OWLDataProperty) in.read();
            OWLDataRange dataRange = (OWLDataRange) in.read();
            return in.getOWLDataFactory().getOWLDataSomeValuesFrom(property, dataRange);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDataSomeValuesFrom ce = (OWLDataSomeValuesFrom) o;
            out.write(ce.getProperty());
            out.write(ce.getFiller());
        }
    },
    
    DATA_ALL_VALUES_FROM {

        @Override
        public OWLDataAllValuesFrom read(OWLInputStream in) throws IOException {
            OWLDataProperty property = (OWLDataProperty) in.read();
            OWLDataRange dataRange = (OWLDataRange) in.read();
            return in.getOWLDataFactory().getOWLDataAllValuesFrom(property, dataRange);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDataAllValuesFrom ce = (OWLDataAllValuesFrom) o;
            out.write(ce.getProperty());
            out.write(ce.getFiller());
        }
    },
    
    DATA_MIN_CARDINALTY {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLDataPropertyExpression property = (OWLDataPropertyExpression) in.read();
            int cardinality = IOUtils.readInt(in.getInputStream());
            OWLDataRange dataRange = (OWLDataRange) in.read();
            return in.getOWLDataFactory().getOWLDataMinCardinality(cardinality, property, dataRange);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDataMinCardinality ce = (OWLDataMinCardinality) o;
            out.write(ce.getProperty());
            IOUtils.writeInt(out.getOutputStream(), ce.getCardinality());
            out.write(ce.getFiller());
        }
        
    },
    
    DATA_MAX_CARDINALTY {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLDataPropertyExpression property = (OWLDataPropertyExpression) in.read();
            int cardinality = IOUtils.readInt(in.getInputStream());
            OWLDataRange dataRange = (OWLDataRange) in.read();
            return in.getOWLDataFactory().getOWLDataMaxCardinality(cardinality, property, dataRange);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDataMaxCardinality ce = (OWLDataMaxCardinality) o;
            out.write(ce.getProperty());
            IOUtils.writeInt(out.getOutputStream(), ce.getCardinality());
            out.write(ce.getFiller());
        }
        
    },
    
    DATA_EXACT_CARDINALTY {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLDataPropertyExpression property = (OWLDataPropertyExpression) in.read();
            int cardinality = IOUtils.readInt(in.getInputStream());
            OWLDataRange dataRange = (OWLDataRange) in.read();
            return in.getOWLDataFactory().getOWLDataExactCardinality(cardinality, property, dataRange);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDataExactCardinality ce = (OWLDataExactCardinality) o;
            out.write(ce.getProperty());
            IOUtils.writeInt(out.getOutputStream(), ce.getCardinality());
            out.write(ce.getFiller());
        }
        
    },
    
    DATA_HAS_VALUE {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLDataPropertyExpression dpe = (OWLDataPropertyExpression) in.read();
            OWLLiteral literal = (OWLLiteral) in.read();
            return in.getOWLDataFactory().getOWLDataHasValue(dpe, literal);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDataHasValue ce = (OWLDataHasValue) o;
            out.write(ce.getProperty());
            out.write(ce.getValue());
        }
        
    },
    
    OBJECT_ONE_OF {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            Set<OWLIndividual> individuals = new TreeSet<OWLIndividual>();
            int count = IOUtils.readInt(in.getInputStream());
            for (int i = 0; i < count; i++) {
                individuals.add((OWLIndividual) in.read());
            }
            return in.getOWLDataFactory().getOWLObjectOneOf(individuals);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLObjectOneOf ce = (OWLObjectOneOf) o;
            IOUtils.writeInt(out.getOutputStream(), ce.getIndividuals().size());
            for (OWLIndividual i : ce.getIndividuals()) {
                out.write(i);
            }
        }
        
    },
    
    INVERSE_OBJECT_PROPERTY {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression pe = (OWLObjectPropertyExpression) in.read();
            return in.getOWLDataFactory().getOWLObjectInverseOf(pe);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLObjectInverseOf pe = (OWLObjectInverseOf) o;
            out.write(pe.getInverse());
        }
        
    },
    
    NEGATIVE_OBJECT_PROPERTY_ASSERTION {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLIndividual i = (OWLIndividual) in.read();
            OWLObjectPropertyExpression pe = (OWLObjectPropertyExpression) in.read();
            OWLIndividual j = (OWLIndividual) in.read();
            return in.getOWLDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(pe, i, j);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLNegativeObjectPropertyAssertionAxiom axiom = (OWLNegativeObjectPropertyAssertionAxiom) o;
            out.write(axiom.getSubject());
            out.write(axiom.getProperty());
            out.write(axiom.getObject());
        }
        
    },

    OBJECT_PROPERTY_ASSERTION {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLIndividual i = (OWLIndividual) in.read();
            OWLObjectPropertyExpression pe = (OWLObjectPropertyExpression) in.read();
            OWLIndividual j = (OWLIndividual) in.read();
            return in.getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(pe, i, j);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLObjectPropertyAssertionAxiom axiom = (OWLObjectPropertyAssertionAxiom) o;
            out.write(axiom.getSubject());
            out.write(axiom.getProperty());
            out.write(axiom.getObject());
        }
        
    },
    
    DATA_PROPERTY_ASSERTION {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLIndividual i = (OWLIndividual) in.read();
            OWLDataPropertyExpression pe = (OWLDataPropertyExpression) in.read();
            OWLLiteral literal = (OWLLiteral) in.read();
            return in.getOWLDataFactory().getOWLDataPropertyAssertionAxiom(pe, i, literal);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLDataPropertyAssertionAxiom axiom = (OWLDataPropertyAssertionAxiom) o;
            out.write(axiom.getSubject());
            out.write(axiom.getProperty());
            out.write(axiom.getObject());
        }
        
    },
    
    NEGATIVE_DATA_PROPERTY_ASSERTION {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLIndividual i = (OWLIndividual) in.read();
            OWLDataPropertyExpression pe = (OWLDataPropertyExpression) in.read();
            OWLLiteral literal = (OWLLiteral) in.read();
            return in.getOWLDataFactory().getOWLNegativeDataPropertyAssertionAxiom(pe, i, literal);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLNegativeDataPropertyAssertionAxiom axiom = (OWLNegativeDataPropertyAssertionAxiom) o;
            out.write(axiom.getSubject());
            out.write(axiom.getProperty());
            out.write(axiom.getObject());
        }
        
    },
    
    SAME_INDIVIDUALS_AXIOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            Set<OWLIndividual> individuals = new TreeSet<OWLIndividual>();
            int count = IOUtils.readInt(in.getInputStream());
            for (int i = 0; i < count; i++) {
                individuals.add((OWLIndividual) in.read());
            }
            return in.getOWLDataFactory().getOWLSameIndividualAxiom(individuals);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            OWLSameIndividualAxiom axiom = (OWLSameIndividualAxiom) o;
            Set<OWLIndividual> individuals = axiom.getIndividuals();
            IOUtils.writeInt(out.getOutputStream(), individuals.size());
            for (OWLIndividual i : individuals) {
                out.write(i);
            }
        }
        
    },
    
    SWRL_RULE {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            Set<SWRLAtom> head = in.readSet(SWRLAtom.class);
            Set<SWRLAtom> body = in.readSet(SWRLAtom.class);
            return in.getOWLDataFactory().getSWRLRule(body, head);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            SWRLRule rule = (SWRLRule) o;
            out.write(rule.getHead());
            out.write(rule.getBody());
        }
        
    },
    
    SWRL_CLASS_ATOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLClassExpression predicate = (OWLClassExpression) in.read();
            SWRLIArgument argument = (SWRLIArgument) in.read();
            return in.getOWLDataFactory().getSWRLClassAtom(predicate, argument);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            SWRLClassAtom atom = (SWRLClassAtom) o;
            out.write(atom.getPredicate());
            out.write(atom.getArgument());
        }
        
    },
    
    SWRL_OBJECT_PROPERTY_ATOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLObjectPropertyExpression property = (OWLObjectPropertyExpression) in.read();
            SWRLIArgument arg1 = (SWRLIArgument) in.read();
            SWRLIArgument arg2 = (SWRLIArgument) in.read();
            return in.getOWLDataFactory().getSWRLObjectPropertyAtom(property, arg1, arg2);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            SWRLObjectPropertyAtom atom = (SWRLObjectPropertyAtom) o;
            out.write(atom.getPredicate());
            out.write(atom.getFirstArgument());
            out.write(atom.getSecondArgument());
        }
        
    },
    
    SWRL_DATA_PROPERTY_ATOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLDataPropertyExpression property = (OWLDataPropertyExpression) in.read();
            SWRLIArgument arg1 = (SWRLIArgument) in.read();
            SWRLDArgument arg2 = (SWRLDArgument) in.read();
            return in.getOWLDataFactory().getSWRLDataPropertyAtom(property, arg1, arg2);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            SWRLDataPropertyAtom atom = (SWRLDataPropertyAtom) o;
            out.write(atom.getPredicate());
            out.write(atom.getFirstArgument());
            out.write(atom.getSecondArgument());
        }
        
    },
    
    SWRL_DATA_RANGE_ATOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLDataRange predicate = (OWLDataRange) in.read();
            SWRLDArgument arg = (SWRLDArgument) in.read();
            return in.getOWLDataFactory().getSWRLDataRangeAtom(predicate, arg);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            SWRLDataRangeAtom atom = (SWRLDataRangeAtom) o;
            out.write(atom.getPredicate());
            out.write(atom.getArgument());
        }
        
    },
    
    SWRL_SAME_AS_ATOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            SWRLIArgument arg1 = (SWRLIArgument) in.read();
            SWRLIArgument arg2 = (SWRLIArgument) in.read();
            return in.getOWLDataFactory().getSWRLSameIndividualAtom(arg1, arg2);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            SWRLSameIndividualAtom atom = (SWRLSameIndividualAtom) o;
            out.write(atom.getFirstArgument());
            out.write(atom.getSecondArgument());
        }
        
    },
    
    SWRL_DIFFERENT_FROM_ATOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            SWRLIArgument arg1 = (SWRLIArgument) in.read();
            SWRLIArgument arg2 = (SWRLIArgument) in.read();
            return in.getOWLDataFactory().getSWRLDifferentIndividualsAtom(arg1, arg2);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            SWRLDifferentIndividualsAtom atom = (SWRLDifferentIndividualsAtom) o;
            out.write(atom.getFirstArgument());
            out.write(atom.getSecondArgument());
        }
        
    },
    
    SWRL_BUILTIN_ATOM {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            IRI builtInIRI = (IRI) in.read();
            List<SWRLDArgument> args = in.readList(SWRLDArgument.class);
            return in.getOWLDataFactory().getSWRLBuiltInAtom(builtInIRI, args);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            SWRLBuiltInAtom atom = (SWRLBuiltInAtom) o;
            out.write(atom.getPredicate());
            out.write(atom.getArguments());
        }
        
    },
    
    SWRL_INDIVIDUAL_ARGUMENT {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLIndividual individual = (OWLIndividual) in.read();
            return in.getOWLDataFactory().getSWRLIndividualArgument(individual);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            SWRLIndividualArgument i = (SWRLIndividualArgument) o;
            out.write(i.getIndividual());
        }
        
    },
    
    SWRL_VARIABLE {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            IRI var = (IRI) in.read();
            return in.getOWLDataFactory().getSWRLVariable(var);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            SWRLVariable node = (SWRLVariable) o;
            out.write(node.getIRI());
        }
        
    },
    
    SWRL_LITERAL_ARGUMENT {

        @Override
        public Object read(OWLInputStream in) throws IOException {
            OWLLiteral literal = (OWLLiteral) in.read();
            return in.getOWLDataFactory().getSWRLLiteralArgument(literal);
        }

        @Override
        public void write(OWLOutputStream out, Object o) throws IOException {
            SWRLLiteralArgument literal = (SWRLLiteralArgument) o;
            out.write(literal.getLiteral());
        }
        
    }
    
    ;
    
    public abstract Object read(OWLInputStream in) throws IOException;
    public abstract void write(OWLOutputStream out, Object o) throws IOException;
}
