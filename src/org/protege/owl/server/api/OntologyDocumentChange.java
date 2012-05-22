package org.protege.owl.server.api;

import org.semanticweb.owlapi.model.OWLAxiom;


public abstract class OntologyDocumentChange {
    private final OntologyDocument ont;
    /**
     * @param ont the ontology to which the change is to be applied
     */
    public OntologyDocumentChange(OntologyDocument ont) {
        this.ont = ont;
    }


    /**
     * Determines if the change will cause the addition or
     * removal of an axiom from an ontology.
     * @return <code>true</code> if the change is an <code>OWLAddAxiomChange</code>
     *         or <code>OWLRemoveAxiomChange</code> otherwise <code>false</code>.
     */
    public abstract boolean isAxiomChange();

    /**
     * If the change is an axiom change (i.e. AddAxiom or RemoveAxiom)
     * this method obtains the axiom.
     * @return The Axiom if this change is an axiom change
     * @throws UnsupportedOperationException If the change is not an axiom change (check
     *                                       with the <code>isAxiomChange</code> method first).
     */
    public abstract OWLAxiom getAxiom();


    /**
     * Determines if this change is an import change and hence causes a change to the imports closure of an ontology.
     * @return <code>true</code> if this change is an import change, otherwise <code>false</code>.
     */
    public abstract boolean isImportChange();

    /**
     * Gets the ontology that the change is/was applied to
     * @return The ontology that the change is applicable to
     */
    public OntologyDocument getOWLDocument() {
        return ont;
    }


    @SuppressWarnings("javadoc")
	public abstract void accept(OntologyDocumentChangeVisitor visitor);
}
